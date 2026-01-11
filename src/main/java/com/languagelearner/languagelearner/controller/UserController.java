package com.languagelearner.languagelearner.controller;

import com.languagelearner.languagelearner.dto.UserDTO;
import com.languagelearner.languagelearner.mapper.UserMapper;
import com.languagelearner.languagelearner.model.User;
import com.languagelearner.languagelearner.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:3000")
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
            logger.info("Email verification successful");
            return ResponseEntity.ok("Email successfully verified!");
        } catch (RuntimeException e) {
            logger.warn("Email verification failed: {}", e.getMessage());
            return ResponseEntity.status(400).body("Invalid token or token expired.");
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationEmail(@RequestBody EmailRequest request) {
        logger.info("API /users/resend-verification called");
        try {
            userService.resendVerificationEmail(request.email);
            logger.info("Verification email sent successfully");
            return ResponseEntity.ok("Verification email sent successfully.");
        } catch (RuntimeException e) {
            logger.warn("Resend verification failed: {}", e.getMessage());
            if (e.getMessage().contains("already been sent")) {
                return ResponseEntity.status(429).body(e.getMessage());
            }
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        logger.info("API /users/register called");
        try {
            User registeredUser = userService.registerUser(user);
            String token = userService.createEmailVerificationToken(user);
            userService.sendVerificationEmail(user.getEmail(), token);
            logger.info("User registered successfully");
            return ResponseEntity.ok(UserMapper.toDTO(registeredUser));
        } catch (RuntimeException e) {
            logger.warn("User registration failed: {}", e.getMessage());
            if ("Email already registered".equals(e.getMessage())) {
                return ResponseEntity.status(409).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        logger.info("API /users/login called");
        try {
            User loggedInUser = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
            logger.info("User login successful");
            return ResponseEntity.ok(UserMapper.toDTO(loggedInUser));
        } catch (RuntimeException e) {
            logger.warn("Login failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        logger.info("API /users called to get all users");
        List<UserDTO> users = userService.getAllUsers()
                .stream()
                .map(UserMapper::toDTO)
                .collect(Collectors.toList());
        logger.info("Returned {} users", users.size());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        logger.info("API /users/email called");
        return userService.getUserByEmail(email)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(UserMapper.toDTO(user)))
                .orElseGet(() -> {
                    logger.warn("User not found");
                    return ResponseEntity.status(404).body("User not found");
                });
    }

    @DeleteMapping("/email/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable String email) {
        logger.info("API /users/email delete request called");
        try {
            userService.deleteUserByEmail(email);
            logger.info("User deleted successfully");
            return ResponseEntity.ok("User deleted successfully");
        } catch (RuntimeException e) {
            logger.warn("Failed to delete user: {}", e.getMessage());
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/update-email")
    public ResponseEntity<?> updateEmail(@RequestParam String currentEmail,
                                         @RequestParam String newEmail) {
        logger.info("API /users/update-email called");
        try {
            User updatedUser = userService.updateEmail(currentEmail, newEmail);
            logger.info("Email updated successfully");
            return ResponseEntity.ok(UserMapper.toDTO(updatedUser));
        } catch (RuntimeException e) {
            logger.warn("Failed to update email: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestParam String email,
                                            @RequestParam String newPassword) {
        logger.info("API /users/update-password called");
        try {
            User updatedUser = userService.updatePassword(email, newPassword);
            logger.info("Password updated successfully");
            return ResponseEntity.ok(UserMapper.toDTO(updatedUser));
        } catch (RuntimeException e) {
            logger.warn("Failed to update password: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
