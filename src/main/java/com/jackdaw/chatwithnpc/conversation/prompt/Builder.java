package com.jackdaw.chatwithnpc.conversation.prompt;

import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import com.jackdaw.chatwithnpc.conversation.Record;
import com.jackdaw.chatwithnpc.group.Group;
import com.jackdaw.chatwithnpc.group.GroupManager;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Builder {

    final ArrayList<Map<String, String>> messages = new ArrayList<>();
    private final StringBuilder systemMessage = new StringBuilder();

    @Contract(pure = true)
    static @NotNull String role2String(Record.Role role) {
        if (role == Record.Role.NPC) return "assistant";
        if (role == Record.Role.PLAYER) return "user";
        return "system";
    }

    public Builder addMessage(@NotNull Record.Role role, @NotNull String content) {
        this.messages.add(Map.of("role", role2String(role), "content", content));
        return this;
    }

    public Builder fromNPC(@NotNull NPCEntity npc) {
        ArrayList<Map<Long, String>> longTermMemory = npc.getLongTermMemory();
        return addInitialMessage(npc.getName(), npc.getType(), npc.getCareer(), npc.getBasicPrompt())
                .addGroupMessages(npc.getGroup())
                .addLongTermMemoryMessages(longTermMemory);
    }

    public Builder fromConversation(@NotNull ConversationHandler conversation) {
        Record messageRecord = conversation.getMessageRecord();
        if (messageRecord == null || messageRecord.isEmpty()) {
            return fromNPC(conversation.getNpc()).addInitialConversationMessage(conversation.getNpc().getName());
        } else {
            return fromNPC(conversation.getNpc()).addMessageRecordMessages(messageRecord);
        }
    }

    public Builder addInitialMessage(String npcName, String npcType, String npcCareer, String npcBasicPrompt) {
        String basicInfo = "You are an NPC with type `" + npcType + "` and named `" + npcName + "`. ";
        String npcCareerPrompt = "You career is `" + npcCareer + "`. ";
        String languagePrompt = "You can only use `" + SettingManager.language + "` language to communicate. ";
        this.systemMessage.append(basicInfo).append(npcCareerPrompt).append(npcBasicPrompt).append(languagePrompt);
        this.messages.add(Map.of("role", "system", "content", basicInfo + npcCareerPrompt + npcBasicPrompt + languagePrompt));
        return this;
    }

    public Builder addGroupMessages(String npcGroup) {
        List<String> groups = GroupManager.getParentGroups(npcGroup).stream().map(Group::getName).toList();
        for (String aGroup : groups) {
            Group group = GroupManager.getGroup(aGroup);
            // connect the group.permanentPrompt to the groupPrompt with ",";
            StringBuilder prompt = new StringBuilder();
            if (group.getName().equals("Global")) {
                prompt.append("The overall environment is ");
            } else {
                prompt.append("You live in(/belongs to/are member of) `").append(group.getName()).append("` where is ");
            }
            prompt.append(String.join(", ", group.getPermanentPrompt()));
            if (!group.getTempEvent().isEmpty()) {
                prompt.append(" and happening ");
                for (Map<Long, String> event : group.getTempEvent()) {
                    prompt.append(event.values().iterator().next());
                    if (group.getTempEvent().indexOf(event) != group.getTempEvent().size() - 1) {
                        prompt.append(", ");
                    }
                }
            }
            prompt.append(". ");
            this.messages.add(Map.of("role", "system", "content", prompt.toString()));
        }
        return this;
    }

    public Builder addLongTermMemoryMessages(ArrayList<Map<Long, String>> longTermMemory) {
        if (longTermMemory == null || longTermMemory.isEmpty()) return this;
        for (Map<Long, String> memory : longTermMemory) {
            for (Map.Entry<Long, String> entry : memory.entrySet()) {
                this.messages.add(Map.of("role", "system", "content", "This is a summary of one of the previous conversations: " + entry.getValue()));
            }
        }
        return this;
    }

    public Builder addMessageRecordMessages(Record messageRecord) {
        if (messageRecord == null || messageRecord.isEmpty()) {
            return this;
        }
        for (Record.Message message : messageRecord.getTreeMap().values()) {
            if (message.getEntityName() != null) {
                this.messages.add(Map.of("role", role2String(message.getRole()), "content", message.getMessage(), "name", message.getEntityName()));
            } else {
                this.messages.add(Map.of("role", role2String(message.getRole()), "content", message.getMessage()));
            }
        }
        return this;
    }

    public Builder addInitialConversationMessage(String npcName) {
        this.messages.add(Map.of("role", "system", "content", "Please start the conversation as " + npcName + " with a greeting."));
        return this;
    }

    public Prompt build() {
        return new Prompt(this.systemMessage.toString(), this.messages);
    }

}
