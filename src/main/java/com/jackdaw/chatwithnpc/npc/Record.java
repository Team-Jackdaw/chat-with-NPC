package com.jackdaw.chatwithnpc.npc;

import java.util.TreeMap;

public class Record {
    public enum Role {
        PLAYER, NPC
    }
    public static final class Message {
        public final Role role;
        private final String message;

        private Message(Role role, String message) {
            this.role = role;
            this.message = message;
        }

        public String getRole() {
            return role.toString();
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

    /**
     * 获取NPC的最后一次消息时间，随着时间的退役该NPC会逐渐遗忘他讲过的内容。
     * @return NPC的最后一次消息时间
     */
    public long lastMessageTime() {
        return messageRecord.lastKey();
    }

    public TreeMap<Long, Message> getTreeMap() {
        return messageRecord;
    }

    public void deleteMessageBefore(long time) {
        messageRecord.remove(time);
    }
}
