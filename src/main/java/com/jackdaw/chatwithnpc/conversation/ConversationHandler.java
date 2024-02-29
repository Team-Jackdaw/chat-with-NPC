package com.jackdaw.chatwithnpc.conversation;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.api.OpenAIHandler;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.npc.Record;
import com.jackdaw.chatwithnpc.prompt.Prompt;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;

public class ConversationHandler {

    final NPCEntity npc;

    final PlayerEntity player;

    long updateTime = 0L;

    public ConversationHandler(NPCEntity npc, PlayerEntity player) {
        this.npc = npc;
        this.player = player;
        startConversation();
    }

    private void sendWaitMessage() {
        player.sendMessage(Text.of("<" + npc.getName() + "> ..."));
    }

    public void getResponse(PlayerEntity player, String message) {
        // 1.5 second cooldown between requests
//        if (npc.getLastMessageTime() + 1500L > System.currentTimeMillis()) return;
        if (SettingManager.apiKey.isEmpty()) {
            player.sendMessage(Text.of("[chat-with-npc] You have not set an API key! Get one from https://beta.openai.com/account/api-keys and set it with /chat-with-npc setkey"));
            return;
        }
        npc.updateLastMessageTime(System.currentTimeMillis());
        Thread t = new Thread(() -> {
            try {
                OpenAIHandler.updateSetting();
                String response = OpenAIHandler.sendRequest(message, npc.getMessageRecord());
                player.sendMessage(Text.of("<" + npc.getName() + "> " + response));
                npc.addMessageRecord(System.currentTimeMillis(), Record.Role.NPC, response);
            } catch (Exception e) {
                player.sendMessage(Text.of("[chat-with-npc] Error getting response"));
                ChatWithNPCMod.LOGGER.error(e.getMessage());
            }
        });
        t.start();
    }

    private void startConversation() {
        sendWaitMessage();
        getResponse(player, Prompt.builder()
                .setNpc(npc)
                .build()
                .getInitialPrompt());
        updateTime = System.currentTimeMillis();
    }

    public void replyToEntity(String message) {
        sendWaitMessage();
        npc.addMessageRecord(System.currentTimeMillis(), Record.Role.PLAYER, message);
        getResponse(player, Prompt.builder()
                .setNpc(npc)
                .build()
                .getInitialPrompt());
        updateTime = System.currentTimeMillis();
    }

    public long getUpdateTime() {
        return updateTime;
    }

}
