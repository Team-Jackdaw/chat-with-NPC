package com.jackdaw.chatwithnpc.openaiapi.functioncalling.functionset;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import com.jackdaw.chatwithnpc.npc.Actions;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.jackdaw.chatwithnpc.openaiapi.functioncalling.CustomFunction;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BasicActionFunction extends CustomFunction {
    public BasicActionFunction() {
        description = "This function is used to let the NPC do some basic actions. The actions only includes `walk to player`, `escape`";
        properties = Map.of(
                "action", "This is the action of the NPC. You can only select `WALK_TO_PLAYER` or `ESCAPE` as the value."
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
