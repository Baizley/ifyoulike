package com.baizley.ifyoulike.recommendations.stub;

import com.baizley.ifyoulike.model.Comment;
import com.baizley.ifyoulike.model.Listing;
import com.baizley.ifyoulike.model.ResponseKind;
import com.baizley.ifyoulike.model.SearchResult;
import com.baizley.ifyoulike.recommendations.RecommendationProvider;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class NightCrawlerProvider implements RecommendationProvider {

    @Value("classpath:stub-data/nightcrawler/subreddit-search.json")
    Resource searchStub;
    @Value("classpath:stub-data/nightcrawler/thread.json")
    Resource threadStub;

    @Override
    public ResponseKind<Listing<SearchResult>> searchSubreddit(String searchTerm) {
        Type type = new TypeToken<ResponseKind<Listing<SearchResult>>>() {}.getType();
        try {
            return new Gson().fromJson(new JsonReader(new FileReader(searchStub.getFile())), type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ResponseKind<Listing<Comment>>> fetchCommentTree(String articleId) {

        if ("917p98".equals(articleId)) {
            Type type = new TypeToken<List<ResponseKind<Listing<Comment>>>>() {}.getType();
            try {
                List<ResponseKind<Listing<Comment>>> responseKinds = new Gson().fromJson(new JsonReader(new FileReader(threadStub.getFile())), type);
                return responseKinds.stream()
                        .filter(kind ->
                                kind.data()
                                        .children()
                                        .stream()
                                        .map(ResponseKind::data)
                                        .map(Comment::body)
                                        .anyMatch(Objects::nonNull)
                        )
                        .collect(Collectors.toList());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new ArrayList<>();
        }
    }
}