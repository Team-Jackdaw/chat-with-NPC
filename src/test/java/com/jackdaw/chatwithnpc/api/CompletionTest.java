package com.jackdaw.chatwithnpc.api;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import com.jackdaw.chatwithnpc.SettingManager;
import com.jackdaw.chatwithnpc.api.json.CompletionRequest;
import com.jackdaw.chatwithnpc.api.json.CompletionResponse;

import java.util.Map;

import static com.jackdaw.chatwithnpc.ConfigTest.setOllamaConfig;

public class CompletionTest {
static {
    setOllamaConfig();
}

    @Test
    public void testCompletionRequest() {
        try {
            CompletionResponse res = Ollama.completion("Hello");
            System.out.println(res.response);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testMarkRequest() {
        try {
            CompletionRequest req = new CompletionRequest();
            req.model = SettingManager.chat_model;
            req.system = "You are a mark assistant, you should mark the prompt from 0 to 10 based on how importance it is as a daily live memory.";
            req.prompt = "I went to the park with my friends today.";
            req.stream = false;
            String formatJson = """
                    {
                        "type": "object",
                        "properties": {
                          "grade": {
                            "type": "integer"
                          }
                        },
                        "required": [
                          "grade"
                        ]
                      }
                    """;
            req.format = new Gson().fromJson(formatJson, Map.class);
            CompletionResponse res = Ollama.completion(req);
            Map grade = new Gson().fromJson(res.response, Map.class);
            Double gradeValue = (Double) grade.get("grade");
            System.out.println(gradeValue.intValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
