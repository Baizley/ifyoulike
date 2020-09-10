package com.baizley.ifyoulike.recommendations.stub;

import com.baizley.ifyoulike.recommendations.reddit.RedditApi;
import com.baizley.ifyoulike.recommendations.reddit.model.Comment;
import com.baizley.ifyoulike.recommendations.reddit.model.Link;
import com.baizley.ifyoulike.recommendations.reddit.model.Listing;
import com.baizley.ifyoulike.recommendations.reddit.model.ResponseKind;
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
import java.util.concurrent.CompletableFuture;

public class NightCrawler implements RedditApi {

    @Value("classpath:stub-data/nightcrawler/subreddit-search.json")
    Resource searchStub;
    @Value("classpath:stub-data/nightcrawler/thread.json")
    Resource threadStub;

    public ResponseKind<Listing<Link>> searchSubreddit(String searchTerm) {
        if (!"nightcrawler".equals(searchTerm)) {
            return new ResponseKind<>(new Listing<>(new ArrayList<>()));
        }

        Type type = new TypeToken<ResponseKind<Listing<Link>>>(){}.getType();
        try {
            return new Gson().fromJson(new JsonReader(new FileReader(searchStub.getFile())), type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<List<ResponseKind<Listing<Comment>>>> fetchComments(String articleId) {
        if (!"917p98".equals(articleId)) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        Type type = new TypeToken<List<ResponseKind<Listing<Comment>>>>(){}.getType();
        try {
            List<ResponseKind<Listing<Comment>>> responseKinds = new Gson().fromJson(new JsonReader(new FileReader(threadStub.getFile())), type);
            return CompletableFuture.completedFuture(responseKinds);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
