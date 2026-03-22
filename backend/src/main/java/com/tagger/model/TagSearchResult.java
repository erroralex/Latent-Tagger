package com.tagger.model;

public record TagSearchResult(
    String name,
    int category,
    int postCount,
    boolean valid,
    String resolvedFrom
) {}
