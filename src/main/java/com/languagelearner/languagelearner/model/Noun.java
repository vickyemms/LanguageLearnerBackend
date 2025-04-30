package com.languagelearner.languagelearner.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "nouns")
public class Noun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String category;
    private String imageUrl;

    @OneToMany(mappedBy = "noun", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<NounTranslation> translations;
}

