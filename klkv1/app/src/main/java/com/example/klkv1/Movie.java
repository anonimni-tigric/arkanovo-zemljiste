package com.example.klkv1;

public class Movie {
    private String name;
    private int rating;
    private boolean watched;

    public Movie(String name, int rating, boolean watched) {
        this.name = name;
        this.rating = rating;
        this.watched = watched;
    }

    public String getName() {
        return name;
    }

    public int getRating() {
        return rating;
    }

    public boolean isWatched() {
        return watched;
    }
}