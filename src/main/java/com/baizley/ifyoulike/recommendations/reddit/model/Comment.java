package com.baizley.ifyoulike.recommendations.reddit.model;

public record Comment(String body, String permalink, int score) {}