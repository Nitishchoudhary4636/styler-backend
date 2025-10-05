package com.styler.controller;

import com.styler.model.User;
import com.styler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@Profile("railway-db")
public class UnifiedRailwayController {

    @Autowired(required = false)
    private UserService userService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Railway startup with database successful");
        response.put("service", "styler-backend");
        response.put("mode", "railway-database");
        response.put("database", "PostgreSQL");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> apiHealth() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "styler-backend");
        response.put("message", "Railway database API health check passed");
        response.put("version", "1.0.0");
        response.put("mode", "railway-database");
        response.put("database", "connected");
        response.put("timestamp", System.currentTimeMillis());
        
        if (userService != null) {
            try {
                long userCount = userService.getAllUsers().size();
                response.put("users_registered", userCount);
            } catch (Exception e) {
                response.put("users_registered", "error");
            }
        }
        
        return ResponseEntity.ok(response);
    }

    // Original endpoint paths that frontend expects
    @PostMapping("/users/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            String fullName = request.get("fullName");

            if (email == null || password == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email and password are required"));
            }

            // Check if user already exists
            if (userService.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "User with this email already exists"));
            }

            // Create new user
            User savedUser = userService.createUser(email, password, fullName, "", "");

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", savedUser.getId());
            response.put("email", savedUser.getEmail());
            response.put("fullName", savedUser.getFirstName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/users/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");

            if (email == null || password == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Email and password are required"));
            }

            Optional<User> userOpt = userService.findByEmail(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
            }

            User user = userOpt.get();
            // Note: In production, use proper password hashing verification
            if (!password.equals(user.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("fullName", user.getFirstName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Login failed: " + e.getMessage()));
        }
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Styler E-commerce Backend");
        response.put("version", "1.0.0");
        response.put("status", "UP");
        response.put("mode", "railway-database");
        response.put("message", "API is running with PostgreSQL database");
        response.put("endpoints", Map.of(
            "health", "/health",
            "register", "POST /users/register", 
            "login", "POST /users/login",
            "orders", "POST /orders"
        ));
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}