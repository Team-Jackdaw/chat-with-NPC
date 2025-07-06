package com.jackdaw.chatwithnpc.api.json;

import com.google.gson.Gson;

import java.util.Map;

public class CompletionRequest {
    public String model;
    public String prompt;
    public String system;
    public boolean stream;
    public Map format;
    public int keep_alive = 300;

    public String toJson() {
        return new Gson().toJson(this);
    }
}
