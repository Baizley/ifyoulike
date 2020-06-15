package com.baizley.ifyoulike.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.Optional;

public class Authorization {

    private static final String AUTHORIZATION_HEADER = new String(Base64.getEncoder().encode(readEnvVar("BASIC_AUTHORIZATION").getBytes()));
    private static final String USER_AGENT = readEnvVar("USER_AGENT");

    private final HttpClient httpClient;
    private final String username;
    private final String password;

    private AccessToken accessToken;

    public Authorization() {
        this.httpClient = HttpClient.newHttpClient();
        this.username = readEnvVar("REDDIT_USERNAME");
        this.password = readEnvVar("REDDIT_PASSWORD");
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

    private static String readEnvVar(String envVar) {
        return Optional.ofNullable(System.getenv(envVar))
                .orElseThrow(() -> new RuntimeException(envVar));
    }
}
