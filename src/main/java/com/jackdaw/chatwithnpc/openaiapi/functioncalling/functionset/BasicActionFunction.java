package com.jackdaw.chatwithnpc.openaiapi.functioncalling.functionset;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import com.jackdaw.chatwithnpc.npc.npcentityset.Actions;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.jackdaw.chatwithnpc.openaiapi.functioncalling.CustomFunction;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class BasicActionFunction extends CustomFunction {
    public BasicActionFunction() {
        description = "This function is used to express your movement. You can `walk to player` if you want, or `escape` if you think you are in danger.";
        properties = Map.of(
                "action", Map.of(
                        "description", "This is your movement.",
                        "enum", List.of("WALK_TO_PLAYER", "ESCAPE")
                )
        );
    }

    @Override
    public Map<String, String> execute(@NotNull ConversationHandler conversation, @NotNull Map<String, String> args) {
        String action = args.get("action");
        try {
            NPCEntity npc = conversation.getNpc();
            PlayerEntity player = npc.getEntity().getEntityWorld().getClosestPlayer(npc.getEntity(), 10);
            npc.doAction(Actions.valueOf(action), player);
        } catch (Exception e) {
            ChatWithNPCMod.LOGGER.error("Failed to execute the action: " + action + e);
            return Map.of("status", "failed");
        }
        return Map.of("status", "success, you `" + action + "`");
    }
}
