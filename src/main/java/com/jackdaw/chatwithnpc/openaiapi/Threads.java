package com.jackdaw.chatwithnpc.openaiapi;

import com.google.gson.Gson;
import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class Threads {
    private String id;

    private static String toJson(Map<String, String> map) {
        return new Gson().toJson(map);
    }

    private static Threads fromJson(String json) {
        return new Gson().fromJson(json, Threads.class);
    }

    public static void createThread(@NotNull ConversationHandler conversationHandler) throws Exception {
        String res = Request.sendRequest("", "threads", Header.buildBeta());
        String id = fromJson(res).id;
        conversationHandler.getNpc().setThreadId(id);
    }

    public static void addMessage(String threadId, String message) throws Exception {
        Map<String, String> content = Map.of("role", "user", "content", message);
        String ignore = Request.sendRequest(toJson(content), "threads/" + threadId + "/messages", Header.buildBeta());
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
