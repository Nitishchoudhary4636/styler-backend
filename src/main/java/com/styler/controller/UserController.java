package com.styler.controller;

import com.styler.model.User;
import com.styler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
@Profile({"prod", "dev", "default"})
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            String name = request.get("name");
            String phone = request.get("phone");
            
            System.out.println("Registration request received: email=" + email + ", name=" + name + ", phone=" + phone);
            
            if (email == null || password == null || name == null || phone == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Name, email, password, and phone are required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String[] nameParts = name.trim().split("\\s+", 2);
            String firstName = nameParts[0];
            String lastName = nameParts.length > 1 ? nameParts[1] : "";
            
            System.out.println("Creating user: firstName=" + firstName + ", lastName=" + lastName);
            User user = userService.createUser(email, password, firstName, lastName, phone);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("name", user.getFirstName() + " " + user.getLastName());
            response.put("email", user.getEmail());
            response.put("phone", user.getPhone());
            response.put("success", true);
            response.put("message", "User registered successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            System.err.println("IllegalArgumentException in registration: " + e.getMessage());
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            System.err.println("Unexpected error in registration: " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred during registration: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        
        if (email == null || password == null) {
            return ResponseEntity.badRequest().body("Email and password are required");
        }
        
        Optional<User> userOpt = userService.authenticateUser(email, password);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("name", (user.getFirstName() != null ? user.getFirstName() : "") + 
                               (user.getLastName() != null ? " " + user.getLastName() : "").trim());
            response.put("email", user.getEmail());
            response.put("phone", user.getPhone() != null ? user.getPhone() : "");
            response.put("success", true);
            response.put("message", "Login successful");
            
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    @GetMapping("/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        Optional<User> userOpt = userService.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());
            response.put("lastName", user.getLastName());
            response.put("phone", user.getPhone());
            response.put("joinDate", user.getJoinDate());
            response.put("lastLogin", user.getLastLogin());
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Email is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String resetToken = userService.generatePasswordResetToken(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password reset token generated. In a real app, this would be sent via email.");
            response.put("resetToken", resetToken); // In production, don't return this - send via email instead
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String resetToken = request.get("resetToken");
            String newPassword = request.get("newPassword");
            
            if (email == null || resetToken == null || newPassword == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Email, reset token, and new password are required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            boolean success = userService.resetPassword(email, resetToken, newPassword);
            
            if (success) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Password reset successfully");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Invalid or expired reset token");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "An error occurred while resetting password");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{email}")
    public ResponseEntity<?> updateUser(@PathVariable String email, @RequestBody Map<String, String> updates) {
        Optional<User> userOpt = userService.findByEmail(email);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            if (updates.containsKey("firstName")) {
                user.setFirstName(updates.get("firstName"));
            }
            if (updates.containsKey("lastName")) {
                user.setLastName(updates.get("lastName"));
            }
            if (updates.containsKey("phone")) {
                user.setPhone(updates.get("phone"));
            }
            
            User updatedUser = userService.updateUser(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User updated successfully");
            response.put("user", Map.of(
                "id", updatedUser.getId(),
                "email", updatedUser.getEmail(),
                "firstName", updatedUser.getFirstName() != null ? updatedUser.getFirstName() : "",
                "lastName", updatedUser.getLastName() != null ? updatedUser.getLastName() : ""
            ));
            
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}