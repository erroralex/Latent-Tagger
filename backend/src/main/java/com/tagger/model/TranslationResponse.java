package com.tagger.model;

import java.util.List;

public record TranslationResponse(
    List<String> tags,
    String modelFamily,
    String warning
) {}
