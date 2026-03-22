package com.tagger.data;

import com.tagger.service.TagDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagDataLoader {

    private static final Logger log = LoggerFactory.getLogger(TagDataLoader.class);

    public record LoadResult(String[] names, int[] categories, int[] postCounts) {}

    public static TagDatabaseService load() {
        LoadResult tagLoadResult = loadTags();
        Map<String, String> aliases = loadAliases();
        Map<String, List<String>> implications = loadImplications();
        return new TagDatabaseService(tagLoadResult, aliases, implications);
    }

    private static LoadResult loadTags() {
        List<String> names = new ArrayList<>();
        List<Integer> categories = new ArrayList<>();
        List<Integer> postCounts = new ArrayList<>();

        try (InputStream is = TagDataLoader.class.getResourceAsStream("/data/tags.csv")) {
            if (is == null) {
                log.warn("tags.csv not found on classpath. Tag database will be empty.");
                return new LoadResult(new String[0], new int[0], new int[0]);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",", -1);
                    if (parts.length < 3) continue;

                    try {
                        String name = parts[0].trim();
                        int category = Integer.parseInt(parts[1].trim());
                        int postCount = Integer.parseInt(parts[2].trim());

                        if (postCount < 20) continue;

                        names.add(name);
                        categories.add(category);
                        postCounts.add(postCount);
                    } catch (NumberFormatException e) {
                        // Skip line if parsing fails
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to load tags.csv", e);
        }

        // Sort the tags by name
        Integer[] indices = new Integer[names.size()];
        for (int i = 0; i < indices.length; i++) indices[i] = i;
        Arrays.sort(indices, Comparator.comparing(names::get));

        String[] sortedNames = new String[indices.length];
        int[] sortedCats = new int[indices.length];
        int[] sortedCounts = new int[indices.length];
        for (int i = 0; i < indices.length; i++) {
            sortedNames[i] = names.get(indices[i]);
            sortedCats[i] = categories.get(indices[i]);
            sortedCounts[i] = postCounts.get(indices[i]);
        }

        return new LoadResult(sortedNames, sortedCats, sortedCounts);
    }

    private static Map<String, String> loadAliases() {
        Map<String, String> aliases = new HashMap<>();
        try (InputStream is = TagDataLoader.class.getResourceAsStream("/data/tag-aliases.csv")) {
            if (is == null) {
                log.info("tag-aliases.csv not found. No aliases loaded.");
                return aliases;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                reader.readLine(); // Skip header
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",", -1);
                    if (parts.length < 6) continue;

                    String status = parts[5].trim();
                    if ("active".equals(status)) {
                        String antecedent = parts[1].trim();
                        String consequent = parts[2].trim();
                        aliases.put(antecedent, consequent);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to load tag-aliases.csv", e);
        }
        return aliases;
    }

    private static Map<String, List<String>> loadImplications() {
        Map<String, List<String>> implications = new HashMap<>();
        try (InputStream is = TagDataLoader.class.getResourceAsStream("/data/tag-implications.csv")) {
            if (is == null) {
                log.info("tag-implications.csv not found. No implications loaded.");
                return implications;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                reader.readLine(); // Skip header
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",", -1);
                    if (parts.length < 6) continue;

                    String status = parts[5].trim();
                    if ("active".equals(status)) {
                        String antecedent = parts[1].trim();
                        String consequent = parts[2].trim();
                        implications.computeIfAbsent(antecedent, k -> new ArrayList<>()).add(consequent);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to load tag-implications.csv", e);
        }
        return implications;
    }
}
