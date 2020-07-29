package com.baizley.ifyoulike.recommendations.reddit;

import com.baizley.ifyoulike.Environment;
import com.baizley.ifyoulike.recommendations.RedditApi;
import com.baizley.ifyoulike.recommendations.reddit.model.Comment;
import com.baizley.ifyoulike.recommendations.reddit.model.Link;
import com.baizley.ifyoulike.recommendations.reddit.model.Listing;
import com.baizley.ifyoulike.recommendations.reddit.model.ResponseKind;
import com.google.common.base.Suppliers;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class Reddit implements RedditApi {

    private static final String USER_AGENT = Environment.read("USER_AGENT");

    private final HttpClient httpClient = HttpClient.newHttpClient();
    ;
    private final HttpRequest accessTokenRequest = buildAccessTokenRequest();
    private final Supplier<AccessToken> accessTokenSupplier = buildAccessTokenSupplier();

    public ResponseKind<Listing<Link>> searchSubreddit(String searchTerm) {
        AccessToken accessToken = accessTokenSupplier.get();


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
            return GsonTypes.toLink(response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<List<ResponseKind<Listing<Comment>>>> fetchCommentTree(String articleId) {
        AccessToken accessToken = accessTokenSupplier.get();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .header(AUTHORIZATION, accessToken.toHeader())
                    .header(HttpHeaders.USER_AGENT, USER_AGENT)
                    .uri(new URI("https://oauth.reddit.com/r/ifyoulikeblank/comments/" + articleId))
                    .build();

            // TODO: Check status codes.
            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(GsonTypes::toComment);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public AccessToken fetchAccessToken() {
        try {
            // TODO: Check status codes.
            HttpResponse<String> response = httpClient.send(accessTokenRequest, HttpResponse.BodyHandlers.ofString());
            return GsonTypes.toAccessToken(response.body());
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private HttpRequest buildAccessTokenRequest() {

        String basicAuthorization = base64Encode(Environment.read("BASIC_AUTHORIZATION"));
        String username = Environment.read("REDDIT_USERNAME");
        String password = Environment.read("REDDIT_PASSWORD");

        try {
            return HttpRequest.newBuilder()
                    .header("Accept", "application/json")
                    .header("Authorization", "Basic " + basicAuthorization)
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
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private Supplier<AccessToken> buildAccessTokenSupplier() {
        return Suppliers.memoizeWithExpiration(
                this::fetchAccessToken,
                58,
                TimeUnit.MINUTES
        );
    }

    private static String base64Encode(String content) {
        return new String(
                Base64.getEncoder()
                        .encode(content.getBytes())
        );
    }
}
