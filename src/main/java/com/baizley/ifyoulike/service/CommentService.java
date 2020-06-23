package com.baizley.ifyoulike.service;

import com.baizley.ifyoulike.model.Comment;
import com.baizley.ifyoulike.model.Listing;
import com.baizley.ifyoulike.model.ResponseKind;
import com.baizley.ifyoulike.model.SearchResult;
import com.baizley.ifyoulike.reddit.Reddit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommentService {

    private final Reddit reddit;

    @Autowired
    public CommentService(Reddit reddit) {
        this.reddit = reddit;
    }

    public List<String> retrieveTopLevelComments(String blank) {
        try {
            List<ResponseKind<SearchResult>> searchResults =
                    reddit.searchSubreddit(blank)
                            .data()
                            .children();

            List<String> comments = new ArrayList<>();

            for (ResponseKind<SearchResult> searchResult : searchResults) {
                List<ResponseKind<Listing<Comment>>> thread = reddit.fetchCommentTree(searchResult.data().id());

                thread.stream()
                        .flatMap(
                                kind -> kind.data()
                                            .children()
                                            .stream()
                                            .map(commentKind -> commentKind.data().body())

                        )
                        .forEach(comments::add);
            }

            return comments;
        } catch (IOException | URISyntaxException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
