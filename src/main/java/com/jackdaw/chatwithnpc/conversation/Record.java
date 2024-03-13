package com.jackdaw.chatwithnpc.conversation;

import java.util.TreeMap;

public class Record {
    public enum Role {
        PLAYER, SYSTEM, NPC
    }
    public static final class Message {
        private final Role role;
        private final String message;

        public Message(Role role, String message) {
            this.role = role;
            this.message = message;
        }

        public Role getRole() {
            return role;
        }

        public String getMessage() {
            return message;
        }
    }
    private final TreeMap<Long, Message> messageRecord = new TreeMap<>();

    public boolean isEmpty() {
        return messageRecord.isEmpty();
    }

    public void addMessage(long time, Role role, String message) {
        messageRecord.put(time, new Message(role, message));
    }

    public TreeMap<Long, Message> getTreeMap() {
        return messageRecord;
    }

    public void popMessage() {
        messageRecord.pollFirstEntry();
    }

    public void clear() {
        messageRecord.clear();
    }
}
