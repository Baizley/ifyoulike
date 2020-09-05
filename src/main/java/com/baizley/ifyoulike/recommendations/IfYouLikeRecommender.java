package com.baizley.ifyoulike.recommendations;

import com.baizley.ifyoulike.model.Recommendation;
import com.baizley.ifyoulike.recommendations.reddit.RedditApi;
import com.baizley.ifyoulike.recommendations.reddit.model.Comment;
import com.baizley.ifyoulike.recommendations.reddit.model.Link;
import com.baizley.ifyoulike.recommendations.reddit.model.Listing;
import com.baizley.ifyoulike.recommendations.reddit.model.ResponseKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class IfYouLikeRecommender {

    private Logger logger = LoggerFactory.getLogger(IfYouLikeRecommender.class);
    private final RedditApi redditApi;

    @Autowired
    public IfYouLikeRecommender(RedditApi redditApi) {
        this.redditApi = redditApi;
    }

    public List<CompletableFuture<List<Recommendation>>> fetchRecommendations(String blank) {
        List<Link> searchResults = performSearch(blank);
        
        return searchResults.stream()
                .map(this::toRecommendation)
                .collect(Collectors.toList());
    }

    private List<Link> performSearch(String blank) {
        return redditApi.searchSubreddit(blank)
                .data()
                .children()
                .stream()
                .map(ResponseKind::data)
                .collect(Collectors.toList());
    }

    private CompletableFuture<List<Recommendation>> toRecommendation(Link link) {
        return redditApi.fetchCommentTree(link.id())
                .thenApply(this::extractComments)
                .thenApply(comments -> constructRecommendations(link, comments));
    }

    private List<Recommendation> constructRecommendations(Link threadlink, List<Comment> comments) {
        return comments.stream()
                .map(comment -> constructRecommendation(threadlink, comment))
                .collect(Collectors.toList());
    }

    private Recommendation constructRecommendation(Link threadlink, Comment comment) {
        try {
            return new Recommendation(comment.body(), new URL("http://reddit.com" + comment.permalink()), threadlink.score() + comment.score());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Comment> extractComments(List<ResponseKind<Listing<Comment>>> thread) {
        return thread.stream()
                .filter(this::isComment)
                .flatMap(this::extractComment)
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

