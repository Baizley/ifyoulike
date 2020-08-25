package com.baizley.ifyoulike.recommendations;

import com.baizley.ifyoulike.model.Recommendation;
import com.baizley.ifyoulike.recommendations.reddit.model.Comment;
import com.baizley.ifyoulike.recommendations.reddit.model.Link;
import com.baizley.ifyoulike.recommendations.reddit.model.Listing;
import com.baizley.ifyoulike.recommendations.reddit.model.ResponseKind;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
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

        List<ResponseKind<Link>> searchResults =
                redditApi.searchSubreddit(blank)
                        .data()
                        .children();
        
        return searchResults.stream()
                .map(ResponseKind::data)
                .sorted(Comparator.comparingInt(Link::score))
                .map(Link::id)
                .map(redditApi::fetchCommentTree)
                .map(future -> future.thenApply(this::extractComments))
                .collect(Collectors.toList());
    }

    private List<Recommendation> extractComments(List<ResponseKind<Listing<Comment>>> thread) {
        return thread.stream()
                .filter(this::isComment)
                .flatMap(this::extractComment)
                .map(comment -> {
                    try {
                        return new Recommendation(comment.body(), new URL("https://reddit.com" + comment.permalink()));
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    private boolean isComment(ResponseKind<Listing<Comment>> kind) {
        return kind.data()
                .children()
                .stream()
                .map(ResponseKind::data)
                .map(Comment::body)
                .anyMatch(Objects::nonNull);
    }

    private Stream<Comment> extractComment(ResponseKind<Listing<Comment>> kind) {
        return kind.data()
                .children()
                .stream()
                .map(ResponseKind::data);
    }
}

