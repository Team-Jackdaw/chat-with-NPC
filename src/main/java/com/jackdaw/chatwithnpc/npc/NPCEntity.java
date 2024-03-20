package com.jackdaw.chatwithnpc.npc;

import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

/**
 * <b>NPC Entity Class</b>
 * <p>
 * This is a class used to define the interaction with NPC.
 * This class should record the basic information of NPC, such as name, career, basicPrompt, and localGroup, etc., which are all characteristics of NPC.
 * At the same time, this class should record the dialogue state of NPC, such as the last message time, message record, and whether it is in dialogue with the player, etc.
 * This class should provide methods for receiving player information, replying to player information, and executing actions, etc.
 * <p>
 * <b>Please note: this class should set a life cycle to reduce repeated reading and memory usage<b/>
 *
 * @version 1.0
 */
public abstract class NPCEntity {

    protected final String name;
    protected final Entity entity;
    protected final UUID uuid;
    protected String assistantId;
    protected String ThreadId;
    protected String career = "unemployed";
    protected String instructions = "You are an NPC.";
    protected String group = "Global";
    protected boolean needMemory = true;
    protected TextBubbleEntity textBubble;

    /**
     * This is a constructor used to initialize the NPC with the entity.
     *
     * @param entity The entity of the NPC.
     */
    public NPCEntity(@NotNull Entity entity) {
        if (entity.getCustomName() == null) {
            throw new IllegalArgumentException("[chat-with-npc] The entity must have a custom name.");
        }
        this.name = entity.getCustomName().getString();
        this.entity = entity;
        this.uuid = entity.getUuid();
        this.textBubble = new TextBubbleEntity(entity);
    }

    /**
     * Do an action of the NPC.
     *
     * @param action The action of the NPC
     * @param player To player
     */
    public abstract void doAction(Actions action, PlayerEntity player);

    /**
     * Get the name of the NPC, which should be the unique identifier of the NPC in this plugin and will be used as the file name for storage.
     *
     * @return The name of the NPC
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the type of the NPC, which should be one of the characteristics of the NPC.
     *
     * @return The type of the NPC
     */
    public String getType() {
        return this.entity.getType().toString();
    }

    /**
     * Get the career of the NPC, which should be one of the characteristics of the NPC.
     *
     * @return The career of the NPC
     */
    public String getCareer() {
        return this.career;
    }

    /**
     * Set the career of the NPC, which should be one of the characteristics of the NPC.
     *
     * @param career The career of the NPC
     */
    public void setCareer(String career) {
        this.career = career;
    }

    /**
     * Get the basic instructions of the NPC, which should be one of the characteristics of the NPC.
     *
     * @return The instructions of the NPC
     */
    public String getInstructions() {
        return this.instructions;
    }

    /**
     * Set the basic instructions of the NPC, which should be one of the characteristics of the NPC.
     *
     * @param instructions The instructions of the NPC
     */
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    /**
     * Get the local group of the NPC, which should be one of the characteristics of the NPC.
     *
     * @return The local group of the NPC
     */
    public String getGroup() {
        return group;
    }

    /**
     * Set the local group of the NPC, which should be one of the characteristics of the NPC.
     *
     * @param group The local group of the NPC
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Get the NPC data manager, which should be used to manage the NPC's data.
     *
     * @return The NPC data manager
     */
    public NPCDataManager getDataManager() {
        return new NPCDataManager(this);
    }

    /**
     * Reply the message to the players nearby the NPC.
     *
     * @param message The message of the NPC
     * @param range  The range of the NPC
     */
    public void replyMessage(String message, double range) {
        if (SettingManager.isBubble) textBubble.update(message);
        if (SettingManager.isChatBar)
            findNearbyPlayers(range).forEach(player -> player.sendMessage(Text.of("<" + name + "> " + message)));
    }

    /**
     * Find the nearby players.
     *
     * @param range The range of the NPC
     * @return The nearby players
     */
    public List<PlayerEntity> findNearbyPlayers(double range) {
        World world = entity.world;
        return world.getEntitiesByClass(PlayerEntity.class, entity.getBoundingBox().expand(range), player -> true);
    }

    /**
     * Get the UUID of the NPC, which should be the unique identifier of the NPC in the game.
     *
     * @return The UUID of the NPC
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Get the entity of the NPC, which should be the entity of the NPC in the game.
     *
     * @return The entity of the NPC
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Check if the NPC needs memory.
     * @return If the NPC needs memory
     */
    public boolean isNeedMemory() {
        return needMemory;
    }

    /**
     * Set if the NPC needs memory.
     * @param needMemory If the NPC needs memory
     */
    public void setNeedMemory(boolean needMemory) {
        this.needMemory = needMemory;
    }

    /**
     * Check if the NPC has an assistant.
     * @return If the NPC has an assistant
     */
    public boolean hasAssistant() {
        return this.assistantId != null;
    }

    /**
     * Get the NPC's assistant ID.
     * @return The NPC's assistant ID
     */
    public String getAssistantId() {
        return this.assistantId;
    }

    /**
     * Set the NPC's assistant ID.
     * @param id The NPC's assistant ID
     */
    public void setAssistantId(String id) {
        this.assistantId = id;
    }

    /**
     * Get the NPC's thread ID.
     * @return The NPC's thread ID
     */
    public String getThreadId() {
        return ThreadId;
    }

    /**
     * Set the NPC's thread ID.
     * @param threadId The NPC's thread ID
     */
    public void setThreadId(String threadId) {
        ThreadId = threadId;
    }

    /**
     * Check if the NPC has a thread ID.
     * @return If the NPC has a thread ID
     */
    public boolean hasThreadId() {
        return ThreadId != null;
    }

    /**
     * Discard the NPC. If the NPC is not needed to remember the conversation, all the messages in this conversation will be deleted.
     */
    public void discard() {
        this.getDataManager().save();
        this.textBubble.discard();
    }
}
