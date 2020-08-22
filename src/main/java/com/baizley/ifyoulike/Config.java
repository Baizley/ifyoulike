package com.baizley.ifyoulike;

import com.baizley.ifyoulike.recommendations.RedditApi;
import com.baizley.ifyoulike.recommendations.reddit.Converter;
import com.baizley.ifyoulike.recommendations.reddit.Reddit;
import com.baizley.ifyoulike.recommendations.stub.NightCrawler;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class Config {

    @Autowired
    private Gson gson;

    @Profile("production")
    @Bean
    public RedditApi productionRedditApi() {
        return new Reddit(new Converter(gson));
    }

    @Profile("development")
    @Bean
    public RedditApi developmentRedditApi() {
        return new NightCrawler();
    }
}
