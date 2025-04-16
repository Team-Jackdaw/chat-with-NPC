package com.jackdaw.chatwithnpc.api.json;

import com.google.gson.Gson;

public class Tool {
    public String type;
    public Function function;
    public String call;
    public static Tool fromJson(String json) {
        return new Gson().fromJson(json, Tool.class);
    }
}

