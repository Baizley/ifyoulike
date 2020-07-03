package com.baizley.ifyoulike.recommendations;

import com.baizley.ifyoulike.model.Recommendation;
import com.baizley.ifyoulike.recommendations.reddit.model.Comment;
import com.baizley.ifyoulike.recommendations.reddit.model.Listing;
import com.baizley.ifyoulike.recommendations.reddit.model.ResponseKind;
import com.baizley.ifyoulike.recommendations.reddit.model.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class IfYouLikeRecommender {

    private final RedditApi redditApi;

    @Autowired
    public IfYouLikeRecommender(RedditApi redditApi) {
        this.redditApi = redditApi;
    }

    public List<CompletableFuture<List<Recommendation>>> fetchRecommendations(String blank) {

        List<ResponseKind<SearchResult>> searchResults =
                redditApi.searchSubreddit(blank)
                         .data()
                         .children();

        return searchResults.stream()
                .map(ResponseKind::data)
                .map(SearchResult::id)
                .map(redditApi::fetchCommentTree)
                .map(future -> future.thenApply(this::extractComments))
                .collect(Collectors.toList());
    }

    private List<Recommendation> extractComments(List<ResponseKind<Listing<Comment>>> thread) {
        return thread.stream()
                .filter(kind ->
                        kind.data()
                                .children()
                                .stream()
                                .map(ResponseKind::data)
                                .map(Comment::body)
                                .anyMatch(Objects::nonNull)
                )
                .flatMap(this::extractCommentBody)
                .map(Recommendation::new)
                .collect(Collectors.toList());
    }

    private Stream<String> extractCommentBody(ResponseKind<Listing<Comment>> kind) {
        return kind.data()
                .children()
                .stream()
                .map(ResponseKind::data)
                .map(Comment::body);
    }
}

