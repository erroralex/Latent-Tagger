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

/**
 * {@code TagDataLoader} is responsible for loading tag data, aliases, and implications
 * from CSV files located in the application's resources. It parses these files
 * and prepares the data for the {@link TagDatabaseService}.
 *
 * <p>The class handles the following data sources:
 * <ul>
 *     <li>{@code /data/tags.csv}: Contains the primary tag information, including
 *         tag names, categories, and post counts. Tags with a post count less than 20
 *         are filtered out. The loaded tags are sorted alphabetically by name.</li>
 *     <li>{@code /data/tag-aliases.csv}: Contains tag alias mappings. Only active
 *         aliases are loaded, mapping an antecedent tag to a consequent tag.</li>
 *     <li>{@code /data/tag-implications.csv}: Contains tag implication mappings.
 *         Only active implications are loaded, where one tag implies another.</li>
 * </ul>
 *
 * <p>This class provides static methods to load all necessary data and construct
 * a {@link TagDatabaseService} instance. It logs warnings if resource files are
 * not found and errors if parsing fails.
 *
 * <p>The {@code LoadResult} record encapsulates the arrays of sorted tag names,
 * categories, and post counts.
 *
 * @see TagDatabaseService
 */
public class TagDataLoader {

    private static final Logger log = LoggerFactory.getLogger(TagDataLoader.class);

    public record LoadResult(String[] names, int[] categories, int[] postCounts) {
    }

    public static TagDatabaseService load() {
        LoadResult tagLoadResult = loadTags();
        Map<String, String> aliases = loadAliases();
        Map<String, List<String>> implications = loadImplications();
        return new TagDatabaseService(tagLoadResult, aliases, implications);
    }

    /**
     * Parses a single CSV line, respecting double-quoted fields that may contain commas.
     * This custom character scanner avoids the overhead of regex compilation per line
     * and external dependencies like OpenCSV, making it extremely fast for millions of rows.
     * It also automatically strips the surrounding quotes from extracted values.
     *
     * @param line The raw CSV line string.
     * @return An array of parsed and unquoted CSV fields.
     */
    private static String[] parseCsvLine(String line) {
        if (line == null || line.isEmpty()) {
            return new String[0];
        }

        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes; // Toggle quote state
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0); // Reset buffer
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim()); // Add the last token

        return tokens.toArray(new String[0]);
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
                    // Refactoring: Use the custom CSV parser to handle quotes safely
                    String[] parts = parseCsvLine(line);
                    if (parts.length < 3) continue;

                    try {
                        String name = parts[0];
                        int category = Integer.parseInt(parts[1]);
                        int postCount = Integer.parseInt(parts[2]);

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
                    // Refactoring: Use the custom CSV parser to handle quotes safely
                    String[] parts = parseCsvLine(line);
                    if (parts.length < 6) continue;

                    String status = parts[5];
                    if ("active".equals(status)) {
                        String antecedent = parts[1];
                        String consequent = parts[2];
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
                reader.readLine();
                String line;
                while ((line = reader.readLine()) != null) {
                    // Refactoring: Use the custom CSV parser to handle quotes safely
                    String[] parts = parseCsvLine(line);
                    if (parts.length < 6) continue;

                    String status = parts[5];
                    if ("active".equals(status)) {
                        String antecedent = parts[1];
                        String consequent = parts[2];
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