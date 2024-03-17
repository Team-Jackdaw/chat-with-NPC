package com.jackdaw.chatwithnpc.conversation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.openaiapi.Header;
import com.jackdaw.chatwithnpc.openaiapi.Request;
import org.jetbrains.annotations.Nullable;

public class OpenAIHandler {
    private static final String roting = "chat/completions";

    /**
     * Send a request to the OpenAI API
     *
     * @param requestJson The request to send
     * @return The response from the API
     * @throws Exception If the request fails
     */
    public static @Nullable String sendRequest(String requestJson) throws Exception {
        String res = Request.sendRequest(requestJson, roting, Header.buildDefault());
        JsonObject jsonObject = JsonParser.parseString(res).getAsJsonObject();
        try {
            return jsonObject.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
        } catch (Exception e) {
            ChatWithNPCMod.LOGGER.error("[chat-with-npc] Failed to parse the response from OpenAI API.", e);
            ChatWithNPCMod.LOGGER.error("[chat-with-npc] Response: \n" + res);
            return null;
        }
    }

}

