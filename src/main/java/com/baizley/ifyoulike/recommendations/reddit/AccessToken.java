package com.baizley.ifyoulike.recommendations.reddit;

public record AccessToken(
        String access_token,
        String token_type,
        int ttl,
        String scope
    ) {

    public String toHeader() {
        return token_type + " " + access_token;
    }
}
