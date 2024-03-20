package com.jackdaw.chatwithnpc.openaiapi;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class Request {
    private static String url = "https://api.openai.com/v1/";

    /**
     * Update the url setting of the OpenAI API
     */
    public static void updateSetting() {
        url = "https://" + SettingManager.apiURL + "/v1/";
    }

    /**
     * Send a request to the OpenAI API
     *
     * @param requestJson The request to send
     * @param routing     The routing of the request, should like "chat/completions" or "assistants"
     * @return The response from the API in Json format
     * @throws Exception If the request fails
     */
    public static @NotNull String sendRequest(@Nullable String requestJson, @NotNull String routing, @NotNull Map<String, String> headers, @NotNull Action action) throws Exception {
        updateSetting();
        if (ChatWithNPCMod.debug) {
            ChatWithNPCMod.LOGGER.info("[chat-with-npc] Request roting: \n" + routing);
            ChatWithNPCMod.LOGGER.info("[chat-with-npc] Request: \n" + requestJson);
        }

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(10 * 1000)
                .setSocketTimeout(10 * 1000)
                .setCookieSpec("ignoreCookies")
                .build();

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {
            HttpRequestBase request = getHttpRequestBase(routing, headers, action);

            // 构建请求体
            if (requestJson != null && request instanceof HttpPost) {
                ((HttpPost) request).setEntity(new StringEntity(requestJson, "UTF-8"));
            }

            try (CloseableHttpResponse response = client.execute(request)) {
                String res = EntityUtils.toString(response.getEntity());
                if (ChatWithNPCMod.debug) {
                    ChatWithNPCMod.LOGGER.info("[chat-with-npc] Response: \n" + res);
                }
                return res;
            }
        }
    }

    @NotNull
    private static HttpRequestBase getHttpRequestBase(@NotNull String routing, @NotNull Map<String, String> headers, @NotNull Action action) {
        HttpRequestBase request = null;
        if (action == Action.GET) {
            request = new HttpGet(url + routing);
        } else if (action == Action.POST) {
            request = new HttpPost(url + routing);
        } else if (action == Action.DELETE) {
            request = new HttpDelete(url + routing);
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            request.setHeader(entry.getKey(), entry.getValue());
        }
        return request;
    }

    /**
     * The action of the request
     */
    public enum Action {
        /**
         * Get request
         */
        GET,
        /**
         * Post request
         */
        POST,
        /**
         * Delete request
         */
        DELETE
    }
}

