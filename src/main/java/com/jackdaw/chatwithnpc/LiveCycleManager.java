package com.jackdaw.chatwithnpc;

import com.jackdaw.chatwithnpc.async.AsyncTaskQueue;
import com.jackdaw.chatwithnpc.conversation.ConversationManager;
import com.jackdaw.chatwithnpc.group.GroupManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LiveCycleManager {

    private static ScheduledExecutorService executorService;

    private static final AsyncTaskQueue reloadService = new AsyncTaskQueue();

    /**
     * Start the live cycle manager
     * @param updateInterval the interval in milliseconds to update the live cycle manager
     */
    public static void start(long updateInterval) {
        // Check for out of time static data
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(LiveCycleManager::update, 0, updateInterval, TimeUnit.MILLISECONDS);
    }

    /**
     * Update the live cycle manager
     */
    public static void update() {
        ConversationManager.endOutOfTimeConversations();
        GroupManager.endOutOfTimeGroup();
    }

    /**
     * Save all conversations, NPC entities, and environments
     */
    public static void asyncSaveAll() {
        reloadService.addTask(LiveCycleManager::saveAll);
    }

    /**
     * Save all conversations, NPC entities, and environments
     */
    public static void saveAll() {
        if (ChatWithNPCMod.debug) {
            ChatWithNPCMod.LOGGER.info("[chat-with-npc] Saving all conversations, NPC entities, and environments.");
        }
        ConversationManager.endAllConversations();
        GroupManager.endAllEnvironments();
    }

    /**
     * Shutdown the live cycle manager
     */
    public static void shutdown() {
        executorService.shutdown();
        reloadService.shutdown();
    }
}
