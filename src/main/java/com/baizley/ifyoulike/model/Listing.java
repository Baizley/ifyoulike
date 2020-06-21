package com.baizley.ifyoulike.model;

import java.util.List;

public record Listing<T>(List<ResponseKind<T>> children) {}
