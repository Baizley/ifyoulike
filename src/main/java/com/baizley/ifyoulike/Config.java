package com.baizley.ifyoulike;

import com.baizley.ifyoulike.reddit.Reddit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    @Bean
    public Reddit reddit() {
        return new Reddit();
    }
}
