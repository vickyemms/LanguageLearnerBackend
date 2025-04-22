package com.languagelearner.languagelearner.controller;

import com.languagelearner.languagelearner.model.User;
import com.languagelearner.languagelearner.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JavaMailSender emailSender;

    private void sendVerificationEmail(String email, String token) {
        String verificationUrl = "http://localhost:3000/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Email Verification");
        message.setText("Please click the link below to verify your email address:\n\n" + verificationUrl);

        emailSender.send(message);
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

    @PostMapping("/users/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);

            String token = userService.createEmailVerificationToken(user);
            sendVerificationEmail(user.getEmail(), token);

            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            if ("Email already registered".equals(e.getMessage())) {
                return ResponseEntity.status(409).body(e.getMessage());  // 409 Conflict
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/users/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginRequest) {
        try {
            User loggedInUser = userService.loginUser(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(loggedInUser);  // You may want to return a token here instead of the user object
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());  // 401 for invalid login attempts
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

    // PUT /api/users/update-password
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
