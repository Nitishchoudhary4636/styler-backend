package com.styler.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@CrossOrigin(origins = "*")
@Profile("railway-fast")
public class FastRailwayController {

    // In-memory storage for demonstration
    private static final Map<String, Map<String, Object>> users = new ConcurrentHashMap<>();
    private static final Map<String, Map<String, Object>> orders = new ConcurrentHashMap<>();
    private static final AtomicLong userIdGenerator = new AtomicLong(1);
    private static final AtomicLong orderIdGenerator = new AtomicLong(1);

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
            "/api/users/login - User Login (In-Memory)",
            "/api/orders - Order Creation (In-Memory)",
            "/api/orders/{orderId} - Order Details (In-Memory)"
        });
        response.put("note", "Ultra-fast startup for Railway health checks. Data stored in memory.");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/orders")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate required fields
            if (!request.containsKey("items") || !request.containsKey("totalAmount")) {
                response.put("success", false);
                response.put("message", "Items and totalAmount are required");
                return ResponseEntity.badRequest().body(response);
            }

            // Get or create user
            String userEmail = (String) request.get("userEmail");
            String userId = (String) request.get("userId");
            
            Map<String, Object> user = null;
            if (userEmail != null) {
                user = users.get(userEmail);
            } else if (userId != null) {
                user = users.values().stream()
                    .filter(u -> userId.equals(u.get("id").toString()))
                    .findFirst()
                    .orElse(null);
            }
            
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found. Please register first.");
                return ResponseEntity.badRequest().body(response);
            }

            // Generate order
            Long orderId = orderIdGenerator.getAndIncrement();
            String orderIdString = "ORD-" + String.format("%06d", orderId);
            
            Map<String, Object> order = new HashMap<>();
            order.put("id", orderId);
            order.put("orderId", orderIdString);
            order.put("userId", user.get("id"));
            order.put("userEmail", user.get("email"));
            order.put("items", request.get("items"));
            order.put("totalAmount", request.get("totalAmount"));
            order.put("shippingAddress", request.get("shippingAddress"));
            order.put("paymentMethod", request.getOrDefault("paymentMethod", "COD"));
            order.put("status", "CONFIRMED");
            order.put("createdAt", System.currentTimeMillis());
            order.put("mode", "railway-fast");
            
            // Store order
            orders.put(orderIdString, order);
            
            // Success response
            response.put("success", true);
            response.put("message", "Order created successfully");
            response.put("id", orderId);
            response.put("orderId", orderIdString);
            response.put("status", "CONFIRMED");
            response.put("totalAmount", request.get("totalAmount"));
            response.put("items", request.get("items"));
            response.put("shippingAddress", request.get("shippingAddress"));
            response.put("createdAt", order.get("createdAt"));
            response.put("mode", "railway-fast");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Order creation failed: " + e.getMessage());
            response.put("mode", "railway-fast");
            
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/api/orders/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable String orderId) {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, Object> order = orders.get(orderId);
        if (order == null) {
            response.put("success", false);
            response.put("message", "Order not found");
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(order);
    }

    @GetMapping("/api/orders/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserOrders(@PathVariable String userId) {
        Map<String, Object> response = new HashMap<>();
        
        List<Map<String, Object>> userOrders = orders.values().stream()
            .filter(order -> userId.equals(order.get("userId").toString()))
            .collect(java.util.stream.Collectors.toList());
        
        response.put("success", true);
        response.put("orders", userOrders);
        response.put("count", userOrders.size());
        response.put("mode", "railway-fast");
        
        return ResponseEntity.ok(response);
    }
}