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
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

@Controller
public class IfYouLikeController {

    private final RecommendationService recommendationService;

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
        SseEmitter emitter = new SseEmitter();

        List<Recommendation> recommendations = recommendationService.retrieveRecommendations(blank);

        ExecutorService service = Executors.newSingleThreadExecutor();

        service.submit(() -> {
            for (Recommendation recommendation : recommendations) {
                SseEmitter.SseEventBuilder name = SseEmitter.event()
                        .data(recommendation)
                        .name("recommendation");
                try {
                    emitter.send(name);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return emitter;
    }



    @RequestMapping(value = "/ifyoulike{blank}", produces = TEXT_HTML_VALUE)
    public String ifYouLikeHtml() {
        return "forward:/ifyoulike/index.html";
    }
}
