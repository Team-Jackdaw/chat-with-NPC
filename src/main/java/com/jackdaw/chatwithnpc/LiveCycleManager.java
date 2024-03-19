package com.jackdaw.chatwithnpc;

import com.jackdaw.chatwithnpc.conversation.ConversationManager;
import com.jackdaw.chatwithnpc.group.GroupManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LiveCycleManager {

    private static ScheduledExecutorService executorService;

    public static void start(long updateInterval) {
        // Check for out of time static data
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(LiveCycleManager::update, 0, updateInterval, TimeUnit.MILLISECONDS);
    }

    public static void update() {
        if (ChatWithNPCMod.debug) {
            ChatWithNPCMod.LOGGER.info("[chat-with-npc] Updating conversations and environments.");
        }
        ConversationManager.endOutOfTimeConversations();
        GroupManager.endOutOfTimeGroup();
    }

    public static void saveAll() {
        if (ChatWithNPCMod.debug) {
            ChatWithNPCMod.LOGGER.info("[chat-with-npc] Saving all conversations, NPC entities, and environments.");
        }
        try {
            ConversationManager.endAllConversations();
            GroupManager.endAllEnvironments();
        } catch (Exception ignore) {
        }
    }

    public static void shutdown() {
        executorService.shutdown();
    }
}
