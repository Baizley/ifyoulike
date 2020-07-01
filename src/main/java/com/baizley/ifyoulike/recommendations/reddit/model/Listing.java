package com.baizley.ifyoulike.recommendations.reddit.model;

import java.util.List;

public record Listing<T>(List<ResponseKind<T>> children) {}
