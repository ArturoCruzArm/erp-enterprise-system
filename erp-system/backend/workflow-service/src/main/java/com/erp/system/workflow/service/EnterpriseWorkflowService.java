package com.erp.system.workflow.service;

import com.erp.system.workflow.dto.*;
import com.erp.system.workflow.entity.WorkflowInstance;
import com.erp.system.workflow.entity.WorkflowDefinition;
import com.erp.system.workflow.repository.WorkflowInstanceRepository;
import com.erp.system.workflow.repository.WorkflowDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EnterpriseWorkflowService {
    
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final RepositoryService repositoryService;
    private final HistoryService historyService;
    private final ManagementService managementService;
    private final DecisionService decisionService;
    
    private final WorkflowInstanceRepository workflowInstanceRepository;
    private final WorkflowDefinitionRepository workflowDefinitionRepository;
    
    private final WorkflowNotificationService notificationService;
    private final WorkflowEventService eventService;
    private final BusinessRulesService businessRulesService;
    
    /**
     * Deploy a new workflow definition from BPMN XML
     */
    public WorkflowDefinitionDto deployWorkflow(String bpmnXml, String workflowName, String version) {
        log.info("Deploying workflow: {} version: {}", workflowName, version);
        
        try {
            // Validate BPMN model
            BpmnModelInstance modelInstance = Bpmn.readModelFromStream(
                new java.io.ByteArrayInputStream(bpmnXml.getBytes())
            );
            
            // Deploy to Camunda engine
            var deployment = repositoryService.createDeployment()
                    .name(workflowName + "_v" + version)
                    .addString(workflowName + ".bpmn", bpmnXml)
                    .enableDuplicateFiltering(false)
                    .deploy();
            
            // Save to database
            WorkflowDefinition definition = WorkflowDefinition.builder()
                    .name(workflowName)
                    .version(version)
                    .bpmnXml(bpmnXml)
                    .deploymentId(deployment.getId())
                    .processDefinitionKey(extractProcessDefinitionKey(modelInstance))
                    .isActive(true)
                    .deployedAt(LocalDateTime.now())
                    .build();
            
            definition = workflowDefinitionRepository.save(definition);
            
            // Publish deployment event
            eventService.publishWorkflowDeployed(definition);
            
            log.info("Workflow deployed successfully: {}", definition.getId());
            return mapToDto(definition);
            
        } catch (Exception e) {
            log.error("Error deploying workflow: {}", workflowName, e);
            throw new RuntimeException("Failed to deploy workflow: " + e.getMessage());
        }
    }
    
    /**
     * Start a new workflow instance
     */
    public WorkflowInstanceDto startWorkflow(String processDefinitionKey, Map<String, Object> variables, String startedBy) {
        log.info("Starting workflow: {} by user: {}", processDefinitionKey, startedBy);
        
        try {
            // Add system variables
            variables.put("startedBy", startedBy);
            variables.put("startedAt", LocalDateTime.now());
            variables.put("tenantId", extractTenantId(variables));
            
            // Start process instance
            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                    processDefinitionKey, variables
            );
            
            // Save to database
            WorkflowInstance instance = WorkflowInstance.builder()
                    .processInstanceId(processInstance.getId())
                    .processDefinitionKey(processDefinitionKey)
                    .businessKey(processInstance.getBusinessKey())
                    .status("RUNNING")
                    .startedBy(startedBy)
                    .startedAt(LocalDateTime.now())
                    .variables(variables)
                    .build();
            
            instance = workflowInstanceRepository.save(instance);
            
            // Send notification
            notificationService.sendWorkflowStartedNotification(instance);
            
            // Publish event
            eventService.publishWorkflowStarted(instance);
            
            log.info("Workflow started successfully: {}", instance.getId());
            return mapToDto(instance);
            
        } catch (Exception e) {
            log.error("Error starting workflow: {}", processDefinitionKey, e);
            throw new RuntimeException("Failed to start workflow: " + e.getMessage());
        }
    }
    
    /**
     * Complete a user task
     */
    public void completeTask(String taskId, Map<String, Object> variables, String completedBy) {
        log.info("Completing task: {} by user: {}", taskId, completedBy);
        
        try {
            // Get task details
            Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
            if (task == null) {
                throw new RuntimeException("Task not found: " + taskId);
            }
            
            // Check if user is authorized
            if (!isUserAuthorizedForTask(task, completedBy)) {
                throw new RuntimeException("User not authorized to complete task: " + taskId);
            }
            
            // Add completion metadata
            variables.put("completedBy", completedBy);
            variables.put("completedAt", LocalDateTime.now());
            
            // Execute business rules if any
            variables = businessRulesService.executeTaskRules(task, variables);
            
            // Complete the task
            taskService.complete(taskId, variables);
            
            // Update workflow instance
            updateWorkflowInstanceStatus(task.getProcessInstanceId());
            
            // Send notifications
            notificationService.sendTaskCompletedNotification(task, completedBy);
            
            // Publish event
            eventService.publishTaskCompleted(task, completedBy, variables);
            
            log.info("Task completed successfully: {}", taskId);
            
        } catch (Exception e) {
            log.error("Error completing task: {}", taskId, e);
            throw new RuntimeException("Failed to complete task: " + e.getMessage());
        }
    }
    
    /**
     * Get pending tasks for a user
     */
    public List<TaskDto> getPendingTasks(String userId, String role, List<String> groups) {
        log.debug("Getting pending tasks for user: {}", userId);
        
        try {
            var taskQuery = taskService.createTaskQuery()
                    .active()
                    .orderByTaskCreateTime()
                    .desc();
            
            // Filter by user, role, or groups
            if (userId != null) {
                taskQuery.or()
                        .taskAssignee(userId)
                        .taskCandidateUser(userId);
                
                if (role != null) {
                    taskQuery.taskCandidateGroup(role);
                }
                
                if (groups != null && !groups.isEmpty()) {
                    taskQuery.taskCandidateGroupIn(groups);
                }
                
                taskQuery.endOr();
            }
            
            List<Task> tasks = taskQuery.list();
            
            return tasks.stream()
                    .map(this::mapTaskToDto)
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error getting pending tasks for user: {}", userId, e);
            throw new RuntimeException("Failed to get pending tasks: " + e.getMessage());
        }
    }
    
    /**
     * Get workflow instances with filtering
     */
    public List<WorkflowInstanceDto> getWorkflowInstances(WorkflowInstanceFilterDto filter) {
        log.debug("Getting workflow instances with filter");
        
        try {
            var query = runtimeService.createProcessInstanceQuery();
            
            if (filter.getProcessDefinitionKey() != null) {
                query.processDefinitionKey(filter.getProcessDefinitionKey());
            }
            
            if (filter.getBusinessKey() != null) {
                query.processInstanceBusinessKey(filter.getBusinessKey());
            }
            
            if (filter.getStartedBy() != null) {
                query.variableValueEquals("startedBy", filter.getStartedBy());
            }
            
            var processInstances = query.orderByProcessInstanceId().desc().list();
            
            return processInstances.stream()
                    .map(pi -> {
                        var instance = workflowInstanceRepository.findByProcessInstanceId(pi.getId())
                                .orElse(WorkflowInstance.builder()
                                        .processInstanceId(pi.getId())
                                        .processDefinitionKey(pi.getProcessDefinitionKey())
                                        .businessKey(pi.getBusinessKey())
                                        .status("RUNNING")
                                        .build());
                        return mapToDto(instance);
                    })
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            log.error("Error getting workflow instances", e);
            throw new RuntimeException("Failed to get workflow instances: " + e.getMessage());
        }
    }
    
    /**
     * Cancel a workflow instance
     */
    public void cancelWorkflow(String processInstanceId, String reason, String cancelledBy) {
        log.info("Cancelling workflow: {} by user: {}", processInstanceId, cancelledBy);
        
        try {
            // Cancel process instance
            runtimeService.deleteProcessInstance(processInstanceId, reason);
            
            // Update database
            var instance = workflowInstanceRepository.findByProcessInstanceId(processInstanceId)
                    .orElseThrow(() -> new RuntimeException("Workflow instance not found"));
            
            instance.setStatus("CANCELLED");
            instance.setCancelledBy(cancelledBy);
            instance.setCancelledAt(LocalDateTime.now());
            instance.setCancellationReason(reason);
            
            workflowInstanceRepository.save(instance);
            
            // Send notifications
            notificationService.sendWorkflowCancelledNotification(instance, reason);
            
            // Publish event
            eventService.publishWorkflowCancelled(instance, reason);
            
            log.info("Workflow cancelled successfully: {}", processInstanceId);
            
        } catch (Exception e) {
            log.error("Error cancelling workflow: {}", processInstanceId, e);
            throw new RuntimeException("Failed to cancel workflow: " + e.getMessage());
        }
    }
    
    /**
     * Execute decision table (DMN)
     */
    public DecisionResultDto executeDecision(String decisionKey, Map<String, Object> variables) {
        log.info("Executing decision: {}", decisionKey);
        
        try {
            var decisionResult = decisionService.evaluateDecisionByKey(decisionKey)
                    .variables(variables)
                    .evaluate();
            
            var result = DecisionResultDto.builder()
                    .decisionKey(decisionKey)
                    .variables(variables)
                    .results(decisionResult.stream()
                            .map(entry -> entry.entryMap())
                            .collect(Collectors.toList()))
                    .executedAt(LocalDateTime.now())
                    .build();
            
            // Publish event
            eventService.publishDecisionExecuted(result);
            
            log.info("Decision executed successfully: {}", decisionKey);
            return result;
            
        } catch (Exception e) {
            log.error("Error executing decision: {}", decisionKey, e);
            throw new RuntimeException("Failed to execute decision: " + e.getMessage());
        }
    }
    
    /**
     * Get workflow analytics
     */
    public WorkflowAnalyticsDto getWorkflowAnalytics(String processDefinitionKey, int days) {
        log.info("Getting workflow analytics for: {} over {} days", processDefinitionKey, days);
        
        try {
            var startDate = LocalDateTime.now().minusDays(days);
            
            // Get completed instances
            var completedInstances = historyService.createHistoricProcessInstanceQuery()
                    .processDefinitionKey(processDefinitionKey)
                    .finished()
                    .startedAfter(java.util.Date.from(startDate.atZone(java.time.ZoneId.systemDefault()).toInstant()))
                    .list();
            
            // Get running instances
            var runningInstances = runtimeService.createProcessInstanceQuery()
                    .processDefinitionKey(processDefinitionKey)
                    .list();
            
            // Calculate metrics
            double avgDuration = completedInstances.stream()
                    .mapToLong(pi -> pi.getDurationInMillis() != null ? pi.getDurationInMillis() : 0)
                    .average()
                    .orElse(0.0);
            
            var analytics = WorkflowAnalyticsDto.builder()
                    .processDefinitionKey(processDefinitionKey)
                    .periodDays(days)
                    .totalStarted(completedInstances.size() + runningInstances.size())
                    .totalCompleted(completedInstances.size())
                    .totalRunning(runningInstances.size())
                    .averageDurationHours(avgDuration / (1000 * 60 * 60))
                    .completionRate(completedInstances.size() / (double)(completedInstances.size() + runningInstances.size()) * 100)
                    .build();
            
            log.info("Workflow analytics calculated for: {}", processDefinitionKey);
            return analytics;
            
        } catch (Exception e) {
            log.error("Error getting workflow analytics: {}", processDefinitionKey, e);
            throw new RuntimeException("Failed to get workflow analytics: " + e.getMessage());
        }
    }
    
    // Helper methods
    private String extractProcessDefinitionKey(BpmnModelInstance modelInstance) {
        return modelInstance.getModelElementsByType(org.camunda.bpm.model.bpmn.instance.Process.class)
                .stream()
                .findFirst()
                .map(org.camunda.bpm.model.bpmn.instance.Process::getId)
                .orElseThrow(() -> new RuntimeException("No process definition found in BPMN"));
    }
    
    private String extractTenantId(Map<String, Object> variables) {
        return (String) variables.getOrDefault("tenantId", "default");
    }
    
    private boolean isUserAuthorizedForTask(Task task, String userId) {
        return task.getAssignee() != null && task.getAssignee().equals(userId) ||
               taskService.createTaskQuery().taskId(task.getId()).taskCandidateUser(userId).count() > 0;
    }
    
    private void updateWorkflowInstanceStatus(String processInstanceId) {
        var instance = workflowInstanceRepository.findByProcessInstanceId(processInstanceId);
        if (instance.isPresent()) {
            var pi = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            
            if (pi == null) {
                // Process completed
                instance.get().setStatus("COMPLETED");
                instance.get().setCompletedAt(LocalDateTime.now());
                workflowInstanceRepository.save(instance.get());
            }
        }
    }
    
    private WorkflowDefinitionDto mapToDto(WorkflowDefinition definition) {
        return WorkflowDefinitionDto.builder()
                .id(definition.getId())
                .name(definition.getName())
                .version(definition.getVersion())
                .processDefinitionKey(definition.getProcessDefinitionKey())
                .isActive(definition.getIsActive())
                .deployedAt(definition.getDeployedAt())
                .build();
    }
    
    private WorkflowInstanceDto mapToDto(WorkflowInstance instance) {
        return WorkflowInstanceDto.builder()
                .id(instance.getId())
                .processInstanceId(instance.getProcessInstanceId())
                .processDefinitionKey(instance.getProcessDefinitionKey())
                .businessKey(instance.getBusinessKey())
                .status(instance.getStatus())
                .startedBy(instance.getStartedBy())
                .startedAt(instance.getStartedAt())
                .completedAt(instance.getCompletedAt())
                .variables(instance.getVariables())
                .build();
    }
    
    private TaskDto mapTaskToDto(Task task) {
        return TaskDto.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .assignee(task.getAssignee())
                .processInstanceId(task.getProcessInstanceId())
                .processDefinitionKey(task.getProcessDefinitionKey())
                .createdDate(task.getCreateTime().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                .dueDate(task.getDueDate() != null ? 
                        task.getDueDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .priority(task.getPriority())
                .formKey(task.getFormKey())
                .build();
    }
}