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
@Profile("render-prod")
public class RenderController {

    @Autowired(required = false)
    private UserService userService;

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Styler E-commerce Backend");
        response.put("version", "1.0.0");
        response.put("status", "UP");
        response.put("platform", "Render");
        response.put("database", "PostgreSQL");
        response.put("message", "Welcome to Styler Backend API");
        response.put("endpoints", Map.of(
            "health", "GET /health",
            "register", "POST /users/register", 
            "login", "POST /users/login",
            "api-docs", "GET /api"
        ));
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Render deployment successful");
        response.put("service", "styler-backend");
        response.put("platform", "Render");
        response.put("database", "PostgreSQL");
        response.put("timestamp", System.currentTimeMillis());
        
        // Test database connection
        if (userService != null) {
            try {
                long userCount = userService.getAllUsers().size();
                response.put("database_status", "connected");
                response.put("users_count", userCount);
            } catch (Exception e) {
                response.put("database_status", "error: " + e.getMessage());
                response.put("users_count", 0);
            }
        } else {
            response.put("database_status", "service_not_available");
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api")
    public ResponseEntity<Map<String, Object>> apiInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("service", "Styler E-commerce API");
        response.put("version", "1.0.0");
        response.put("platform", "Render");
        response.put("endpoints", Map.of(
            "POST /users/register", "Register new user",
            "POST /users/login", "User login",
            "GET /health", "Health check",
            "GET /api", "API information"
        ));
        response.put("status", "operational");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    // User Registration - Original endpoint path
    @PostMapping("/users/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            String password = request.get("password");
            String fullName = request.get("fullName");

            if (email == null || password == null) {
                response.put("success", false);
                response.put("message", "Email and password are required");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if user already exists
            if (userService.findByEmail(email).isPresent()) {
                response.put("success", false);
                response.put("message", "User with this email already exists");
                return ResponseEntity.badRequest().body(response);
            }

            // Create new user
            User savedUser = userService.createUser(email, password, fullName, "", "");

            response.put("success", true);
            response.put("message", "User registered successfully");
            response.put("userId", savedUser.getId());
            response.put("email", savedUser.getEmail());
            response.put("fullName", savedUser.getFirstName());
            response.put("platform", "Render");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration failed: " + e.getMessage());
            response.put("platform", "Render");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // User Login - Original endpoint path
    @PostMapping("/users/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            String password = request.get("password");

            if (email == null || password == null) {
                response.put("success", false);
                response.put("message", "Email and password are required");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<User> userOpt = userService.authenticateUser(email, password);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Invalid email or password");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            User user = userOpt.get();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("userId", user.getId());
            response.put("email", user.getEmail());
            response.put("fullName", user.getFirstName());
            response.put("platform", "Render");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Login failed: " + e.getMessage());
            response.put("platform", "Render");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // Test endpoint for debugging
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Test endpoint working");
        response.put("platform", "Render");
        response.put("timestamp", System.currentTimeMillis());
        
        if (userService != null) {
            response.put("userService", "available");
            try {
                response.put("users", userService.getAllUsers().size());
            } catch (Exception e) {
                response.put("userService", "error: " + e.getMessage());
            }
        } else {
            response.put("userService", "not_available");
        }
        
        return ResponseEntity.ok(response);
    }
}