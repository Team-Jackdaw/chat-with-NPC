package com.jackdaw.chatwithnpc.conversation;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.conversation.prompt.Prompt;
import com.jackdaw.chatwithnpc.npc.NPCEntity;

public class ConversationHandler {

    final NPCEntity npc;

    protected final Record messageRecord = new Record();

    long updateTime = 0L;

    protected boolean isTalking = false;

    public ConversationHandler(NPCEntity npc) {
        this.npc = npc;
        startConversation();
    }

    private void sendWaitMessage() {
        npc.replyMessage("...", SettingManager.range);
    }

    public NPCEntity getNpc() {
        return npc;
    }

    public void getResponse(String message) {
        if (SettingManager.apiKey.isEmpty()) {
            npc.replyMessage("[chat-with-npc] You have not set an API key! Get one from https://beta.openai.com/account/api-keys and set it with /chat-with-npc setkey", SettingManager.range);
            return;
        }
        setTalking(true);
        Thread t = new Thread(() -> {
            try {
                OpenAIHandler.updateSetting();
                String response;
                response = OpenAIHandler.sendRequest(message, npc.getLongTermMemory(), messageRecord, npc.getName());
                npc.replyMessage(response, SettingManager.range);
                addMessageRecord(System.currentTimeMillis(), Record.Role.NPC, response, npc.getName());
                setTalking(false);
            } catch (Exception e) {
                setTalking(false);
                npc.replyMessage("[chat-with-npc] Error getting response", SettingManager.range);
                ChatWithNPCMod.LOGGER.error(e.getMessage());
            }
        });
        t.start();
    }

    private void startConversation() {
        sendWaitMessage();
        getResponse(Prompt.builder()
                .setNpc(npc)
                .build()
                .getInitialPrompt());
        updateTime = System.currentTimeMillis();
    }

    public void replyToEntity(String message, String playerName) {
        sendWaitMessage();
        addMessageRecord(System.currentTimeMillis(), Record.Role.PLAYER, message, playerName);
        getResponse(Prompt.builder()
                .setNpc(npc)
                .build()
                .getInitialPrompt());
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
     * @param time 消息时间
     * @param role 消息发出者的身份
     * @param message 消息内容
     * @param name 消息发出者的名称
     */
    public void addMessageRecord(long time, Record.Role role, String message, String name) {
        this.messageRecord.addMessage(time, role, message, name);
    }

    /**
     * 通过模型，获取当前会话的长期记忆。并清空当前会话的消息记录。
     */
    public void getLongTermMemory() {
        if (messageRecord.isEmpty() || SettingManager.apiKey.isEmpty()) return;
        messageRecord.changeAllRole(Record.Role.PLAYER);
        String endingPrompt = "The Minecraft NPC '"+ npc.getName() +"' is having conversation with players. The first message was sent by '" + npc.getName() + "'. Chat crosses over.";
        messageRecord.addMessage(System.currentTimeMillis(), Record.Role.SYSTEM, "Now the conversation is over. Summarize the above conversation in the tone you informed '" + npc.getName() + "'. (No more than 50 words)");
        try {
            OpenAIHandler.updateSetting();
            String memory = OpenAIHandler.sendRequest(endingPrompt, null, messageRecord, null);
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
     * @return NPC的对话状态
     */
    public boolean isTalking() {
        return isTalking;
    }

    /**
     * 设置NPC的对话状态
     * @param isTalking NPC的对话状态
     */
    public void setTalking(boolean isTalking) {
        this.isTalking = isTalking;
    }
}
