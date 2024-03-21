package com.jackdaw.chatwithnpc.auxiliary.command;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.LiveCycleManager;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import com.jackdaw.chatwithnpc.conversation.ConversationManager;
import com.jackdaw.chatwithnpc.openaiapi.functioncalling.FunctionManager;
import com.jackdaw.chatwithnpc.group.Group;
import com.jackdaw.chatwithnpc.group.GroupManager;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.jackdaw.chatwithnpc.npc.NPCEntityManager;
import com.jackdaw.chatwithnpc.openaiapi.Assistant;
import com.jackdaw.chatwithnpc.openaiapi.Threads;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CommandSet {

    private static final SuggestionProvider<ServerCommandSource> groupSuggestionProvider = (context, builder) -> {
        for (String group : GroupManager.getGroupList()) {
            builder.suggest(group);
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<ServerCommandSource> functionsSuggestionProvider = (context, builder) -> {
        for (String function : FunctionManager.getRegistryList()) {
            builder.suggest(function);
        }
        return builder.buildFuture();
    };

    private static boolean hasOPPermission(@NotNull ServerCommandSource source) {
        return source.hasPermissionLevel(4);
    }

    public static void setupCommand(@NotNull CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("npchat")
                .executes(CommandSet::status)
                .then(literal("help")
                        .requires(CommandSet::hasOPPermission)
                        .executes(CommandSet::help))
                .then(literal("setKey")
                        .requires(CommandSet::hasOPPermission)
                        .then(argument("key", StringArgumentType.string())
                                .executes(CommandSet::setAPIKey)))
                .then(literal("setModel")
                        .requires(CommandSet::hasOPPermission)
                        .then(argument("model", StringArgumentType.string())
                                .executes(CommandSet::setModel)))
                .then(literal("enable")
                        .requires(CommandSet::hasOPPermission)
                        .executes(context -> setEnabled(context, true)))
                .then(literal("disable")
                        .requires(CommandSet::hasOPPermission)
                        .executes(context -> setEnabled(context, false)))
                .then(literal("setRange")
                        .requires(CommandSet::hasOPPermission)
                        .then(argument("range", StringArgumentType.word())
                                .executes(CommandSet::setRange)))
                .then(literal("setLanguage")
                        .requires(CommandSet::hasOPPermission)
                        .then(argument("language", StringArgumentType.word())
                                .executes(CommandSet::setLanguage)))
                .then(literal("setMaxTokens")
                        .requires(CommandSet::hasOPPermission)
                        .then(argument("maxTokens", StringArgumentType.word())
                                .executes(CommandSet::setMaxTokens)))
                .then(literal("setURL")
                        .requires(CommandSet::hasOPPermission)
                        .then(argument("url", StringArgumentType.string())
                                .executes(CommandSet::setURL)))
                .then(literal("setBubble")
                        .requires(CommandSet::hasOPPermission)
                        .then(argument("isBubble", BoolArgumentType.bool())
                                .executes(CommandSet::setBubble)))
                .then(literal("setChatBar")
                        .requires(CommandSet::hasOPPermission)
                        .then(argument("isChatBar", BoolArgumentType.bool())
                                .executes(CommandSet::setChatBar)))
                .then(literal("npc")
                        .requires(CommandSet::hasOPPermission)
                        .then(literal("setCareer")
                                .then(argument("career", StringArgumentType.greedyString())
                                        .executes(CommandSet::setNPCCareer)))
                        .then(literal("setGroup")
                                .then(argument("group", StringArgumentType.word())
                                        .suggests(groupSuggestionProvider)
                                        .executes(CommandSet::setNPCGroup)))
                        .then(literal("setInstructions")
                                .then(argument("instructions", StringArgumentType.greedyString())
                                        .executes(CommandSet::setNPCInstructions)))
                        .then(literal("addFunction")
                                .then(argument("function", StringArgumentType.word())
                                        .suggests(functionsSuggestionProvider)
                                        .executes(CommandSet::addNPCFunction)))
                        .then(literal("deleteFunction")
                                .then(argument("function", StringArgumentType.word())
                                        .suggests(functionsSuggestionProvider)
                                        .executes(CommandSet::deleteNPCFunction)))
                        .then(literal("isNeedMemory")
                                .then(argument("isNeedMemory", BoolArgumentType.bool())
                                        .executes(CommandSet::setNeedMemory)))
                        .then(literal("update")
                                .executes(CommandSet::npcUpdate))
                        .then(literal("clearMemory")
                                .executes(CommandSet::clearNPCMemory))
                        .executes(CommandSet::npcStatus))
                .then(literal("group")
                        .requires(CommandSet::hasOPPermission)
                        .then(argument("group", StringArgumentType.word())
                                .suggests(groupSuggestionProvider)
                                .then(literal("setParent")
                                        .then(argument("parent", StringArgumentType.word())
                                                .suggests(groupSuggestionProvider)
                                                .executes(CommandSet::setGroupParent)))
                                .then(literal("addPermanentPrompt")
                                        .then(argument("prompt", StringArgumentType.greedyString())
                                                .executes(CommandSet::addGroupPermanentPrompt)))
                                .then(literal("popPermanentPrompt")
                                        .executes(CommandSet::popGroupPermanentPrompt))
                                .then(literal("addTempEvent")
                                        .then(argument("event", StringArgumentType.greedyString())
                                                .executes(CommandSet::addGroupTempEvent)))
                                .then(literal("popTempEvent")
                                        .executes(CommandSet::popGroupTempEvent))
                                .executes(CommandSet::groupStatus)))
                .then(literal("addGroup")
                        .requires(CommandSet::hasOPPermission)
                        .then(argument("newGroup", StringArgumentType.word())
                                .executes(CommandSet::addGroup)))
                .then(literal("saveAll")
                        .requires(CommandSet::hasOPPermission)
                        .executes(CommandSet::saveAll))
        );
    }

    public static int help(@NotNull CommandContext<ServerCommandSource> context) {
        Text helpText = Text.literal("")
                .append("[chat-with-npc] ChatWithNPC Commands").formatted(Formatting.UNDERLINE)
                .append("").formatted(Formatting.RESET)
                .append("\n/npchat - View configuration status")
                .append("\n/npchat help - View commands help")
                .append("\n/npchat enable/disable - Enable/disable the mod")
                .append("\n/npchat setKey <key> - Set OpenAI API key")
                .append("\n/npchat setModel <model> - Set AI model")
                .append("\n/npchat setRange <range> - Set the range of the conversation")
                .append("\n/npchat setForgetTime <time> - Set the time to forget the memory")
                .append("\n/npchat setLanguage <language> - Set the response language")
                .append("\n/npchat setMaxTokens <maxTokens> - Set the max tokens of a conversation")
                .append("\n/npchat setURL <url> - Set the AI Model URL")
                .append("\n/npchat saveAll - Save all the data.")
                .append("\n --------------------------------")
                .append("\nOnce you are in a conversation, you can use /npchat npc to set the properties of the NPC.")
                .append("\n/npchat npc - NPC commands")
                .append("\n/npchat npc setCareer <career> - Set the career for the closest NPC.")
                .append("\n/npchat npc setGroup <group> - Set the group for the closest NPC.")
                .append("\n/npchat npc setInstructions <instructions> - Set the instructions for the closest NPC.")
                .append("\n/npchat npc isNeedMemory <isNeedMemory> - Set the need memory for the closest NPC.")
                .append("\n/npchat npc addFunction <function> - Add a function to the closest NPC.")
                .append("\n/npchat npc deleteFunction <function> - Delete a function from the closest NPC.")
                .append("\n/npchat npc update - Update the current information of closest NPC to OpenAI.")
                .append("\n/npchat npc clearMemory - Clear the memory for the closest NPC.")
                .append("\n --------------------------------")
                .append("\n/npchat group <group> - Group commands")
                .append("\n/npchat group <group> setParent <parent> - Set the parent group for the group.")
                .append("\n/npchat group <group> addPermanentPrompt <prompt> - Add a permanent prompt to the group.")
                .append("\n/npchat group <group> popPermanentPrompt - Pop a permanent prompt from the group.")
                .append("\n/npchat group <group> addTempEvent <event> - Add a temporary event to the group.")
                .append("\n/npchat group <group> popTempEvent - Pop a temporary event from the group.")
                .append("\n/npchat addGroup <newGroup> - Add a new group")
                .append("\n --------------------------------")
                .append("\nYou can start a conversation to mobs by shift-clicking on them!")
                .append("\nOnce you are in a conversation, you can reply to the NPC by typing in the chat.");
        context.getSource().sendFeedback(helpText, false);
        return 1;
    }

    private static int status(@NotNull CommandContext<ServerCommandSource> context) {
        Text yes = Text.literal("Yes").formatted(Formatting.GREEN);
        Text no = Text.literal("No").formatted(Formatting.RED);
        if (!context.getSource().hasPermissionLevel(4)) {
            Text helpText = Text.literal("")
                    .append(Text.literal("[chat-with-npc] ChatWithNPC").formatted(Formatting.UNDERLINE))
                    .append("").formatted(Formatting.RESET)
                    .append("\nEnabled: ").append(SettingManager.enabled ? yes : no)
                    .append("\nChat Bubble: ").append(SettingManager.isBubble ? yes : no)
                    .append("\nChat Bar: ").append(SettingManager.isChatBar ? yes : no)
                    .append("\nModel: ").append(SettingManager.model)
                    .append("\nLanguage: ").append(SettingManager.language)
                    .append(Text.literal("\nYou can start a conversation to mobs by shift-clicking on them! " +
                                    "\nOnce you are in a conversation, you can reply to the NPC by typing in the chat.")
                            .formatted(Formatting.UNDERLINE));
            context.getSource().sendFeedback(helpText, false);
        } else {
            boolean hasKey = !SettingManager.apiKey.isEmpty();
            Text helpText = Text.literal("")
                    .append(Text.literal("[chat-with-npc] ChatWithNPC").formatted(Formatting.UNDERLINE))
                    .append("").formatted(Formatting.RESET)
                    .append("\nEnabled: ").append(SettingManager.enabled ? yes : no)
                    .append("\nAPI Key: ").append(hasKey ? yes : no)
                    .append("\nChat Bubble: ").append(SettingManager.isBubble ? yes : no)
                    .append("\nChat Bar: ").append(SettingManager.isChatBar ? yes : no)
                    .append("\nModel: ").append(SettingManager.model)
                    .append("\nRange: ").append(String.valueOf(SettingManager.range))
                    .append("\nLanguage: ").append(SettingManager.language)
                    .append("\nMax Tokens: ").append(String.valueOf(SettingManager.maxTokens))
                    .append("\nAPI URL: ").append(SettingManager.apiURL)
                    .append("\nUse ").append(Text.literal("/npchat help").formatted(Formatting.GRAY)).append(" for help");
            context.getSource().sendFeedback(helpText, false);
        }
        return 1;
    }

    private static int npcStatus(@NotNull CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ConversationHandler conversation = ConversationManager.getConversation(player);
        if (conversation == null) return 0;
        NPCEntity npc = conversation.getNpc();
        // show all the properties of the npc
        Text yes = Text.literal("Yes").formatted(Formatting.GREEN);
        Text no = Text.literal("No").formatted(Formatting.RED);
        Text statusText = Text.literal("")
                .append(Text.literal("[chat-with-npc] NPC Status:").formatted(Formatting.UNDERLINE))
                .append("").formatted(Formatting.RESET)
                .append("\nName: ").append(Text.literal(npc.getName()).formatted(Formatting.GOLD))
                .append("\nGroups: ").append(Text.literal(
                        String.join("->", GroupManager.getParentGroups(npc.getGroup()).stream().map(Group::getName).toList())
                ).formatted(Formatting.GOLD))
                .append("\nCareer: ").append(Text.literal(npc.getCareer()).formatted(Formatting.AQUA))
                .append("\nInstructions: ").append(Text.literal(npc.getInstructions()).formatted(Formatting.BLUE))
                .append("\nNeed memory: ").append(npc.isNeedMemory() ? yes : no)
                .append("\nFunctions: ").append(Text.literal(String.join(", ", npc.getFunctions())).formatted(Formatting.DARK_PURPLE))
                // converge Long to real time
                .append("\nLast Message Time: ").append(Text.literal(String.valueOf(conversation.getUpdateTimeString())).formatted(Formatting.GRAY))
                .append("\nUse ").append(Text.literal("/npchat help").formatted(Formatting.GRAY)).append(" for help");
        context.getSource().sendFeedback(statusText, false);
        return 1;
    }

    private static int groupStatus(@NotNull CommandContext<ServerCommandSource> context) {
        String group = context.getArgument("group", String.class);
        Group g = GroupManager.getGroup(group);
        Text statusText = Text.literal("")
                .append(Text.literal("[chat-with-npc] Group Status:").formatted(Formatting.UNDERLINE))
                .append("").formatted(Formatting.RESET)
                .append("\nName: ").append(Text.literal(g.getName()).formatted(Formatting.GOLD))
                .append("\nParent Groups: ").append(Text.literal(
                        String.join("->", GroupManager.getParentGroups(g.getName()).stream().map(Group::getName).toList())
                ).formatted(Formatting.GOLD))
                .append("\nPermanent Prompts: ").append(Text.literal(String.join(", ", g.getPermanentPrompt())).formatted(Formatting.AQUA))
                .append("\nTemp Events: ").append(Text.literal(
                        String.join(", ", g.getTempEvent().stream().map(longStringMap -> longStringMap.values().iterator().next()).toList())
                ).formatted(Formatting.BLUE))
                .append("\nLast Load Time: ").append(Text.literal(String.valueOf(g.getLastLoadTimeString())).formatted(Formatting.GRAY))
                .append("\nUse ").append(Text.literal("/npchat help").formatted(Formatting.GRAY)).append(" for help");
        context.getSource().sendFeedback(statusText, false);
        return 1;
    }

    private static int saveAll(@NotNull CommandContext<ServerCommandSource> context) {
        LiveCycleManager.asyncSaveAll();
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Reloaded"), true);
        return 1;
    }

    public static int setEnabled(@NotNull CommandContext<ServerCommandSource> context, boolean enabled) {
        SettingManager.enabled = enabled;
        SettingManager.save();
        context.getSource().sendFeedback(Text.of("[chat-with-npc] ChatWithNPC " + (enabled ? "enabled" : "disabled")), true);
        return 1;
    }

    public static int setAPIKey(@NotNull CommandContext<ServerCommandSource> context) {
        String apiKey = context.getArgument("key", String.class);
        if (!apiKey.isEmpty()) {
            SettingManager.apiKey = apiKey;
            SettingManager.save();
            context.getSource().sendFeedback(Text.of("[chat-with-npc] API key set"), true);
            return 1;
        }
        return 0;
    }

    public static int setModel(@NotNull CommandContext<ServerCommandSource> context) {
        String model = context.getArgument("model", String.class);
        if (!model.isEmpty()) {
            SettingManager.model = model;
            SettingManager.save();
            context.getSource().sendFeedback(Text.of("[chat-with-npc] Model set"), true);
            return 1;
        }
        return 0;
    }

    private static int setChatBar(@NotNull CommandContext<ServerCommandSource> context) {
        SettingManager.isChatBar = context.getArgument("isChatBar", Boolean.class);
        SettingManager.save();
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Chat bar set"), true);
        return 1;
    }

    private static int setBubble(@NotNull CommandContext<ServerCommandSource> context) {
        SettingManager.isBubble = context.getArgument("isBubble", Boolean.class);
        SettingManager.save();
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Bubble set"), true);
        return 1;
    }

    private static int setURL(@NotNull CommandContext<ServerCommandSource> context) {
        SettingManager.apiURL = context.getArgument("url", String.class);
        SettingManager.save();
        context.getSource().sendFeedback(Text.of("[chat-with-npc] URL set"), true);
        return 1;
    }

    private static int setMaxTokens(@NotNull CommandContext<ServerCommandSource> context) {
        SettingManager.maxTokens = context.getArgument("maxTokens", Integer.class);
        SettingManager.save();
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Max tokens set"), true);
        return 1;
    }

    private static int setLanguage(@NotNull CommandContext<ServerCommandSource> context) {
        SettingManager.language = context.getArgument("language", String.class);
        SettingManager.save();
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Language set"), true);
        return 1;
    }

    private static int setRange(@NotNull CommandContext<ServerCommandSource> context) {
        SettingManager.range = Double.parseDouble(context.getArgument("range", String.class));
        SettingManager.save();
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Range set"), true);
        return 1;
    }

    private static int addGroup(@NotNull CommandContext<ServerCommandSource> context) {
        String group = context.getArgument("newGroup", String.class);
        GroupManager.loadGroup(group);
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Group added"), true);
        return 1;
    }

    private static int addGroupTempEvent(@NotNull CommandContext<ServerCommandSource> context) {
        String group = context.getArgument("group", String.class);
        String event = context.getArgument("event", String.class);
        // default time is 7 days
        GroupManager.getGroup(group).addTempEvent(event, 7 * 24 * 60 * 60 * 1000);
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Temp event added"), true);
        return 1;
    }

    private static int popGroupTempEvent(@NotNull CommandContext<ServerCommandSource> context) {
        String group = context.getArgument("group", String.class);
        GroupManager.getGroup(group).popTempEvent();
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Temp event popped"), true);
        return 1;
    }

    private static int addGroupPermanentPrompt(@NotNull CommandContext<ServerCommandSource> context) {
        String group = context.getArgument("group", String.class);
        String prompt = context.getArgument("prompt", String.class);
        GroupManager.getGroup(group).addPermanentPrompt(prompt);
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Permanent prompt added"), true);
        return 1;
    }

    private static int popGroupPermanentPrompt(@NotNull CommandContext<ServerCommandSource> context) {
        String group = context.getArgument("group", String.class);
        GroupManager.getGroup(group).popPermanentPrompt();
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Permanent prompt popped"), true);
        return 1;
    }

    private static int setGroupParent(@NotNull CommandContext<ServerCommandSource> context) {
        String group = context.getArgument("group", String.class);
        String parent = context.getArgument("parent", String.class);
        GroupManager.getGroup(group).setParentGroup(parent);
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Group parent set"), true);
        return 1;
    }

    private static int setNeedMemory(@NotNull CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ConversationHandler conversation = ConversationManager.getConversation(player);
        if (player != null && conversation != null) {
            boolean isNeedMemory = context.getArgument("isNeedMemory", Boolean.class);
            conversation.getNpc().setNeedMemory(isNeedMemory);
            player.sendMessage(Text.of("[chat-with-npc] Need memory set."), true);
            return 1;
        }
        if (player != null) {
            player.sendMessage(Text.of("[chat-with-npc] You are not in a conversation."), true);
        }
        return 0;
    }

    private static int clearNPCMemory(@NotNull CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ConversationHandler conversation = ConversationManager.getConversation(player);
        if (player != null && conversation != null) {
            NPCEntity npc = conversation.getNpc();
            if (npc.getThreadId() != null){
                boolean isOK = conversation.taskQueue.addTask(() -> {
                    try {
                        Threads.discardThread(npc.getThreadId());
                        npc.setThreadId(null);
                        NPCEntityManager.getNPCEntity(npc.getUUID()).getDataManager().save();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                if (!isOK) return 0;
            }
            player.sendMessage(Text.of("[chat-with-npc] Memory clear."), true);
            return 1;
        }
        if (player != null) {
            player.sendMessage(Text.of("[chat-with-npc] You are not in a conversation."), true);
        }
        return 0;
    }

    private static int setNPCInstructions(@NotNull CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ConversationHandler conversation = ConversationManager.getConversation(player);
        if (player != null && conversation != null) {
            String instructions = context.getArgument("instructions", String.class);
            conversation.getNpc().setInstructions(instructions);
            player.sendMessage(Text.of("[chat-with-npc] Instructions set."), true);
            return 1;
        }
        if (player != null) {
            player.sendMessage(Text.of("[chat-with-npc] You are not in a conversation."), true);
        }
        return 0;
    }

    private static int setNPCGroup(@NotNull CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ConversationHandler conversation = ConversationManager.getConversation(player);
        if (player != null && conversation != null) {
            String group = context.getArgument("group", String.class);
            conversation.getNpc().setGroup(group);
            player.sendMessage(Text.of("[chat-with-npc] Group set."), true);
            return 1;
        }
        if (player != null) {
            player.sendMessage(Text.of("[chat-with-npc] You are not in a conversation."), true);
        }
        return 0;
    }

    private static int setNPCCareer(@NotNull CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ConversationHandler conversation = ConversationManager.getConversation(player);
        if (player != null && conversation != null) {
            String career = context.getArgument("career", String.class);
            conversation.getNpc().setCareer(career);
            player.sendMessage(Text.of("[chat-with-npc] Career set."), true);
            return 1;
        }
        if (player != null) {
            player.sendMessage(Text.of("[chat-with-npc] You are not in a conversation."), true);
        }
        return 0;
    }

    private static int addNPCFunction(@NotNull CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ConversationHandler conversation = ConversationManager.getConversation(player);
        if (player != null && conversation != null) {
            String function = context.getArgument("function", String.class);
            conversation.getNpc().addFunction(function);
            player.sendMessage(Text.of("[chat-with-npc] Function added."), true);
            return 1;
        }
        if (player != null) {
            player.sendMessage(Text.of("[chat-with-npc] You are not in a conversation."), true);
        }
        return 0;
    }

    private static int deleteNPCFunction(@NotNull CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ConversationHandler conversation = ConversationManager.getConversation(player);
        if (player != null && conversation != null) {
            String function = context.getArgument("function", String.class);
            conversation.getNpc().removeFunction(function);
            player.sendMessage(Text.of("[chat-with-npc] Function removed."), true);
            return 1;
        }
        if (player != null) {
            player.sendMessage(Text.of("[chat-with-npc] You are not in a conversation."), true);
        }
        return 0;
    }

    private static int npcUpdate(@NotNull CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ConversationHandler conversation = ConversationManager.getConversation(player);
        if (player != null && conversation != null) {
            NPCEntity npc = conversation.getNpc();
            if (npc.hasAssistant()) {
                boolean isOK = conversation.taskQueue.addTask(() -> {
                    try {
                        Assistant.modifyAssistant(npc);
                    } catch (Exception e) {
                        ChatWithNPCMod.LOGGER.error(e.getMessage());
                    }
                });
                if (!isOK) return 0;
            }
        }
        return 1;
    }
}
