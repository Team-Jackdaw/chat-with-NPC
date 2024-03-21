package com.jackdaw.chatwithnpc.openaiapi.functioncalling.functionset;

import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import com.jackdaw.chatwithnpc.openaiapi.functioncalling.CustomFunction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class GiveDiamondFunction extends CustomFunction {

    public GiveDiamondFunction() {
        description = "This function is used to give player a diamond. If the player make a request to the NPC to give them some diamonds, this function will be called.";
        properties = Map.of(
                "number", "the number of diamonds to give to the player."
        );
    }

    @Override
    public Map<String, String> execute(@NotNull ConversationHandler conversation, @NotNull Map<String, String> args) {
        int number;
        try{
            number = Integer.parseInt(args.get("number"));
        } catch (NumberFormatException e) {
            number = 1;
        }
        ItemStack diamond = new ItemStack(Items.DIAMOND, number);
        conversation.getNpc().findNearbyPlayers(10).forEach(player -> player.giveItemStack(diamond));
        return Map.of("status", "success");
    }
}
