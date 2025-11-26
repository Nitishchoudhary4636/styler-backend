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
            
            String requestEmail = request.containsKey("userEmail") ? (String) request.get("userEmail") : null;
            Long requestUserId = request.containsKey("userId") ? Long.valueOf(request.get("userId").toString()) : null;
            user = resolveUser(requestUserId, requestEmail, request);
            if (user == null) {
                return badRequest("User not found. Please log in or register before placing an order.");
            }
            
            // Parse order items
            List<Map<String, Object>> itemsData = getList(request, "items");
            if (itemsData.isEmpty()) {
                return badRequest("items array is required and cannot be empty");
            }
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
            Map<String, String> addressData = getMap(request, "shippingAddress");
            if (addressData.isEmpty()) {
                return badRequest("shippingAddress object is required");
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
            BigDecimal totalAmount = calculateTotalAmount(request, items);
            
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

    private List<Map<String, Object>> getList(Map<String, Object> payload, String key) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> value = (List<Map<String, Object>>) payload.get(key);
        return value == null ? Collections.emptyList() : value;
    }

    private Map<String, String> getMap(Map<String, Object> payload, String key) {
        @SuppressWarnings("unchecked")
        Map<String, String> value = (Map<String, String>) payload.get(key);
        return value == null ? Collections.emptyMap() : value;
    }

    private ResponseEntity<Map<String, Object>> badRequest(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        return ResponseEntity.badRequest().body(response);
    }

    private BigDecimal calculateTotalAmount(Map<String, Object> request, List<OrderItem> items) {
        if (request.containsKey("totalAmount")) {
            return new BigDecimal(request.get("totalAmount").toString());
        }
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : items) {
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        return total;
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
}