package com.jackdaw.chatwithnpc.npc;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class is used to manage the NPC entities.
 */
public class NPCEntityManager {
    public static final ConcurrentHashMap<UUID, NPCEntity> npcMap = new ConcurrentHashMap<>();

    private static final long outOfTime = ChatWithNPCMod.outOfTime;

    public static boolean isRegistered(UUID uuid) {
        return npcMap.containsKey(uuid);
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
            return;
        }
        NPCEntity npcEntity = new NPCEntity(entity);
        NPCDataManager npcDataManager = npcEntity.getDataManager();
        if (!isOP && !npcDataManager.isExist()) return;
        npcDataManager.sync();
        npcMap.put(entity.getUuid(), npcEntity);
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
     * Get an NPC entity by its UUID.
     * @param uuid The UUID of the NPC entity
     * @return The NPC entity
     */
    public static NPCEntity getNPCEntity(UUID uuid) {
        return npcMap.get(uuid);
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
