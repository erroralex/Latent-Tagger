package com.tagger.model;

public record Tag(String name, int category, int postCount) {
    public interface Category {
        int GENERAL = 0;
        int ARTIST = 1;
        int COPYRIGHT = 3;
        int CHARACTER = 4;
        int META = 5;
    }
}
