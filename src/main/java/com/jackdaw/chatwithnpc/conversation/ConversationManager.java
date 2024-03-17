package com.jackdaw.chatwithnpc.conversation;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.jackdaw.chatwithnpc.npc.NPCEntityManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to manage conversations between players and NPCs.
 * <p>
 * <b>Every player can only have one conversation at a time.</b>
 */
public class ConversationManager {

    public static final ConcurrentHashMap<UUID, ConversationHandler> conversationMap = new ConcurrentHashMap<>();
    // The time in milliseconds that a conversation is considered out of time
    private static final long outOfTime = ChatWithNPCMod.outOfTime;

    /**
     * Start a conversation for Player with an NPC
     *
     * @param entity The Entity to start a conversation with
     */
    public static void startConversation(Entity entity, boolean isOP) {
        NPCEntityManager.registerNPCEntity(entity, isOP);
        NPCEntity npc = NPCEntityManager.getNPCEntity(entity.getUuid());
        if (npc == null) return;
        if (isConversing(npc)) {
            return;
        }
        ConversationHandler conversationHandler = new ConversationHandler(npc);
        conversationMap.put(npc.getUUID(), conversationHandler);
    }

    public static void endConversation(UUID uuid) {
//        conversationMap.get(uuid).getLongTermMemory();
        conversationMap.get(uuid).taskStack.shutdown();
        NPCEntityManager.removeNPCEntity(uuid);
        conversationMap.remove(uuid);
    }

    /**
     * Get the conversation with a specific UUID
     *
     * @param uuid The UUID of the conversation
     * @return The conversation with the UUID
     */
    public static ConversationHandler getConversation(UUID uuid) {
        return conversationMap.get(uuid);
    }

    /**
     * Get the closest conversation to a player
     *
     * @param player The player to check
     * @return The closest conversation to the player
     */
    public static @Nullable ConversationHandler getConversation(PlayerEntity player) {
        List<ConversationHandler> conversations = getConversations(player);
        if (conversations.isEmpty()) return null;
        return conversations.stream().sorted((conversation1, conversation2) -> {
            double distance1 = conversation1.getNpc().getEntity().getPos().distanceTo(player.getPos());
            double distance2 = conversation2.getNpc().getEntity().getPos().distanceTo(player.getPos());
            return Double.compare(distance1, distance2);
        }).toList().get(0);
    }

    /**
     * Check if an NPC is chatting
     *
     * @param npcEntity The NPC to check
     * @return True if the NPC is chatting, false otherwise
     */
    public static boolean isConversing(@NotNull NPCEntity npcEntity) {
        return conversationMap.containsKey(npcEntity.getUUID());
    }

    /**
     * End all conversations that are out of time
     */
    public static void endOutOfTimeConversations() {
        if (conversationMap.isEmpty()) return;
        conversationMap.forEach((uuid, conversationHandler) -> {
            if (conversationHandler.getUpdateTime() + outOfTime < System.currentTimeMillis()) {
                endConversation(uuid);
            }
        });
    }

    /**
     * End all conversations
     */
    public static void endAllConversations() {
        if (conversationMap.isEmpty()) return;
        conversationMap.forEach((uuid, conversationHandler) -> endConversation(uuid));
    }

    /**
     * Check if there is a Conversation nearby
     *
     * @param player The player to check
     * @return True if there is a Conversation nearby, false otherwise
     */
    public static boolean isConversationNearby(PlayerEntity player) {
        List<UUID> entities = getEntitiesInRange(player, SettingManager.range).stream().map(Entity::getUuid).toList();
        return conversationMap.keySet().stream().anyMatch(entities::contains);
    }

    /**
     * Get all conversations within a certain range of a player
     *
     * @param player The player to check
     * @return A list of Conversations within the range of the player
     */
    public static List<ConversationHandler> getConversations(PlayerEntity player) {
        List<UUID> entities = getEntitiesInRange(player, SettingManager.range).stream().map(Entity::getUuid).toList();
        return conversationMap.keySet().stream().filter(entities::contains).map(conversationMap::get).toList();
    }


    /**
     * Get all Entities within a certain range of a player
     *
     * @param player The player to check
     * @param range  The range to check
     * @return A list of Entities within the range of the player
     */
    public static List<Entity> getEntitiesInRange(@NotNull PlayerEntity player, double range) {
        return player.world.getEntitiesByClass(Entity.class, player.getBoundingBox().expand(range), entity -> entity.getCustomName() != null);
    }
}
