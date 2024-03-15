package com.jackdaw.chatwithnpc.conversation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;

public class OpenAIHandler {

    private static final class RequestJson {
        private String model = OpenAIHandler.model;
        private final ArrayList<Map<String, String>> messages = new ArrayList<>();
        private int max_tokens = OpenAIHandler.maxTokens;

        private static String role2String(Record.Role role) {
            if (role == Record.Role.NPC) return "assistant";
            if (role == Record.Role.PLAYER) return "user";
            return "system";
        }

        private RequestJson(String initialPrompt, ArrayList<Map<Long, String>> longTermMemory, Record messages, String npcName) {
            this.messages.add(Map.of("role", "system", "content", initialPrompt));
            if (longTermMemory != null && !longTermMemory.isEmpty()) {
                for (Map<Long, String> memory : longTermMemory) {
                    for (Map.Entry<Long, String> entry : memory.entrySet()) {
                        this.messages.add(Map.of("role", "system", "content", "This is a summary of one of the previous conversations: " + entry.getValue()));
                    }
                }
            }
            if (messages != null && !messages.isEmpty()) {
                for (Record.Message message : messages.getTreeMap().values()) {
                    if (message.getEntityName() != null) {
                        this.messages.add(Map.of("role", role2String(message.getRole()), "content", message.getMessage(), "name", message.getEntityName()));
                    } else {
                        this.messages.add(Map.of("role", role2String(message.getRole()), "content", message.getMessage()));
                    }
                }
            } else {
                this.messages.add(Map.of("role", "system", "content", "Please start the conversation as " + npcName + " with a greeting."));
            }
        }

        public String toJson() {
            Gson gson = new Gson();
            ChatWithNPCMod.LOGGER.debug("[chat-with-npc] Draft Request: \n" + gson.toJson(this));
            return gson.toJson(this);
        }
    }

    private static String url = "https://api.openai.com/v1/chat/completions";
    private static String apiKey = "";
    private static String model = "gpt-3.5-turbo";
    private static int maxTokens = 512;

    public static void updateSetting() {
        apiKey = SettingManager.apiKey;
        model = SettingManager.model;
        maxTokens = SettingManager.maxTokens;
        url = "https://" + SettingManager.apiURL + "/v1/chat/completions";
    }

    public static @Nullable String sendRequest(@NotNull String initialPrompt, ArrayList<Map<Long, String>> longTermMemory, Record messageRecord, String npcName) throws Exception {
        if (initialPrompt.length() > 4096) initialPrompt = initialPrompt.substring(initialPrompt.length() - 4096);
        RequestJson requestJson = new RequestJson(initialPrompt, longTermMemory, messageRecord, npcName);
        return senRequest(requestJson.toJson());
    }

    /**
     * Send a request to the OpenAI API
     * @param requestJson The request to send
     * @return The response from the API
     * @throws Exception If the request fails
     */
    public static @Nullable String senRequest (String requestJson) throws Exception {
        ChatWithNPCMod.LOGGER.debug("[chat-with-npc] Request: \n" + requestJson);

        RequestConfig requestConfig = RequestConfig.custom()
                .setCookieSpec("ignoreCookies")
                .build();

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {
            HttpPost request = new HttpPost(url);

            // 设置请求头
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
            request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            // 构建请求体
            request.setEntity(new StringEntity(requestJson, "UTF-8"));

            try (CloseableHttpResponse response = client.execute(request)) {
                String res =  EntityUtils.toString(response.getEntity());
                ChatWithNPCMod.LOGGER.debug("[chat-with-npc] Response: \n" + res);
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
    }

}

