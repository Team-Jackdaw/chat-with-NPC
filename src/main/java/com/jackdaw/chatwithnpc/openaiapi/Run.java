package com.jackdaw.chatwithnpc.openaiapi;

import com.google.gson.Gson;
import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Run {

    /**
     * Run the assistant for the NPC from the OpenAI API
     * @param conversation The conversation handler
     * @throws Exception If the run status is null
     */
    public static void run(@NotNull ConversationHandler conversation) throws Exception {
        String threadId = conversation.getNpc().getThreadId();
        String assistantId = conversation.getNpc().getAssistantId();
        Map<String, String> assistant = Map.of("assistant_id", assistantId);
        String res = Request.sendRequest(RunClass.toJson(assistant), "threads/" + threadId + "/runs", Header.buildBeta(), Request.Action.POST);
        RunClass run = RunClass.fromJson(res);
        if (run.status == null) {
            ChatWithNPCMod.LOGGER.error("[chat-with-npc] API error: " + res);
            throw new Exception("Run status is null");
        }
        // Wait for the run to complete or timeout
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            while (!run.isCompleted()) {
                try {
                    run.updateStatus();
                    java.lang.Thread.sleep(100);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        try {
            future.get(10, TimeUnit.SECONDS); // Wait for the run to complete or timeout
            run.callback(conversation);
        } catch (TimeoutException e) {
            future.cancel(true); // Cancel the task if it's not completed within the timeout
        }
    }

    private static class RunClass {
        private String id;
        private String thread_id;
        private String assistant_id;
        private String status;

        private static String toJson(Map<String, String> map) {
            return new Gson().toJson(map);
        }

        private static RunClass fromJson(String json) {
            return new Gson().fromJson(json, RunClass.class);
        }

        public boolean isCompleted() {
            return this.status.equals("completed");
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
            RunClass run = fromJson(res);
            if (run.status == null) {
                ChatWithNPCMod.LOGGER.error("[chat-with-npc] API error: " + res);
                throw new Exception("Run status is null");
            }
            this.status = run.status;
        }

        /**
         * Callback function to send the response to the player
         *
         * @param conversation The conversation handler
         * @throws Exception If the response is null
         */
        private void callback(@NotNull ConversationHandler conversation) throws Exception {
            String response = Threads.getLastMessage(thread_id);
            conversation.getNpc().replyMessage(response, SettingManager.range);
        }
    }


}
