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
@CrossOrigin(origins = "*")
@Profile({"prod", "dev", "default", "railway-prod"})
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
            
            for (Map<String, Object> itemData : itemsData) {
                OrderItem item = new OrderItem();
                item.setProductId(Long.valueOf(itemData.get("productId").toString()));
                item.setProductName((String) itemData.get("productName"));
                item.setPrice(new BigDecimal(itemData.get("price").toString()));
                item.setQuantity(Integer.valueOf(itemData.get("quantity").toString()));
                
                // Optional fields with defaults
                item.setColor(itemData.containsKey("color") ? (String) itemData.get("color") : "");
                item.setSize(itemData.containsKey("size") ? (String) itemData.get("size") : "");
                item.setProductCategory(itemData.containsKey("category") ? (String) itemData.get("category") : "");
                item.setImageUrl(itemData.containsKey("imageUrl") ? (String) itemData.get("imageUrl") : "");
                
                items.add(item);
            }
            
            // Parse shipping address
            @SuppressWarnings("unchecked")
            Map<String, String> addressData = (Map<String, String>) request.get("shippingAddress");
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
            shippingAddress.setLandmark(addressData.getOrDefault("phone", "")); // Store phone in landmark field temporarily
            
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
                orderMap.put("orderId", order.getOrderId());
                orderMap.put("status", order.getStatus().toString());
                orderMap.put("totalAmount", order.getTotalAmount());
                orderMap.put("orderDate", order.getOrderDate());
                orderMap.put("estimatedDelivery", order.getEstimatedDelivery());
                orderMap.put("itemCount", order.getItems().size());
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
}