package com.styler.service;

import com.styler.model.User;
import com.styler.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile({"prod", "dev", "default", "railway-prod"})
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    
    private Map<String, String> resetTokens = new ConcurrentHashMap<>();
    private Map<String, LocalDateTime> tokenExpiry = new ConcurrentHashMap<>();
    
    public User createUser(String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }
        
        User user = new User(email, password);
        return userRepository.save(user);
    }
    
    public User createUser(String email, String password, String firstName, String lastName, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("User with email " + email + " already exists");
        }
        
        User user = new User(email, password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhone(phone);
        return userRepository.save(user);
    }
    
    public Optional<User> authenticateUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
           
            if (password.equals(user.getPassword())) {
                
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
                
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    public List<User> getNewUsers(LocalDateTime since) {
        return userRepository.findByJoinDateBetween(since, LocalDateTime.now());
    }
    
    public List<User> getInactiveUsers(LocalDateTime lastLoginBefore) {
        return userRepository.findInactiveUsers(lastLoginBefore);
    }
    
    public long getNewUserCount(LocalDateTime since) {
        return userRepository.countNewUsersAfter(since);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public String generatePasswordResetToken(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("No user found with email: " + email);
        }
        
        
        String resetToken = UUID.randomUUID().toString();
        
       
        resetTokens.put(email, resetToken);
        tokenExpiry.put(email, LocalDateTime.now().plusMinutes(15));
        
  
        return resetToken;
    }
    
    public boolean resetPassword(String email, String resetToken, String newPassword) {
      
        if (!resetTokens.containsKey(email)) {
            return false;
        }
        
        String storedToken = resetTokens.get(email);
        LocalDateTime expiry = tokenExpiry.get(email);
        
        
        if (!storedToken.equals(resetToken) || LocalDateTime.now().isAfter(expiry)) {
           
            resetTokens.remove(email);
            tokenExpiry.remove(email);
            return false;
        }
        
       
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(newPassword); 
            userRepository.save(user);
            
           
            resetTokens.remove(email);
            tokenExpiry.remove(email);
            
            return true;
        }
        
        return false;
    }
}