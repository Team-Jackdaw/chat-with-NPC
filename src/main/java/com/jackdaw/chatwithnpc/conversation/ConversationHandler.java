package com.jackdaw.chatwithnpc.conversation;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.conversation.prompt.Prompt;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.jackdaw.chatwithnpc.npc.Record;

public class ConversationHandler {

    final NPCEntity npc;

    long updateTime = 0L;

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
        // 1.5 second cooldown between requests
//        if (npc.getLastMessageTime() + 1500L > System.currentTimeMillis()) return;
        if (SettingManager.apiKey.isEmpty()) {
            npc.replyMessage("[chat-with-npc] You have not set an API key! Get one from https://beta.openai.com/account/api-keys and set it with /chat-with-npc setkey", SettingManager.range);
            return;
        }
        npc.updateLastMessageTime(System.currentTimeMillis());
        Thread t = new Thread(() -> {
            try {
                OpenAIHandler.updateSetting();
                String response = OpenAIHandler.sendRequest(message, npc.getMessageRecord());
                npc.replyMessage(response, SettingManager.range);
                npc.addMessageRecord(System.currentTimeMillis(), Record.Role.NPC, response);
            } catch (Exception e) {
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

    public void replyToEntity(String message) {
        sendWaitMessage();
        npc.addMessageRecord(System.currentTimeMillis(), Record.Role.PLAYER, message);
        getResponse(Prompt.builder()
                .setNpc(npc)
                .build()
                .getInitialPrompt());
        updateTime = System.currentTimeMillis();
    }

    public long getUpdateTime() {
        return updateTime;
    }

}
