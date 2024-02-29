package com.jackdaw.chatwithnpc.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.npc.Record;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

public class OpenAIHandler {

    private static final class RequestJson {
        private final String model;
        private final ArrayList<Map<String, String>> messages = new ArrayList<>();
        private final String maxTokens;

        public RequestJson(String model, String initialPrompt, Record messages, String maxTokens) {
            this.model = model;
            this.maxTokens = maxTokens;
            this.messages.add(Map.of("role", "system", "content", initialPrompt));
            for (Record.Message message : messages.getTreeMap().values()) {
                this.messages.add(Map.of("role", message.getRole().toString().toLowerCase(), "content", encodeString(message.getMessage())));
            }
        }

        public String toJson() {
            Gson gson = new Gson();
            ChatWithNPCMod.LOGGER.info("[chat-with-npc] Draft Request: \n" + gson.toJson(this));
            return gson.toJson(this);
        }
    }

    private static final String url = "https://api.openai.com/v1/chat/completions";
    private static String apiKey = "";
    private static String model = "gpt-3.5-turbo";
    private static final String maxTokens = "512";

    public static void updateSetting() {
        apiKey = SettingManager.apiKey;
        model = SettingManager.model;
    }

    public static String encodeString(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Send a request to the OpenAI API
     * @param initialPrompt The initialPrompt to send
     * @return The response from the API
     * @throws Exception If the request fails
     */
    public static String sendRequest(String initialPrompt, Record messageRecord) throws Exception {
        if (initialPrompt.length() > 4096) initialPrompt = initialPrompt.substring(initialPrompt.length() - 4096);
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(url);

            // 设置请求头
            request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
            request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

            // 构建请求体
            RequestJson requestJson = new RequestJson(model, initialPrompt, messageRecord, maxTokens);
            request.setEntity(new StringEntity(requestJson.toJson()));

            try (CloseableHttpResponse response = client.execute(request)) {
                String res =  EntityUtils.toString(response.getEntity());
                ChatWithNPCMod.LOGGER.info("[chat-with-npc] Draft Response: \n" + res);
                JsonObject jsonObject = JsonParser.parseString(res).getAsJsonObject();
                return jsonObject.getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content").getAsString();
            }
        }
    }

}

