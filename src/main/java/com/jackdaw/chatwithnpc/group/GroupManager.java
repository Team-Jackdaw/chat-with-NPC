package com.jackdaw.chatwithnpc.group;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used to manage the groups of the game.
 */
public class GroupManager {
    public static final ConcurrentHashMap<String, Group> GroupMap = new ConcurrentHashMap<>();
    // The time in milliseconds that a group is considered out of time
    private static final long outOfTime = ChatWithNPCMod.outOfTime;

    public static boolean isLoaded(String name) {
        return GroupMap.containsKey(name);
    }


    /**
     * Initialize a group if the group is not loaded.
     *
     * @param name The name of the group
     */
    public static void loadGroup(String name, boolean canCreate) {
        if (isLoaded(name)) return;
        Group group = new Group(name);
        GroupDataManager groupDataManager = group.getDataManager();
        if (!groupDataManager.isExist() && !canCreate && !name.equals("Global")) return;
        groupDataManager.sync();
        GroupMap.put(name, group);
    }

    /**
     * Discard a group from the group map.
     * @param name The name of the group
     */
    public static void discardGroup(String name) {
        Group current = GroupMap.get(name);
        current.getDataManager().save();
        GroupMap.remove(name);
    }

    /**
     * Get the group by the name.
     * @param name The name of the group
     * @return The group
     */
    public static @Nullable Group getGroup(String name) {
        if (!isLoaded(name)) {
            loadGroup(name, false);
        }
        Group group = GroupMap.get(name);
        if (group == null) return null;
        group.updateLastLoadTime(System.currentTimeMillis());
        return group;
    }

    /**
     * Discard the groups that have not been load for a long time.
     */
    public static void endOutOfTimeGroup() {
        if (GroupMap.isEmpty()) return;
        GroupMap.forEach((name, environment) -> {
            if (environment.getName().equals("Global")) {
                return;
            }
            if (environment.getLastLoadTime() + outOfTime < System.currentTimeMillis()) {
                environment.getDataManager().save();
                discardGroup(name);
            }
        });
    }

    /**
     * Discard all the groups.
     */
    public static void endAllGroup() {
        if (GroupMap.isEmpty()) return;
        GroupMap.forEach((name, environment) -> discardGroup(name));
    }

    /**
     * Get all the parent groups of the group. Include the group itself.
     *
     * @param currentGroup the parent group of the group.
     * @return parentGroups the parent groups of the group.
     */
    public static @NotNull ArrayList<Group> getParentGroups(@NotNull String currentGroup) {
        ArrayList<String> visited = new ArrayList<>();
        return getParentGroups(currentGroup, visited);
    }

    private static @NotNull ArrayList<Group> getParentGroups(@NotNull String currentGroup, @NotNull ArrayList<String> visited) {
        ArrayList<Group> parentGroups = new ArrayList<>();
        Group current = GroupManager.getGroup(currentGroup);
        if (current == null) return parentGroups;
        parentGroups.add(current);
        String parentGroup = current.getParentGroup();
        if (parentGroup != null && !visited.contains(parentGroup)) {
            visited.add(currentGroup);
            parentGroups.addAll(GroupManager.getParentGroups(parentGroup, visited));
        }
        return parentGroups;
    }

    /**
     * Get the list of the groups from the files.
     * @return The list of the groups.
     */
    public static @NotNull ArrayList<String> getGroupList() {
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

    /**
     * Add a member to the group, and all the parent groups.
     * @param groupName The name of the group
     * @param member The name of the member
     */
    public static void addGroupMember(String groupName, String member) {
        ArrayList<Group> groups = getParentGroups(groupName);
        groups.forEach(group -> group.addMember(member));
    }

    /**
     * Remove a member from the group, and all the parent groups.
     * @param groupName The name of the group
     * @param member The name of the member
     */
    public static void removeGroupMember(String groupName, String member) {
        ArrayList<Group> groups = getParentGroups(groupName);
        groups.forEach(group -> group.removeMember(member));
    }

    /**
     * Set the parent group of the group. And move all the members to the new parent group.
     * @param groupName The name of the group
     * @param newParentName The name of the new parent group
     */
    public static void setGroupParent(String groupName, String newParentName) {
        Group group = getGroup(groupName);
        if (group == null) return;
        if (!getGroupList().contains(newParentName) || groupName.equals(newParentName) || getParentGroups(newParentName).contains(group)) return;
        ArrayList<String> groupMembers = group.getMemberList();
        groupMembers.forEach(member -> {
            removeGroupMember(group.getParentGroup(), member);
            addGroupMember(newParentName, member);
        });
        group.setParentGroup(newParentName);
    }

    /**
     * Get the prompt of the group and all the parent groups.
     *
     * @param group the group name
     * @return the prompt of the group
     */
    public static @NotNull String getGroupsPrompt(String group) {
        StringBuilder groupsPrompt = new StringBuilder();
        ArrayList<Group> groups = GroupManager.getParentGroups(group);
        if (groups.isEmpty()) return "";
        for (Group aGroup : groups) {
            StringBuilder prompt = new StringBuilder();
            if (aGroup.getName().equals("Global")) {
                prompt.append("The overall environment is ");
            } else {
                prompt.append("You live in(/belongs to/are member of) `").append(aGroup.getName()).append("` where is ");
            }
            prompt.append(aGroup.getInstruction());
            if (!aGroup.getEvent().isEmpty()) {
                prompt.append(" and happening ");
                for (String event : aGroup.getEvent()) {
                    prompt.append(event);
                    if (aGroup.getEvent().indexOf(event) != aGroup.getEvent().size() - 1) prompt.append(", ");
                    else prompt.append(". ");
                }
            }
            groupsPrompt.append(prompt);
        }
        return groupsPrompt.toString();
    }

    /**
     * Get the group tree.
     * @param root The root of the tree
     * @return The group tree
     */
    public static @NotNull String getGroupTree(String root) {
        GroupToTree groupToTree = new GroupToTree();
        List<Group> groupList = getGroupList().stream().map(GroupManager::getGroup).filter(Objects::nonNull).toList();
        ArrayList<Group> visited = new ArrayList<>();
        groupList.forEach(group -> {
            if (group.getParentGroup() != null) {
                if (visited.contains(group)) return;
                groupToTree.addEdge(group.getParentGroup(), group.getName());
                visited.add(group);
            }
        });
        return TreeNode.toString(groupToTree.generateTree(root), 0);
    }

    static class GroupToTree {
        final Map<String, List<String>> graph = new HashMap<>();

        void addEdge(String source, String destination) {
            graph.computeIfAbsent(source, x -> new ArrayList<>()).add(destination);
            graph.computeIfAbsent(destination, x -> new ArrayList<>()).add(source);
        }

        TreeNode generateTree(String root) {
            return buildTree(root, new HashSet<>());
        }

        @NotNull TreeNode buildTree(String node, @NotNull Set<String> visited) {
            visited.add(node);
            TreeNode treeNode = new TreeNode(node);
            List<TreeNode> children = new ArrayList<>();
            if (graph.containsKey(node)) {
                for (String neighbor : graph.get(node)) {
                    if (!visited.contains(neighbor)) {
                        children.add(buildTree(neighbor, visited));
                    }
                }
            }
            treeNode.children = children;
            return treeNode;
        }
    }

    static class TreeNode {
        String val;
        List<TreeNode> children;

        TreeNode(String val) {
            this.val = val;
            this.children = new ArrayList<>();
        }

        static @NotNull String toString(TreeNode node, int level) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < level; i++) {
                if (i == level - 1)
                    sb.append("└── ");
                else
                    sb.append("│   ");
            }
            sb.append(node.val).append("\n");
            for (TreeNode child : node.children) {
                sb.append(toString(child, level + 1));
            }
            return sb.toString();
        }
    }

}
