package com.jackdaw.chatwithnpc.api;

import org.junit.jupiter.api.Test;
import com.jackdaw.chatwithnpc.api.json.ChatResponse;
import com.jackdaw.chatwithnpc.api.json.Message;
import com.jackdaw.chatwithnpc.api.json.Role;
import com.jackdaw.chatwithnpc.api.json.Tool;

import java.util.ArrayList;
import java.util.List;

import static com.jackdaw.chatwithnpc.ConfigTest.setOllamaConfig;

public class ChatCompletionTest {

    static {
        setOllamaConfig();
    }

    @Test
    public void testChatRequest() {
        try {
            ChatResponse res = Ollama.chat(
                    Ollama.messageBuilder()
                            .addMessage(Role.USER, "Hello")
                            .build(),
                    null
            );
            System.out.println(res.message.content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String getWeather(String location, String format) {
        return "The weather in " + location + " is 20 degrees " + format;
    }

    @Test
    public void testChatRequestFunction() {
        String functionJson = """
                {
                  "type": "function",
                  "function": {
                    "name": "get_current_weather",
                    "description": "Get the current weather for a location",
                    "parameters": {
                      "type": "object",
                      "properties": {
                        "location": {
                          "type": "string",
                          "description": "The location to get the weather for, e.g. San Francisco, CA"
                        },
                        "format": {
                          "type": "string",
                          "description": "The format to return the weather in, e.g. 'celsius' or 'fahrenheit'",
                          "enum": ["celsius", "fahrenheit"]
                        }
                      },
                      "required": ["location", "format"]
                    }
                  }
                }""";
        ArrayList<Tool> tools = new ArrayList<>();
        tools.add(Tool.fromJson(functionJson));
        List<Message> messages = Ollama.messageBuilder()
                .addMessage(Role.USER, "What is the weather today in Paris?")
                .build();
        try {
            ChatResponse res = Ollama.chat(messages, tools);
            if (res.message.tool_calls != null) {
                for (ChatResponse.Message.ToolCall toolCall : res.message.tool_calls) {
                    String functionResult = getWeather((String) toolCall.function.arguments.get("location"), (String) toolCall.function.arguments.get("format"));
                    List<Message> messages2 = Ollama.messageBuilder(messages)
                            .addToolMessage(toolCall.function.name, functionResult)
                            .build();
                    ChatResponse res2 = Ollama.chat(messages2, null);
                    System.out.println(res2.message.content);
                }
            } else {
                System.out.println(res.message.content);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
