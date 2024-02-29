package com.jackdaw.chatwithnpc.prompt;

public class Prompt {

    private final String systemMessage;

    private final String historyMessage;

    Prompt(String systemMessage, String historyMessage) {
        this.systemMessage = systemMessage;
        this.historyMessage = historyMessage;
    }

    public String getInitialPrompt() {
        return systemMessage;
    }

    public String getHistoryMessage() {
        return historyMessage;
    }

    public static Builder builder() {
        return new Builder();
    }
}
