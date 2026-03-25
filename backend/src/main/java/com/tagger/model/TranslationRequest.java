package com.tagger.model;

/**
 * Represents a request to translate natural language text into tags.
 * This record holds the input natural language string and the desired model family
 * to be used for the translation process.
 *
 * @param naturalText
 *         The natural language text to be translated into tags.
 * @param modelFamily
 *         The identifier for the family of models to be used for translation (e.g., "phi3").
 */
public record TranslationRequest(String naturalText, String modelFamily) {
}
