package com.tagger.model;

import java.util.List;

/**
 * Represents a single entry in the translation history.
 * This record stores the details of a translation request and its outcome,
 * including the input, the generated tags, the model used, and the timestamp.
 *
 * @param id
 *         A unique identifier for this history entry.
 * @param tags
 *         A list of generated tags as a result of the translation.
 * @param nlInput
 *         The natural language input string that was translated.
 * @param model
 *         The identifier of the model used for the translation.
 * @param createdAt
 *         The timestamp (in milliseconds since epoch) when this entry was created.
 */
public record HistoryEntry(
        long id,
        List<String> tags,
        String nlInput,
        String model,
        long createdAt
) {
}
