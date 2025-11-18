package com.saumyabot.discord_bot.model;

public class Problem {
    private final String slug;
    private final String title;
    private final int difficulty;

    public Problem(String slug, String title, int difficulty) {
        this.slug = slug;
        this.title = title;
        this.difficulty = difficulty;
    }

    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public int getDifficulty() { return difficulty; }
}
