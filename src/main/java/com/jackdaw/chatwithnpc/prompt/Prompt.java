package com.jackdaw.chatwithnpc.prompt;

public class Prompt {

    private final String systemMessage;
    Prompt(String systemMessage) {
        this.systemMessage = systemMessage;
    }

    public String getInitialPrompt() {
        return systemMessage;
    }

    public static Builder builder() {
        return new Builder();
    }
}
