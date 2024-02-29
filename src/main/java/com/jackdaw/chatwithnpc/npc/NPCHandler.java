package com.jackdaw.chatwithnpc.npc;

import com.jackdaw.chatwithnpc.data.NPCDataManager;
import net.minecraft.entity.player.PlayerEntity;

public interface NPCHandler {

    /**
     * 操作该NPC回复玩家信息。
     * @param message 该NPC应该回复玩家的消息。
     * @param player 该NPC应该回复的玩家。
     */
    void replyMessage(String message, PlayerEntity player);

    /**
     * 操作该NPC执行动作。
     * @param action 该NPC应该回应玩家的动作。
     * @param player 该NPC应该回应的玩家。
     */
    void doAction(Actions action, PlayerEntity player);

    /**
     * 获取该NPC的历史聊天记录。
     * @return 该NPC的聊天记录。
     */
    Record readMessageRecord();

    /**
     * 获取该NPC的数据管理器。
     * @return 该NPC的数据管理器。
     */
    NPCDataManager getDataManager();
}
