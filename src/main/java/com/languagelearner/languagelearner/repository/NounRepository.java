package com.languagelearner.languagelearner.repository;

import com.languagelearner.languagelearner.model.Noun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NounRepository extends JpaRepository<Noun, Long> {
    List<Noun> findByCategory(String category);
}

