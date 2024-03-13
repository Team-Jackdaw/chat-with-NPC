package com.jackdaw.chatwithnpc;

import com.jackdaw.chatwithnpc.group.GroupManager;
import com.jackdaw.chatwithnpc.conversation.ConversationManager;
import com.jackdaw.chatwithnpc.npc.NPCEntityManager;

public class UpdateStaticData {
    public static void update() {
        ConversationManager.endOutOfTimeConversations();
        GroupManager.endOutOfTimeEnvironments();
    }

    public static void close() {
        ChatWithNPCMod.LOGGER.info("[chat-with-npc] Saving all conversations, NPC entities, and environments.");
        try {
            ConversationManager.endAllConversations();
            NPCEntityManager.endAllNPCEntity();
            GroupManager.endAllEnvironments();
        } catch (Exception ignore) {}
    }
}
