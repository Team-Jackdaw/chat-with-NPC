package com.jackdaw.chatwithnpc.openaiapi;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

public class Assistant {
    private String name;
    private String instructions;
    private final String model = SettingManager.model;
    private String id;


    public Assistant(String name, String instructions) {
        this.name = name;
        this.instructions = instructions;
    }

    private static String toJson(Map<String, String> map) {
        return new Gson().toJson(map);
    }

    public void createAssistant() {
        Map<String, String> createAssistantRequest = Map.of(
                "name", name,
                "model", model,
                "instructions", instructions
        );
        java.lang.Thread t = new java.lang.Thread(() -> {
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost request = new HttpPost("https://api.openai.com/v1/assistants");
                request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + SettingManager.apiKey);
                request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
                request.setEntity(new StringEntity(toJson(createAssistantRequest), "UTF-8"));
                try (CloseableHttpResponse response = client.execute(request)) {
                    String res =  EntityUtils.toString(response.getEntity());
                    Gson gson = new Gson();
                    Type type = new TypeToken<Map<String, String>>() {}.getType();
                    Map<String, String> map = gson.fromJson(res, type);
                    createCallback(map);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void createCallback(Map<String, String> response) {

    }
}
