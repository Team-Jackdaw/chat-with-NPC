package com.jackdaw.chatwithnpc.openaiapi.function;

import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import com.jackdaw.chatwithnpc.group.Group;
import com.jackdaw.chatwithnpc.group.GroupManager;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class QueryGroupFunction extends CustomFunction{
    public QueryGroupFunction() {
        description = "Get the information of a place or a group.";
        properties = Map.of(
                "groupName", Map.of(
                        "type", "string",
                        "description", "The place or group of to query",
                        "enum", GroupManager.getGroupList()
                )
        );
        required = new String[]{"groupName"};
    }
    @Override
    public Map<String, String> execute(@NotNull ConversationHandler conversation, @NotNull Map<String, Object> args) {
        String groupName = (String) args.get("groupName");
        NPCEntity npc = conversation.getNpc();
        List<String> groupList = GroupManager.getParentGroups(npc.getGroup()).stream().map(Group::getName).toList();
        if (!groupList.contains(groupName)) return Map.of("error", "You can only query the information of the place or group you are in.");
        String instruction = GroupManager.getGroupsPrompt(groupName);
        if (instruction == null) return Map.of("error", "Group not found");
        return Map.of("instruction", instruction);
    }
}
