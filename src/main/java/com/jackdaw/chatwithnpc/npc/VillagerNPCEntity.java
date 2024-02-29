package com.jackdaw.chatwithnpc.npc;

import com.jackdaw.chatwithnpc.prompt.Builder;
import com.jackdaw.chatwithnpc.prompt.Prompt;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class VillagerNPCEntity extends NPCEntity {

    /**
     * This is a constructor used to initialize the NPC with the entity.
     *
     * @param entity The entity of the NPC.
     */
    public VillagerNPCEntity(@NotNull VillagerEntity entity) {
        super(entity);
    }

    @Override
    public void replyMessage(String message, PlayerEntity player) {
        player.sendMessage(Text.of("<" + this.getName() + "> " + message));
    }

    @Override
    public void doAction(Actions action, PlayerEntity player) {
        // TODO: 不在这一版本的考虑范围中
    }

}
