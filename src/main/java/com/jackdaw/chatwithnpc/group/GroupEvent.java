package com.jackdaw.chatwithnpc.group;

import org.jetbrains.annotations.NotNull;

public class GroupEvent implements Comparable<GroupEvent> {
    private final String event;
    private final long startTime;
    private final long endTime;

    public GroupEvent(String event, long startTime, long period) {
        this.event = event;
        this.startTime = startTime;
        this.endTime = startTime + period;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getEvent() {
        return event;
    }

    public long getEndTime() {
        return endTime;
    }

    @Override
    public int compareTo(@NotNull GroupEvent o) {
        return Long.compare(this.startTime, o.startTime);
    }
}
