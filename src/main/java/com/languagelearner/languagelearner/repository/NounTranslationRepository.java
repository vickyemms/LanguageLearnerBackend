package com.languagelearner.languagelearner.repository;

import com.languagelearner.languagelearner.model.NounTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NounTranslationRepository extends JpaRepository<NounTranslation, Long> {
    List<NounTranslation> findByLanguage(String language);
}

