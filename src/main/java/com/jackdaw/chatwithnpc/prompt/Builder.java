package com.jackdaw.chatwithnpc.prompt;

import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.group.Group;
import com.jackdaw.chatwithnpc.group.GroupEvent;
import com.jackdaw.chatwithnpc.group.GroupManager;
import com.jackdaw.chatwithnpc.npc.NPCEntityManager;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.jackdaw.chatwithnpc.npc.Record;

import java.util.ArrayList;

public class Builder {
    String npcName = "NPC";
    String npcGroup = "Global";
    String npcType = "Minecraft:villager";
    String npcCareer = "unemployed";
    String npcBasicPrompt = "I'm an NPC.";

    public Builder setNpc(String npcName) {
        NPCEntity npc = NPCEntityManager.getNPCEntity(npcName);
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
        String historyMessage = buildHistoryMessage();
        return new Prompt(systemMessage, historyMessage);
    }

    String buildSystemMessage() {
        String npcbasicPrompt = "You are an NPC with type `" + npcType + "` and named `" + npcName + "`. ";
        String groupInitialPrompt = "You are living in places as listed: ";
        ArrayList<String> groupPrompt = new ArrayList<>();
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
                for (GroupEvent event : group.getTempEvent()) {
                    prompt.append(event.getEvent());
                    if (!group.getTempEvent().last().equals(event)) {
                        prompt.append(", ");
                    }
                }
            }
            prompt.append(". ");
            groupPrompt.add(prompt.toString());
        }
        String languagePrompt = "Please use `" + SettingManager.language + "` language to continue the conversation. ";
        return npcbasicPrompt + groupInitialPrompt + String.join("", groupPrompt) + languagePrompt;
    }

    String buildHistoryMessage() {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("This is the history of our conversation: ");
        for (Record.Message message : NPCEntityManager.getNPCEntity(npcName).getMessageRecord().getTreeMap().values()) {
            if (message.role == Record.Role.NPC) {
                messageBuilder.append("You said: " + message.getMessage() + ". ");
            } else {
                messageBuilder.append("You heard: " + message.getMessage() + ". ");
            }
        }
        return messageBuilder.toString();
    }
}
