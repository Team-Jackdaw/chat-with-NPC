package com.jackdaw.chatwithnpc.npc.npcentityset;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

public class VillagerNPCEntity extends LivingNPCEntity {

    /**
     * This is a constructor used to initialize the NPC with the entity.
     *
     * @param entity The entity of the NPC.
     */
    public VillagerNPCEntity(@NotNull VillagerEntity entity) {
        super(entity);
    }

    @Override
    public void doAction(@NotNull Actions action, PlayerEntity player) {
        switch (action) {
            case WALK_TO_PLAYER -> walkToPlayer(player);
            case ESCAPE -> escape();
            case SHAKE_HEAD -> shakeHead();
            case FEEL_HAPPY -> feelHappy();
            default -> {}
        }
    }

    private void shakeHead() {
        VillagerEntity villager = (VillagerEntity) this.entity;
        villager.setHeadRollingTimeLeft(60);
    }

}
