package com.languagelearner.languagelearner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable()) // Proper way in Spring Security 6.1+
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/users/login",
                                "/users/register",
                                "/users/verify-email",
                                "/resend-verification",
                                "/users/email/**"
                        ).permitAll()
                        .anyRequest().permitAll() // Or `.authenticated()` for protected routes
                )
                .formLogin(login -> login.disable()) // Also updated syntax
                .httpBasic(basic -> basic.disable()); // Updated syntax

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

