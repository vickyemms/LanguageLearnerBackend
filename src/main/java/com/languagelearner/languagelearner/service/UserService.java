package com.languagelearner.languagelearner.service;

import com.languagelearner.languagelearner.model.EmailVerificationToken;
import com.languagelearner.languagelearner.model.User;
import com.languagelearner.languagelearner.repository.EmailVerificationTokenRepository;
import com.languagelearner.languagelearner.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String createEmailVerificationToken(User user) {
        logger.debug("Creating email verification token for user");

        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken(token, user);
        tokenRepository.save(verificationToken);

        logger.debug("Email verification token created successfully");
        return token;
    }

    public void sendVerificationEmail(String email, String token) {
        logger.info("Sending verification email");

        String verificationUrl = "http://localhost:3000/verify-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Email Verification");
        message.setText(
                "Please click the link below to verify your email address:\n\n" + verificationUrl
        );

        mailSender.send(message);
        logger.info("Verification email sent successfully");
    }

    public void resendVerificationEmail(String email) {
        logger.info("Resend verification email requested");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Resend verification failed: user not found");
                    return new RuntimeException("User not found");
                });

        Optional<EmailVerificationToken> existingTokenOpt = tokenRepository.findByUser(user);

        if (existingTokenOpt.isPresent()) {
            EmailVerificationToken existingToken = existingTokenOpt.get();
            long currentTime = System.currentTimeMillis();

            if (existingToken.getSentAt() != null &&
                    currentTime - existingToken.getSentAt().getTime() < 5 * 60 * 1000) {

                logger.warn("Verification email resend blocked due to rate limit");
                throw new RuntimeException(
                        "Verification email has already been sent. Please try again later."
                );
            }

            if (existingToken.getExpiresAt().before(new Date())) {
                logger.info("Existing verification token expired, generating new one");
                tokenRepository.delete(existingToken);
            } else {
                logger.info("Reusing existing verification token");
                existingToken.setSentAt(new Date());
                tokenRepository.save(existingToken);
                sendVerificationEmail(user.getEmail(), existingToken.getToken());
                return;
            }
        }

        logger.info("Creating and sending new verification token");
        String newToken = UUID.randomUUID().toString();
        EmailVerificationToken newVerificationToken =
                new EmailVerificationToken(newToken, user);
        tokenRepository.save(newVerificationToken);
        sendVerificationEmail(user.getEmail(), newToken);
    }

    public void verifyEmail(String token) {
        logger.info("Email verification attempt received");

        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> {
                    logger.warn("Email verification failed: invalid token");
                    return new RuntimeException("Invalid or expired token");
                });

        if (verificationToken.getExpiresAt().before(new Date())) {
            logger.warn("Email verification failed: token expired");
            tokenRepository.delete(verificationToken);
            throw new RuntimeException("Token has expired");
        }

        User user = verificationToken.getUser();
        if (!user.isVerified()) {
            user.setVerified(true);
            userRepository.save(user);
            logger.info("User email verified successfully");
        }
    }

    public User registerUser(User user) {
        logger.info("Registering new user");

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            logger.warn("User registration failed: email already registered");
            throw new RuntimeException("Email already registered");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        logger.info("User registered successfully");
        return savedUser;
    }

    public User loginUser(String email, String password) {
        logger.info("Login attempt received");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Login failed: user not found");
                    return new RuntimeException("Invalid credentials");
                });

        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.warn("Login failed: invalid password");
            throw new RuntimeException("Invalid credentials");
        }

        logger.info("User logged in successfully");
        return user;
    }

    public List<User> getAllUsers() {
        logger.debug("Fetching all users");
        return userRepository.findAll();
    }

    public Optional<User> getUserByEmail(String email) {
        logger.debug("Fetching user by email");
        return userRepository.findByEmail(email);
    }

    public void deleteUserByEmail(String email) {
        logger.info("Delete user requested");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Delete failed: user not found");
                    return new RuntimeException("User not found");
                });

        userRepository.delete(user);
        logger.info("User deleted successfully");
    }

    public User updateEmail(String currentEmail, String newEmail) {
        logger.info("Update email requested");

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> {
                    logger.warn("Update email failed: user not found");
                    return new RuntimeException("User not found");
                });

        if (userRepository.findByEmail(newEmail).isPresent()) {
            logger.warn("Update email failed: new email already taken");
            throw new RuntimeException("New email is already taken");
        }

        user.setEmail(newEmail);
        User updatedUser = userRepository.save(user);

        logger.info("Email updated successfully");
        return updatedUser;
    }

    public User updatePassword(String email, String newPassword) {
        logger.info("Update password requested");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Update password failed: user not found");
                    return new RuntimeException("User not found");
                });

        if (newPassword == null || newPassword.isBlank()) {
            logger.warn("Update password failed: empty password");
            throw new RuntimeException("Password must not be empty");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        User updatedUser = userRepository.save(user);

        logger.info("Password updated successfully");
        return updatedUser;
    }
}
