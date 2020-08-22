package com.baizley.ifyoulike.recommendations.reddit;

import com.baizley.ifyoulike.recommendations.reddit.model.Comment;
import com.baizley.ifyoulike.recommendations.reddit.model.Link;
import com.baizley.ifyoulike.recommendations.reddit.model.Listing;
import com.baizley.ifyoulike.recommendations.reddit.model.ResponseKind;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class Converter {

    private final Gson gson;

    public Converter(Gson gson) {
        this.gson = gson;
    }

    private final Type link = new TypeToken<ResponseKind<Listing<Link>>>() {}.getType();
    private final Type comment = new TypeToken<List<ResponseKind<Listing<Comment>>>>() {}.getType();

    public ResponseKind<Listing<Link>> toLink(String body) {
        return gson.fromJson(body, this.link);
    }

    public List<ResponseKind<Listing<Comment>>> toComment(String body) {
        return gson.fromJson(body, this.comment);
    }

    public AccessToken toAccessToken(String body) {
        return gson.fromJson(body, AccessToken.class);
    }
}
