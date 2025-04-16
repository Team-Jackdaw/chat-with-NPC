package com.jackdaw.chatwithnpc.api;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface Request {
    /**
     * Send a request to a URL
     *
     * @param requestJson The request to send
     * @param url     The URL of the request, should include the protocol
     * @return The response from the API in Json format
     * @throws Exception If the request fails
     */
    static @NotNull String sendRequest(@Nullable String requestJson, @NotNull String url, @NotNull Map<String, String> headers, @NotNull Action action) throws Exception {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(30 * 1000)
                .setSocketTimeout(30 * 1000)
                .setCookieSpec("ignoreCookies")
                .build();

        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build()) {
            HttpRequestBase request = getHttpRequestBase(url, headers, action);

            if (requestJson != null && request instanceof HttpPost) {
                ((HttpPost) request).setEntity(new StringEntity(requestJson, "UTF-8"));
            }

            try (CloseableHttpResponse response = client.execute(request)) {
                return EntityUtils.toString(response.getEntity());
            }
        }
    }

    @NotNull
    private static HttpRequestBase getHttpRequestBase(@NotNull String url, @NotNull Map<String, String> headers, @NotNull Action action) {
        HttpRequestBase request = null;
        if (action == Action.GET) {
            request = new HttpGet(url);
        } else if (action == Action.POST) {
            request = new HttpPost(url);
        } else if (action == Action.DELETE) {
            request = new HttpDelete(url);
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            request.setHeader(entry.getKey(), entry.getValue());
        }
        return request;
    }

    /**
     * The action of the request
     */
    enum Action {
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

