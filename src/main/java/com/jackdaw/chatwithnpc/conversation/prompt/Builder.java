package com.jackdaw.chatwithnpc.conversation.prompt;

import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.group.Group;
import com.jackdaw.chatwithnpc.group.GroupManager;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.jackdaw.chatwithnpc.npc.NPCEntityManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

public class Builder {
    String npcName = "NPC";
    String npcGroup = "Global";
    String npcType = "Minecraft:villager";
    String npcCareer = "unemployed";
    String npcBasicPrompt = "You are an NPC.";
    ArrayList<Map<Long, String>> longTermMemory = new ArrayList<>();

    public Builder setNpc(UUID npcUUID) {
        NPCEntity npc = NPCEntityManager.getNPCEntity(npcUUID);
        setNpc(npc);
        return this;
    }

    public Builder setNpc(@NotNull NPCEntity npc) {
        this.npcName = npc.getName();
        this.npcGroup = npc.getGroup();
        this.npcType = npc.getType();
        this.npcCareer = npc.getCareer();
        this.npcBasicPrompt = npc.getBasicPrompt();
        this.longTermMemory = npc.getLongTermMemory();
        return this;
    }

    public Prompt build() {
        String systemMessage = buildSystemMessage();
        return new Prompt(systemMessage);
    }

    String buildSystemMessage() {
        String npcbasicPrompt = "You are an NPC with type `" + npcType + "` and named `" + npcName + "`. ";
        String npcCareerPrompt = "You career is `" + npcCareer + "`. ";
        ArrayList<String> groupPrompt = new ArrayList<>();
        for (Group group : GroupManager.getParentGroups(npcGroup)) {
            // connect the group.permanentPrompt to the groupPrompt with ",";
            StringBuilder prompt = new StringBuilder();
            if (group.getName().equals("Global")) {
                prompt.append("The overall environment is ");
            } else {
                prompt.append("You living in(/belongs to/are member of) `").append(group.getName()).append("` where is ");
            }
            prompt.append(String.join(", ", group.getPermanentPrompt()));
            if (!group.getTempEvent().isEmpty()) {
                prompt.append(" and happening ");
                for (Map<Long, String> event : group.getTempEvent()) {
                    prompt.append(event.values().iterator().next());
                    if (group.getTempEvent().indexOf(event) != group.getTempEvent().size() - 1){
                        prompt.append(", ");
                    }
                }
            }
            prompt.append(". ");
            groupPrompt.add(prompt.toString());
        }
        String languagePrompt = "Please use `" + SettingManager.language + "` language to communicate.";
        return npcbasicPrompt + npcCareerPrompt + this.npcBasicPrompt + String.join("", groupPrompt) + languagePrompt;
    }
}
