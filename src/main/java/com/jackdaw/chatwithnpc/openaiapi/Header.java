package com.jackdaw.chatwithnpc.openaiapi;

import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import org.apache.http.HttpHeaders;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Header {
    private final Map<String, String> header = new HashMap<>();

    private Header() {
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull Header builder() {
        return new Header();
    }

    public static Map<String, String> buildDefault() {
        return Header.builder()
                .add(Header.Type.CONTENT_TYPE)
                .add(Header.Type.AUTHORIZATION)
                .build();
    }

    public static Map<String, String> buildBeta() {
        return Header.builder()
                .add(Header.Type.CONTENT_TYPE)
                .add(Header.Type.AUTHORIZATION)
                .add(Header.Type.OPENAI_BETA)
                .build();
    }

    public Header add(@NotNull Type type) {
        switch (type) {
            case AUTHORIZATION:
                header.put(HttpHeaders.AUTHORIZATION, "Bearer " + SettingManager.apiKey);
                break;
            case CONTENT_TYPE:
                header.put(HttpHeaders.CONTENT_TYPE, "application/json");
                break;
            case OPENAI_BETA:
                header.put("OpenAI-Beta", "assistants=v1");
                break;
        }
        return this;
    }

    public Map<String, String> build() {
        return header;
    }

    public enum Type {
        AUTHORIZATION,
        CONTENT_TYPE,
        OPENAI_BETA
    }
}
