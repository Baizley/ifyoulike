package com.baizley.ifyoulike.recommendations.reddit;

import com.baizley.ifyoulike.Environment;
import com.baizley.ifyoulike.recommendations.reddit.model.Comment;
import com.baizley.ifyoulike.recommendations.reddit.model.Link;
import com.baizley.ifyoulike.recommendations.reddit.model.Listing;
import com.baizley.ifyoulike.recommendations.reddit.model.ResponseKind;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class Reddit implements RedditApi {

    private final Converter converter;
    private final RedditAuthenticator authenticator;

    public Reddit(Converter converter, RedditAuthenticator authenticator) {
        this.converter = converter;
        this.authenticator = authenticator;
    }

    private static final String USER_AGENT = Environment.read("USER_AGENT");

    private final HttpClient httpClient = HttpClient.newHttpClient();

    public ResponseKind<Listing<Link>> searchSubreddit(String searchTerm) {
        AccessToken accessToken = authenticator.getAccessToken();

        HttpRequest request = HttpRequest.newBuilder()
                .header(AUTHORIZATION, accessToken.toHeader())
                .header(HttpHeaders.USER_AGENT, USER_AGENT)
                .uri(
                    UriComponentsBuilder
                        .fromHttpUrl("https://oauth.reddit.com/r/ifyoulikeblank/search.json?restrict_sr=true&q={searchTerm}")
                        .encode()
                        .buildAndExpand(searchTerm)
                        .toUri()
                )
                .build();

        try {
            // TODO: Check status codes.
            // 500: Report Reddit is down
            // 401: Report server error based on unauthorized
            // 403: Report server error based on forbidden
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return converter.toLink(response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<List<ResponseKind<Listing<Comment>>>> fetchComments(String articleId) {
        AccessToken accessToken = authenticator.getAccessToken();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .header(AUTHORIZATION, accessToken.toHeader())
                    .header(HttpHeaders.USER_AGENT, USER_AGENT)
                    .uri(new URI("https://oauth.reddit.com/r/ifyoulikeblank/comments/" + articleId))
                    .build();

            // TODO: Check status codes.
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(converter::toComment);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
