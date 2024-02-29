package com.jackdaw.chatwithnpc.group;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is used to manage the group of the game.
 */
public class GroupManager {
    // The time in milliseconds that an group is considered out of time
    private static final long outOfTime = ChatWithNPCMod.outOfTime;
    public static final HashMap<String, Group> GroupMap = new HashMap<>();

    public static boolean isLoaded(String name) {
        return GroupMap.containsKey(name);
    }


    /**
     * Initialize a group if the group is not conversing.
     * @param name The name of the group
     */
    public static void loadEnvironment(String name) {
        if (isLoaded(name)) {
            return;
        }
        Group group = new Group(name);
        GroupDataManager groupDataManager = group.getDataManager();
        groupDataManager.sync();
        GroupMap.put(name, group);
    }

    public static void removeEnvironment(String name) {
        GroupMap.get(name).getDataManager().save();
        GroupMap.remove(name);
    }

    public static Group getGroup(String name) {
        if (!isLoaded(name)) {
            loadEnvironment(name);
        }
        return GroupMap.get(name);
    }

    public static void endOutOfTimeEnvironments() {
        GroupMap.forEach((name, environment) -> {
            if (environment.getName().equals("Global")) {
                return;
            }
            if (environment.getLastLoadTime() + outOfTime < System.currentTimeMillis()) {
                environment.getDataManager().save();
                removeEnvironment(name);
            }
        });
    }

    public static void endAllEnvironments() {
        GroupMap.forEach((name, environment) -> {
            removeEnvironment(name);
        });
    }

    /**
     * Get the parent groups of the group. Include the group itself.
     *
     * @param currentGroup the parent group of the group.
     * @return parentGroups the parent groups of the group.
     */
    public static ArrayList<Group> getParentGroups(String currentGroup) {
        Group current = GroupManager.getGroup(currentGroup);
        ArrayList<Group> parentGroups = new ArrayList<>();
        parentGroups.add(current);
        String parentGroup = current.getParentGroup();
        if (parentGroup != null) {
            parentGroups.addAll(GroupManager.getParentGroups(parentGroup));
        }
        return parentGroups;
    }
}
