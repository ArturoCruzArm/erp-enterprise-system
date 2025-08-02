package com.erp.system.digitaltwin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for Digital Twin Service
 * 
 * This service provides advanced digital twin capabilities including:
 * - Real-time digital twin creation and management
 * - 3D visualization and simulation
 * - Predictive modeling and analytics
 * - Physical-digital synchronization
 * - Complex system behavior modeling
 * - Manufacturing process optimization
 * - Asset lifecycle management
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class DigitalTwinServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DigitalTwinServiceApplication.class, args);
    }
}