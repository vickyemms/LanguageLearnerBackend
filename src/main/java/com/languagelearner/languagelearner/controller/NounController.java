package com.languagelearner.languagelearner.controller;

import com.languagelearner.languagelearner.service.NounService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/nouns")
public class NounController {

    private static final Logger logger = LoggerFactory.getLogger(NounController.class);

    @Autowired
    private NounService nounService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getNounsByLanguages(
            @RequestParam String sourceLang,
            @RequestParam String targetLang,
            @RequestParam(required = false) String category
    ) {
        logger.info(
                "API GET /nouns called [sourceLang={}, targetLang={}, category={}]",
                sourceLang,
                targetLang,
                category != null ? category : "none"
        );

        try {
            List<Map<String, Object>> result =
                    nounService.getNounsByLanguages(sourceLang, targetLang, category);

            logger.info(
                    "API GET /nouns success - returned {} nouns",
                    result.size()
            );

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error(
                    "API GET /nouns failed [sourceLang={}, targetLang={}, category={}]",
                    sourceLang,
                    targetLang,
                    category != null ? category : "none",
                    e
            );

            return ResponseEntity.internalServerError().build();
        }
    }
}
