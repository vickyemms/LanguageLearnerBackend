package com.languagelearner.languagelearner.model;
import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @ManyToOne
    private User user;

    private Date createdAt;

    public EmailVerificationToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.createdAt = new Date();
    }
}

