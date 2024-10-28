package com.jackdaw.chatwithnpc.openaiapi;

import com.jackdaw.chatwithnpc.SettingManager;
import org.apache.http.HttpHeaders;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * A class to build headers for the OpenAI API
 */
public class Header {
    private final Map<String, String> header = new HashMap<>();

    private Header() {
    }

    /**
     * Create a new instance of the header builder
     * @return A new instance of the header builder
     */
    @Contract(value = " -> new", pure = true)
    public static @NotNull Header builder() {
        return new Header();
    }

    /**
     * Build the default headers for the OpenAI API
     * @return The default headers for the OpenAI API
     */
    public static Map<String, String> buildDefault() {
        return Header.builder()
                .add(Header.Type.CONTENT_TYPE)
                .add(Header.Type.AUTHORIZATION)
                .build();
    }

    /**
     * Build the beta headers for the OpenAI API
     * @return The beta headers for the OpenAI API
     */
    public static Map<String, String> buildBeta() {
        return Header.builder()
                .add(Header.Type.CONTENT_TYPE)
                .add(Header.Type.AUTHORIZATION)
                .add(Header.Type.OPENAI_BETA)
                .build();
    }

    /**
     * Add a header to the builder
     * @param type The type of header to add
     * @return The header builder
     */
    public Header add(@NotNull Type type) {
        switch (type) {
            case AUTHORIZATION:
                header.put(HttpHeaders.AUTHORIZATION, "Bearer " + SettingManager.apiKey);
                break;
            case CONTENT_TYPE:
                header.put(HttpHeaders.CONTENT_TYPE, "application/json");
                break;
            case OPENAI_BETA:
                header.put("OpenAI-Beta", "assistants=v2");
                break;
        }
        return this;
    }

    /**
     * Build the headers
     * @return The headers
     */
    public Map<String, String> build() {
        return header;
    }

    /**
     * The type of header
     */
    public enum Type {
        /**
         * The authorization header
         */
        AUTHORIZATION,
        /**
         * The content type header
         */
        CONTENT_TYPE,
        /**
         * The OpenAI beta header
         */
        OPENAI_BETA
    }
}
