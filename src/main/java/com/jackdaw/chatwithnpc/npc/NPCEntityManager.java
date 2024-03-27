package com.jackdaw.chatwithnpc.npc;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.SettingManager;
import com.jackdaw.chatwithnpc.openaiapi.Assistant;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class is used to manage the NPC entities.
 */
public class NPCEntityManager {
    public static final ConcurrentHashMap<UUID, NPCEntity> npcMap = new ConcurrentHashMap<>();

    private static final long outOfTime = ChatWithNPCMod.outOfTime;

    /**
     * Check if an NPC is registered
     * @param uuid The UUID of the NPC
     * @return True if the NPC is registered, false otherwise
     */
    public static boolean isRegistered(UUID uuid) {
        return npcMap.containsKey(uuid);
    }

    /**
     * Get an NPC entity by its UUID.
     *
     * @param uuid The UUID of the NPC entity
     * @return The NPC entity
     */
    public static @Nullable NPCEntity getNPCEntity(UUID uuid) {
        return npcMap.get(uuid);
    }

    /**
     * Get the closest NPC around a player. This will register all NPCs within the range of the player.
     *
     * @param player The player to check
     * @return The closest NPC to the player
     */
    public static @Nullable NPCEntity getNPCEntity(PlayerEntity player) {
        List<NPCEntity> npcEntities = getNPCEntities(player);
        if (npcEntities.isEmpty()) return null;
        return npcEntities.stream().sorted((npc1, npc2) -> {
            double distance1 = npc1.getEntity().getPos().distanceTo(player.getPos());
            double distance2 = npc2.getEntity().getPos().distanceTo(player.getPos());
            return Double.compare(distance1, distance2);
        }).toList().get(0);
    }

    /**
     * Get all NPCs within a certain range of a player. If the NPC is not registered, register it.
     *
     * @param player The player to check
     * @return A list of NPCs within the range of the player
     */
    public static List<NPCEntity> getNPCEntities(PlayerEntity player) {
        List<Entity> entities = getEntitiesInRange(player, SettingManager.range);
        entities.forEach(entity -> registerNPCEntity(entity, false));
        List<UUID> entityUUIDs = entities.stream().map(Entity::getUuid).toList();
        return npcMap.keySet().stream().filter(entityUUIDs::contains).map(npcMap::get).toList();
    }

    private static List<Entity> getEntitiesInRange(@NotNull PlayerEntity player, double range) {
        return player.world.getEntitiesByClass(Entity.class, player.getBoundingBox().expand(range), entity -> entity.getCustomName() != null);
    }

    /**
     * Initialize an NPC entity if the NPC is not conversing.
     *
     * @param entity The NPC entity to initialize
     */
    public static void registerNPCEntity(@NotNull Entity entity, boolean isOP) {
        if (entity.getCustomName() == null) return;
        if (!entity.getCustomName().getString().matches("^[a-zA-Z0-9_-]{1,64}$")) return;
        if (isRegistered(entity.getUuid())) {
            npcMap.get(entity.getUuid()).setUpdateTime();
            return;
        }
        NPCEntity npcEntity = new NPCEntity(entity);
        NPCDataManager npcDataManager = npcEntity.getDataManager();
        if (!isOP && !npcDataManager.isExist()) return;
        npcDataManager.sync();
        npcMap.put(entity.getUuid(), npcEntity);
        try {
            if (!npcEntity.hasAssistant()) Assistant.createAssistant(npcEntity);
            else Assistant.modifyAssistant(npcEntity);
        } catch (Exception e) {
            ChatWithNPCMod.LOGGER.error("[chat-with-npc] Modify assistant failed." + e.getMessage());
        }
    }

    /**
     * Remove an NPC entity from the map.
     * @param uuid The UUID of the NPC entity to remove
     */
    public static void removeNPCEntity(UUID uuid) {
        npcMap.get(uuid).discard();
        npcMap.remove(uuid);
    }

    /**
     * End the out of time NPC entities.
     */
    public static void endOutOfTimeNPCEntity() {
        if (npcMap.isEmpty()) return;
        npcMap.forEach((uuid, npc) -> {
            if (npc.getUpdateTime() + outOfTime < System.currentTimeMillis()) {
                removeNPCEntity(uuid);
            }
        });
    }

    /**
     * End all NPC entities.
     */
    public static void endAllNPCEntity() {
        if (npcMap.isEmpty()) return;
        npcMap.forEach((uuid, npc) -> removeNPCEntity(uuid));
    }
}
