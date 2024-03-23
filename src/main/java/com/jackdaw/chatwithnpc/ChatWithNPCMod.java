package com.jackdaw.chatwithnpc;

import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import com.jackdaw.chatwithnpc.conversation.ConversationManager;
import com.jackdaw.chatwithnpc.listener.PlayerSendMessageCallback;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.jackdaw.chatwithnpc.npc.NPCEntityManager;
import com.jackdaw.chatwithnpc.openaiapi.function.FunctionManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ChatWithNPCMod implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("chat-with-npc");

    public static final Path workingDirectory = Paths.get(System.getProperty("user.dir"), "config", "chat-with-npc");

    // The time in milliseconds that a static data is considered out of time
    public static final long outOfTime = 300000L;

    // The time in milliseconds that check for out of time static data
    public static final long updateInterval = 30000L;

    public static final boolean debug = false;

    @Override
    public void onInitialize() {
        // Create the working directory if it does not exist
        if (!Files.exists(workingDirectory)) {
            try {
                Files.createDirectories(workingDirectory);
            } catch (IOException e) {
                LOGGER.error("[chat-with-npc] Failed to create the working directory");
                LOGGER.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }
        // Load the configuration
        SettingManager.sync();
        // Load the functions
        FunctionManager.sync();
        // Register the command
        CommandRegistrationCallback.EVENT.register(CommandSet::setupCommand);
        // Register the conversation
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            // The mod must be enabled
            if (!SettingManager.enabled) return ActionResult.PASS;
            // The player must be sneaking to start a conversation
            if (!player.isSneaking()) return ActionResult.PASS;
            // The entity must have a custom name to be an NPC
            if (entity.getCustomName() == null) return ActionResult.PASS;
            // register the NPC entity and start a conversation
            NPCEntityManager.registerNPCEntity(entity, player.hasPermissionLevel(4));
            NPCEntity npc = NPCEntityManager.getNPCEntity(entity.getUuid());
            if (npc != null) ConversationManager.startConversation(npc);
            return ActionResult.FAIL;
        });
        // Register the player chat listener
        PlayerSendMessageCallback.EVENT.register((player, message) -> {
            // The mod must be enabled
            if (!SettingManager.enabled) return ActionResult.PASS;
            // The player must be in a conversation
            ConversationHandler conversationHandler = ConversationManager.getConversation(player);
            if (conversationHandler == null) return ActionResult.PASS;
            if (conversationHandler.isTalking()) {
                player.sendMessage(Text.of("[chat-with-npc] The NPC is talking, please wait"), false);
                return ActionResult.PASS;
            }
            conversationHandler.replyToEntity(message);
            return ActionResult.PASS;
        });
        // Register the server tick listener to check the task queue that need to be executed in main thread
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            while (!AsyncTask.isTaskQueueEmpty()) {
                AsyncTask.TaskResult result = AsyncTask.pollTaskQueue();
                result.execute();
            }
        });
        // Start the live cycle manager
        LiveCycleManager.start(updateInterval);
        // Shutdown the executor service when the server is stopped
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            LiveCycleManager.shutdown();
            LiveCycleManager.saveAll();
        });
    }
}
