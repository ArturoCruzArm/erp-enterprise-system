package com.erp.system.quantum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for Quantum Computing Service
 * 
 * This service provides advanced quantum computing capabilities including:
 * - Quantum optimization algorithms (QAOA, VQE, etc.)
 * - Supply chain optimization using quantum annealing
 * - Portfolio optimization with quantum machine learning
 * - Cryptographic security using quantum key distribution
 * - Complex combinatorial problem solving
 * - Quantum simulation for materials science
 * - Hybrid classical-quantum algorithms
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class QuantumComputingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuantumComputingServiceApplication.class, args);
    }
}