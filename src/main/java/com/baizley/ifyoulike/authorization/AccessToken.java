package com.baizley.ifyoulike.authorization;

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
