package com.baizley.ifyoulike.controller;

import com.baizley.ifyoulike.model.Recommendation;
import com.baizley.ifyoulike.recommendations.IfYouLikeRecommender;
import org.apache.juli.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IfYouLikeEmitter {

    private final SseEmitter emitter;
    private final ExecutorService executor;

    private final Logger logger = LoggerFactory.getLogger(IfYouLikeEmitter.class);

    public IfYouLikeEmitter() {
        long sseTimeout = Duration.ofMinutes(1).toMillis();
        emitter = new SseEmitter(sseTimeout);
        executor = Executors.newSingleThreadExecutor();
    }

    public SseEmitter emitRecommendations(List<CompletableFuture<List<Recommendation>>> futures) {
        executor.submit(() -> {
            futures.forEach(future -> future.thenAccept(recommendations -> {
                for (Recommendation recommendation : recommendations) {
                    this.emitRecommendation(recommendation);
                }
            }));

            awaitFutures(futures);
            emitCompletion();
        });

        return emitter;
    }

    private void awaitFutures(List<CompletableFuture<List<Recommendation>>> futures) {
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private void emitCompletion() {
        var completionEvent = SseEmitter.event().name("COMPLETE").data("COMPLETE");
        try {
            emitter.send(completionEvent);
            emitter.complete();
        } catch (IOException exception) {
            throw new RuntimeException("Failed to send completion event.", exception);
        }
    }

    private void emitRecommendation(Recommendation recommendation) {
        var recommendationEvent = SseEmitter.event()
                .data(recommendation)
                .name("recommendation");
        try {
            emitter.send(recommendationEvent);
        } catch (IOException exception) {
            logger.error("Failed to sent recommendation event.", exception);
        }
    }
}
