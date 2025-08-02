package com.erp.system.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        long startTime = System.currentTimeMillis();
        
        log.info("Request: {} {} from {} at {}",
                request.getMethod(),
                request.getURI(),
                request.getRemoteAddress(),
                LocalDateTime.now());

        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    
                    log.info("Response: {} {} - Status: {} - Duration: {}ms",
                            request.getMethod(),
                            request.getURI(),
                            exchange.getResponse().getStatusCode(),
                            duration);
                })
        );
    }

    @Override
    public int getOrder() {
        return -2; // Execute before authentication filter
    }
}