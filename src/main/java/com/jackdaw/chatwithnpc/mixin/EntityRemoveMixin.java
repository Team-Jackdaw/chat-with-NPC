package com.jackdaw.chatwithnpc.mixin;

import com.jackdaw.chatwithnpc.conversation.ConversationManager;
import com.jackdaw.chatwithnpc.npc.NPCEntityManager;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(Entity.class)
public abstract class EntityRemoveMixin {
    @Shadow public abstract UUID getUuid();

    @Inject(method = "remove", at = @At("HEAD"))
    private void onRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        if (NPCEntityManager.isRegistered(this.getUuid())) {
            if (ConversationManager.isConversing(this.getUuid())) {
                ConversationManager.endConversation(this.getUuid());
            }
            NPCEntityManager.removeNPCEntity(this.getUuid());
        }
    }
}
