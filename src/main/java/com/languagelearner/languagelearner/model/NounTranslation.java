package com.languagelearner.languagelearner.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "noun_translations")
public class NounTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String language;

    private String baseForm;
    private String singularIndefinite;
    private String singularDefinite;
    private String pluralIndefinite;
    private String pluralDefinite;

    @ManyToOne
    @JoinColumn(name = "noun_id")
    private Noun noun;
}

