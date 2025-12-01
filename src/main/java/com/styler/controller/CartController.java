package com.styler.controller;

import com.styler.model.CartItem;
import com.styler.model.User;
import com.styler.service.CartService;
import com.styler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
@Profile({"prod", "dev", "default", "render-prod"})
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getCart(@PathVariable Long userId) {
        User user = resolveUserOrThrow(userId);
        if (user == null) {
            return userNotFoundResponse(userId);
        }

        List<CartItem> items = cartService.getCartForUser(user);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<?> saveCart(@PathVariable Long userId,
                                      @RequestBody(required = false) List<CartItemPayload> payload) {
        User user = resolveUserOrThrow(userId);
        if (user == null) {
            return userNotFoundResponse(userId);
        }

        List<CartItem> items = mapPayloadToEntities(payload);
        cartService.replaceCart(user, items);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("items", cartService.getCartForUser(user));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable Long userId) {
        User user = resolveUserOrThrow(userId);
        if (user == null) {
            return userNotFoundResponse(userId);
        }

        cartService.clearCart(user);
        return ResponseEntity.ok(Map.of("success", true));
    }

    private User resolveUserOrThrow(Long userId) {
        if (userId == null) {
            return null;
        }
        Optional<User> userOpt = userService.findById(userId);
        return userOpt.orElse(null);
    }

    private ResponseEntity<Map<String, Object>> userNotFoundResponse(Long userId) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", "User not found with ID: " + userId);
        return ResponseEntity.badRequest().body(errorResponse);
    }

    private List<CartItem> mapPayloadToEntities(List<CartItemPayload> payload) {
        if (payload == null || payload.isEmpty()) {
            return Collections.emptyList();
        }

        return payload.stream()
                .filter(item -> item.getProductId() != null && item.getQuantity() != null && item.getQuantity() > 0)
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    private CartItem toEntity(CartItemPayload payload) {
        CartItem item = new CartItem();
        item.setProductId(payload.getProductId());
        item.setProductName(payload.getProductName());
        item.setColor(payload.getColor());
        item.setSize(payload.getSize());
        item.setCategory(payload.getCategory());
        item.setQuantity(payload.getQuantity());
        item.setImage(payload.getImage());
        item.setPrice(payload.getPrice() != null ? payload.getPrice() : BigDecimal.ZERO);
        return item;
    }

    private static class CartItemPayload {
        private Long productId;
        private String productName;
        private BigDecimal price;
        private Integer quantity;
        private String color;
        private String size;
        private String category;
        private String image;

        public Long getProductId() {
            return productId;
        }

        public void setProductId(Long productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public String getColor() {
            return color;
        }

        public void setColor(String color) {
            this.color = color;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }
    }
}
