package com.styler.repository;

import com.styler.model.Order;
import com.styler.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderId(String orderId);
    
    List<Order> findByUserOrderByOrderDateDesc(User user);
    
    List<Order> findByOrderDateBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.orderDate >= :date")
    List<Order> findRecentOrdersByUser(@Param("user") User user, @Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderDate >= :date")
    long countOrdersAfter(@Param("date") LocalDateTime date);
    
    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.orderDate >= :date")
    Double getTotalRevenueAfter(@Param("date") LocalDateTime date);
}