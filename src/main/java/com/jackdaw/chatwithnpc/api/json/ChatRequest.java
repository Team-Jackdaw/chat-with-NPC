package com.jackdaw.chatwithnpc.api.json;

import com.google.gson.Gson;

import java.util.List;

public class ChatRequest {
    public String model;
    public List<Message> messages;
    public boolean stream;
    public List<Tool> tools;

    public String toJson() {
        return new Gson().toJson(this);
    }
}
