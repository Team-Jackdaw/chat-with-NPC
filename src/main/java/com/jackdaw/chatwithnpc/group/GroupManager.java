package com.jackdaw.chatwithnpc.group;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.data.DataManager;

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
        Group group;
        if (name.equals("Global")) {
            group = new GlobalGroup();
        } else {
            group = new LocalGroup(name);
        }
        DataManager groupDataManager = group.getDataManager();
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
}
