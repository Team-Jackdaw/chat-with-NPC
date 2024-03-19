package com.jackdaw.chatwithnpc.npc;

import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 这是一个用于定义与 NPC 交互的类。
 * 该类应记录 NPC 的基本信息，如名称、职业、basicPrompt 和 localGroup 等，这些都是 NPC 的特征。
 * 同时该类应记录 NPC 的对话状态，如最后一次消息时间、消息记录和是否正在与玩家对话等。
 * 该类应提供接收玩家信息、回复玩家信息和执行动作等方法。
 * <p>
 * <b>请注意：该类应该设置一个生命周期以降低重复读取和内存占用<b/>
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
    protected String basicPrompt = "You are an NPC.";
    protected String instructions = "";
    protected String group = "Global";
    protected boolean needMemory = true;
    protected ArrayList<Map<Long, String>> longTermMemory = new ArrayList<>();
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
     * 获取NPC的名字，该名字应该作为该NPC在本插件中的唯一标识，并将作为储存的文件名。
     *
     * @return NPC的名字
     */
    public String getName() {
        return this.name;
    }

    /**
     * 获取NPC的类型，该类型应该作为该NPC的特征之一。
     *
     * @return NPC的类型
     */
    public String getType() {
        return this.entity.getType().toString();
    }

    /**
     * 获取NPC的职业，该职业应该作为该NPC的特征之一。
     *
     * @return NPC的职业
     */
    public String getCareer() {
        return this.career;
    }

    /**
     * 设置NPC的职业，该职业应该作为该NPC的特征之一。
     *
     * @param career NPC的职业
     */
    public void setCareer(String career) {
        this.career = career;
    }

    /**
     * 获取NPC的基本提示信息，该信息应该作为该NPC的基本信息之一。
     *
     * @return NPC的基本提示信息
     */
    public String getBasicPrompt() {
        return this.basicPrompt;
    }

    /**
     * 设置NPC的基本提示信息，该信息应该作为该NPC的基本信息之一。
     *
     * @param basicPrompt NPC的基本提示信息
     */
    public void setBasicPrompt(String basicPrompt) {
        this.basicPrompt = basicPrompt;
    }

    /**
     * 获取对NPC的指示，该信息应该作为该NPC的基本信息之一。
     *
     * @return 对NPC的指示
     */
    public String getInstructions() {
        return this.instructions;
    }

    /**
     * 设置对NPC的指示，该信息应该作为该NPC的基本信息之一。
     *
     * @param instructions 对NPC的指示
     */
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    /**
     * 获取NPC的本地群组，该群组应该作为该NPC的特征之一，即该NPC的所在位置相关信息。
     *
     * @return NPC的本地群组
     */
    public String getGroup() {
        return group;
    }

    /**
     * 设置NPC的本地群组，该群组应该作为该NPC的特征之一，即该NPC的所在位置相关信息。
     *
     * @param group NPC的本地群组
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * 获取NPC的数据管理器，该管理器应该用于管理NPC的数据。
     *
     * @return NPC的数据管理器
     */
    public NPCDataManager getDataManager() {
        return new NPCDataManager(this);
    }

    /**
     * 接收玩家的消息，该消息应该是NPC对玩家的交互。
     *
     * @param message NPC的信息
     * @param range   玩家的范围
     */
    public void replyMessage(String message, double range) {
        if (SettingManager.isBubble) textBubble.update(message);
        if (SettingManager.isChatBar)
            findNearbyPlayers(range).forEach(player -> player.sendMessage(Text.of("<" + name + "> " + message)));
    }

    /**
     * 获取附近的玩家，该玩家应该是NPC的交互对象。
     *
     * @param range 玩家的范围
     * @return 附近的玩家
     */
    public List<PlayerEntity> findNearbyPlayers(double range) {
        World world = entity.world;
        return world.getEntitiesByClass(PlayerEntity.class, entity.getBoundingBox().expand(range), player -> true);
    }

    /**
     * 执行动作，该动作应该是NPC对玩家的交互。
     *
     * @param action 动作
     * @param player 玩家的实体
     */
    public abstract void doAction(Actions action, PlayerEntity player);

    /**
     * 获取NPC的UUID，该UUID应该作为该NPC的唯一标识。
     *
     * @return NPC的UUID
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * 获取NPC的实体，该实体应该是该NPC的实体。
     *
     * @return NPC的实体
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * 添加NPC的长期记忆，该记忆应该是NPC的特征之一。
     *
     * @param memoryTime 记忆时间
     * @param memory     记忆
     */
    public void addLongTermMemory(long memoryTime, String memory) {
        Map<Long, String> newMemory = Map.of(memoryTime, memory);
        longTermMemory.add(newMemory);
    }

    /**
     * 获取NPC的长期记忆，该记忆应该是NPC的特征之一。
     *
     * @return NPC的长期记忆
     */
    public ArrayList<Map<Long, String>> getLongTermMemory() {
        return longTermMemory;
    }

    /**
     * 设置NPC的长期记忆，该记忆应该是NPC的特征之一。
     *
     * @param longTermMemory NPC的长期记忆
     */
    public void setLongTermMemory(ArrayList<Map<Long, String>> longTermMemory) {
        this.longTermMemory = longTermMemory;
    }

    /**
     * 删除某个时间以前的记忆。
     *
     * @param forgetTime 遗忘时间
     */
    public void deleteLongTermMemory(long forgetTime) {
        longTermMemory.removeIf(memory -> memory.keySet().stream().anyMatch(time -> time < forgetTime));
    }

    /**
     * 根据随机函数遗忘一些记忆。
     */
    public void randomForget() {
        if (longTermMemory.size() < 20) return;
        for (Map<Long, String> memory : longTermMemory) {
            long time = memory.keySet().iterator().next();
            long duration = System.currentTimeMillis() - time;
            double probability = Math.min(1, duration / SettingManager.forgetTime);
            if (Math.random() < probability) {
                longTermMemory.removeIf(m -> m.keySet().stream().anyMatch(t -> t == time));
            }
        }
    }

    /**
     * 设置NPC是否需要记忆，该记忆应该是NPC的特征之一。
     * @return NPC是否需要记忆
     */
    public boolean isNeedMemory() {
        return needMemory;
    }

    /**
     * 设置NPC是否需要记忆，该记忆应该是NPC的特征之一。
     * @param needMemory NPC是否需要记忆
     */
    public void setNeedMemory(boolean needMemory) {
        this.needMemory = needMemory;
    }

    /**
     * 保存NPC的数据。
     */
    public void discard() {
        this.randomForget();
        this.getDataManager().save();
        this.textBubble.discard();
    }

    /**
     * 查看是否已经注册了助手。
     *
     * @return 是否已经注册了助手
     */
    public boolean hasAssistant() {
        return this.assistantId != null;
    }

    /**
     * 获取NPC的助手ID。
     *
     * @return NPC的助手ID
     */
    public String getAssistantId() {
        return this.assistantId;
    }

    /**
     * 设置NPC的助手ID。
     *
     * @param id 助手ID
     */
    public void setAssistantId(String id) {
        this.assistantId = id;
    }

    /**
     * 获取当前会话的线程ID。
     *
     * @return 当前会话的线程ID
     */
    public String getThreadId() {
        return ThreadId;
    }

    /**
     * 设置当前会话的线程ID。
     *
     * @param threadId 当前会话的线程ID
     */
    public void setThreadId(String threadId) {
        ThreadId = threadId;
    }

    /**
     * 查看是否已经注册了线程。
     *
     * @return 是否已经注册了线程
     */
    public boolean hasThreadId() {
        return ThreadId != null;
    }
}
