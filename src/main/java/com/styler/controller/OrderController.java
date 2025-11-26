package com.styler.controller;

import com.styler.model.*;
import com.styler.service.OrderService;
import com.styler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@Profile("prod")
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private UserService userService;
    
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Map<String, Object> request) {
        try {
           
            User user = null;
            
            if (request.containsKey("userId")) {
                Long userId = Long.valueOf(request.get("userId").toString());
                Optional<User> userOpt = userService.findById(userId);
                if (!userOpt.isPresent()) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "User not found with ID: " + userId);
                    return ResponseEntity.badRequest().body(errorResponse);
                }
                user = userOpt.get();
            } else if (request.containsKey("userEmail")) {
                String userEmail = (String) request.get("userEmail");
                Optional<User> userOpt = userService.findByEmail(userEmail);
                if (!userOpt.isPresent()) {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("success", false);
                    errorResponse.put("message", "User not found with email: " + userEmail);
                    return ResponseEntity.badRequest().body(errorResponse);
                }
                user = userOpt.get();
            } else {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Either userId or userEmail is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Parse order items
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsData = (List<Map<String, Object>>) request.get("items");
            List<OrderItem> items = new ArrayList<>();
            
            if (itemsData == null || itemsData.isEmpty()) {
                throw new IllegalArgumentException("Order must contain at least one item.");
            }

            for (Map<String, Object> itemData : itemsData) {
                OrderItem item = new OrderItem();
                
                // Safely parse required fields
                item.setProductId(safeGetLong(itemData, "productId"));
                item.setProductName(safeGetString(itemData, "productName"));
                item.setPrice(safeGetBigDecimal(itemData, "price"));
                item.setQuantity(safeGetInteger(itemData, "quantity"));

                // Optional fields with defaults
                item.setColor(itemData.containsKey("color") ? (String) itemData.get("color") : "");
                item.setSize(itemData.containsKey("size") ? (String) itemData.get("size") : "");
                item.setProductCategory(itemData.containsKey("category") ? (String) itemData.get("category") : "");
                item.setImageUrl(itemData.containsKey("imageUrl") ? (String) itemData.get("imageUrl") : "");
                
                // Basic validation
                if (item.getQuantity() < 1) {
                    throw new IllegalArgumentException("Item quantity must be at least 1 for product: " + item.getProductName());
                }
                if (item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Item price must be greater than 0 for product: " + item.getProductName());
                }

                items.add(item);
            }
            
            // Parse shipping address
            @SuppressWarnings("unchecked")
            Map<String, String> addressData = (Map<String, String>) request.get("shippingAddress");
            
            if (addressData == null) {
                throw new IllegalArgumentException("shippingAddress object is required.");
            }

            ShippingAddress shippingAddress = new ShippingAddress();
            
            // Handle both fullName and firstName/lastName
            if (addressData.containsKey("fullName")) {
                String[] nameParts = addressData.get("fullName").split("\\s+", 2);
                shippingAddress.setFirstName(nameParts[0]);
                shippingAddress.setLastName(nameParts.length > 1 ? nameParts[1] : "");
            } else {
                shippingAddress.setFirstName(addressData.getOrDefault("firstName", ""));
                shippingAddress.setLastName(addressData.getOrDefault("lastName", ""));
            }
            
            shippingAddress.setAddressLine1(addressData.getOrDefault("addressLine1", ""));
            shippingAddress.setAddressLine2(addressData.getOrDefault("addressLine2", ""));
            shippingAddress.setCity(addressData.getOrDefault("city", ""));
            shippingAddress.setState(addressData.getOrDefault("state", ""));
            shippingAddress.setPostalCode(addressData.getOrDefault("pincode", addressData.getOrDefault("postalCode", "")));
            shippingAddress.setPhone(addressData.getOrDefault("phone", ""));
            shippingAddress.setLandmark(addressData.getOrDefault("landmark", ""));
            
            // Parse total amount (use totalAmount if available, otherwise calculate)
            BigDecimal totalAmount;
            if (request.containsKey("totalAmount")) {
                totalAmount = new BigDecimal(request.get("totalAmount").toString());
            } else {
                totalAmount = new BigDecimal("0.00");
                for (OrderItem item : items) {
                    totalAmount = totalAmount.add(item.getPrice().multiply(new BigDecimal(item.getQuantity())));
                }
            }
            
            // Create order with simplified approach
            Order order = orderService.createSimpleOrder(user, items, shippingAddress, totalAmount, 
                (String) request.getOrDefault("paymentMethod", "COD"));
            
            // Create response matching frontend expectations
            Map<String, Object> response = new HashMap<>();
            response.put("id", order.getId());
            response.put("orderId", order.getOrderId());
            response.put("totalAmount", order.getTotalAmount());
            response.put("status", order.getStatus().toString());
            response.put("createdAt", order.getOrderDate());
            response.put("items", items.stream().map(item -> {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("productId", item.getProductId());
                itemMap.put("productName", item.getProductName());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("price", item.getPrice());
                return itemMap;
            }).toArray());
            response.put("shippingAddress", shippingAddress);
            response.put("success", true);
            response.put("message", "Order created successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error creating order: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable String orderId) {
        Optional<Order> orderOpt = orderService.findByOrderId(orderId);
        
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("orderId", order.getOrderId());
            response.put("status", order.getStatus().toString());
            response.put("totalAmount", order.getTotalAmount());
            response.put("subtotal", order.getSubtotal());
            response.put("shippingCost", order.getShippingCost());
            response.put("taxAmount", order.getTaxAmount());
            response.put("orderDate", order.getOrderDate());
            response.put("estimatedDelivery", order.getEstimatedDelivery());
            response.put("actualDelivery", order.getActualDelivery());
            response.put("shippingAddress", order.getShippingAddress());
            
            List<Map<String, Object>> itemsResponse = order.getItems().stream()
                .map(item -> {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("productId", item.getProductId());
                    itemMap.put("productName", item.getProductName());
                    itemMap.put("productCategory", item.getProductCategory());
                    itemMap.put("price", item.getPrice());
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("color", item.getColor());
                    itemMap.put("size", item.getSize());
                    itemMap.put("imageUrl", item.getImageUrl());
                    return itemMap;
                })
                .collect(Collectors.toList());
            
            response.put("items", itemsResponse);
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserOrders(@PathVariable Long userId) {
        Optional<User> userOpt = userService.findById(userId);
        
        if (!userOpt.isPresent()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "User not found with ID: " + userId);
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        User user = userOpt.get();
        List<Order> orders = orderService.getUserOrders(user);
        
        List<Map<String, Object>> ordersResponse = orders.stream()
            .map(order -> {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("id", order.getId());
                orderMap.put("orderId", order.getOrderId());
                orderMap.put("status", order.getStatus().toString());
                orderMap.put("totalAmount", order.getTotalAmount());
                orderMap.put("createdAt", order.getOrderDate()); // Use 'createdAt' for consistency
                orderMap.put("estimatedDelivery", order.getEstimatedDelivery());
                orderMap.put("shippingAddress", order.getShippingAddress());
                
                // Include item details, as the frontend likely needs them
                orderMap.put("items", order.getItems().stream().map(item -> {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("productId", item.getProductId());
                    itemMap.put("productName", item.getProductName());
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("price", item.getPrice());
                    itemMap.put("imageUrl", item.getImageUrl());
                    return itemMap;
                }).collect(Collectors.toList()));

                return orderMap;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(ordersResponse);
    }
    
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable String orderId, @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            Order updatedOrder = orderService.updateOrderStatus(orderId, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order status updated successfully");
            response.put("orderId", updatedOrder.getOrderId());
            response.put("status", updatedOrder.getStatus().toString());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // Helper methods for safe parsing from a Map
    private String safeGetString(Map<String, Object> map, String key) {
        if (!map.containsKey(key) || map.get(key) == null) {
            throw new IllegalArgumentException("Missing required field: " + key);
        }
        return map.get(key).toString();
    }

    private Long safeGetLong(Map<String, Object> map, String key) {
        String value = safeGetString(map, key);
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format for field '" + key + "': " + value);
        }
    }

    private Integer safeGetInteger(Map<String, Object> map, String key) {
        String value = safeGetString(map, key);
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer format for field '" + key + "': " + value);
        }
    }

    private BigDecimal safeGetBigDecimal(Map<String, Object> map, String key) {
        String value = safeGetString(map, key);
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid decimal format for field '" + key + "': " + value);
        }
    }
}