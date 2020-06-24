package com.baizley.ifyoulike.reddit;

import com.baizley.ifyoulike.Environment;
import com.baizley.ifyoulike.model.Comment;
import com.baizley.ifyoulike.model.Listing;
import com.baizley.ifyoulike.model.ResponseKind;
import com.baizley.ifyoulike.model.SearchResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Reddit {

    private static final String AUTHORIZATION_HEADER = base64Encode(Environment.read("BASIC_AUTHORIZATION"));
    private static final String USER_AGENT = Environment.read("USER_AGENT");

    private final HttpClient httpClient;
    private final String username;
    private final String password;

    private AccessToken accessToken;

    public Reddit() {
        this.httpClient = HttpClient.newHttpClient();
        this.username = Environment.read("REDDIT_USERNAME");
        this.password = Environment.read("REDDIT_PASSWORD");
        accessToken = fetchAccessToken();
    }

    public ResponseKind<Listing<SearchResult>> searchSubreddit(String searchTerm) throws IOException, InterruptedException{
        // TODO: Check status codes.
        String body = httpClient.send(
                HttpRequest.newBuilder()
                        .header("Authorization", accessToken.toHeader())
                        .header("User-Agent", USER_AGENT)
                        .uri(
                            UriComponentsBuilder
                                    .fromHttpUrl("https://oauth.reddit.com/r/ifyoulikeblank/search.json?restrict_sr=true&q={searchTerm}")
                                    .encode()
                                    .buildAndExpand(searchTerm)
                                    .toUri()
                        )
                        .build(),
                HttpResponse.BodyHandlers.ofString()
        ).body();

        Type type = new TypeToken<ResponseKind<Listing<SearchResult>>>() {}.getType();

        return new Gson().fromJson(body, type);
    }

    public List<ResponseKind<Listing<Comment>>> fetchCommentTree(String articleId) throws URISyntaxException, IOException, InterruptedException {
        String body = httpClient.send(
                HttpRequest.newBuilder()
                        .header("Authorization", accessToken.toHeader())
                        .header("User-Agent", USER_AGENT)
                        .uri(new URI("https://oauth.reddit.com/r/ifyoulikeblank/comments/" + articleId))
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

    public AccessToken fetchAccessToken() {
        // TODO: Add logic for refreshing access token using refresh token.
        if (accessToken != null) {
            return accessToken;
        }

        try {
            // TODO: Check status codes.
            HttpRequest accessTokenRequest = HttpRequest.newBuilder()
                    .header("Accept", "application/json")
                    .header("Authorization", "Basic " + AUTHORIZATION_HEADER)
                    .header("User-Agent", USER_AGENT)
                    .uri(new URI("https://www.reddit.com/api/v1/access_token"))
                    .POST(
                            HttpRequest
                                    .BodyPublishers
                                    .ofString(
                                            "grant_type=password" +
                                                  "&username=" + username +
                                                  "&password=" + password
                                    )
                    )
                    .build();

            HttpResponse<String> response = httpClient.send(accessTokenRequest, HttpResponse.BodyHandlers.ofString());

            this.accessToken = new Gson().fromJson(response.body(), AccessToken.class);

            return accessToken;
        } catch (URISyntaxException | InterruptedException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static String base64Encode(String content) {
        return new String(
                Base64.getEncoder()
                      .encode(content.getBytes())
        );
    }
}
