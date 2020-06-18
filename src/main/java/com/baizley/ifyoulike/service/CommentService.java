package com.baizley.ifyoulike.service;

import com.baizley.ifyoulike.authorization.AccessToken;
import com.baizley.ifyoulike.authorization.Authorization;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class CommentService {

    private final AccessToken accessToken;
    private final HttpClient httpClient;
    private static final String USER_AGENT = System.getenv("USER_AGENT");

    public CommentService(Authorization authorization) {
        accessToken = authorization.fetchAccessToken();
        this.httpClient = HttpClient.newHttpClient();
    }

    public JSONArray retrieveTopLevelComments(String blank) {
        try {
            // TODO: Check status codes
            JSONArray threads = performSubredditSearch(blank);

            JSONArray comments = new JSONArray();

            for (int threadIndex = 0; threadIndex < threads.length(); threadIndex++) {
                String id = threads.getJSONObject(threadIndex).getJSONObject("data").getString("id");
                JSONArray thread = fetchTread(id);

                JSONArray commentThreads = extractCommentThreads(thread);

                for (int commentIndex = 0; commentIndex < commentThreads.length(); commentIndex++) {
                    comments.put(extractToplevelComment(commentThreads.getJSONObject(commentIndex)));
                }
            }

            return comments;
        } catch (IOException | URISyntaxException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private String extractToplevelComment(JSONObject topLevelComment) {
        return topLevelComment.getJSONObject("data").getString("body");
    }

    private JSONArray extractCommentThreads(JSONArray thread) {
        return thread.getJSONObject(1).getJSONObject("data").getJSONArray("children");
    }

    private JSONArray fetchTread(String threadId) throws URISyntaxException, IOException, InterruptedException {
        return new JSONArray(
                    httpClient.send(
                            HttpRequest.newBuilder()
                                    .header("Authorization", accessToken.toHeader())
                                    .header("User-Agent", USER_AGENT)
                                    .uri(new URI("https://oauth.reddit.com/r/ifyoulikeblank/comments/" + threadId))
                                    .build(),
                            HttpResponse.BodyHandlers.ofString()
                    ).body()
            );
    }

    private JSONArray performSubredditSearch(String searchTerm) throws IOException, InterruptedException, URISyntaxException {
        String encodedBlank = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8.toString());

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


        return body.getJSONObject("data").getJSONArray("children");
    }
}
