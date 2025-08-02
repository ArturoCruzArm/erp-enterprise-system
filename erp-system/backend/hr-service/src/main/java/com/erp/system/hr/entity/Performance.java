package com.erp.system.hr.entity;

import com.erp.system.entity.BaseEntity;
import com.erp.system.hr.enums.PerformanceRating;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "performances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Performance extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required")
    private Employee employee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    @NotNull(message = "Reviewer is required")
    private Employee reviewer;
    
    @Column(name = "review_period_start", nullable = false)
    @NotNull(message = "Review period start is required")
    private LocalDate reviewPeriodStart;
    
    @Column(name = "review_period_end", nullable = false)
    @NotNull(message = "Review period end is required")
    private LocalDate reviewPeriodEnd;
    
    @Column(name = "review_date", nullable = false)
    @NotNull(message = "Review date is required")
    private LocalDate reviewDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "overall_rating", nullable = false)
    @NotNull(message = "Overall rating is required")
    private PerformanceRating overallRating;
    
    @Column(name = "technical_skills_rating")
    @Enumerated(EnumType.STRING)
    private PerformanceRating technicalSkillsRating;
    
    @Column(name = "communication_rating")
    @Enumerated(EnumType.STRING)
    private PerformanceRating communicationRating;
    
    @Column(name = "teamwork_rating")
    @Enumerated(EnumType.STRING)
    private PerformanceRating teamworkRating;
    
    @Column(name = "leadership_rating")
    @Enumerated(EnumType.STRING)
    private PerformanceRating leadershipRating;
    
    @Column(name = "problem_solving_rating")
    @Enumerated(EnumType.STRING)
    private PerformanceRating problemSolvingRating;
    
    @Column(name = "achievements", columnDefinition = "TEXT")
    private String achievements;
    
    @Column(name = "areas_for_improvement", columnDefinition = "TEXT")
    private String areasForImprovement;
    
    @Column(name = "goals_for_next_period", columnDefinition = "TEXT")
    private String goalsForNextPeriod;
    
    @Column(name = "training_recommendations", columnDefinition = "TEXT")
    private String trainingRecommendations;
    
    @Column(name = "employee_comments", columnDefinition = "TEXT")
    private String employeeComments;
    
    @Column(name = "manager_comments", columnDefinition = "TEXT")
    private String managerComments;
    
    @Column(name = "hr_comments", columnDefinition = "TEXT")
    private String hrComments;
    
    @Column(name = "promotion_recommended")
    private Boolean promotionRecommended = false;
    
    @Column(name = "salary_increase_recommended")
    private Boolean salaryIncreaseRecommended = false;
    
    @Column(name = "performance_improvement_plan")
    private Boolean performanceImprovementPlan = false;
}