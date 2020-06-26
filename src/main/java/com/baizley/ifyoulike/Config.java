package com.baizley.ifyoulike;

import com.baizley.ifyoulike.recommendations.RecommendationProvider;
import com.baizley.ifyoulike.recommendations.reddit.Reddit;
import com.baizley.ifyoulike.recommendations.stub.NightCrawlerProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class Config {

    @Profile("production")
    @Bean
    public RecommendationProvider productionRecommendationProvider() {
        return new Reddit();
    }

    @Profile("development")
    @Bean
    public RecommendationProvider developmentRecommendationProvider() {
        return new NightCrawlerProvider();
    }
}
