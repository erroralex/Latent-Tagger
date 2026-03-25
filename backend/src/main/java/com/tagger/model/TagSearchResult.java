package com.tagger.model;

/**
 * Represents the result of a tag search operation.
 * This record encapsulates the details of a found tag, including its name, category,
 * post count, validity status, and the original term it was resolved from (if it was an alias).
 *
 * @param name
 *         The canonical name of the tag found.
 * @param category
 *         The category ID of the tag, as defined in {@link Tag.Category}.
 * @param postCount
 *         The number of posts associated with this tag.
 * @param valid
 *         A boolean indicating whether the tag is considered valid in the system.
 * @param resolvedFrom
 *         The original search term if this tag was resolved from an alias; otherwise, null.
 */
public record TagSearchResult(
        String name,
        int category,
        int postCount,
        boolean valid,
        String resolvedFrom
) {
}
