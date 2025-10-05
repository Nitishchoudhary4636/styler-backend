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
            "/api/health - API Health Check",
            "/api/users/register - User Registration (Minimal Mode)",
            "/api/users/login - User Login (Minimal Mode)"
        });
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/users/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            String name = request.get("name");
            String phone = request.get("phone");
            String password = request.get("password");
            
            if (email == null || name == null || phone == null || password == null) {
                response.put("success", false);
                response.put("message", "All fields are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Simple success response for Railway minimal mode
            response.put("success", true);
            response.put("message", "User registered successfully in Railway minimal mode");
            response.put("mode", "railway-minimal");
            response.put("id", Math.abs(email.hashCode()) % 10000); // Simple ID generation
            response.put("email", email);
            response.put("name", name);
            response.put("phone", phone);
            response.put("note", "Data not persisted - upgrade to production mode for database storage");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            response.put("mode", "railway-minimal");
            
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/api/users/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Login simulation - Railway minimal mode");
        response.put("mode", "railway-minimal");
        response.put("email", request.get("email"));
        response.put("note", "Upgrade to production mode for real authentication");
        
        return ResponseEntity.ok(response);
    }
}