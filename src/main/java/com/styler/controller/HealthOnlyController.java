package com.styler.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.context.annotation.Profile;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@Profile("health-only")
public class HealthOnlyController {

    @GetMapping("/")
    public Map<String, Object> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "Styler E-commerce Backend");
        response.put("version", "1.0.0");
        response.put("status", "Running (Health Check Mode)");
        response.put("mode", "health-only");
        response.put("endpoints", new String[]{
            "/health - Health check",
            "/api/health - API Health check"
        });
        return response;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Styler Backend is running successfully (Health Mode)");
        response.put("timestamp", System.currentTimeMillis());
        response.put("mode", "health-only");
        return response;
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> apiHealth() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("status", "UP");
            response.put("message", "Styler Backend API is healthy");
            response.put("timestamp", System.currentTimeMillis());
            response.put("service", "styler-backend");
            response.put("version", "1.0.0");
            response.put("environment", "railway-production");
            response.put("mode", "health-only");
            
            return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(response);
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("message", "Health check failed: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(503)
                .header("Content-Type", "application/json")
                .body(response);
        }
    }
}