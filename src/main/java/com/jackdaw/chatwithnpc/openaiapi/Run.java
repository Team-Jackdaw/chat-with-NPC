package com.jackdaw.chatwithnpc.openaiapi;

import com.google.gson.Gson;
import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.AsyncTask;
import com.jackdaw.chatwithnpc.SettingManager;
import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import com.jackdaw.chatwithnpc.openaiapi.functioncalling.FunctionManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;

public class Run {
    public String id;
    public String thread_id;
    public String assistant_id;
    public String status;
    public RequiredAction required_action;

    /**
     * Run the assistant for the NPC from the OpenAI API, and it will return the result.
     *
     * @param conversation The conversation handler
     * @return The result of the run
     * @throws Exception If the run status is null
     */
    public static @NotNull RunResult run(@NotNull ConversationHandler conversation) throws Exception {
        String threadId = conversation.getNpc().getThreadId();
        String assistantId = conversation.getNpc().getAssistantId();
        Map<String, String> assistant = Map.of("assistant_id", assistantId);
        String res = Request.sendRequest(toJson(assistant), "threads/" + threadId + "/runs", Header.buildBeta(), Request.Action.POST);
        Run run = fromJson(res);
        if (run.status == null) {
            ChatWithNPCMod.LOGGER.error("[chat-with-npc] API error: " + res);
            throw new Exception("Run status is null");
        }
        run = checkOrTimeOut(run);
        // If the run requires action, return the run result
        if (run.isRequiresAction()) return new RunResult(conversation, run);
        if (run.isCompleted()) run.replyMessage(conversation);
        return RunResult.nothingToDo();
    }

    private static Run checkOrTimeOut(@NotNull Run run) throws Exception {
        long expire = System.currentTimeMillis() + 10000;
        String newRes;
        newRes = run.updateStatus();
        while (!run.isCompleted() && !run.isRequiresAction()) {
            if (System.currentTimeMillis() > expire) throw new Exception("Time out");
            newRes = run.updateStatus();
            Thread.sleep(100);
        }
        return fromJson(newRes);
    }

    private static String toJson(Map map) {
        return new Gson().toJson(map);
    }

    private static Run fromJson(String json) {
        return new Gson().fromJson(json, Run.class);
    }

    /**
     * Check if the run is completed
     * @return True if the run is completed
     */
    public boolean isCompleted() {
        return this.status.equals("completed");
    }

    /**
     * Check if the run requires action
     * @return True if the run requires action
     */
    public boolean isRequiresAction() {
        return this.status.equals("requires_action");
    }

    private @NotNull String updateStatus() throws Exception {
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
        return res;
    }

    /**
     * Call the functions and reply the message
     * @param conversation The conversation handler
     */
    public void callFunctionsAndReply(ConversationHandler conversation){
        ArrayList<Map> outputs = new ArrayList<>();
        for (FunctionManager.ToolCall toolCall : required_action.submit_tool_outputs.tool_calls) {
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
        AsyncTask.call(() -> {
            try {
                Request.sendRequest(response, "threads/" + thread_id + "/runs/" + id + "/submit_tool_outputs", Header.buildBeta(), Request.Action.POST);
                Run newRun = checkOrTimeOut(this);
                newRun.replyMessage(conversation);
            } catch (Exception e) {
                ChatWithNPCMod.LOGGER.error("[chat-with-npc] Error while submitting tool outputs: " + e.getMessage());
            }
            return AsyncTask.nothingToDo();
        });
    }

    private void replyMessage(@NotNull ConversationHandler conversation) throws Exception {
        String response = Threads.getLastMessage(thread_id);
        conversation.getNpc().replyMessage(response, SettingManager.range);
    }

    public static class RequiredAction {
        public String type;
        public SubmitToolOutputs submit_tool_outputs;

        public static class SubmitToolOutputs {
            public FunctionManager.ToolCall[] tool_calls;
        }
    }

    /**
     * Represents the result of a Run request.
     */
    public static class RunResult implements AsyncTask.TaskResult {
        @Nullable
        private final ConversationHandler conversation;
        @Nullable
        private final Run run;

        RunResult(@Nullable ConversationHandler conversation, @Nullable Run run) {
            this.conversation = conversation;
            this.run = run;
        }

        /**
         * Executes the RunResult by calling the functions and replying to the conversation.
         */
        public void execute(){
            if (isCallable() && run != null) {
                try {
                    run.callFunctionsAndReply(conversation);
                } catch (Exception e) {
                    ChatWithNPCMod.LOGGER.error("[chat-with-npc] Error while calling functions: " + e.getMessage());
                }
            }
        }

        /**
         * Returns whether the RunResult is callable. A RunResult is callable if it has a Run and a ConversationHandler and the Run should require an action.
         * @return True if the RunResult is callable, false otherwise.
         */
        public boolean isCallable() {
            return run != null && run.isRequiresAction() && conversation != null;
        }

        /**
         * Returns a RunResult that does nothing. This is used when there is nothing to do after a Run request has been made
         * @return A RunResult that does nothing.
         */
        @Contract(value = " -> new", pure = true)
        public static @NotNull RunResult nothingToDo() {
            return new RunResult(null, null);
        }
    }

}
