package com.jackdaw.chatwithnpc.openaiapi.functioncalling;

import com.google.gson.Gson;
import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FunctionManager {
    private static final Logger logger = ChatWithNPCMod.LOGGER;
    private static final Path folder = ChatWithNPCMod.workingDirectory.resolve("functions");
    private static final Map<String, CustomFunction> functionRegistry = new HashMap<>();

    /**
     * Register a function that can be used by the NPC. It will be called by the OpenAI Assistant.
     * @param name The name of the function
     * @param function The function
     */
    public static void registerFunction(@NotNull String name, @NotNull CustomFunction function) {
        functionRegistry.put(name, function);
    }

    public static @NotNull ArrayList<String> getFileList() {
        File[] files = folder.toFile().listFiles();
        ArrayList<String> functionList = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                if (name.endsWith(".json")) {
                    functionList.add(name.substring(0, name.length() - 5));
                }
            }
        }
        return functionList;
    }

    /**
     * Get the list of the functions, and register them.
     */
    public static void sync() {
        if (!Files.exists(folder)) {
            try {
                Files.createDirectories(folder);
            } catch (IOException e) {
                logger.error("[chat-with-npc] Failed to create the functions directory");
                logger.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }
        File workingDirectory = folder.toFile();
        File[] files = workingDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                if (name.endsWith(".json")) {
                    String json;
                    try {
                        json = Files.readString(file.toPath());
                    } catch (IOException e) {
                        logger.error("[chat-with-npc] Failed to read the function file: " + name);
                        logger.error(e.getMessage());
                        continue;
                    }
                    registerFromJson(json);
                }
            }
        }
    }

    /**
     * Register a function from a JSON string. It will be called by the OpenAI Assistant but with no response.
     * <p>
     * The JSON string should be in the following format:
     * <pre>
     *     {
     *         "type": "function",
     *         "function": {
     *             "name": "functionName",
     *             "description": "This function does something",
     *             "parameters": {
     *                 "type": "object",
     *                 "properties": {
     *                     "param1": {
     *                         "type": "string",
     *                         "description": "This is the first parameter"
     *                     },
     *                     "param2": {
     *                         "type": "string",
     *                         "description": "This is the second parameter"
     *                     }
     *                 },
     *                 "required": ["param1", "param2"]
     *             }
     *         }
     *     }
     * </pre>
     * This function will not be executed and will not have a response. Once it is called, it will add all the parameters including the function name to NPC's NBT data.
     * @param json The JSON string
     */
    public static void registerFromJson(String json) {
        Tools tools = Tools.fromJson(json);
        HashMap<String, String> properties = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : tools.function.parameters.properties.entrySet()) {
            properties.put(entry.getKey(), entry.getValue().get("description"));
        }
        CustomFunction function = new NoResponseFunction(tools.function.description, properties);
        functionRegistry.put(tools.function.name, function);
    }

    /**
     * Call a function by its name. It will be executed by the OpenAI Assistant and work on the conversation.
     * @param name The name of the function
     * @param conversation The conversation handler
     * @param args The arguments
     */
    public static Map<String, String> callFunction(@NotNull ConversationHandler conversation, @NotNull String name, @NotNull Map<String, String> args) {
        CustomFunction function = functionRegistry.get(name);
        if (function == null) {
            throw new IllegalArgumentException("Function not found: " + name);
        }
        return function.execute(conversation, args);
    }

    /**
     * Get the JSON string of a function by its name.
     * @param name The name of the function
     * @return The JSON string
     */
    public static String getFunctionJson(@NotNull String name) {
        CustomFunction function = functionRegistry.get(name);
        return getFunctionJson(name, function.description, function.properties);
    }

    private static String getFunctionJson(@NotNull String name, @NotNull String description, @NotNull Map<String, String> properties) {
        Tools tools = new Tools();
        tools.type = "function";
        tools.function = new Tools.Function();
        tools.function.name = name;
        tools.function.description = description;
        tools.function.parameters = new Tools.Function.Parameters();
        tools.function.parameters.type = "object";
        tools.function.parameters.properties = new HashMap<>();
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            tools.function.parameters.properties.put(entry.getKey(), Map.of(
                "type", "string",
                "description", entry.getValue()
            ));
        }
        tools.function.parameters.required = properties.keySet().toArray(new String[0]);
        return tools.toJson();
    }

    private static class Tools {
        private String type;
        private Function function;

        private static class Function {
            private String name;
            private String description;
            private Parameters parameters;

            private static class Parameters {
                private String type;
                private Map<String, Map<String, String>> properties;
                private String[] required;
            }
        }

        private String toJson() {
            return new Gson().toJson(this);
        }

        private static Tools fromJson(String json) {
            return new Gson().fromJson(json, Tools.class);
        }
    }

    public static class ToolCall {
        public String id;
        public String type;
        public Function function;

        public static class Function {
            public String name;
            // The arguments should be a JSON string
            public String arguments;
        }

        public String toJson() {
            return new Gson().toJson(this);
        }

        public static ToolCall fromJson(String json) {
            return new Gson().fromJson(json, ToolCall.class);
        }
    }
}
