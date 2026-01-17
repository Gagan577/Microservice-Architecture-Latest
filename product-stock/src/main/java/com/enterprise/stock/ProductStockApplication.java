package com.enterprise.stock;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Product Stock Application - Backend Service
 * Runs on Server B (Port 8081)
 * 
 * Responsibilities:
 * - Manages product stock inventory
 * - Provides REST, SOAP, and GraphQL APIs
 * - Handles stock reservations, thresholds, and warehouse operations
 */
@SpringBootApplication
@EnableAspectJAutoProxy
public class ProductStockApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductStockApplication.class, args);
    }
}
