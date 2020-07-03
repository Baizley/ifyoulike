package com.baizley.ifyoulike;

import com.baizley.ifyoulike.recommendations.RedditApi;
import com.baizley.ifyoulike.recommendations.reddit.Reddit;
import com.baizley.ifyoulike.recommendations.stub.NightCrawler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class Config {

    @Profile("production")
    @Bean
    public RedditApi productionRedditApi() {
        return new Reddit();
    }

    @Profile("development")
    @Bean
    public RedditApi developmentRedditApi() {
        return new NightCrawler();
    }
}
