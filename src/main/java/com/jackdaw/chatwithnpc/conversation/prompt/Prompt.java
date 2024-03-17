package com.jackdaw.chatwithnpc.conversation.prompt;

import com.google.gson.Gson;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

public final class Prompt {
    private final ArrayList<Map<String, String>> messages = new ArrayList<>();

    Prompt(ArrayList<Map<String, String>> messages) {
        this.messages.addAll(messages);
    }

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public String toRequestJson() {
        Gson gson = new Gson();
        return gson.toJson(new Request(this.messages));
    }

    private final static class Request {
        private final ArrayList<Map<String, String>> messages = new ArrayList<>();
        private final int max_tokens;
        private final String model;

        Request(ArrayList<Map<String, String>> messages) {
            this.max_tokens = SettingManager.maxTokens;
            this.model = SettingManager.model;
            this.messages.addAll(messages);
        }
    }
}
