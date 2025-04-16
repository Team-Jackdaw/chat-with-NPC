package com.jackdaw.chatwithnpc.api.json;

import java.util.Map;

public class Function {
    public String name;
    public String description;
    public Parameters parameters;

    public static class Parameters {
        public String type;
        public Map<String, Map<String, Object>> properties;
        public String[] required;
    }
}
