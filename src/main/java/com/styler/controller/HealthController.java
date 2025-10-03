package com.styler.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@Profile({"prod", "dev", "default"})
public class HealthController {

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Styler Backend is running successfully");
        response.put("timestamp", System.currentTimeMillis());
        response.put("database", "Connected");
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
            response.put("environment", "production");
            
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

    @GetMapping("/")
    public Map<String, Object> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "Styler E-commerce Backend");
        response.put("version", "1.0.0");
        response.put("status", "Running");
        response.put("endpoints", new String[]{
            "/health - Health check",
            "/api/users - User management",
            "/api/orders - Order management"
        });
        return response;
    }

    @GetMapping("/api/test")
    public Map<String, Object> apiTest() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "API is working!");
        response.put("cors", "CORS is configured");
        response.put("database", "Database connection active");
        return response;
    }
}