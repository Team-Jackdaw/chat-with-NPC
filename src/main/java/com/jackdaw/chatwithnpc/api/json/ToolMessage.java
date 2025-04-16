package com.jackdaw.chatwithnpc.api.json;

import org.jetbrains.annotations.NotNull;

public class ToolMessage extends Message{
    public String name;
    public static @NotNull ToolMessage newToolMessage(String name, String content) {
        ToolMessage toolMessage = new ToolMessage();
        toolMessage.role = "tool";
        toolMessage.name = name;
        toolMessage.content = content;
        return toolMessage;
    }
}
