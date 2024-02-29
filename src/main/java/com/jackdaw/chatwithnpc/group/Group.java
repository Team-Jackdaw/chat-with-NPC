package com.jackdaw.chatwithnpc.group;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;

public class Group {
    private final String name;

    private String parentGroup = "Global";

    private final ArrayList<String> permanentPrompt = new ArrayList<>(Arrays.asList("good weather", "very save"));

    private final TreeSet<GroupEvent> tempEvent = new TreeSet<>();

    private long lastLoadTime = System.currentTimeMillis();

    Group(@NotNull String name) {
        this.name = name;
        if (name.equals("Global")) {
            parentGroup = null;
        }
    }

    public String getName() {
        return name;
    }

    public void setParentGroup(String parentGroup) {
        this.parentGroup = parentGroup;
    }

    public String getParentGroup() {
        return parentGroup;
    }

    public long getLastLoadTime() {
        return lastLoadTime;
    }

    public void updateLastLoadTime(long time) {
        lastLoadTime = time;
    }

    public void setTempEvent(TreeSet<GroupEvent> tempEvent) {
        this.tempEvent.clear();
        this.tempEvent.addAll(tempEvent);
    }
    
    public void addTempEvent(String event, long time) {
        long now = System.currentTimeMillis();
        tempEvent.add(new GroupEvent(event, now, now + time));
    }

    public TreeSet<GroupEvent> getTempEvent() {
        return new TreeSet<>(tempEvent);
    }

    public void addPermanentPrompt(String prompt) {
        permanentPrompt.add(prompt);
    }

    public TreeSet<String> getPermanentPrompt() {
        return new TreeSet<>(permanentPrompt);
    }

    public void setPermanentPrompt(ArrayList<String> permanentPrompt) {
        this.permanentPrompt.clear();
        this.permanentPrompt.addAll(permanentPrompt);
    }
    
    public GroupDataManager getDataManager() {
        return new GroupDataManager(this);
    }
}
