package com.styler.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@CrossOrigin(origins = "*")
@Profile("railway-fast")
public class FastRailwayController {

    // In-memory storage for demonstration
    private static final Map<String, Map<String, Object>> users = new ConcurrentHashMap<>();
    private static final AtomicLong userIdGenerator = new AtomicLong(1);

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Ultra-fast Railway startup successful");
        response.put("service", "styler-backend");
        response.put("mode", "railway-fast");
        response.put("startup_time", "< 15 seconds");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> apiHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "styler-backend");
        response.put("message", "Railway fast API health check passed");
        response.put("version", "1.0.0");
        response.put("mode", "railway-fast");
        response.put("users_registered", users.size());
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

            // Check if user already exists
            if (users.containsKey(email)) {
                response.put("success", false);
                response.put("message", "User with email " + email + " already exists");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Create user record
            Long userId = userIdGenerator.getAndIncrement();
            Map<String, Object> user = new HashMap<>();
            user.put("id", userId);
            user.put("email", email);
            user.put("name", name);
            user.put("phone", phone);
            user.put("password", password); // In real app, this would be hashed
            user.put("joinDate", System.currentTimeMillis());
            
            users.put(email, user);
            
            // Success response
            response.put("success", true);
            response.put("message", "User registered successfully");
            response.put("id", userId);
            response.put("email", email);
            response.put("name", name);
            response.put("phone", phone);
            response.put("mode", "railway-fast");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            response.put("mode", "railway-fast");
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/api/users/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            if (email == null || password == null) {
                response.put("success", false);
                response.put("message", "Email and password are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> user = users.get(email);
            if (user == null || !password.equals(user.get("password"))) {
                response.put("success", false);
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(401).body(response);
            }
            
            // Success response
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("id", user.get("id"));
            response.put("email", user.get("email"));
            response.put("name", user.get("name"));
            response.put("phone", user.get("phone"));
            response.put("mode", "railway-fast");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Login failed: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "Styler E-commerce Backend");
        response.put("version", "1.0.0");
        response.put("status", "Running on Railway - Ultra Fast Mode");
        response.put("mode", "railway-fast");
        response.put("startup_time", "< 15 seconds");
        response.put("users_registered", users.size());
        response.put("endpoints", new String[]{
            "/health - Railway Health Check",
            "/api/health - API Health Check",
            "/api/users/register - User Registration (In-Memory)",
            "/api/users/login - User Login (In-Memory)"
        });
        response.put("note", "Ultra-fast startup for Railway health checks. Data stored in memory.");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}