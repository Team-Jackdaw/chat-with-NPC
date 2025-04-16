package com.jackdaw.chatwithnpc.api.json;

import com.google.gson.Gson;

import java.util.List;

public class EmbeddingResponse {
    public String model;
    public List<List<Float>> embeddings;

    public static EmbeddingResponse fromJson(String res) {
        return new Gson().fromJson(res, EmbeddingResponse.class);
    }
}
