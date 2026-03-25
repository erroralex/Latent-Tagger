package com.tagger.service;

import com.tagger.data.TagDataLoader;
import com.tagger.model.TagSearchResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * {@code TagDatabaseService} provides functionalities for searching, validating,
 * and retrieving implications for tags. It acts as a central repository for tag-related data,
 * including canonical tag names, aliases, and implication rules.
 *
 * <p>This service is initialized with data loaded by {@link TagDataLoader}, which includes
 * sorted tag names, categories, post counts, tag aliases, and tag implications.
 * It uses efficient search algorithms (like binary search) for quick lookups.
 *
 * <p>Key functionalities include:
 * <ul>
 *     <li>Searching for tags based on a query string, returning a limited number of
 *         results sorted by post count.</li>
 *     <li>Validating a given tag name, resolving aliases to their canonical names,
 *         and indicating if the tag exists in the database.</li>
 *     <li>Retrieving a list of tags that are implied by a given tag.</li>
 * </ul>
 *
 * <p>All tag names are normalized to lowercase and spaces are replaced with underscores
 * before processing to ensure consistent matching.
 *
 * @see TagDataLoader
 * @see TagSearchResult
 */
public class TagDatabaseService {

    private final TagDataLoader.LoadResult loadResult;
    private final Map<String, String> aliases;
    private final Map<String, List<String>> implications;

    public TagDatabaseService(TagDataLoader.LoadResult loadResult, Map<String, String> aliases, Map<String, List<String>> implications) {
        this.loadResult = loadResult;
        this.aliases = aliases;
        this.implications = implications;
    }

    public List<TagSearchResult> search(String query, int limit) {
        String normalizedQuery = query.toLowerCase().replace(' ', '_');
        List<TagSearchResult> results = new ArrayList<>();

        int insertionPoint = Arrays.binarySearch(loadResult.names(), normalizedQuery);
        if (insertionPoint < 0) {
            insertionPoint = -insertionPoint - 1;
        }

        for (int i = insertionPoint; i < loadResult.names().length && results.size() < limit; i++) {
            String name = loadResult.names()[i];
            if (name.startsWith(normalizedQuery)) {
                String resolvedFrom = null;
                String canonicalName = name;
                if (aliases.containsKey(name)) {
                    resolvedFrom = name;
                    canonicalName = aliases.get(name);
                }
                int category = loadResult.categories()[i];
                int postCount = loadResult.postCounts()[i];
                results.add(new TagSearchResult(canonicalName, category, postCount, true, resolvedFrom));
            }
        }

        results.sort(Comparator.comparingInt(TagSearchResult::postCount).reversed());
        return results;
    }

    public TagSearchResult validate(String tagName) {
        String normalizedTag = tagName.toLowerCase().replace(' ', '_');
        String resolvedFrom = null;
        String canonicalName = normalizedTag;

        if (aliases.containsKey(normalizedTag)) {
            resolvedFrom = normalizedTag;
            canonicalName = aliases.get(normalizedTag);
        }

        int index = Arrays.binarySearch(loadResult.names(), canonicalName);
        if (index >= 0) {
            return new TagSearchResult(canonicalName, loadResult.categories()[index], loadResult.postCounts()[index], true, resolvedFrom);
        } else {
            return new TagSearchResult(tagName, 0, 0, false, null);
        }
    }

    public List<String> getImplications(String tagName) {
        String normalizedTag = tagName.toLowerCase().replace(' ', '_');
        String canonicalName = aliases.getOrDefault(normalizedTag, normalizedTag);
        return implications.getOrDefault(canonicalName, Collections.emptyList());
    }

    public int getTagCount() {
        return loadResult.names().length;
    }

    public int getAliasCount() {
        return aliases.size();
    }

    public int getImplicationCount() {
        return implications.size();
    }
}
