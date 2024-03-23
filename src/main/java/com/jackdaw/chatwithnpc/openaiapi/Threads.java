package com.jackdaw.chatwithnpc.openaiapi;

import com.google.gson.Gson;
import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class Threads {
    /**
     * Create a new thread and set the thread id to the npc
     * @param conversationHandler The conversation handler
     * @throws Exception If the thread id is null
     */
    public static void createThread(@NotNull ConversationHandler conversationHandler) throws Exception {
        String res = Request.sendRequest("", "threads", Header.buildBeta(), Request.Action.POST);
        String id = ThreadsClass.fromJson(res).id;
        if (id == null) {
            ChatWithNPCMod.LOGGER.error("[chat-with-npc] API error: " + res);
            throw new Exception("Thread id is null");
        }
        conversationHandler.getNpc().setThreadId(id);
    }

    /**
     * Add a message to the thread
     * @param threadId The thread id
     * @param message The message
     * @throws Exception If the message is not sent
     */
    public static void addMessage(String threadId, String message) throws Exception {
        Map<String, String> content = Map.of("role", "user", "content", message);
        String res = Request.sendRequest(ThreadsClass.toJson(content), "threads/" + threadId + "/messages", Header.buildBeta(), Request.Action.POST);
        MessageList.Message message1 = new Gson().fromJson(res, MessageList.Message.class);
        if (message1.content == null) {
            ChatWithNPCMod.LOGGER.error("[chat-with-npc] API error: " + res);
            throw new Exception("Message not sent");
        }
    }

    /**
     * Discard the thread id from the npc
     * @param threadId The thread id
     * @throws Exception If is there any error
     */
    public static void discardThread(String threadId) throws Exception {
        Request.sendRequest("", "threads/" + threadId, Header.buildBeta(), Request.Action.DELETE);
    }

    /**
     * Get the last message from the thread
     * @param threadId The thread id
     * @return The last message
     * @throws Exception If the message is not received
     */
    static String getLastMessage(String threadId) throws Exception {
        Map<String, String> filter = Map.of("limit", "1", "order", "desc");
        String res = Request.sendRequest(ThreadsClass.toJson(filter), "threads/" + threadId + "/messages", Header.buildBeta(), Request.Action.GET);
        MessageList messageList = new Gson().fromJson(res, MessageList.class);
        if (messageList.data == null) {
            ChatWithNPCMod.LOGGER.error("[chat-with-npc] API error: " + res);
            throw new Exception("Message not received");
        }
        return messageList.data.get(0).content.get(0).text.value;
    }

    private static class ThreadsClass {
        private String id;

        private static String toJson(Map<String, String> map) {
            return new Gson().toJson(map);
        }

        private static ThreadsClass fromJson(String json) {
            return new Gson().fromJson(json, ThreadsClass.class);
        }
    }

    static class MessageList {
        List<Message> data;

        static class Message {
            List<Content> content;

            static class Content {
                Text text;

                static class Text {
                    String value;
                }
            }
        }

    }

}
