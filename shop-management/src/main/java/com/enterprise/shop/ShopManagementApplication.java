package com.enterprise.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Shop Management Application - Orchestrator Service
 * Runs on Server A (Port 8080)
 * 
 * Responsibilities:
 * - Acts as the gateway for external clients
 * - Orchestrates calls to product-stock service (Server B)
 * - Supports REST, SOAP, and GraphQL protocols
 */
@SpringBootApplication
@EnableAspectJAutoProxy
@EnableAsync
public class ShopManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopManagementApplication.class, args);
    }
}
