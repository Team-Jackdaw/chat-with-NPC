package com.jackdaw.chatwithnpc.conversation;

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
import org.jetbrains.annotations.Nullable;

public class OpenAIHandler {
    private static String url = "https://api.openai.com/v1/chat/completions";
    private static String apiKey = "";

    public static void updateSetting() {
        apiKey = SettingManager.apiKey;
        url = "https://" + SettingManager.apiURL + "/v1/chat/completions";
    }

    /**
     * Send a request to the OpenAI API
     *
     * @param requestJson The request to send
     * @return The response from the API
     * @throws Exception If the request fails
     */
    public static @Nullable String sendRequest(String requestJson) throws Exception {
        updateSetting();
        ChatWithNPCMod.LOGGER.debug("[chat-with-npc] Request: \n" + requestJson);

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(10 * 1000)
                .setSocketTimeout(10 * 1000)
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
                String res = EntityUtils.toString(response.getEntity());
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

