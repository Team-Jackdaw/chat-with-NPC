package com.jackdaw.chatwithnpc;

import com.jackdaw.chatwithnpc.group.GroupManager;
import com.jackdaw.chatwithnpc.conversation.ConversationManager;
import com.jackdaw.chatwithnpc.npc.NPCEntityManager;

public class UpdateStaticData {
    public static void update() {
        ConversationManager.endOutOfTimeConversations();
        NPCEntityManager.endOutOfTimeNPCEntity();
        GroupManager.endOutOfTimeEnvironments();
    }

    public static void close() {
        try {
            ConversationManager.endAllConversations();
            NPCEntityManager.endAllNPCEntity();
            GroupManager.endAllEnvironments();
        }
        catch (Exception ignored) {
        }
    }
}
