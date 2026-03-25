package com.tagger.model;

import java.util.List;

/**
 * Represents the response from a tag translation operation.
 * This record contains the list of generated tags, the model family that performed
 * the translation, and an optional warning message if any issues occurred during
 * the translation process.
 *
 * @param tags
 *         A list of strings, where each string is a generated tag.
 * @param modelFamily
 *         The identifier for the family of models that performed the translation.
 * @param warning
 *         An optional warning message related to the translation, or {@code null} if no warnings.
 */
public record TranslationResponse(
        List<String> tags,
        String modelFamily,
        String warning
) {
}
