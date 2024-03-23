package com.jackdaw.chatwithnpc.openaiapi.prompt;

import com.jackdaw.chatwithnpc.SettingManager;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import org.jetbrains.annotations.NotNull;

public class NPCPrompt {

    /**
     * Get the prompt for the NPC
     *
     * @param npc the NPC
     * @return the prompt
     */
    public static @NotNull String instructions(@NotNull NPCEntity npc) {
        return "You are an NPC with type `" + npc.getType() + "` and named `" + npc.getName() + "`. " +
                "You career is `" + npc.getCareer() + "`. " + npc.getInstructions() +
                GroupPrompt.getGroupsPrompt(npc.getGroup()) +
                "You can only use `" + SettingManager.language + "` language to communicate. Your word limit is 30.";
    }
}
