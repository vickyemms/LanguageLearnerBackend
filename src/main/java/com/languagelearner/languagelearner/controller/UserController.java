package com.languagelearner.languagelearner.controller;

import com.languagelearner.languagelearner.model.User;
import com.languagelearner.languagelearner.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    public static class EmailRequest {
        public String email;
    }

    @GetMapping("/users/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        try {
            userService.verifyEmail(token);
            return ResponseEntity.ok("Email successfully verified!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body("Invalid token or token expired.");
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationEmail(@RequestBody EmailRequest request) {
        try {
            userService.resendVerificationEmail(request.email);
            return ResponseEntity.ok("Verification email sent successfully.");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Verification email has already been sent")) {
                return ResponseEntity.status(429).body(e.getMessage());
            }
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/users/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);

            String token = userService.createEmailVerificationToken(user);
            userService.sendVerificationEmail(user.getEmail(), token);

            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            if ("Email already registered".equals(e.getMessage())) {
                return ResponseEntity.status(409).body(e.getMessage());
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/users/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        try {
            User loggedInUser = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(loggedInUser);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        return userService.getUserByEmail(email)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity
                        .status(404)
                        .body("User not found with email: " + email));
    }

    @DeleteMapping("/users/email/{email}")
    public ResponseEntity<?> deleteUser(@PathVariable String email) {
        try {
            userService.deleteUserByEmail(email);
            return ResponseEntity.ok("User deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/users/update-email")
    public ResponseEntity<?> updateEmail(@RequestParam String currentEmail,
                                         @RequestParam String newEmail) {
        try {
            User updatedUser = userService.updateEmail(currentEmail, newEmail);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/users/update-password")
    public ResponseEntity<?> updatePassword(@RequestParam String email,
                                            @RequestParam String newPassword) {
        try {
            User updatedUser = userService.updatePassword(email, newPassword);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
