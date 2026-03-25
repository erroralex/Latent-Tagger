package com.tagger.model;

/**
 * Represents a tag with its name, category, and the number of posts it's associated with.
 * This record is an immutable data carrier for tag information within the application.
 *
 * <p>The {@code Category} interface defines constants for different types of tags,
 * allowing for categorization and filtering of tags based on their nature.
 *
 * @param name
 *         The name of the tag (e.g., "blue_eyes").
 * @param category
 *         The category ID of the tag, as defined in {@link Category}.
 * @param postCount
 *         The number of posts associated with this tag.
 */
public record Tag(String name, int category, int postCount) {
    /**
     * Defines constants for the different categories a tag can belong to.
     * These categories help in organizing and filtering tags.
     */
    public interface Category {
        int GENERAL = 0;
        int ARTIST = 1;
        int COPYRIGHT = 3;
        int CHARACTER = 4;
        int META = 5;
    }
}
