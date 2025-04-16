package com.jackdaw.chatwithnpc.conversation;

import com.jackdaw.chatwithnpc.AsyncTask;
import com.jackdaw.chatwithnpc.SettingManager;
import com.jackdaw.chatwithnpc.api.Ollama;
import com.jackdaw.chatwithnpc.api.json.*;
import com.jackdaw.chatwithnpc.function.FunctionManager;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
    protected List<Message> messages;
    protected boolean isTalking = false;
    protected long updateTime = 0L;

    /**
     * Construct a new Conversation for an NPC. This will start a conversation with the NPC asynchronously.
     *
     * @param npc The NPC to start a conversation with.
     */
    public ConversationHandler(@NotNull NPCEntity npc) {
        this.npc = npc;
        messages = npc.getMessages();
        startConversation();
    }

    /**
     * Send a wait message to the NPC. This will make the NPC reply to the player with a waiting message.
     */
    public void sendWaitMessage() {
        npc.replyMessage("...", SettingManager.range);
    }

    void startConversation() {
        AsyncTask.call(() -> {
            setTalking(true);
            sendWaitMessage();
            messages = Ollama.messageBuilder(messages)
                    .addMessage(Role.SYSTEM, "Below is a new conversation.")
                    .build();
            chat("Hello!");
            npc.replyMessage(getLastMessage(), SettingManager.range);
            setTalking(false);
            return AsyncTask.nothingToDo();
        });
        this.updateTime = System.currentTimeMillis();
    }

    public void continueConversation() {
        AsyncTask.call(() -> {
            replyFunctionCall();
            npc.replyMessage(getLastMessage(), SettingManager.range);
            setTalking(false);
            return AsyncTask.nothingToDo();
        });
        this.updateTime = System.currentTimeMillis();
    }

    public void replyToEntity(String message) {
        AsyncTask.call(() -> {
            sendWaitMessage();
            this.setTalking(true);
            ChatResponse res = chat(message);
            if (res.message.tool_calls != null) {
                return new FunctionManager.FunctionCall(this, res.message.tool_calls);
            }
            npc.replyMessage(getLastMessage(), SettingManager.range);
            setTalking(false);
            return AsyncTask.nothingToDo();
        });
        this.updateTime = System.currentTimeMillis();
    }

    /**
     * Get the NPC that this conversation is handling.
     *
     * @return The NPC that this conversation is handling.
     */
    public NPCEntity getNpc() {
        return npc;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public String getLastMessage() {
        return messages.get(messages.size() - 1).content;
    }

    public List<Tool> getTools() {
        return npc.getFunctions()
                .stream()
                .map(FunctionManager.getInstance()::getTools)
                .toList();
    }

    /**
     * Reply to the function call. This will make the NPC reply to the player with the function call result.
     */
    public void replyFunctionCall() {
        ChatResponse response;
        try {
            response = Ollama.chat(messages, getTools());
            messages = Ollama.messageBuilder(messages)
                    .addMessage(Role.ASSISTANT, response.message.content)
                    .build();
        } catch (Exception e) {
            messages = Ollama.messageBuilder(messages)
                    .addMessage(Role.ASSISTANT, "I'm sorry, I can't do that.")
                    .build();
        }
    }

    /**
     * Say a message to the agent in this conversation
     *
     * @return The response
     */
    public ChatResponse chat(String message) {
        updateTime = System.currentTimeMillis();
        messages = Ollama.messageBuilder(messages)
                .addMessage(Role.USER, message)
                .build();
        ChatResponse response;
        try {
            response = Ollama.chat(messages, getTools());
            if (response.message.tool_calls != null) {
                return response;
            }
            messages = Ollama.messageBuilder(messages)
                    .addMessage(Role.ASSISTANT, response.message.content)
                    .build();
        } catch (Exception e) {
            messages = Ollama.messageBuilder(messages)
                    .addMessage(Role.ASSISTANT, "I'm sorry, I can't do that.")
                    .build();
            response = null;
        }
        return response;
    }

    /**
     * Get the time when the conversation was last updated.
     *
     * @return The time when the conversation was last updated.
     */
    public long getUpdateTime() {
        return updateTime;
    }

    /**
     * Get the time when the conversation was last updated in a human-readable format.
     *
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
        npc.setMessages(messages);
    }
}
