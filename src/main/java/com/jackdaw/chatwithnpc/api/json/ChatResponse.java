package com.jackdaw.chatwithnpc.api.json;

import com.google.gson.Gson;

import java.util.List;
import java.util.Map;

public class ChatResponse {
    public String model;
    public String created_at;
    public Message message;
    public static class Message {
        public String role;
        public String content;
        public List<ToolCall> tool_calls;
        public static class ToolCall {
            public Function function;
            public static class Function {
                public String name;
                public Map<String, Object> arguments;
            }
        }
    }
    public String done_reason;
    public boolean done;
    public static ChatResponse fromJson(String json) {
        return new Gson().fromJson(json, ChatResponse.class);
    }
}
