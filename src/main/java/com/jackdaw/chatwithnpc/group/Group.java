package com.jackdaw.chatwithnpc.group;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeSet;

/**
 * <b>Group of NPC</b>
 * <p>
 * The group will record the permanent prompt and temporary event and its parent group.
 * <p>
 * The permanent prompt and the events will be known by all the members of the group.
 * <p>
 * Every group will have parents until the parent is "Global".
 *
 * @version 1.0
 */
public class Group {
    private final String name;
    private final ArrayList<String> permanentPrompt = new ArrayList<>(Arrays.asList("good weather", "very save"));
    private final ArrayList<Map<Long, String>> tempEvent = new ArrayList<>();
    private String parentGroup = "Global";
    private long lastLoadTime;

    Group(@NotNull String name) {
        this.name = name;
        if (name.equals("Global")) {
            parentGroup = null;
        }
        lastLoadTime = System.currentTimeMillis();
    }

    /**
     * Get the name of the group
     * @return the name of the group
     */
    public String getName() {
        return name;
    }

    /**
     * Get the name of the parent group
     * @return the name of the parent group
     */
    public String getParentGroup() {
        return parentGroup;
    }

    /**
     * Set the name of the parent group
     * @param parentGroup the name of the parent group
     */
    public void setParentGroup(String parentGroup) {
        this.parentGroup = parentGroup;
    }

    /**
     * Get the last load time of the group
     * @return the last load time of the group
     */
    public long getLastLoadTime() {
        return lastLoadTime;
    }

    /**
     * Get the last load time of the group in string
     * @return the last load time of the group in string
     */
    public String getLastLoadTimeString() {
        // converge long to real time
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(lastLoadTime));
    }

    /**
     * Update the last load time of the group
     * @param time the last load time of the group
     */
    public void updateLastLoadTime(long time) {
        lastLoadTime = time;
    }

    /**
     * Add a temporary event to the group
     * @param event the event
     * @param time the period of this event. The unit is millisecond
     */
    public void addTempEvent(String event, long time) {
        long now = System.currentTimeMillis();
        tempEvent.add(Map.of(now + time, event));
    }

    /**
     * Remove the last temporary event from the group
     */
    public void popTempEvent() {
        tempEvent.remove(tempEvent.size() - 1);
    }

    /**
     * Get the temporary event of the group
     * @return the temporary event of the group
     */
    public ArrayList<Map<Long, String>> getTempEvent() {
        return new ArrayList<>(tempEvent);
    }

    void setTempEvent(ArrayList<Map<Long, String>> tempEvent) {
        this.tempEvent.clear();
        this.tempEvent.addAll(tempEvent);
    }

    /**
     * Add a permanent prompt to the group
     * @param prompt the prompt
     */
    public void addPermanentPrompt(String prompt) {
        permanentPrompt.add(prompt);
    }

    /**
     * Remove the last permanent prompt from the group
     */
    public void popPermanentPrompt() {
        permanentPrompt.remove(permanentPrompt.size() - 1);
    }

    /**
     * Get the permanent prompt of the group
     * @return the permanent prompt of the group
     */
    public TreeSet<String> getPermanentPrompt() {
        return new TreeSet<>(permanentPrompt);
    }

    void setPermanentPrompt(ArrayList<String> permanentPrompt) {
        this.permanentPrompt.clear();
        this.permanentPrompt.addAll(permanentPrompt);
    }

    /**
     * Get the data manager of the group
     * @return the data manager of the group
     */
    public GroupDataManager getDataManager() {
        return new GroupDataManager(this);
    }

    /**
     * Remove the temporary event that has expired
     */
    public void autoDeleteTempEvent() {
        long now = System.currentTimeMillis();
        tempEvent.removeIf(map -> map.keySet().iterator().next() < now);
    }
}
