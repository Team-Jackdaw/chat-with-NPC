package com.jackdaw.chatwithnpc.npc;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;

import java.util.HashMap;
import java.util.UUID;


/**
 * This class is used to manage the NPC entities.
 */
public class NPCEntityManager {

    // The time in milliseconds that an NPCEntity is considered out of time
    private static final long outOfTime = ChatWithNPCMod.outOfTime;

    public static final HashMap<UUID, NPCEntity> npcMap = new HashMap<>();

    public static boolean isRegistered(UUID uuid) {
        return npcMap.containsKey(uuid);
    }

    /**
     * Initialize an NPC entity if the NPC is not conversing.
     * @param entity The NPC entity to initialize
     */
    public static void registerNPCEntity(Entity entity) {
        if (isRegistered(entity.getUuid())) {
            return;
        }
        NPCEntity npcEntity;
        if (entity instanceof VillagerEntity villager) {
            npcEntity = new VillagerNPCEntity(villager);
        } else if (entity instanceof LivingEntity entityLiving) {
            npcEntity = new LivingNPCEntity(entityLiving);
        } else {
            return;
        }
        NPCDataManager npcDataManager = npcEntity.getDataManager();
        npcDataManager.sync();
        npcMap.put(entity.getUuid(), npcEntity);
    }

    public static void removeNPCEntity(UUID uuid) {
        npcMap.get(uuid).getDataManager().save();
        npcMap.remove(uuid);
    }

    public static NPCEntity getNPCEntity(UUID uuid) {
        return npcMap.get(uuid);
    }

    public static void endOutOfTimeNPCEntity() {
        npcMap.forEach((uuid, npcEntity) -> {
            if (npcEntity.getLastMessageTime() + outOfTime < System.currentTimeMillis()) {
                removeNPCEntity(uuid);
            }
        });
    }

    public static void endAllNPCEntity() {
        npcMap.forEach((uuid, npcEntity) -> removeNPCEntity(uuid));
    }
}
