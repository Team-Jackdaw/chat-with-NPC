package com.jackdaw.chatwithnpc.conversation;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.jackdaw.chatwithnpc.openaiapi.Assistant;
import com.jackdaw.chatwithnpc.async.AsyncTaskQueue;
import com.jackdaw.chatwithnpc.openaiapi.Run;
import com.jackdaw.chatwithnpc.openaiapi.Threads;
import org.jetbrains.annotations.NotNull;

/**
 * <b>Conversation of an NPC</b>
 * <p>
 * The conversation will record the conversation of a NPC.
 * <p>
 * It is used to execute the conversation with OpenAI asynchronously.
 *
 * @version 1.0
 */
public class ConversationHandler {

    protected final NPCEntity npc;
    protected boolean isTalking = false;
    protected long updateTime = 0L;
    public AsyncTaskQueue taskQueue = new AsyncTaskQueue();

    /**
     * Construct a new Conversation for an NPC. This will start a conversation with the NPC asynchronously.
     * @param npc The NPC to start a conversation with.
     */
    ConversationHandler(@NotNull NPCEntity npc) {
        this.npc = npc;
        startConversation();
    }

    /**
     * Get the NPC that this conversation is handling.
     * @return The NPC that this conversation is handling.
     */
    public NPCEntity getNpc() {
        return npc;
    }

    private void sendWaitMessage() {
        npc.replyMessage("...", SettingManager.range);
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

    /**
     * Reply to the NPC with a message. Then the NPC will reply to the player something. This method is asynchronous.
     * @param message The message sent to the NPC.
     */
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

    /**
     * Get the time when the conversation was last updated.
     * @return The time when the conversation was last updated.
     */
    public long getUpdateTime() {
        return updateTime;
    }

    /**
     * Get the time when the conversation was last updated in a human-readable format.
     * @return The time when the conversation was last updated in a human-readable format.
     */
    public String getUpdateTimeString() {
        // converge Long to real time
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date(updateTime));
    }

    /**
     * Get the NPC's current conversation.
     *
     * @return The NPC's current conversation.
     */
    public boolean isTalking() {
        return isTalking;
    }

    /**
     * Set the NPC's current conversation.
     *
     * @param isTalking The NPC's current conversation.
     */
    public void setTalking(boolean isTalking) {
        this.isTalking = isTalking;
    }

    /**
     * Discard the conversation. This will stop the conversation with the NPC. If the NPC is not needed to remember the conversation, all the messages in this conversation will be deleted.
     */
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
