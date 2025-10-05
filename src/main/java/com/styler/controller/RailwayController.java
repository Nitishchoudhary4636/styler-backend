package com.styler.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@Profile("railway")
public class RailwayController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Railway deployment healthy");
        response.put("service", "styler-backend");
        response.put("mode", "railway-minimal");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> apiHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "styler-backend");
        response.put("message", "Railway API health check passed");
        response.put("version", "1.0.0");
        response.put("mode", "railway-minimal");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "Styler E-commerce Backend");
        response.put("version", "1.0.0");
        response.put("status", "Running on Railway");
        response.put("mode", "railway-minimal");
        response.put("message", "Switch to production mode with database for full functionality");
        response.put("endpoints", new String[]{
            "/health - Railway Health Check",
            "/api/health - API Health Check"
        });
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/users/register")
    public ResponseEntity<Map<String, Object>> registerNotAvailable() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "User registration not available in railway-minimal mode");
        response.put("mode", "railway-minimal");
        response.put("action", "Add PostgreSQL database and switch to production profile");
        
        return ResponseEntity.ok(response);
    }
}