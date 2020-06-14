package com.baizley.ifyoulike.authorization;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccessToken(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") int ttl,
        @JsonProperty("scope") String scope
    ) {

    public String toHeader() {
        return tokenType + " " + accessToken;
    }
}
