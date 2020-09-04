package com.baizley.ifyoulike.controller;

import com.baizley.ifyoulike.model.Recommendation;
import com.baizley.ifyoulike.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
        List<CompletableFuture<List<Recommendation>>> futures = recommendationService.retrieveRecommendationsAsync(blank);

        IfYouLikeEmitter ifYouLikeEmitter = new IfYouLikeEmitter();

        return ifYouLikeEmitter.emitRecommendations(futures);
    }

    @RequestMapping(value = "/ifyoulike{blank}", produces = TEXT_HTML_VALUE)
    public String ifYouLikeHtml() {
        return "forward:/ifyoulike/index.html";
    }
}
