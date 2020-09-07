package com.baizley.ifyoulike.recommendations.reddit;

import com.baizley.ifyoulike.Environment;
import com.google.common.base.Suppliers;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class RedditAuthenticator {

    private static final String USER_AGENT = Environment.read("USER_AGENT");
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final HttpRequest accessTokenRequest = buildAccessTokenRequest();
    private final Supplier<AccessToken> accessTokenSupplier = buildAccessTokenSupplier();
    private final Converter converter;

    @Autowired
    public RedditAuthenticator(Converter converter) {
        this.converter = converter;
    }

    public AccessToken getAccessToken() {
        return accessTokenSupplier.get();
    }

    private AccessToken fetchAccessToken() {
        try {
            // TODO: Check status codes.
            HttpResponse<String> response = httpClient.send(accessTokenRequest, HttpResponse.BodyHandlers.ofString());
            return converter.toAccessToken(response.body());
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
        return Base64.getEncoder().encodeToString(content.getBytes());
    }
}
