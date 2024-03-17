package com.jackdaw.chatwithnpc.openaiapi.prompt;

import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import org.jetbrains.annotations.NotNull;

public class NPCPrompt {

    public static @NotNull String description(@NotNull NPCEntity npc) {
        return  "You are an NPC with type `" + npc.getType() + "` and named `" + npc.getName() + "`. " +
                "You career is `" + npc.getCareer() + "`. " + npc.getBasicPrompt();
    }

    public static @NotNull String instructions(@NotNull NPCEntity npc) {
        return npc.getInstructions() + "You can only use `" + SettingManager.language + "` language to communicate. Your word limit is 30.";
    }
}
