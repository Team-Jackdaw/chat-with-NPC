package com.jackdaw.chatwithnpc.prompt;

import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.group.Group;
import com.jackdaw.chatwithnpc.group.GroupManager;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.jackdaw.chatwithnpc.npc.NPCEntityManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

public class Builder {
    String npcName = "NPC";
    String npcGroup = "Global";
    String npcType = "Minecraft:villager";
    String npcCareer = "unemployed";
    String npcBasicPrompt = "I'm an NPC.";

    public Builder setNpc(String npcName) {
        NPCEntity npc = NPCEntityManager.getNPCEntity(npcName);
        setNpc(npc);
        return this;
    }

    public Builder setNpc(@NotNull NPCEntity npc) {
        this.npcName = npc.getName();
        this.npcGroup = npc.getGroup();
        this.npcType = npc.getType();
        this.npcCareer = npc.getCareer();
        this.npcBasicPrompt = npc.getBasicPrompt();
        return this;
    }

    public Builder setNpcName(String npcName) {
        this.npcName = npcName;
        return this;
    }

    public Builder setNpcCareer(String npcCareer) {
        this.npcCareer = npcCareer;
        return this;
    }

    public Builder setNpcBasicPrompt(String npcBasicPrompt) {
        this.npcBasicPrompt = npcBasicPrompt;
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
        groupPrompt.add("You are living in places as listed: ");
        for (Group group : GroupManager.getParentGroups(npcGroup)) {
            // connect the group.permanentPrompt to the groupPrompt with ",";
            StringBuilder prompt = new StringBuilder();
            if (group.getName().equals("Global")) {
                prompt.append("The global environment where is ");
            } else {
                prompt.append("`").append(group.getName()).append("` where is ");
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
        String languagePrompt = "Please use `" + SettingManager.language + "` language to continue the conversation. ";
        return npcbasicPrompt + npcCareerPrompt + String.join("", groupPrompt) + languagePrompt;
    }
}
