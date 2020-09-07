package com.baizley.ifyoulike.service;

import com.baizley.ifyoulike.model.Recommendation;
import com.baizley.ifyoulike.recommendations.IfYouLikeRecommender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class RecommendationService {

    private final IfYouLikeRecommender ifYouLikeRecommender;

    @Autowired
    public RecommendationService(IfYouLikeRecommender ifYouLikeRecommender) {
        this.ifYouLikeRecommender = ifYouLikeRecommender;
    }

    public List<CompletableFuture<List<Recommendation>>> retrieveRecommendationsAsync(String blank) {
        return ifYouLikeRecommender.fetchRecommendations(blank);
    }

    public List<Recommendation> retrieveRecommendations(String blank) {
        var futures = ifYouLikeRecommender.fetchRecommendations(blank);

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        List<Recommendation> recommendations = new ArrayList<>();

        for (CompletableFuture<List<Recommendation>> future : futures) {
            try {
                recommendations.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        return recommendations;
    }
}
