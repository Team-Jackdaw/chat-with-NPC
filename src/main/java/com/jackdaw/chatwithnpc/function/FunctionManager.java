package com.jackdaw.chatwithnpc.function;

import com.jackdaw.chatwithnpc.AsyncTask;
import com.jackdaw.chatwithnpc.BaseManager;
import com.jackdaw.chatwithnpc.api.json.*;
import com.jackdaw.chatwithnpc.conversation.ConversationHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FunctionManager extends BaseManager<String, CustomFunction> {
    private static final FunctionManager INSTANCE = new FunctionManager();
    private FunctionManager() {}

    public static FunctionManager getInstance() {
        return INSTANCE;
    }

    public ArrayList<String> getRegistryList() {
        return new ArrayList<>(map.keySet());
    }

    /**
     * Call a function by its name. It will be executed by Ollama LLM and work on the conversation.
     *
     * @param functionName The name of the function
     * @param conversation The conversation handler
     * @param args         The arguments
     */
    public Map<String, String> callFunction(ConversationHandler conversation, String functionName, Map<String, Object> args) {
        CustomFunction function = get(functionName);
        if (function == null) {
            throw new IllegalArgumentException("Function not found: " + functionName);
        }
        if (conversation != null && conversation.getNpc().getPermissionLevel() < function.permissionLevel) return CustomFunction.FAILURE;
        return function.execute(conversation, args);
    }

    /**
     * Get the JSON string of a function by its functionName.
     *
     * @param functionName The functionName of the function
     * @return The JSON string
     */
    public Tool getTools(String functionName) {
        CustomFunction function = get(functionName);
        Tool tool = new Tool();
        tool.type = "function";
        tool.function = new Function();
        tool.function.name = functionName;
        tool.function.description = function.description;
        tool.function.parameters = new Function.Parameters();
        tool.function.parameters.type = "object";
        tool.function.parameters.properties = function.properties;
        tool.function.parameters.required = Objects.requireNonNullElseGet(function.required, () -> function.properties.keySet().stream().map(Object::toString).toArray(String[]::new));
        return tool;
    }

    public static class FunctionCall implements AsyncTask.TaskResult {

        private final ConversationHandler conversation;
        private final List<ChatResponse.Message.ToolCall> tool_calls;

        public FunctionCall(ConversationHandler conversation, List<ChatResponse.Message.ToolCall> tool_calls) {
            this.conversation = conversation;
            this.tool_calls = tool_calls;
        }

        @Override
        public void execute() {
            for (ChatResponse.Message.ToolCall toolCall : tool_calls) {
                String functionResult = FunctionManager.getInstance()
                        .callFunction(conversation, toolCall.function.name, toolCall.function.arguments)
                        .toString();
                Message message = ToolMessage.newToolMessage(toolCall.function.name, functionResult);
                conversation.addMessage(message);
            }
            conversation.continueConversation();
        }

        @Override
        public boolean isCallable() {
            return true;
        }
    }
}
