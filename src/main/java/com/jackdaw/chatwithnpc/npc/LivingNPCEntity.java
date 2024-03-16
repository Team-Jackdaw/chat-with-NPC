package com.jackdaw.chatwithnpc.npc;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

public class LivingNPCEntity extends NPCEntity{
    /**
     * This is a constructor used to initialize the NPC with the entity.
     *
     * @param entity The entity of the NPC.
     */
    public LivingNPCEntity(@NotNull LivingEntity entity) {
        super(entity);
    }

    @Override
    public void doAction(Actions action, PlayerEntity player) {
        // TODO: 不在这一版本的考虑范围中
    }

}
