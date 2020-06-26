package com.baizley.ifyoulike.recommendations;

import com.baizley.ifyoulike.model.Comment;
import com.baizley.ifyoulike.model.Listing;
import com.baizley.ifyoulike.model.ResponseKind;
import com.baizley.ifyoulike.model.SearchResult;

import java.util.List;

public interface RecommendationProvider {
    ResponseKind<Listing<SearchResult>> searchSubreddit(String searchTerm);
    List<ResponseKind<Listing<Comment>>> fetchCommentTree(String articleId);
}
