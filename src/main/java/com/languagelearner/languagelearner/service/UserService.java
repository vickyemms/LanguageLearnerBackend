package com.languagelearner.languagelearner.service;

import com.languagelearner.languagelearner.model.EmailVerificationToken;
import com.languagelearner.languagelearner.model.User;
import com.languagelearner.languagelearner.repository.EmailVerificationTokenRepository;
import com.languagelearner.languagelearner.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    public String createEmailVerificationToken(User user) {
        // Create a unique verification token
        String token = UUID.randomUUID().toString();

        // Save token in the database
        EmailVerificationToken verificationToken = new EmailVerificationToken(token, user);
        tokenRepository.save(verificationToken);

        return token;
    }

    public void sendVerificationEmail(String email, String token) {
        String verificationUrl = "http://localhost:3000/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Email Verification");
        message.setText("Please click the link below to verify your email address:\n\n" + verificationUrl);

        mailSender.send(message);
    }

    public void resendVerificationEmail(String email) {
        // Find the user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if a token already exists for the user
        Optional<EmailVerificationToken> existingTokenOpt = tokenRepository.findByUser(user);

        if (existingTokenOpt.isPresent()) {
            EmailVerificationToken existingToken = existingTokenOpt.get();
            long currentTime = new Date().getTime();

            // Check if the token was sent within the last 5 minutes (cooldown check)
            if (existingToken.getSentAt() != null && currentTime - existingToken.getSentAt().getTime() < 5 * 60 * 1000) {
                // If a token is sent within the last 5 minutes, prevent resending
                throw new RuntimeException("Verification email has already been sent. Please try again later.");
            }

            // Check if the token was created more than an hour ago and if it's expired
            if (currentTime - existingToken.getCreatedAt().getTime() > 60 * 60 * 1000 || new Date().after(existingToken.getExpiresAt())) {
                // If token is older than an hour or expired, delete the old token
                tokenRepository.delete(existingToken);

                // Generate a new token
                String newToken = UUID.randomUUID().toString();
                EmailVerificationToken newVerificationToken = new EmailVerificationToken(newToken, user);
                tokenRepository.save(newVerificationToken);

                // Send the verification email with the new token
                sendVerificationEmail(user.getEmail(), newToken);
                return;
            } else {
                // If token is still valid and within the time window (but older than 5 minutes), resend the existing token
                sendVerificationEmail(user.getEmail(), existingToken.getToken());
                existingToken.setSentAt(new Date());
                tokenRepository.save(existingToken);
                return;
            }
        }

        // Generate a new verification token
        String newToken = UUID.randomUUID().toString();
        EmailVerificationToken newVerificationToken = new EmailVerificationToken(newToken, user);

        // Save the new token in the repository
        tokenRepository.save(newVerificationToken);

        // Send the verification email to the user with the new token
        sendVerificationEmail(user.getEmail(), newToken);

    }

    public void verifyEmail(String token) {
        Optional<EmailVerificationToken> optionalToken = tokenRepository.findByToken(token);

        if (optionalToken.isEmpty()) {
            throw new RuntimeException("Invalid or expired token");
        }

        EmailVerificationToken verificationToken = optionalToken.get();

        // Check if token is expired
        if (verificationToken.getExpiresAt().before(new Date())) {
            tokenRepository.delete(verificationToken); // Clean up expired token
            throw new RuntimeException("Token has expired. Please request a new verification email.");
        }

        User user = verificationToken.getUser();

        if (!user.isVerified()) {
            user.setVerified(true);
            userRepository.save(user);
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
