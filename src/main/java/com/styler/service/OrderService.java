package com.styler.service;

import com.styler.model.*;
import com.styler.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Profile({"prod", "dev", "default", "railway-prod", "railway-db"})
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    public Order createOrder(User user, List<OrderItem> items, ShippingAddress shippingAddress, 
                           BigDecimal subtotal, BigDecimal shippingCost, BigDecimal taxAmount) {
        
        String orderId = "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Order order = new Order();
        order.setOrderId(orderId);
        order.setUser(user);
        order.setSubtotal(subtotal);
        order.setShippingCost(shippingCost);
        order.setTaxAmount(taxAmount);
        order.setTotalAmount(subtotal.add(shippingCost).add(taxAmount));
        order.setShippingAddress(shippingAddress);
        order.setEstimatedDelivery(LocalDateTime.now().plusDays(5)); // 5 days delivery
        
        
        for (OrderItem item : items) {
            item.setOrder(order);
        }
        order.setItems(items);
        
        return orderRepository.save(order);
    }
    
    public Optional<Order> findByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId);
    }
    
    public List<Order> getUserOrders(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }
    
    public Order createSimpleOrder(User user, List<OrderItem> items, ShippingAddress shippingAddress, 
                                 BigDecimal totalAmount, String paymentMethod) {
        
        String orderId = "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        Order order = new Order();
        order.setOrderId(orderId);
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setSubtotal(totalAmount); // Simplified - assume no separate shipping/tax
        order.setShippingCost(new BigDecimal("0.00"));
        order.setTaxAmount(new BigDecimal("0.00"));
        order.setShippingAddress(shippingAddress);
        order.setEstimatedDelivery(LocalDateTime.now().plusDays(5));
        order.setStatus(OrderStatus.CONFIRMED);
        
   
        for (OrderItem item : items) {
            item.setOrder(order);
        }
        order.setItems(items);
        
        return orderRepository.save(order);
    }
    
    public Order updateOrderStatus(String orderId, String status) {
        Optional<Order> orderOpt = orderRepository.findByOrderId(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
            
            if ("DELIVERED".equals(status.toUpperCase())) {
                order.setActualDelivery(LocalDateTime.now());
            }
            
            return orderRepository.save(order);
        }
        throw new IllegalArgumentException("Order not found: " + orderId);
    }
    
    public List<Order> getRecentOrders(LocalDateTime since) {
        return orderRepository.findByOrderDateBetween(since, LocalDateTime.now());
    }
    
    public long getOrderCount(LocalDateTime since) {
        return orderRepository.countOrdersAfter(since);
    }
    
    public Double getTotalRevenue(LocalDateTime since) {
        Double revenue = orderRepository.getTotalRevenueAfter(since);
        return revenue != null ? revenue : 0.0;
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    public boolean hasRecentPurchase(User user, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<Order> recentOrders = orderRepository.findRecentOrdersByUser(user, since);
        return !recentOrders.isEmpty();
    }
    
    public BigDecimal getCustomerLifetimeValue(User user) {
        List<Order> userOrders = getUserOrders(user);
        return userOrders.stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}