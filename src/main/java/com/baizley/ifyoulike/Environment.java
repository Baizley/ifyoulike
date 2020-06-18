package com.baizley.ifyoulike;

import java.util.Optional;

public class Environment {
    public static String read(String environmentVariable) {
        return Optional.ofNullable(System.getenv(environmentVariable))
                .orElseThrow(() -> new RuntimeException("Environment variable " + environmentVariable + " not set" ));
    }
}
