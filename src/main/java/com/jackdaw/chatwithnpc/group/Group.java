package com.jackdaw.chatwithnpc.group;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * <b>Group of NPC</b>
 * <p>
 * The group will record the permanent prompt and temporary event and its parent group.
 * <p>
 * The permanent prompt and the events will be known by all the members of the group.
 * <p>
 * Every group will have parents until the parent is "Global".
 *
 * @version 1.1
 */
public class Group {
    protected final String name;
    protected String instruction = "A place with good weather.";
    protected final ArrayList<String> events = new ArrayList<>();
    protected String parentGroup = "Global";
    protected long lastLoadTime;
    protected final ArrayList<String> memberList = new ArrayList<>();

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
    protected void setParentGroup(String parentGroup) {
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
     * Add an event to the group
     * @param event the event
     */
    public void addEvent(String event) {
        events.add(event);
    }

    /**
     * Remove the last event from the group
     */
    public void popEvent() {
        if (!events.isEmpty()) {
            events.remove(events.size() - 1);
        }
    }

    /**
     * Get the temporary event of the group
     * @return the temporary event of the group
     */
    public ArrayList<String> getEvent() {
        return new ArrayList<>(events);
    }

    void setEvent(ArrayList<String> event) {
        events.clear();
        events.addAll(event);
    }

    /**
     * Set the instruction of the group
     * @param instruction the instruction of the group
     */
    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    /**
     * Get the instruction of the group
     * @return the instruction of the group
     */
    public String getInstruction() {
        return instruction;
    }

    /**
     * Get the data manager of the group
     * @return the data manager of the group
     */
    public GroupDataManager getDataManager() {
        return new GroupDataManager(this);
    }

    /**
     * Add a member to the group
     * @param member the member to add
     */
    protected void addMember(String member) {
        if (!memberList.contains(member)) {
            memberList.add(member);
        }
    }

    /**
     * Remove a member from the group
     * @param member the member to remove
     */
    protected void removeMember(String member) {
        memberList.remove(member);
    }

    /**
     * Get the member list of the group
     * @return the member list of the group
     */
    public ArrayList<String> getMemberList() {
        return new ArrayList<>(memberList);
    }

    void setMemberList(ArrayList<String> memberList) {
        this.memberList.clear();
        this.memberList.addAll(memberList);
    }
}
