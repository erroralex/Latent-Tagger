package com.tagger.model;

import java.util.List;

public record HistoryEntry(
    long id,
    List<String> tags,
    String nlInput,
    String model,
    long createdAt
) {}
