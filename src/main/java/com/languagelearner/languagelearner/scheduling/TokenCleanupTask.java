package com.languagelearner.languagelearner.scheduling;

import com.languagelearner.languagelearner.repository.EmailVerificationTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
public class TokenCleanupTask {

    @Autowired
    private EmailVerificationTokenRepository tokenRepository;

    @Transactional
    @Scheduled(fixedRate = 1000 * 60 * 60)
    public void cleanUpExpiredTokens() {
        Date now = new Date();
        tokenRepository.deleteByExpiresAtBefore(now);
    }
}

