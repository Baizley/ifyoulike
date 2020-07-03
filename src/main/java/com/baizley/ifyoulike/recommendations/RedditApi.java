package com.baizley.ifyoulike.recommendations;

import com.baizley.ifyoulike.recommendations.reddit.model.Comment;
import com.baizley.ifyoulike.recommendations.reddit.model.Listing;
import com.baizley.ifyoulike.recommendations.reddit.model.ResponseKind;
import com.baizley.ifyoulike.recommendations.reddit.model.SearchResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RedditApi {
    ResponseKind<Listing<SearchResult>> searchSubreddit(String blank);
    CompletableFuture<List<ResponseKind<Listing<Comment>>>> fetchCommentTree(String articleId);
}
