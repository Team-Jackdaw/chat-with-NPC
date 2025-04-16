package com.jackdaw.chatwithnpc.api.json;

import com.google.gson.Gson;

public class CompletionResponse {
    public String model;
    public String created_at;
    public String response;
    public String done_reason;
    public boolean done;
    public static CompletionResponse fromJson(String json) {
        return new Gson().fromJson(json, CompletionResponse.class);
    }
}
