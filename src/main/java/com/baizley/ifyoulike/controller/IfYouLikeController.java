package com.baizley.ifyoulike.controller;

import com.baizley.ifyoulike.authorization.AccessToken;
import com.baizley.ifyoulike.authorization.Authorization;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
public class IfYouLikeController {

    private final AccessToken accessToken;
    private final HttpClient httpClient;
    private static final String USER_AGENT = System.getenv("USER_AGENT");

    @Autowired
    public IfYouLikeController(Authorization authorization) {
        accessToken = authorization.fetchAccessToken();
        this.httpClient = HttpClient.newHttpClient();
    }

    @RequestMapping(value = "/ifyoulike{blank}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> ifYouLikeJson(@PathVariable String blank) throws UnsupportedEncodingException {

        String encodedBlank = URLEncoder.encode(blank, StandardCharsets.UTF_8.toString());
        // TODO: Check status codes
        try {
            JSONObject body = new JSONObject(
                    httpClient.send(
                            HttpRequest.newBuilder()
                                    .header("Authorization", accessToken.toHeader())
                                    .header("User-Agent", USER_AGENT)
                                    .uri(new URI("https://oauth.reddit.com/r/ifyoulikeblank/search.json?restrict_sr=true&q=" + encodedBlank))
                                    .build(),
                            HttpResponse.BodyHandlers.ofString()
                    ).body()
            );


            JSONArray threads = body.getJSONObject("data").getJSONArray("children");

            JSONArray comments = new JSONArray();

            for (int threadIndex = 0; threadIndex < threads.length(); threadIndex++) {
                String id = threads.getJSONObject(threadIndex).getJSONObject("data").getString("id");
                JSONArray commentThread = new JSONArray(
                        httpClient.send(
                                HttpRequest.newBuilder()
                                        .header("Authorization", accessToken.toHeader())
                                        .header("User-Agent", USER_AGENT)
                                        .uri(new URI("https://oauth.reddit.com/r/ifyoulikeblank/comments/" + id))
                                        .build(),
                                HttpResponse.BodyHandlers.ofString()
                        ).body()
                );

                JSONArray topLevelComments = commentThread.getJSONObject(1).getJSONObject("data").getJSONArray("children");

                for (int commentIndex = 0; commentIndex < topLevelComments.length(); commentIndex++) {
                    String topLevelComment = topLevelComments.getJSONObject(commentIndex).getJSONObject("data").getString("body");
                    comments.put(topLevelComment);
                }
            }

            return ResponseEntity.ok(comments.toString());
        } catch (IOException | URISyntaxException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @RequestMapping(value = "/ifyoulike{blank}")
    public String ifYouLikeHtml(@PathVariable String blank, Model model) {
        return "index";
    }
}
