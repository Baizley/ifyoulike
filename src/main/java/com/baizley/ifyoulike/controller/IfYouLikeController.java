package com.baizley.ifyoulike.controller;

import com.baizley.ifyoulike.model.Recommendation;
import com.baizley.ifyoulike.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

@Controller
public class IfYouLikeController {

    private final RecommendationService recommendationService;
    private final long sseTimeout = Duration.ofMinutes(1).toMillis();

    @Autowired
    public IfYouLikeController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @RequestMapping(value = "/ifyoulike{blank}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Recommendation>> ifYouLikeJson(@PathVariable String blank) {
        return ResponseEntity.ok(recommendationService.retrieveRecommendations(blank));
    }

    @RequestMapping(value = "/ifyoulike{blank}")
    public SseEmitter ifYouLikeServerSentEvents(@PathVariable String blank) {
        List<CompletableFuture<List<Recommendation>>> futures = recommendationService.retrieveRecommendationsAsync(blank);

        ExecutorService executor = Executors.newSingleThreadExecutor();

        SseEmitter emitter = new SseEmitter(sseTimeout);

        executor.submit(() -> {
            futures.forEach(future -> future.thenAccept(recommendations -> {
                for (Recommendation recommendation : recommendations) {
                    SseEmitter.SseEventBuilder name = SseEmitter.event()
                            .data(recommendation)
                            .name("recommendation");
                    try {
                        emitter.send(name);
                    } catch (IOException exception) {
                        throw new RuntimeException("Failed to sent recommendation event.", exception);
                    }
                }
            }));

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            try {
                emitter.send(SseEmitter.event().name("COMPLETE").data("COMPLETE"));
            } catch (IOException exception) {
                throw new RuntimeException("Failed to send completion event.", exception);
            }
        });

        return emitter;
    }

    @RequestMapping(value = "/ifyoulike{blank}", produces = TEXT_HTML_VALUE)
    public String ifYouLikeHtml() {
        return "forward:/ifyoulike/index.html";
    }
}
