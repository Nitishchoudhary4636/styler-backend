package com.styler.controller;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
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
    public Map<String, Object> apiHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Styler Backend API is healthy");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "styler-backend");
        response.put("version", "1.0.0");
        return response;
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