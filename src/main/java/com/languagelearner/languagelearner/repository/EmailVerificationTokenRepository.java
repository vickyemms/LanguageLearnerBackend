package com.languagelearner.languagelearner.repository;
import com.languagelearner.languagelearner.model.EmailVerificationToken;
import com.languagelearner.languagelearner.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findByUser(User user);
}

