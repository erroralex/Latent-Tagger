package com.tagger.service;

import com.tagger.data.TagDataLoader;
import com.tagger.model.TagSearchResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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

    // Getters for startup logging
    public int getTagCount() { return loadResult.names().length; }
    public int getAliasCount() { return aliases.size(); }
    public int getImplicationCount() { return implications.size(); }
}
