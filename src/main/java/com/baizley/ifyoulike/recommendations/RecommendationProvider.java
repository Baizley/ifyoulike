package com.baizley.ifyoulike.recommendations;

import com.baizley.ifyoulike.model.Recommendation;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RecommendationProvider {
    List<CompletableFuture<List<Recommendation>>> fetchRecommendations(String blank);
}
