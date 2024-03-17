package com.jackdaw.chatwithnpc.openaiapi;

import com.google.gson.Gson;
import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Run {

    private String id;
    private String thread_id;
    private String assistant_id;
    private String status;

    private static String toJson(Map<String, String> map) {
        return new Gson().toJson(map);
    }

    private static Run fromJson(String json) {
        return new Gson().fromJson(json, Run.class);
    }

    public static void run(@NotNull ConversationHandler conversation) throws Exception {
        String threadId = conversation.getNpc().getThreadId();
        String assistantId = conversation.getNpc().getAssistantId();
        Map<String, String> assistant = Map.of("assistant_id", assistantId);
        String res = Request.sendRequest(toJson(assistant), "threads/" + threadId + "/runs", Header.buildBeta(), Request.Action.POST);
        Run run = fromJson(res);
        if (run.status == null) {
            ChatWithNPCMod.LOGGER.error("[chat-with-npc] API error: " + res);
            throw new Exception("Run status is null");
        }
        // Wait for the run to complete or timeout
        long startTime = System.currentTimeMillis();
        while (!run.isCompleted() && System.currentTimeMillis() - startTime < 10000) {
            run.updateStatus();
            try {
                java.lang.Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        run.callback(conversation);
    }

    private void updateStatus() throws Exception {
        String res = Request.sendRequest(
                null,
                "threads/" + thread_id + "/runs/" + id,
                Header.builder()
                        .add(Header.Type.AUTHORIZATION)
                        .add(Header.Type.OPENAI_BETA)
                        .build(),
                Request.Action.GET
        );
        Run run = fromJson(res);
        if (run.status == null) {
            ChatWithNPCMod.LOGGER.error("[chat-with-npc] API error: " + res);
            throw new Exception("Run status is null");
        }
        this.status = run.status;
    }

    public boolean isCompleted() {
        return this.status.equals("completed");
    }

    private void callback(@NotNull ConversationHandler conversation) throws Exception {
        String response = Threads.getLastMessage(thread_id);
        conversation.getNpc().replyMessage(response, SettingManager.range);
    }
}
