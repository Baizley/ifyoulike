package com.baizley.ifyoulike.recommendations.reddit;

import com.baizley.ifyoulike.recommendations.reddit.model.Comment;
import com.baizley.ifyoulike.recommendations.reddit.model.Link;
import com.baizley.ifyoulike.recommendations.reddit.model.Listing;
import com.baizley.ifyoulike.recommendations.reddit.model.ResponseKind;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class GsonTypes {
    private static final Type link = new TypeToken<ResponseKind<Listing<Link>>>() {
    }.getType();
    public static final Type comment = new TypeToken<List<ResponseKind<Listing<Comment>>>>() {
    }.getType();

    public static ResponseKind<Listing<Link>> toLink(String body) {
        return new Gson().fromJson(body, GsonTypes.link);
    }

    public static List<ResponseKind<Listing<Comment>>> toComment(String body) {
        return new Gson().fromJson(body, GsonTypes.comment);
    }

    public static AccessToken toAccessToken(String body) {
        return new Gson().fromJson(body, AccessToken.class);
    }
}
