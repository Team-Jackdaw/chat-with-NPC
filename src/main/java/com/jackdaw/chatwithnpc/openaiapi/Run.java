package com.jackdaw.chatwithnpc.openaiapi;

import com.google.gson.Gson;
import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import com.jackdaw.chatwithnpc.openaiapi.functioncalling.FunctionManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
        CompletableFuture<Void> future = wait(run);
        try {
            future.get(10, TimeUnit.SECONDS);
            RunClass newRun = RunClass.fromJson(run.updateStatus());
            if (newRun.isRequiresAction()) {
                newRun.callFunctions(conversation);
                newRun.updateStatus();
                CompletableFuture<Void> newFuture = wait(newRun);
                try {
                    newFuture.get(10, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    ChatWithNPCMod.LOGGER.info(e.getMessage());
                    future.cancel(true);
                }
            }
            if (newRun.isCompleted()) newRun.callback(conversation);
        } catch (TimeoutException e) {
            ChatWithNPCMod.LOGGER.info(e.getMessage());
            future.cancel(true);
        }
    }

    @Contract("_ -> new")
    private static @NotNull CompletableFuture<Void> wait(RunClass run) {
        return CompletableFuture.runAsync(() -> {
            while (!run.isCompleted() && !run.isRequiresAction()) {
                try {
                    run.updateStatus();
                    Thread.sleep(100);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static class RunClass {
        private String id;
        private String thread_id;
        private String assistant_id;
        private String status;
        private RequiredAction required_action;

        private static String toJson(Map map) {
            return new Gson().toJson(map);
        }

        private static RunClass fromJson(String json) {
            return new Gson().fromJson(json, RunClass.class);
        }

        public boolean isCompleted() {
            return this.status.equals("completed");
        }

        public boolean isRequiresAction() {
            return this.status.equals("requires_action");
        }

        private String updateStatus() throws Exception {
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
            return res;
        }

        private void callFunctions(ConversationHandler conversation) throws Exception {
            ArrayList<Map> outputs = new ArrayList<>();
            for(FunctionManager.ToolCall toolCall : required_action.submit_tool_outputs.tool_calls) {
                if (toolCall.type.equals("function")) {
                    Map<String, String> res = FunctionManager.callFunction(conversation, toolCall.function.name, new Gson().fromJson(toolCall.function.arguments, Map.class));
                    outputs.add(Map.of(
                            "tool_call_id", toolCall.id,
                            "output", res.toString()
                    ));
                }
            }
            Map res = Map.of("tool_outputs", outputs);
            String response = toJson(res);
            Request.sendRequest(response, "threads/" + thread_id + "/runs/" + id + "/submit_tool_outputs", Header.buildBeta(), Request.Action.POST);
        }

        private void callback(@NotNull ConversationHandler conversation) throws Exception {
            String response = Threads.getLastMessage(thread_id);
            conversation.getNpc().replyMessage(response, SettingManager.range);
        }
    }

    private static class RequiredAction {
        private String type;
        private SubmitToolOutputs submit_tool_outputs;

        private static class SubmitToolOutputs {
            private FunctionManager.ToolCall[] tool_calls;
        }
    }


}
