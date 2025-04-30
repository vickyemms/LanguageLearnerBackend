package com.languagelearner.languagelearner.service;

import com.languagelearner.languagelearner.model.Noun;
import com.languagelearner.languagelearner.model.NounTranslation;
import com.languagelearner.languagelearner.repository.NounRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NounService {

    @Autowired
    private NounRepository nounRepository;

    public List<Map<String, Object>> getNounsByLanguages(String sourceLang, String targetLang, String category) {
        List<Noun> nouns = category != null
                ? nounRepository.findByCategory(category)
                : nounRepository.findAll();

        return nouns.stream().map(noun -> {
            Map<String, Object> dto = new HashMap<>();
            dto.put("id", noun.getId());
            dto.put("category", noun.getCategory());
            dto.put("imageUrl", noun.getImageUrl());

            Map<String, Object> translationsMap = new LinkedHashMap<>();

            for (NounTranslation translation : noun.getTranslations()) {
                if (translation.getLanguage().equals(sourceLang)) {
                    Map<String, String> tData = new LinkedHashMap<>();
                    tData.put("baseForm", translation.getBaseForm());
                    tData.put("singularIndefinite", translation.getSingularIndefinite());
                    tData.put("singularDefinite", translation.getSingularDefinite());
                    tData.put("pluralIndefinite", translation.getPluralIndefinite());
                    tData.put("pluralDefinite", translation.getPluralDefinite());
                    translationsMap.put(translation.getLanguage(), tData);
                }
            }

            for (NounTranslation translation : noun.getTranslations()) {
                if (translation.getLanguage().equals(targetLang)) {
                    Map<String, String> tData = new LinkedHashMap<>();
                    tData.put("baseForm", translation.getBaseForm());
                    tData.put("singularIndefinite", translation.getSingularIndefinite());
                    tData.put("singularDefinite", translation.getSingularDefinite());
                    tData.put("pluralIndefinite", translation.getPluralIndefinite());
                    tData.put("pluralDefinite", translation.getPluralDefinite());
                    translationsMap.put(translation.getLanguage(), tData);
                }
            }

            dto.put("translations", translationsMap);
            return dto;
        }).collect(Collectors.toList());
    }

}
