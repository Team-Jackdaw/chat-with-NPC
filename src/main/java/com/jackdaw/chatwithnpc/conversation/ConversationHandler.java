package com.jackdaw.chatwithnpc.conversation;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.conversation.prompt.Prompt;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.jackdaw.chatwithnpc.openaiapi.Assistant;
import com.jackdaw.chatwithnpc.async.AsyncTaskQueue;
import com.jackdaw.chatwithnpc.openaiapi.Run;
import com.jackdaw.chatwithnpc.openaiapi.Threads;
import org.jetbrains.annotations.NotNull;

public class ConversationHandler {

    protected final Record messageRecord = new Record();
    final NPCEntity npc;
    protected boolean isTalking = false;
    long updateTime = 0L;
    public AsyncTaskQueue taskQueue = new AsyncTaskQueue();

    public ConversationHandler(@NotNull NPCEntity npc, boolean newAPI) {
        this.npc = npc;
        startConversation(newAPI);
    }

    private void sendWaitMessage() {
        npc.replyMessage("...", SettingManager.range);
    }

    public NPCEntity getNpc() {
        return npc;
    }

    public void getResponse(String requestJson) {
        if (SettingManager.apiKey.isEmpty()) {
            npc.replyMessage("[chat-with-npc] You have not set an API key! Get one from https://beta.openai.com/account/api-keys and set it with /npchat setkey", SettingManager.range);
            return;
        }
        setTalking(true);
        boolean isOK = taskQueue.addTask(() -> {
            try {
                String response;
                response = tryResponse(requestJson, 3);
                npc.replyMessage(response, SettingManager.range);
                addMessageRecord(System.currentTimeMillis(), Record.Role.NPC, response, npc.getName());
                setTalking(false);
            } catch (Exception e) {
                setTalking(false);
                npc.replyMessage("[chat-with-npc] Error getting response", SettingManager.range);
                ChatWithNPCMod.LOGGER.error(e.getMessage());
            }
        });
        if (!isOK) setTalking(false);
    }

    private @NotNull String tryResponse(String requestJson, int times) throws Exception {
        Exception e = new Exception("Error getting response");
        if (times <= 0) throw e;
        String newResponse = OpenAIHandler.sendRequest(requestJson);
        if (newResponse == null) throw e;
        if (newResponse.equals(npc.getName())) {
            return tryResponse(requestJson, times - 1);
        } else {
            return newResponse;
        }
    }

    private void startConversation() {
        sendWaitMessage();
        getResponse(Prompt.builder()
                .fromNPC(npc)
                .build()
                .toRequestJson());
        updateTime = System.currentTimeMillis();
    }

    private void startConversation(boolean newAPI) {
        if (!newAPI) {
            startConversation();
            return;
        }
        setTalking(true);
        sendWaitMessage();
        boolean isOK =  taskQueue.addTask(() -> {
            try {
                if (!npc.hasAssistant()) {
                    Assistant.createAssistant(npc);
                } else {
                    Assistant.modifyAssistant(npc);
                }
                if (!npc.hasThreadId()) Threads.createThread(this);
                Threads.addMessage(npc.getThreadId(), "Hello!");
                Run.run(this);
                setTalking(false);
            } catch (Exception e) {
                ChatWithNPCMod.LOGGER.error(e.getMessage());
                taskQueue.clear();
                setTalking(false);
            }
        });
        if (!isOK) setTalking(false);
        updateTime = System.currentTimeMillis();
    }

    public void replyToEntity(String message, String playerName) {
        sendWaitMessage();
        addMessageRecord(System.currentTimeMillis(), Record.Role.PLAYER, message, playerName);
        getResponse(Prompt.builder()
                .fromConversation(this)
                .build()
                .toRequestJson());
        updateTime = System.currentTimeMillis();
    }

    public void replyToEntity(String message, String playerName, boolean newAPI) {
        if (!newAPI) {
            replyToEntity(message, playerName);
            return;
        }
        setTalking(true);
        sendWaitMessage();
        boolean isOk = taskQueue.addTask(() -> {
            try {
                if (!npc.hasAssistant()) Assistant.createAssistant(npc);
                if(!npc.hasThreadId()) Threads.createThread(this);
                Threads.addMessage(npc.getThreadId(), message);
                Run.run(this);
                setTalking(false);
            } catch (Exception e) {
                ChatWithNPCMod.LOGGER.error(e.getMessage());
                taskQueue.clear();
                setTalking(false);
            }
        });
        if (!isOk) setTalking(false);
        updateTime = System.currentTimeMillis();
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public String getUpdateTimeString() {
        // converge Long to real time
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(updateTime));
    }

    /**
     * 添加一条消息记录，该记录应该包括了当前会话的最近一条消息。
     *
     * @param time    消息时间
     * @param role    消息发出者的身份
     * @param message 消息内容
     * @param name    消息发出者的名称
     */
    public void addMessageRecord(long time, Record.Role role, String message, String name) {
        this.messageRecord.addMessage(time, role, message, name);
    }

    /**
     * 通过模型，获取当前会话的长期记忆。并清空当前会话的消息记录。
     */
    public void getLongTermMemory() {
        if (messageRecord.isEmpty() || SettingManager.apiKey.isEmpty() || !npc.isNeedMemory()) return;
        messageRecord.changeAllRole(Record.Role.PLAYER);
        String endingPrompt = "The Minecraft NPC '" + npc.getName() + "' is having conversation with players. The first message was sent by '" + npc.getName() + "'. Chat crosses over.";
        messageRecord.addMessage(System.currentTimeMillis(), Record.Role.SYSTEM, "Now the conversation is over. Summarize the above conversation in the tone you informed '" + npc.getName() + "'. (No more than 50 words)");
        try {
            String memory = OpenAIHandler.sendRequest(Prompt.builder().addMessage(Record.Role.SYSTEM, endingPrompt).addMessageRecordMessages(messageRecord).build().toRequestJson());
            if (memory == null) throw new Exception("Error getting response");
            messageRecord.popMessage();
            npc.addLongTermMemory(System.currentTimeMillis(), memory);
        } catch (Exception e) {
            ChatWithNPCMod.LOGGER.error(e.getMessage());
        }
        messageRecord.clear();
    }

    public void clearMessageRecord() {
        messageRecord.clear();
    }

    /**
     * 获取NPC的对话状态
     *
     * @return NPC的对话状态
     */
    public boolean isTalking() {
        return isTalking;
    }

    /**
     * 设置NPC的对话状态
     *
     * @param isTalking NPC的对话状态
     */
    public void setTalking(boolean isTalking) {
        this.isTalking = isTalking;
    }

    public Record getMessageRecord() {
        return messageRecord;
    }

    public void discard() {
        taskQueue.shutdown();
        if (!ChatWithNPCMod.newAPI) {
            getLongTermMemory();
        }
    }
}
