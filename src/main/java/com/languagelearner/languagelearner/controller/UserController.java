package com.languagelearner.languagelearner.controller;

import com.languagelearner.languagelearner.model.User;
import com.languagelearner.languagelearner.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    public static class EmailRequest {
        public String email;
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        logger.info("API /users/verify-email called");
        try {
            userService.verifyEmail(token);
            logger.info("Email verification successful for token");
            return ResponseEntity.ok("Email successfully verified!");
        } catch (RuntimeException e) {
            logger.error("Email verification failed: {}", e.getMessage());
            return ResponseEntity.status(400).body("Invalid token or token expired.");
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationEmail(@RequestBody EmailRequest request) {
        logger.info("API /users/resend-verification called for email: {}", request.email);
        try {
            userService.resendVerificationEmail(request.email);
            logger.info("Verification email sent successfully to {}", request.email);
            return ResponseEntity.ok("Verification email sent successfully.");
        } catch (RuntimeException e) {
            logger.error("Resend verification failed for email {}: {}", request.email, e.getMessage());
            if (e.getMessage().contains("Verification email has already been sent")) {
                return ResponseEntity.status(429).body(e.getMessage());
            }
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        logger.info("API /users/register called for email: {}", user.getEmail());
        try {
            User registeredUser = userService.registerUser(user);
            String token = userService.createEmailVerificationToken(user);
            userService.sendVerificationEmail(user.getEmail(), token);
            logger.info("User registered successfully: {}", user.getEmail());
            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            logger.error("User registration failed for {}: {}", user.getEmail(), e.getMessage());
            if ("Email already registered".equals(e.getMessage())) {
                return ResponseEntity.status(409).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        logger.info("API /users/login called for email: {}", loginRequest.getEmail());
        try {
            User loggedInUser = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
            logger.info("User logged in successfully: {}", loginRequest.getEmail());
            return ResponseEntity.ok(loggedInUser);
        } catch (RuntimeException e) {
            logger.error("Login failed for {}: {}", loginRequest.getEmail(), e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("API /users called to get all users");
        List<User> users = userService.getAllUsers();
        logger.info("Returned {} users", users.size());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        logger.info("API /users/email/{} called", email);
        return userService.getUserByEmail(email)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("User not found with email: {}", email);
                    return ResponseEntity.status(404).body("User not found with email: " + email);
                });
    }

    @DeleteMapping("/email/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable String email) {
        logger.info("API /users/email/{} delete request called", email);
        try {
            userService.deleteUserByEmail(email);
            logger.info("User deleted successfully: {}", email);
            return ResponseEntity.ok("User deleted successfully");
        } catch (RuntimeException e) {
            logger.error("Failed to delete user {}: {}", email, e.getMessage());
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/update-email")
    public ResponseEntity<?> updateEmail(@RequestParam String currentEmail,
                                         @RequestParam String newEmail) {
        logger.info("API /users/update-email called from {} to {}", currentEmail, newEmail);
        try {
            User updatedUser = userService.updateEmail(currentEmail, newEmail);
            logger.info("Email updated successfully from {} to {}", currentEmail, newEmail);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            logger.error("Failed to update email from {} to {}: {}", currentEmail, newEmail, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestParam String email,
                                            @RequestParam String newPassword) {
        logger.info("API /users/update-password called for email: {}", email);
        try {
            User updatedUser = userService.updatePassword(email, newPassword);
            logger.info("Password updated successfully for {}", email);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            logger.error("Failed to update password for {}: {}", email, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
