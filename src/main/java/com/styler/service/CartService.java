package com.styler.service;

import com.styler.model.CartItem;
import com.styler.model.User;
import com.styler.repository.CartItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Profile({"prod", "dev", "default", "render-prod"})
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    public List<CartItem> getCartForUser(User user) {
        List<CartItem> items = cartItemRepository.findByUser(user);
        return items == null ? Collections.emptyList() : items;
    }

    @Transactional
    public void replaceCart(User user, List<CartItem> items) {
        cartItemRepository.deleteByUser(user);

        if (items == null || items.isEmpty()) {
            return;
        }

        items.forEach(item -> item.setUser(user));
        cartItemRepository.saveAll(items);
    }

    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }
}
