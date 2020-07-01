package com.baizley.ifyoulike.service;

import com.baizley.ifyoulike.model.Recommendation;
import com.baizley.ifyoulike.recommendations.RecommendationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final RecommendationProvider recommendationProvider;

    @Autowired
    public RecommendationService(RecommendationProvider recommendationProvider) {
        this.recommendationProvider = recommendationProvider;
    }

    public List<String> retrieveRecommendations(String blank) {
        List<CompletableFuture<List<Recommendation>>> futures = recommendationProvider.fetchRecommendations(blank);

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        List<String> recommendations = new ArrayList<>();

        for (CompletableFuture<List<Recommendation>> future : futures) {
            try {
                recommendations.addAll(
                        future.get()
                                .stream()
                                .map(Recommendation::text)
                                .collect(Collectors.toList())
                );
            } catch (InterruptedException | ExecutionException e) { }
        }

        return recommendations;
    }
}
