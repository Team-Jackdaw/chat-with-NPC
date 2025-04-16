package com.jackdaw.chatwithnpc.api.json;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MessageBuilder {
    private final List<Message> messages = new ArrayList<>();

    public MessageBuilder addMessage(@NotNull Role role, @NotNull String content) {
        Message message = new Message();
        message.role = role.name().toLowerCase();
        message.content = content;
        messages.add(message);
        return this;
    }

    public MessageBuilder addToolMessage(@NotNull String name, @NotNull String content) {
        Message message = ToolMessage.newToolMessage(name, content);
        messages.add(message);
        return this;
    }

    public @NotNull List<Message> build() {
        return messages;
    }
}
