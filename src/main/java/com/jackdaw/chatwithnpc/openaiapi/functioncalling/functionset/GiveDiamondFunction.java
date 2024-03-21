package com.jackdaw.chatwithnpc.openaiapi.functioncalling.functionset;

import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import com.jackdaw.chatwithnpc.openaiapi.functioncalling.CustomFunction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class GiveDiamondFunction extends CustomFunction {

    public GiveDiamondFunction() {
        description = "This function is used to give player a diamond. You can give player diamonds if you want.";
        properties = Map.of(
                "number", Map.of("description", "the number of diamonds to give to the player.")
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
