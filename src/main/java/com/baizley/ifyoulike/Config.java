package com.baizley.ifyoulike;

import com.baizley.ifyoulike.recommendations.reddit.RedditApi;
import com.baizley.ifyoulike.recommendations.reddit.Converter;
import com.baizley.ifyoulike.recommendations.reddit.Reddit;
import com.baizley.ifyoulike.recommendations.reddit.RedditAuthenticator;
import com.baizley.ifyoulike.recommendations.stub.NightCrawler;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class Config {

    private Gson gson;

    @Autowired
    public Config(Gson gson) {
        this.gson = gson;
    }

    @Bean
    public Converter converter() {
        return new Converter(gson);
    }

    @Bean
    public RedditAuthenticator redditAuthenticator() {
        return new RedditAuthenticator(converter());
    }

    @Profile("production")
    @Bean
    public RedditApi productionRedditApi() {
        return new Reddit(converter(), redditAuthenticator());
    }

    @Profile("development")
    @Bean
    public RedditApi developmentRedditApi() {
        return new NightCrawler();
    }
}
