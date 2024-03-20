package com.jackdaw.chatwithnpc.conversation;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.jackdaw.chatwithnpc.openaiapi.Assistant;
import com.jackdaw.chatwithnpc.async.AsyncTaskQueue;
import com.jackdaw.chatwithnpc.openaiapi.Run;
import com.jackdaw.chatwithnpc.openaiapi.Threads;
import org.jetbrains.annotations.NotNull;

public class ConversationHandler {

    final NPCEntity npc;
    protected boolean isTalking = false;
    long updateTime = 0L;
    public AsyncTaskQueue taskQueue = new AsyncTaskQueue();

    public ConversationHandler(@NotNull NPCEntity npc) {
        this.npc = npc;
        startConversation();
    }

    private void sendWaitMessage() {
        npc.replyMessage("...", SettingManager.range);
    }

    public NPCEntity getNpc() {
        return npc;
    }

    private void startConversation() {
        setTalking(true);
        sendWaitMessage();
        boolean isOK =  taskQueue.addTask(() -> {
            try {
                if (!npc.hasAssistant()) {
                    Assistant.createAssistant(npc);
                } else {
                    Assistant.modifyAssistant(npc);
                }
                if (!npc.hasThreadId()) Threads.createThread(this);
                Threads.addMessage(npc.getThreadId(), "Hello!");
                Run.run(this);
                setTalking(false);
            } catch (Exception e) {
                ChatWithNPCMod.LOGGER.error(e.getMessage());
                taskQueue.clear();
                setTalking(false);
            }
        });
        if (!isOK) setTalking(false);
        updateTime = System.currentTimeMillis();
    }

    public void replyToEntity(String message) {
        setTalking(true);
        sendWaitMessage();
        boolean isOk = taskQueue.addTask(() -> {
            try {
                if (!npc.hasAssistant()) Assistant.createAssistant(npc);
                if(!npc.hasThreadId()) Threads.createThread(this);
                Threads.addMessage(npc.getThreadId(), message);
                Run.run(this);
                setTalking(false);
            } catch (Exception e) {
                ChatWithNPCMod.LOGGER.error(e.getMessage());
                taskQueue.clear();
                setTalking(false);
            }
        });
        if (!isOk) setTalking(false);
        updateTime = System.currentTimeMillis();
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public String getUpdateTimeString() {
        // converge Long to real time
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(updateTime));
    }

    /**
     * 获取NPC的对话状态
     *
     * @return NPC的对话状态
     */
    public boolean isTalking() {
        return isTalking;
    }

    /**
     * 设置NPC的对话状态
     *
     * @param isTalking NPC的对话状态
     */
    public void setTalking(boolean isTalking) {
        this.isTalking = isTalking;
    }

    public void discard() {
        if (!npc.isNeedMemory() && npc.hasThreadId()) {
            try {
                Threads.discardThread(npc.getThreadId());
                npc.setThreadId(null);
            } catch (Exception e) {
                ChatWithNPCMod.LOGGER.error(e.getMessage());
            }
        }
        taskQueue.shutdown();
    }
}
