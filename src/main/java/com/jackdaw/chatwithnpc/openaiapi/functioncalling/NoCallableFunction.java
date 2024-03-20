package com.jackdaw.chatwithnpc.openaiapi.functioncalling;

import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class NoCallableFunction extends CustomFunction{
    NoCallableFunction(String description, Map<String, String> properties) {
        this.description = description;
        this.properties = properties;
    }

    public Map<String, String> execute(@NotNull ConversationHandler conversation, @NotNull Map<String, String> args) {
        Map<String, String> ok = Map.of("status", "success");
        if (args.isEmpty()) return ok;
        Entity entity = conversation.getNpc().getEntity();
        // add the args to the entity's NBT data
        NbtCompound nbt = entity.writeNbt(new NbtCompound());
        for (Map.Entry<String, String> entry : args.entrySet()) {
            nbt.putString(entry.getKey(), entry.getValue());
        }
        entity.readNbt(nbt);
        return ok;
    }
}
