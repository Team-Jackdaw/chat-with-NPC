package com.jackdaw.chatwithnpc.npc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.VillagerEntity;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/**
 * This class is used to manage the NPC entities.
 */
public class NPCEntityManager {
    public static final ConcurrentHashMap<UUID, NPCEntity> npcMap = new ConcurrentHashMap<>();

    public static boolean isRegistered(UUID uuid) {
        return npcMap.containsKey(uuid);
    }

    /**
     * Initialize an NPC entity if the NPC is not conversing.
     * @param entity The NPC entity to initialize
     */
    public static void registerNPCEntity(Entity entity, boolean isOP) {
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
        if (!isOP && !npcDataManager.isExist()) return;
        npcDataManager.sync();
        npcMap.put(entity.getUuid(), npcEntity);
    }

    public static void removeNPCEntity(UUID uuid) {
        npcMap.get(uuid).randomForget();
        npcMap.get(uuid).getDataManager().save();
        npcMap.remove(uuid);
    }

    public static NPCEntity getNPCEntity(UUID uuid) {
        return npcMap.get(uuid);
    }
}
