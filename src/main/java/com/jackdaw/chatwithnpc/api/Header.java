package com.jackdaw.chatwithnpc.api;

import org.apache.http.HttpHeaders;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * A class to build headers for the API
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
     * Build the default headers for the API
     * @return The default headers for the API
     */
    public static Map<String, String> buildDefault() {
        return Header.builder()
                .add(Type.CONTENT_TYPE, null)
                .build();
    }

    /**
     * Build the beta headers for the API
     * @param apiKey The API key to use
     * @return The beta headers for the API
     */
    public static Map<String, String> buildBeta(@NotNull String apiKey) {
        return Header.builder()
                .add(Type.CONTENT_TYPE, null)
                .add(Type.AUTHORIZATION, apiKey)
                .add(Type.OPENAI_BETA, null)
                .build();
    }

    /**
     * Add a header to the builder
     * @param type The type of header to add
     * @param value The value of the header (Not needed for some types)
     * @return The header builder
     */
    public Header add(@NotNull Type type, @Nullable String value) {
        switch (type) {
            case AUTHORIZATION:
                header.put(HttpHeaders.AUTHORIZATION, "Bearer " + value);
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
     * Add a header to the builder
     * @param key The key of the header
     * @param value The value of the header
     * @return The header builder
     */
    public Header add(@NotNull String key, @NotNull String value) {
        header.put(key, value);
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
