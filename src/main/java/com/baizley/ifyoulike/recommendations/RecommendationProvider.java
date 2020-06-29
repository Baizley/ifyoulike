package com.baizley.ifyoulike.recommendations;

import com.baizley.ifyoulike.model.Comment;
import com.baizley.ifyoulike.model.Listing;
import com.baizley.ifyoulike.model.ResponseKind;
import com.baizley.ifyoulike.model.SearchResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface RecommendationProvider {
    ResponseKind<Listing<SearchResult>> searchSubreddit(String searchTerm);
    CompletableFuture<List<ResponseKind<Listing<Comment>>>> fetchCommentTree(String articleId);
}
