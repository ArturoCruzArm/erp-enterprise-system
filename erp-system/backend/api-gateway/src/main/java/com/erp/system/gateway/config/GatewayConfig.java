package com.erp.system.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("user-service", r -> r
                        .path("/api/users/**", "/api/auth/**")
                        .uri("lb://user-service"))
                
                // Finance Service Routes
                .route("finance-service", r -> r
                        .path("/api/finance/**", "/api/accounting/**")
                        .uri("lb://finance-service"))
                
                // Inventory Service Routes
                .route("inventory-service", r -> r
                        .path("/api/inventory/**", "/api/products/**")
                        .uri("lb://inventory-service"))
                
                // Purchase Service Routes
                .route("purchase-service", r -> r
                        .path("/api/purchases/**", "/api/suppliers/**")
                        .uri("lb://purchase-service"))
                
                // Sales Service Routes
                .route("sales-service", r -> r
                        .path("/api/sales/**", "/api/customers/**")
                        .uri("lb://sales-service"))
                
                // HR Service Routes
                .route("hr-service", r -> r
                        .path("/api/hr/**", "/api/employees/**")
                        .uri("lb://hr-service"))
                
                // Production Service Routes
                .route("production-service", r -> r
                        .path("/api/production/**", "/api/manufacturing/**")
                        .uri("lb://production-service"))
                
                .build();
    }
}