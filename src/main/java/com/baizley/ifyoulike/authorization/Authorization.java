package com.baizley.ifyoulike.authorization;

import com.baizley.ifyoulike.Environment;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class Authorization {

    private static final String AUTHORIZATION_HEADER = base64Encode(Environment.read("BASIC_AUTHORIZATION"));
    private static final String USER_AGENT = Environment.read("USER_AGENT");

    private final HttpClient httpClient;
    private final String username;
    private final String password;

    private AccessToken accessToken;

    public Authorization() {
        this.httpClient = HttpClient.newHttpClient();
        this.username = Environment.read("REDDIT_USERNAME");
        this.password = Environment.read("REDDIT_PASSWORD");
    }

    public AccessToken fetchAccessToken() {
        if (accessToken != null) {
            return accessToken;
        }

        try {
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

            this.accessToken = new ObjectMapper().readValue(response.body(), AccessToken.class);

            return accessToken;
        } catch (URISyntaxException | InterruptedException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private static String base64Encode(String content) {
        return new String(
                Base64.getEncoder()
                      .encode(
                          content.getBytes()
                      )
        );
    }
}
