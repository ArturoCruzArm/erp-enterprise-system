package com.erp.system.edge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for Edge Computing Service
 * 
 * This service provides advanced edge computing capabilities including:
 * - IoT device management and communication
 * - Real-time stream processing
 * - Edge analytics and machine learning
 * - MQTT and CoAP protocol support
 * - Time series data management
 * - Distributed computing coordination
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class EdgeComputingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EdgeComputingServiceApplication.class, args);
    }
}