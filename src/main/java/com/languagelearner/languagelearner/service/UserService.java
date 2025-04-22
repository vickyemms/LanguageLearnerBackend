package com.languagelearner.languagelearner.service;

import com.languagelearner.languagelearner.model.EmailVerificationToken;
import com.languagelearner.languagelearner.model.User;
import com.languagelearner.languagelearner.repository.EmailVerificationTokenRepository;
import com.languagelearner.languagelearner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    public String createEmailVerificationToken(User user) {
        // Create a unique verification token
        String token = UUID.randomUUID().toString();

        // Save token in the database
        EmailVerificationToken verificationToken = new EmailVerificationToken(token, user);
        tokenRepository.save(verificationToken);

        return token;
    }

    public void verifyEmail(String token) {
        // Find the token in the database
        Optional<EmailVerificationToken> verificationToken = tokenRepository.findByToken(token);

        if (verificationToken.isPresent()) {
            // Get the user from the token
            User user = verificationToken.get().getUser();
            user.setVerified(true);  // Set the user as verified
            userRepository.save(user);
            tokenRepository.delete(verificationToken.get());  // Delete the token after successful verification
        } else {
            throw new RuntimeException("Invalid or expired token");
        }
    }

    public User registerUser(User user) {
        Optional<User> existing = userRepository.findByEmail(user.getEmail());
        if (existing.isPresent()) {
            throw new RuntimeException("Email already registered");
        }
        return userRepository.save(user);
    }

    public User loginUser(String email, String password) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }

        User user = optionalUser.get();

        if (password.equals(user.getPassword())) {
            return user;
        } else {
            throw new RuntimeException("Invalid password");
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void deleteUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }
        userRepository.delete(user.get());
    }

    public User updateEmail(String currentEmail, String newEmail) {
        Optional<User> optionalUser = userRepository.findByEmail(currentEmail);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found with email: " + currentEmail);
        }

        User user = optionalUser.get();

        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new RuntimeException("New email is already taken");
        }

        user.setEmail(newEmail);
        return userRepository.save(user);
    }

    public User updatePassword(String email, String newPassword) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }

        User user = optionalUser.get();

        if (newPassword != null && !newPassword.isBlank()) {
            user.setPassword(newPassword);
        }

        return userRepository.save(user);
    }

}
