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

        for (ResponseKind<SearchResult> searchResult : searchResults) {
            List<ResponseKind<Listing<Comment>>> thread = recommendationProvider.fetchCommentTree(searchResult.data().id());

            thread.stream()
                  .flatMap(
                    kind ->
                      kind.data()
                          .children()
                          .stream()
                          .map(commentKind -> commentKind.data().body())
                    )
                    .forEach(comments::add);
        }

        return comments;
    }
}
