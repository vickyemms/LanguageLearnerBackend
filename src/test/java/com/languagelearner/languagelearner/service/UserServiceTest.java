package com.languagelearner.languagelearner.service;

import com.languagelearner.languagelearner.model.User;
import com.languagelearner.languagelearner.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("plainPassword");
        user.setLevel("BEGINNER");
        user.setVerified(false);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plainPassword")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        User registered = userService.registerUser(user);

        assertEquals("test@example.com", registered.getEmail());
        assertEquals("hashedPassword", registered.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerUser_emailAlreadyExists() {
        User user = new User();
        user.setEmail("existing@example.com");

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new User()));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.registerUser(user));

        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginUser_success() {
        User user = new User();
        user.setEmail("login@example.com");
        user.setPassword("hashedPassword");

        when(userRepository.findByEmail("login@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("plainPassword", "hashedPassword")).thenReturn(true);

        User loggedIn = userService.loginUser("login@example.com", "plainPassword");

        assertEquals("login@example.com", loggedIn.getEmail());
    }

    @Test
    void loginUser_invalidPassword() {
        User user = new User();
        user.setEmail("login@example.com");
        user.setPassword("hashedPassword");

        when(userRepository.findByEmail("login@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.loginUser("login@example.com", "wrongPassword"));

        assertEquals("Invalid password", exception.getMessage());
    }

    @Test
    void loginUser_userNotFound() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.loginUser("notfound@example.com", "anyPassword"));

        assertEquals("User not found", exception.getMessage());
    }
}
