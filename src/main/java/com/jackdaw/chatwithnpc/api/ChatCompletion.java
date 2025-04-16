package com.jackdaw.chatwithnpc.api;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.jackdaw.chatwithnpc.api.json.*;

import java.util.List;

class ChatCompletion {
    static @NotNull ChatResponse chatRequest(@NotNull String url, @NotNull String model, @NotNull List<Message> messages, @Nullable List<Tool> tools) throws Exception {
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.model = model;
        chatRequest.messages = messages;
        chatRequest.stream = false;
        if (tools != null && !tools.isEmpty()) {
            chatRequest.tools = tools;
        }
        String res = Request.sendRequest(chatRequest.toJson(), url + "/api/chat", Header.buildDefault(), Request.Action.POST);
        return ChatResponse.fromJson(res);
    }

    @Contract(" -> new")
    static @NotNull MessageBuilder messageBuilder() {
        return new MessageBuilder();
    }

    static @NotNull MessageBuilder messageBuilder(@NotNull List<Message> messages) {
        MessageBuilder builder = new MessageBuilder();
        for (Message message : messages) {
            builder.addMessage(Role.valueOf(message.role.toUpperCase()), message.content);
        }
        return builder;
    }
}

