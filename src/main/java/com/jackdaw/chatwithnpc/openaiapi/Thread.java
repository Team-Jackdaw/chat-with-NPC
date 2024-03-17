package com.jackdaw.chatwithnpc.openaiapi;

import com.google.gson.Gson;
import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class Thread {
    private String id;

    private static String toJson(Map<String, String> map) {
        return new Gson().toJson(map);
    }

    private static Thread fromJson(String json) {
        return new Gson().fromJson(json, Thread.class);
    }

    public static void createThread(@NotNull ConversationHandler conversationHandler) {
        java.lang.Thread t = new java.lang.Thread(() -> {
            try {
                String res = Request.sendRequest("", "threads", Header.buildBeta());
                String id = fromJson(res).id;
                conversationHandler.getNpc().setThreadId(id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        t.start();
    }

    public static void addMessage(String threadId, String message) {
        Map<String, String> content = Map.of("role", "user", "content", message);
        java.lang.Thread t = new java.lang.Thread(() -> {
            try {
                String res = Request.sendRequest(toJson(content), "threads/" + threadId + "/messages", Header.buildBeta());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        t.start();
    }

    static String getLastMessage(String threadId) throws Exception {
        Map<String, String> filter = Map.of("limit", "1", "order", "desc");
        String res = Request.sendRequest(toJson(filter), "threads/" + threadId + "/messages", Header.buildBeta());
        MessageList messageList = new Gson().fromJson(res, MessageList.class);
        return messageList.data.get(0).content.get(0).text.value;
    }

    private static class MessageList {
        private List<Message> data;

        private static class Message {
            private List<Content> content;

            private static class Content {
                private Text text;

                private static class Text {
                    private String value;
                }
            }
        }

    }

}
