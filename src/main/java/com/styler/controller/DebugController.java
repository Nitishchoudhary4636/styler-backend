package com.styler.controller;

import com.styler.model.*;
import com.styler.service.OrderService;
import com.styler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/debug")
@CrossOrigin(origins = "*")
public class DebugController {

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    // Get all users with full details
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            Map<String, Object> response = new HashMap<>();
            response.put("count", users.size());
            response.put("users", users);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // Get all orders with full details
    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders() {
        try {
            List<Order> orders = orderService.getAllOrders();
            Map<String, Object> response = new HashMap<>();
            response.put("count", orders.size());
            response.put("orders", orders);
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // Get database statistics
    @GetMapping("/stats")
    public ResponseEntity<?> getDatabaseStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", userService.getAllUsers().size());
            stats.put("totalOrders", orderService.getAllOrders().size());
            stats.put("timestamp", System.currentTimeMillis());
            stats.put("status", "Database connected and operational");
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "Database error");
            return ResponseEntity.status(500).body(error);
        }
    }

    // Test order creation with sample data
    @PostMapping("/test-order")
    public ResponseEntity<?> testOrderCreation(@RequestBody(required = false) Map<String, Object> testData) {
        try {
            // Create test order data if none provided
            if (testData == null || testData.isEmpty()) {
                testData = new HashMap<>();
                testData.put("userEmail", "test@example.com");
                testData.put("totalAmount", 99.99);
                testData.put("items", Arrays.asList(
                    Map.of("name", "Test Product", "price", 49.99, "quantity", 2)
                ));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Test order data received successfully");
            response.put("receivedData", testData);
            response.put("timestamp", System.currentTimeMillis());
            response.put("status", "Order creation endpoint is working");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // Test user registration
    @PostMapping("/test-register")
    public ResponseEntity<?> testUserRegistration() {
        try {
            String testEmail = "debugtest" + System.currentTimeMillis() + "@example.com";
            User user = userService.createUser(testEmail, "test123", "Debug", "Test", "1234567890");
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registration working");
            response.put("createdUser", Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "name", user.getFirstName() + " " + user.getLastName()
            ));
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // Clear test data (for cleanup)
    @DeleteMapping("/cleanup")
    public ResponseEntity<?> cleanupTestData() {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cleanup endpoint available");
            response.put("note", "Manual cleanup required - check database for test entries");
            response.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}