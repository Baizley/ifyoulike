package com.baizley.ifyoulike.service;

import com.baizley.ifyoulike.authorization.AccessToken;
import com.baizley.ifyoulike.authorization.Authorization;
import com.baizley.ifyoulike.model.Comment;
import com.baizley.ifyoulike.model.Listing;
import com.baizley.ifyoulike.model.ResponseKind;
import com.baizley.ifyoulike.model.SearchResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final AccessToken accessToken;
    private final HttpClient httpClient;
    private static final String USER_AGENT = System.getenv("USER_AGENT");

    public CommentService(Authorization authorization) {
        accessToken = authorization.fetchAccessToken();
        this.httpClient = HttpClient.newHttpClient();
    }

    public List<String> retrieveTopLevelComments(String blank) {
        try {
            List<ResponseKind<SearchResult>> searchResults = performSubredditSearch(blank)
                    .data()
                    .children();

            List<String> comments = new ArrayList<>();

            for (ResponseKind<SearchResult> searchResult : searchResults) {
                List<ResponseKind<Listing<Comment>>> thread = fetchTread(searchResult.data().id());

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

    private List<ResponseKind<Listing<Comment>>> fetchTread(String threadId) throws URISyntaxException, IOException, InterruptedException {
        String body = httpClient.send(
                HttpRequest.newBuilder()
                        .header("Authorization", accessToken.toHeader())
                        .header("User-Agent", USER_AGENT)
                        .uri(new URI("https://oauth.reddit.com/r/ifyoulikeblank/comments/" + threadId))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        ).body();

        Type type = new TypeToken<List<ResponseKind<Listing<Comment>>>>() {}.getType();
        List<ResponseKind<Listing<Comment>>> comments = new Gson().fromJson(body, type);

        return comments.stream()
                .filter(response -> response.data()
                                            .children()
                                            .stream()
                                            .map(ResponseKind::data)
                                            .map(Comment::body)
                                            .anyMatch(Objects::nonNull)
                )
                .collect(Collectors.toList());
    }

    private ResponseKind<Listing<SearchResult>> performSubredditSearch(String searchTerm) throws IOException, InterruptedException, URISyntaxException {
        String encodedBlank = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8.toString());

        // TODO: Check status codes.
        String body = httpClient.send(
                HttpRequest.newBuilder()
                        .header("Authorization", accessToken.toHeader())
                        .header("User-Agent", USER_AGENT)
                        .uri(new URI("https://oauth.reddit.com/r/ifyoulikeblank/search.json?restrict_sr=true&q=" + encodedBlank))
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        ).body();

        Type type = new TypeToken<ResponseKind<Listing<SearchResult>>>() {}.getType();

        return new Gson().fromJson(body, type);
    }
}
