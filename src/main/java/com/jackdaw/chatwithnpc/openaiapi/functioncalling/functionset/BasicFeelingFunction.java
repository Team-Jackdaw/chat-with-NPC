package com.jackdaw.chatwithnpc.openaiapi.functioncalling.functionset;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import com.jackdaw.chatwithnpc.npc.Actions;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.jackdaw.chatwithnpc.openaiapi.functioncalling.CustomFunction;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BasicFeelingFunction extends CustomFunction {
    public BasicFeelingFunction() {
        description = "This function is used to express your feelings. The felling only includes `sad`, `happy`";
        properties = Map.of(
                "felling", "This is the your feeling. You can only select `SHAKE_HEAD` or `FEEL_HAPPY` as the value."
        );
    }

    @Override
    public Map<String, String> execute(@NotNull ConversationHandler conversation, @NotNull Map<String, String> args) {
        String action = args.get("action");
        try {
            Actions actionEnum = Actions.valueOf(action.toUpperCase());
            NPCEntity npc = conversation.getNpc();
            PlayerEntity player = npc.getEntity().getEntityWorld().getClosestPlayer(npc.getEntity(), 10);
            if (player == null) throw new Exception("no player nearby");
            npc.doAction(actionEnum, player);
        } catch (Exception e) {
            ChatWithNPCMod.LOGGER.error("Failed to execute the action: " + action + e);
            return Map.of("status", "failed, because the action is not valid");
        }
        return Map.of("status", "success, you `" + action + "`");
    }
}
