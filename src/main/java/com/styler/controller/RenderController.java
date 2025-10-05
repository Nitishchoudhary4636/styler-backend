package com.styler.controller;

import com.styler.model.User;
import com.styler.model.Order;
import com.styler.model.OrderItem;
import com.styler.model.ShippingAddress;
import com.styler.service.UserService;
import com.styler.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

@RestController
@CrossOrigin(origins = "*")
@Profile("render-prod")
public class RenderController {

    @Autowired(required = false)
    private UserService userService;

    @Autowired(required = false)
    private OrderService orderService;

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
            "orders", "POST /api/orders",
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
        
        if (orderService != null) {
            try {
                long orderCount = orderService.getAllOrders().size();
                response.put("orders_count", orderCount);
            } catch (Exception e) {
                response.put("orders_error", e.getMessage());
            }
        }
        
        return ResponseEntity.ok(response);
    }

    // Debug endpoint to see all orders
    @GetMapping("/debug/orders")
    public ResponseEntity<Map<String, Object>> debugOrders() {
        Map<String, Object> response = new HashMap<>();
        
        if (orderService != null) {
            try {
                List<Order> orders = orderService.getAllOrders();
                response.put("total_orders", orders.size());
                response.put("orders", orders.stream().map(order -> {
                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("orderId", order.getOrderId());
                    orderMap.put("status", order.getStatus().toString());
                    orderMap.put("totalAmount", order.getTotalAmount());
                    orderMap.put("userEmail", order.getUser().getEmail());
                    orderMap.put("itemCount", order.getItems().size());
                    return orderMap;
                }).toArray());
                response.put("success", true);
            } catch (Exception e) {
                response.put("success", false);
                response.put("error", e.getMessage());
            }
        } else {
            response.put("success", false);
            response.put("error", "OrderService not available");
        }
        
        return ResponseEntity.ok(response);
    }

    // Alternative order creation endpoint with enhanced debugging
    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrderDebug(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== ORDER CREATION DEBUG ===");
            System.out.println("Request received: " + request);
            
            // Find user
            String userEmail = (String) request.get("userEmail");
            if (userEmail == null) {
                response.put("success", false);
                response.put("message", "userEmail is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            User user = userService.findByEmail(userEmail).orElse(null);
            if (user == null) {
                response.put("success", false);
                response.put("message", "User not found: " + userEmail);
                return ResponseEntity.badRequest().body(response);
            }
            
            System.out.println("User found: " + user.getEmail());
            
            // Parse order data
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsData = (List<Map<String, Object>>) request.get("items");
            if (itemsData == null || itemsData.isEmpty()) {
                response.put("success", false);
                response.put("message", "Items are required");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<OrderItem> items = new ArrayList<>();
            for (Map<String, Object> itemData : itemsData) {
                OrderItem item = new OrderItem();
                item.setProductId(Long.valueOf(itemData.get("productId").toString()));
                item.setProductName((String) itemData.get("productName"));
                item.setPrice(new BigDecimal(itemData.get("price").toString()));
                item.setQuantity(Integer.valueOf(itemData.get("quantity").toString()));
                item.setColor((String) itemData.getOrDefault("color", ""));
                item.setSize((String) itemData.getOrDefault("size", ""));
                items.add(item);
            }
            
            System.out.println("Items parsed: " + items.size());
            
            // Parse shipping address
            @SuppressWarnings("unchecked")
            Map<String, String> addressData = (Map<String, String>) request.get("shippingAddress");
            ShippingAddress shippingAddress = new ShippingAddress();
            
            if (addressData.containsKey("fullName")) {
                String[] nameParts = addressData.get("fullName").split("\\s+", 2);
                shippingAddress.setFirstName(nameParts[0]);
                shippingAddress.setLastName(nameParts.length > 1 ? nameParts[1] : "");
            }
            shippingAddress.setAddressLine1(addressData.getOrDefault("addressLine1", ""));
            shippingAddress.setCity(addressData.getOrDefault("city", ""));
            shippingAddress.setState(addressData.getOrDefault("state", ""));
            shippingAddress.setPostalCode(addressData.getOrDefault("pincode", ""));
            
            System.out.println("Shipping address parsed");
            
            // Calculate total
            BigDecimal totalAmount = new BigDecimal(request.get("totalAmount").toString());
            
            System.out.println("Creating order with total: " + totalAmount);
            
            // Create order
            Order order = orderService.createSimpleOrder(user, items, shippingAddress, totalAmount, "COD");
            
            System.out.println("Order created with ID: " + order.getOrderId());
            
            // Return response
            response.put("success", true);
            response.put("message", "Order created successfully");
            response.put("orderId", order.getOrderId());
            response.put("id", order.getId());
            response.put("status", order.getStatus().toString());
            response.put("totalAmount", order.getTotalAmount());
            response.put("userEmail", user.getEmail());
            response.put("itemCount", order.getItems().size());
            response.put("platform", "Render");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("Order creation error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Order creation failed: " + e.getMessage());
            response.put("platform", "Render");
            return ResponseEntity.status(500).body(response);
        }
    }

    // Get order with enhanced debugging
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderDebug(@PathVariable String orderId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            System.out.println("=== ORDER RETRIEVAL DEBUG ===");
            System.out.println("Looking for order: " + orderId);
            
            Optional<Order> orderOpt = orderService.findByOrderId(orderId);
            
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                System.out.println("Order found: " + order.getOrderId());
                
                response.put("success", true);
                response.put("orderId", order.getOrderId());
                response.put("status", order.getStatus().toString());
                response.put("totalAmount", order.getTotalAmount());
                response.put("orderDate", order.getOrderDate());
                response.put("userEmail", order.getUser().getEmail());
                
                // Items
                List<Map<String, Object>> itemsResponse = new ArrayList<>();
                for (OrderItem item : order.getItems()) {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("productId", item.getProductId());
                    itemMap.put("productName", item.getProductName());
                    itemMap.put("price", item.getPrice());
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("color", item.getColor());
                    itemMap.put("size", item.getSize());
                    itemsResponse.add(itemMap);
                }
                response.put("items", itemsResponse);
                
                // Shipping address
                if (order.getShippingAddress() != null) {
                    Map<String, Object> addressMap = new HashMap<>();
                    ShippingAddress addr = order.getShippingAddress();
                    addressMap.put("fullName", addr.getFirstName() + " " + addr.getLastName());
                    addressMap.put("addressLine1", addr.getAddressLine1());
                    addressMap.put("city", addr.getCity());
                    addressMap.put("state", addr.getState());
                    addressMap.put("pincode", addr.getPostalCode());
                    response.put("shippingAddress", addressMap);
                }
                
                return ResponseEntity.ok(response);
            } else {
                System.out.println("Order not found: " + orderId);
                response.put("success", false);
                response.put("message", "Order not found: " + orderId);
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            System.err.println("Order retrieval error: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Order retrieval failed: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Test endpoint
    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Render controller test endpoint working");
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
        
        if (orderService != null) {
            response.put("orderService", "available");
            try {
                response.put("orders", orderService.getAllOrders().size());
            } catch (Exception e) {
                response.put("orderService", "error: " + e.getMessage());
            }
        } else {
            response.put("orderService", "not_available");
        }
        
        return ResponseEntity.ok(response);
    }
}