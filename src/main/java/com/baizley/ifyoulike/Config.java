package com.baizley.ifyoulike;

import com.baizley.ifyoulike.authorization.Authorization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
    @Bean
    public Authorization authorization() {
        return new Authorization();
    }
}
