package com.jackdaw.chatwithnpc.mixin;

import com.jackdaw.chatwithnpc.listener.PlayerSendMessageCallback;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class PlayerSendMessageMixin {
	@Inject(at = @At("TAIL"), method = "broadcast(Lnet/minecraft/network/message/SignedMessage;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V", cancellable = true)
	private void onSend(SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params, CallbackInfo ci) {
		ActionResult result = PlayerSendMessageCallback.EVENT.invoker().interact(sender, message.getContent().getString());

		if(result == ActionResult.FAIL) {
			ci.cancel();
		}
	}
}