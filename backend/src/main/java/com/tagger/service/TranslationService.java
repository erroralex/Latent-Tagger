package com.tagger.service;

import com.tagger.model.TranslationResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TranslationService {

    private final TagDatabaseService tagDatabaseService;
    private final TranslationStrategy translationStrategy;

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "with", "in", "on", "at", "of",
            "and", "or", "is", "are", "wearing", "has", "have", "very"
    );

    public TranslationService(TagDatabaseService tagDatabaseService) {
        this.tagDatabaseService = tagDatabaseService;
        this.translationStrategy = this::ruleBasedTranslate; // Stub implementation
    }

    public TranslationResponse translate(String naturalText, String modelFamily) {
        return translationStrategy.translate(naturalText, modelFamily);
    }

    private TranslationResponse ruleBasedTranslate(String naturalText, String modelFamily) {
        List<String> tokens = Arrays.stream(naturalText.toLowerCase().split("[\\s,]+"))
                .map(token -> token.replace('-', '_').replaceAll("[^a-z0-9_]", ""))
                .filter(token -> !token.isEmpty() && !STOP_WORDS.contains(token))
                .collect(Collectors.toList());

        List<String> validTags = tokens.stream()
                .filter(token -> tagDatabaseService.validate(token).valid())
                .collect(Collectors.toList());

        String warning = null;
        if (validTags.size() < 2) {
            warning = "Low confidence: few recognised tags found";
        }

        return new TranslationResponse(validTags, modelFamily, warning);
    }

    public interface TranslationStrategy {
        TranslationResponse translate(String input, String modelFamily);
    }
}
