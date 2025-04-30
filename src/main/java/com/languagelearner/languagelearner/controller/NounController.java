package com.languagelearner.languagelearner.controller;

import com.languagelearner.languagelearner.service.NounService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
public class NounController {

    @Autowired
    private NounService nounService;

    @GetMapping("/nouns")
    public ResponseEntity<List<Map<String, Object>>> getNounsByLanguages(
            @RequestParam String sourceLang,
            @RequestParam String targetLang,
            @RequestParam(required = false) String category
    ) {
        List<Map<String, Object>> result = nounService.getNounsByLanguages(sourceLang, targetLang, category);
        return ResponseEntity.ok(result);
    }
}

