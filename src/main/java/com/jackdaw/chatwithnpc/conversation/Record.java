package com.jackdaw.chatwithnpc.conversation;

import java.util.TreeMap;

public class Record {
    public enum Role {
        PLAYER, SYSTEM, NPC
    }
    public static final class Message {
        private final Role role;
        private final String message;

        private String entityName;

        public Message(Role role, String message) {
            this.role = role;
            this.message = message;
        }

        public Message(Role role, String message, String name) {
            this.role = role;
            this.message = message;
            this.entityName = name;
        }

        public Role getRole() {
            return role;
        }

        public String getMessage() {
            return message;
        }

        public String getEntityName() {
            return entityName;
        }
    }
    private final TreeMap<Long, Message> messageRecord = new TreeMap<>();

    public boolean isEmpty() {
        return messageRecord.isEmpty();
    }

    public void addMessage(long time, Role role, String message) {
        messageRecord.put(time, new Message(role, message));
    }

    public void addMessage(long time, Role role, String message, String name) {
        messageRecord.put(time, new Message(role, message, name));
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

    public void changeAllRole(Role role) {
        TreeMap<Long, Message> newMessageRecord = new TreeMap<>();
        messageRecord.forEach((time, message) -> {
            newMessageRecord.put(time, new Message(role, message.getMessage(), message.getEntityName()));
        });
        messageRecord.clear();
        messageRecord.putAll(newMessageRecord);
    }
}
