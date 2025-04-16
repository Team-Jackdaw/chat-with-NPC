package com.jackdaw.chatwithnpc.api.json;

public class ToolCall {
    public String id;
    public String type;
    public Function function;

    public static class Function {
        public String name;
        // The arguments should be a JSON string
        public String arguments;
    }
}
