package com.jackdaw.chatwithnpc.openaiapi;

import com.google.gson.Gson;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Run {

    private String id;
    private String threadId;
    private String assistantId;
    private String status;

    private static String toJson(Map<String, String> map) {
        return new Gson().toJson(map);
    }

    private static Run fromJson(String json) {
        return new Gson().fromJson(json, Run.class);
    }

    public static void run(@NotNull ConversationHandler conversation) {
        String threadId = conversation.getNpc().getThreadId();
        String assistantId = conversation.getNpc().getAssistantId();
        Map<String, String> assistant = Map.of("assistant_id", assistantId);
        java.lang.Thread t = new java.lang.Thread(() -> {
            try {
                String res = Request.sendRequest(toJson(assistant), "threads/" + threadId + "/runs", Header.buildBeta());
                Run run = fromJson(res);
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
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        t.start();
    }

    private void updateStatus() throws Exception {
        String res = Request.sendRequest(
                null,
                "threads/" + threadId + "/runs/" + this.id,
                Header.builder()
                        .add(Header.Type.AUTHORIZATION)
                        .add(Header.Type.OPENAI_BETA)
                        .build()
        );
        Run run = fromJson(res);
        this.status = run.status;
    }

    public boolean isCompleted() {
        return this.status.equals("completed");
    }

    private void callback(@NotNull ConversationHandler conversation) throws Exception {
        String response = Thread.getLastMessage(threadId);
        conversation.getNpc().replyMessage(response, SettingManager.range);
    }
}
