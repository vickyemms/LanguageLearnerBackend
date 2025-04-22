package com.languagelearner.languagelearner.scheduling;

import com.languagelearner.languagelearner.repository.EmailVerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class TokenCleanupTask {

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Scheduled(fixedRate = 1000 * 60 * 60) // every hour
    public void cleanUpExpiredTokens() {
        Date now = new Date();
        tokenRepository.findAll().stream()
                .filter(token -> token.getExpiresAt().before(now))
                .forEach(tokenRepository::delete);
    }
}

