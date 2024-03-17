package com.jackdaw.chatwithnpc.openaiapi;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class Request {
    private static String url = "https://api.openai.com/v1/";

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
    public static @NotNull String sendRequest(@Nullable String requestJson, @NotNull String routing, @NotNull Map<String, String> headers) throws Exception {
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
            HttpPost request = new HttpPost(url + routing);

            // 设置请求头
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.setHeader(entry.getKey(), entry.getValue());
            }

            // 构建请求体
            if (requestJson != null) {
                request.setEntity(new StringEntity(requestJson, "UTF-8"));
            }

            try (CloseableHttpResponse response = client.execute(request)) {
                return EntityUtils.toString(response.getEntity());
            }
        }
    }
}

