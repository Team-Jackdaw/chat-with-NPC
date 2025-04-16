package com.jackdaw.chatwithnpc.api.json;

import com.google.gson.Gson;

import java.util.List;

public class EmbeddingRequest {
    public String model;
    public List<String> input;
    public String toJson() {return new Gson().toJson(this);
    }
}
