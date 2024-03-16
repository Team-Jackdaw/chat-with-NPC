package com.jackdaw.chatwithnpc.group;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to manage the group of the game.
 */
public class GroupManager {
    // The time in milliseconds that a group is considered out of time
    private static final long outOfTime = ChatWithNPCMod.outOfTime;
    public static final ConcurrentHashMap<String, Group> GroupMap = new ConcurrentHashMap<>();

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
        Group current = GroupMap.get(name);
        current.autoDeleteTempEvent();
        current.getDataManager().save();
        GroupMap.remove(name);
    }

    public static @NotNull Group getGroup(String name) {
        if (!isLoaded(name)) {
            loadEnvironment(name);
        }
        Group group = GroupMap.get(name);
        group.updateLastLoadTime(System.currentTimeMillis());
        return group;
    }

    public static void endOutOfTimeEnvironments() {
        if (GroupMap.isEmpty()) return;
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
        if (GroupMap.isEmpty()) return;
        GroupMap.forEach((name, environment) -> removeEnvironment(name));
    }

    /**
     * Get the parent groups of the group. Include the group itself.
     *
     * @param currentGroup the parent group of the group.
     * @return parentGroups the parent groups of the group.
     */
    public static @NotNull ArrayList<Group> getParentGroups(String currentGroup) {
        Group current = GroupManager.getGroup(currentGroup);
        ArrayList<Group> parentGroups = new ArrayList<>();
        parentGroups.add(current);
        String parentGroup = current.getParentGroup();
        if (parentGroup != null) {
            parentGroups.addAll(GroupManager.getParentGroups(parentGroup));
        }
        return parentGroups;
    }

    public static ArrayList<String> getGroupList() {
        File workingDirectory = ChatWithNPCMod.workingDirectory.resolve("group").toFile();
        File[] files = workingDirectory.listFiles();
        ArrayList<String> groupList = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                if (name.endsWith(".json")) {
                    groupList.add(name.substring(0, name.length() - 5));
                }
            }
        }
        return groupList;
    }
}
