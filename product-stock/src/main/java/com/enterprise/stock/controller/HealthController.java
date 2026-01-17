package com.enterprise.stock.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller for Product Stock Service
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "product-stock");
        health.put("timestamp", LocalDateTime.now().toString());
        health.put("version", "1.0.0");
        return ResponseEntity.ok(health);
    }

    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "READY");
        health.put("service", "product-stock");
        health.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(health);
    }

    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "LIVE");
        health.put("service", "product-stock");
        health.put("timestamp", LocalDateTime.now().toString());
        return ResponseEntity.ok(health);
    }
}
