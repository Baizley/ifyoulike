package com.baizley.ifyoulike.service;

import com.baizley.ifyoulike.model.Comment;
import com.baizley.ifyoulike.model.Listing;
import com.baizley.ifyoulike.model.ResponseKind;
import com.baizley.ifyoulike.model.SearchResult;
import com.baizley.ifyoulike.recommendations.RecommendationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final RecommendationProvider recommendationProvider;

    @Autowired
    public CommentService(RecommendationProvider recommendationProvider) {
        this.recommendationProvider = recommendationProvider;
    }

    public List<String> retrieveTopLevelComments(String blank) {
        List<ResponseKind<SearchResult>> searchResults =
                recommendationProvider.searchSubreddit(blank)
                        .data()
                        .children();

        List<String> comments = new ArrayList<>();

        List<CompletableFuture<List<ResponseKind<Listing<Comment>>>>> futures = searchResults.stream()
                .map(ResponseKind::data)
                .map(SearchResult::id)
                .map(recommendationProvider::fetchCommentTree)
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));


        for (CompletableFuture<List<ResponseKind<Listing<Comment>>>> future : futures) {
            try {
                List<ResponseKind<Listing<Comment>>> thread = future.get();

                List<String> recommendations = thread.stream()
                        .flatMap(
                                kind ->
                                        kind.data()
                                                .children()
                                                .stream()
                                                .map(commentKind -> commentKind.data().body())
                        ).collect(Collectors.toList());

                comments.addAll(recommendations);
            } catch (InterruptedException | ExecutionException e) { }
        }

        return comments;
    }
}
