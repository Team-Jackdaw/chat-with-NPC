package com.jackdaw.chatwithnpc.group;

import com.jackdaw.chatwithnpc.data.GroupDataManager;

import java.util.ArrayList;
import java.util.TreeSet;


/**
 * This is an interface used to define the group.
 * The class should record the basic information of the group, such as name, weather, environmentPrompt and tempEnvironmentPrompt.
 * The class should provide methods to get the name, event, weather, environmentPrompt and tempEnvironmentPrompt of the group.
 * <p>
 * <b>Please note: This class should be set a life cycle to reduce repeated reading and memory usage<b/>
 *
 * @version 1.0
 */
public interface Group {
    /**
     * Get the name of the group.
     * @return the name of the group.
     */
    String getName();

    /**
     * Get the parent group of the group.
     * @param parentGroup the parent group of the group.
     */
    void setParentGroup(String parentGroup);

    /**
     * Get the parent group of the group.
     * @return the parent group of the group.
     */
    Group getParentGroup();

    /**
     * Get the parent groups of the group. Include the group itself.
     * @return  subGroup the parent groups of the group.
     */
    ArrayList<Group> getParentGroups();

    /**
     * Get the last load time of the group.
     * @return the last load time of the group.
     */
    long getLastLoadTime();

    /**
     * Set the last load time of the group.
     * @param time the last load time of the group.
     */
    void updateLastLoadTime(long time);

    /**
     * Set the temporary group event.
     * @param tempEvent the temporary group event.
     */
    void setTempEvent(TreeSet<GroupEvent> tempEvent);

    /**
     * Add a temporary group event.
     * @param event the event to be added.
     * @param period the period of the event.
     */
    void addTempEvent(String event, long period);

    /**
     * Get all the temporary group event.
     * @return all the temporary group event.
     */
    TreeSet<GroupEvent> getTempEvent();

    /**
     * Set the event of the group.
     * @param permanentPrompt the event of the group.
     */
    void setPermanentPrompt(ArrayList<String> permanentPrompt);

    /**
     * Add a group event.
     * @param prompt the event to be added.
     */
    void addPermanentPrompt(String prompt);

    /**
     * Get all the group event.
     * @return all the group event.
     */
    TreeSet<String> getPermanentPrompt();

    /**
     * Get the data manager of the group.
     * @return the data manager of the group.
     */
    GroupDataManager getDataManager();
}
