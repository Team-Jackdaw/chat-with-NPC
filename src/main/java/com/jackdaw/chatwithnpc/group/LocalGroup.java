package com.jackdaw.chatwithnpc.group;

import com.jackdaw.chatwithnpc.data.GroupDataManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class LocalGroup implements Group {
    private final String name;

    private String parentGroup = "Global";

    private final ArrayList<String> permanentPrompt = new ArrayList<>(Arrays.asList("good weather", "very save"));

    private final TreeSet<GroupEvent> tempEvent = new TreeSet<>();

    private long lastLoadTime = System.currentTimeMillis();

    LocalGroup(@NotNull String name) {
        this.name = name;
        if (name.equals("Global")) {
            parentGroup = null;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setParentGroup(String parentGroup) {
        this.parentGroup = parentGroup;
    }

    @Override
    public Group getParentGroup() {
        return GroupManager.getGroup(parentGroup);
    }

    @Override
    public ArrayList<Group> getParentGroups() {
        ArrayList<Group> subGroup = new ArrayList<>();
        subGroup.add(this);
        if (parentGroup != null) {
            subGroup.addAll(GroupManager.getGroup(parentGroup).getParentGroups());
        }
        return subGroup;
    }

    @Override
    public long getLastLoadTime() {
        return lastLoadTime;
    }

    @Override
    public void updateLastLoadTime(long time) {
        lastLoadTime = time;
    }

    @Override
    public void setTempEvent(TreeSet<GroupEvent> tempEvent) {
        this.tempEvent.clear();
        this.tempEvent.addAll(tempEvent);
    }

    @Override
    public void addTempEvent(String event, long time) {
        long now = System.currentTimeMillis();
        tempEvent.add(new GroupEvent(event, now, now + time));
    }

    @Override
    public TreeSet<GroupEvent> getTempEvent() {
        return new TreeSet<>(tempEvent);
    }

    @Override
    public void addPermanentPrompt(String prompt) {
        permanentPrompt.add(prompt);
    }

    @Override
    public TreeSet<String> getPermanentPrompt() {
        return new TreeSet<>(permanentPrompt);
    }

    @Override
    public void setPermanentPrompt(ArrayList<String> permanentPrompt) {
        this.permanentPrompt.clear();
        this.permanentPrompt.addAll(permanentPrompt);
    }

    @Override
    public GroupDataManager getDataManager() {
        return new GroupDataManager(this);
    }
}
