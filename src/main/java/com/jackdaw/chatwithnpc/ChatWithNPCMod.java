package com.jackdaw.chatwithnpc;

import com.jackdaw.chatwithnpc.auxiliary.command.CommandSet;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import com.jackdaw.chatwithnpc.conversation.ConversationManager;
import com.jackdaw.chatwithnpc.listener.PlayerSendMessageCallback;
import com.jackdaw.chatwithnpc.npc.NPCEntityManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ChatWithNPCMod implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("chat-with-npc");

    public static final Path workingDirectory = Paths.get(System.getProperty("user.dir"), "config", "chat-with-npc");

    // The time in milliseconds that a static data is considered out of time
    public static final long outOfTime = 300000L;

    // The time in milliseconds that check for out of time static data
    public static final long updateInterval = 30000L;

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
            NPCEntityManager.registerNPCEntity(entity);
            ConversationManager.startConversation(NPCEntityManager.getNPCEntity(entity.getUuid()));
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
        // Check for out of time static data
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
            try {
                UpdateStaticData.update();
            } catch (Exception ignore) {
            }
        }, 0, updateInterval, TimeUnit.MILLISECONDS);
        // Shutdown the executor service when the server is stopped
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            executorService.shutdown();
            UpdateStaticData.close();
        });
    }
}
