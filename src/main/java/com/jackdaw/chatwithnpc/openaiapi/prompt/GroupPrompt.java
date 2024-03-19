package com.jackdaw.chatwithnpc.openaiapi.prompt;

import com.jackdaw.chatwithnpc.group.Group;
import com.jackdaw.chatwithnpc.group.GroupManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class GroupPrompt {
    public static @NotNull String getGroupsPrompt(String group) {
        StringBuilder groupsPrompt = new StringBuilder();
        List<Group> groups = GroupManager.getParentGroups(group);
        for (Group aGroup : groups) {
            // connect the group.permanentPrompt to the groupPrompt with ",";
            StringBuilder prompt = new StringBuilder();
            if (aGroup.getName().equals("Global")) {
                prompt.append("The overall environment is ");
            } else {
                prompt.append("You live in(/belongs to/are member of) `").append(aGroup.getName()).append("` where is ");
            }
            prompt.append(String.join(", ", aGroup.getPermanentPrompt()));
            if (!aGroup.getTempEvent().isEmpty()) {
                prompt.append(" and happening ");
                for (Map<Long, String> event : aGroup.getTempEvent()) {
                    prompt.append(event.values().iterator().next());
                    if (aGroup.getTempEvent().indexOf(event) != aGroup.getTempEvent().size() - 1) {
                        prompt.append(", ");
                    }
                }
            }
            prompt.append(". ");
            groupsPrompt.append(prompt);
        }
        return groupsPrompt.toString();
    }
}
