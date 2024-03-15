package com.jackdaw.chatwithnpc.auxiliary.command;

import com.jackdaw.chatwithnpc.UpdateStaticData;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import com.jackdaw.chatwithnpc.conversation.ConversationManager;
import com.jackdaw.chatwithnpc.group.Group;
import com.jackdaw.chatwithnpc.group.GroupManager;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.mojang.brigadier.CommandDispatcher;
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

    public static void setupCommand(@NotNull CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(literal("npchat")
                .executes(CommandSet::status)
                .then(literal("help")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                        .executes(CommandSet::help))
                .then(literal("setkey")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                        .then(argument("key", StringArgumentType.string())
                                .executes(CommandSet::setAPIKey)))
                .then(literal("setmodel")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                        .then(argument("model", StringArgumentType.string())
                                .executes(CommandSet::setModel)))
                .then(literal("enable")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                        .executes(context -> setEnabled(context, true)))
                .then(literal("disable")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                        .executes(context -> setEnabled(context, false)))
                .then(literal("setrange")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                        .then(argument("range", StringArgumentType.word())
                                .executes(context -> {
                                    SettingManager.range = Double.parseDouble(context.getArgument("range", String.class));
                                    SettingManager.save();
                                    context.getSource().sendFeedback(Text.of("[chat-with-npc] Range set"), true);
                                    return 1;})))
                .then(literal("setforgettime")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                        .then(argument("time", StringArgumentType.word())
                                .executes(context -> {
                                    SettingManager.forgetTime = Long.parseLong(context.getArgument("time", String.class));
                                    SettingManager.save();
                                    context.getSource().sendFeedback(Text.of("[chat-with-npc] Forget time set"), true);
                                    return 1;})))
                .then(literal("setlanguage")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                        .then(argument("language", StringArgumentType.word())
                                .executes(context -> {
                                    SettingManager.language = context.getArgument("language", String.class);
                                    SettingManager.save();
                                    context.getSource().sendFeedback(Text.of("[chat-with-npc] Language set"), true);
                                    return 1;})))
                .then(literal("setmaxtokens")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                        .then(argument("maxTokens", StringArgumentType.word())
                                .executes(context -> {
                                    SettingManager.maxTokens = context.getArgument("maxTokens", Integer.class);
                                    SettingManager.save();
                                    context.getSource().sendFeedback(Text.of("[chat-with-npc] Max tokens set"), true);
                                    return 1;})))
                .then(literal("setURL")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                        .then(argument("url", StringArgumentType.string())
                                .executes(context -> {
                                    SettingManager.apiURL = context.getArgument("url", String.class);
                                    SettingManager.save();
                                    context.getSource().sendFeedback(Text.of("[chat-with-npc] URL set"), true);
                                    return 1;})))
                .then(literal("setbubble")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                        .then(argument("isBubble", StringArgumentType.word())
                                .executes(context -> {
                                    SettingManager.isBubble = context.getArgument("isBubble", Boolean.class);
                                    SettingManager.save();
                                    context.getSource().sendFeedback(Text.of("[chat-with-npc] Bubble set"), true);
                                    return 1;})))
                .then(literal("setchatbar")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                        .then(argument("isChatBar", StringArgumentType.word())
                                .executes(context -> {
                                    SettingManager.isChatBar = context.getArgument("isChatBar", Boolean.class);
                                    SettingManager.save();
                                    context.getSource().sendFeedback(Text.of("[chat-with-npc] Chat bar set"), true);
                                    return 1;})))
                .then(literal("npc")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                        .then(literal("setCareer")
                                .then(argument("career", StringArgumentType.greedyString())
                                        .executes(CommandSet::setNPCCareer)))
                        .then(literal("setGroup")
                                .then(argument("group", StringArgumentType.word())
                                        .suggests(groupSuggestionProvider)
                                        .executes(CommandSet::setNPCGroup)))
                        .then(literal("setBackground")
                                .then(argument("prompt", StringArgumentType.greedyString())
                                        .executes(CommandSet::setNPCPrompt)))
                        .then(literal("clearMemory")
                                .executes(CommandSet::clearNPCMemory))
                        .executes(CommandSet::npcStatus))
                .then(literal("group")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
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
                                .executes(CommandSet::groupStatus)))
                .then(literal("addGroup")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                        .then(argument("newGroup", StringArgumentType.word())
                                .executes(context -> {
                                    String group = context.getArgument("newGroup", String.class);
                                    GroupManager.loadEnvironment(group);
                                    context.getSource().sendFeedback(Text.of("[chat-with-npc] Group added"), true);
                                    return 1;})))
                .then(literal("reload")
                        .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(4))
                        .executes(context -> {
                            Thread t = new Thread(() -> {
                                try {
                                    UpdateStaticData.close();
                                    SettingManager.sync();
                                    context.getSource().sendFeedback(Text.of("[chat-with-npc] Reloaded"), true);
                                } catch (Exception ignore) {}
                            });
                            t.start();
                            return 1;
                        }))
        );
    }

    static SuggestionProvider<ServerCommandSource> groupSuggestionProvider = (context, builder) -> {
        for (String group : GroupManager.getGroupList()) {
            builder.suggest(group);
        }
        return builder.buildFuture();
    };

    private static int groupStatus(CommandContext<ServerCommandSource> context) {
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

    private static int popGroupPermanentPrompt(CommandContext<ServerCommandSource> context) {
        String group = context.getArgument("group", String.class);
        GroupManager.getGroup(group).popPermanentPrompt();
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Permanent prompt popped"), true);
        return 1;
    }

    private static int addGroupTempEvent(CommandContext<ServerCommandSource> context) {
        String group = context.getArgument("group", String.class);
        String event = context.getArgument("event", String.class);
        // default time is 7 days
        GroupManager.getGroup(group).addTempEvent(event, 7 * 24 * 60 * 60 * 1000);
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Temp event added"), true);
        return 1;
    }

    private static int addGroupPermanentPrompt(CommandContext<ServerCommandSource> context) {
        String group = context.getArgument("group", String.class);
        String prompt = context.getArgument("prompt", String.class);
        GroupManager.getGroup(group).addPermanentPrompt(prompt);
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Permanent prompt added"), true);
        return 1;
    }


    private static int setGroupParent(CommandContext<ServerCommandSource> context) {
        String group = context.getArgument("group", String.class);
        String parent = context.getArgument("parent", String.class);
        GroupManager.getGroup(group).setParentGroup(parent);
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Group parent set"), true);
        return 1;
    }

    private static int npcStatus(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ConversationHandler conversation = ConversationManager.getConversation(player);
        if (conversation == null) return 0;
        NPCEntity npc = conversation.getNpc();
        // show all the properties of the npc
        Text statusText = Text.literal("")
                .append(Text.literal("[chat-with-npc] NPC Status:").formatted(Formatting.UNDERLINE))
                .append("").formatted(Formatting.RESET)
                .append("\nName: ").append(Text.literal(npc.getName()).formatted(Formatting.GOLD))
                .append("\nGroups: ").append(Text.literal(
                        String.join("->", GroupManager.getParentGroups(npc.getGroup()).stream().map(Group::getName).toList())
                ).formatted(Formatting.GOLD))
                .append("\nCareer: ").append(Text.literal(npc.getCareer()).formatted(Formatting.AQUA))
                .append("\nBackground: ").append(Text.literal(npc.getBasicPrompt()).formatted(Formatting.BLUE))
                .append("\nIs memory: ").append(Text.literal(String.valueOf(!npc.getLongTermMemory().isEmpty())).formatted(Formatting.LIGHT_PURPLE))
                // converge Long to real time
                .append("\nLast Message Time: ").append(Text.literal(String.valueOf(conversation.getUpdateTimeString())).formatted(Formatting.GRAY))
                .append("\nUse ").append(Text.literal("/npchat help").formatted(Formatting.GRAY)).append(" for help");
        context.getSource().sendFeedback(statusText, false);
        return 1;
    }

    private static int clearNPCMemory(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player != null && ConversationManager.getConversation(player) != null){
            ConversationHandler conversation = ConversationManager.getConversation(player);
            if (conversation == null) return 0;
            conversation.clearMessageRecord();
            conversation.getNpc().deleteLongTermMemory(Long.MAX_VALUE);
            player.sendMessage(Text.of("[chat-with-npc] Memory clear."), true);
            return 1;
        }
        if (player != null) {
            player.sendMessage(Text.of("[chat-with-npc] You are not in a conversation."), true);
        }
        return 0;
    }

    private static int setNPCPrompt(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String prompt = context.getArgument("prompt", String.class);
        if (player != null && ConversationManager.getConversation(player) != null){
            ConversationHandler conversation = ConversationManager.getConversation(player);
            if (conversation == null) return 0;
            conversation.getNpc().setBasicPrompt(prompt);
            player.sendMessage(Text.of("[chat-with-npc] Background set."), true);
            return 1;
        }
        if (player != null) {
            player.sendMessage(Text.of("[chat-with-npc] You are not in a conversation."), true);
        }
        return 0;
    }

    private static int setNPCGroup(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String group = context.getArgument("group", String.class);
        if (player != null && ConversationManager.getConversation(player) != null){
            ConversationHandler conversation = ConversationManager.getConversation(player);
            if (conversation == null) return 0;
            conversation.getNpc().setGroup(group);
            player.sendMessage(Text.of("[chat-with-npc] Group set."), true);
            return 1;
        }
        if (player != null) {
            player.sendMessage(Text.of("[chat-with-npc] You are not in a conversation."), true);
        }
        return 0;
    }

    private static int setNPCCareer(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String career = context.getArgument("career", String.class);
        if (player != null && ConversationManager.getConversation(player) != null){
            ConversationHandler conversation = ConversationManager.getConversation(player);
            if (conversation == null) return 0;
            conversation.getNpc().setCareer(career);
            player.sendMessage(Text.of("[chat-with-npc] Career set."), true);
            return 1;
        }
        if (player != null) {
            player.sendMessage(Text.of("[chat-with-npc] You are not in a conversation."), true);
        }
        return 0;
    }

    public static int setEnabled(CommandContext<ServerCommandSource> context, boolean enabled) {
        SettingManager.enabled = enabled;
        SettingManager.save();
        context.getSource().sendFeedback(Text.of("[chat-with-npc] ChatWithNPC " + (enabled ? "enabled" : "disabled")), true);
        return 1;
    }

    public static int status(CommandContext<ServerCommandSource> context) {
        Text yes = Text.literal("Yes").formatted(Formatting.GREEN);
        Text no = Text.literal("No").formatted(Formatting.RED);
        if (!context.getSource().hasPermissionLevel(4)) {
            Text helpText = Text.literal("")
                    .append("[chat-with-npc] ChatWithNPC:").formatted(Formatting.UNDERLINE)
                    .append("\nEnabled: ").append(SettingManager.enabled ? yes : no)
                    .append("\nChat Bubble: ").append(SettingManager.isBubble ? yes : no)
                    .append("\nChat Bar: ").append(SettingManager.isChatBar ? yes : no)
                    .append("\nModel: ").append(SettingManager.model)
                    .append("\nLanguage: ").append(SettingManager.language)
                    .append("\nYou can start a conversation to mobs by shift-clicking on them!")
                    .append("\nOnce you are in a conversation, you can reply to the NPC by typing in the chat.");
            context.getSource().sendFeedback(helpText, false);
            return 1;
        }
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
                .append("\nForget Time: ").append(String.valueOf(SettingManager.forgetTime))
                .append("\nLanguage: ").append(SettingManager.language)
                .append("\nMax Tokens: ").append(String.valueOf(SettingManager.maxTokens))
                .append("\nUse ").append(Text.literal("/npchat help").formatted(Formatting.GRAY)).append(" for help");
        context.getSource().sendFeedback(helpText, false);
        return 1;
    }

    public static int help(CommandContext<ServerCommandSource> context) {
        Text helpText = Text.literal("")
                .append("[chat-with-npc] ChatWithNPC Commands").formatted(Formatting.UNDERLINE)
                .append("").formatted(Formatting.RESET)
                .append("\n/npchat - View configuration status")
                .append("\n/npchat help - View commands help")
                .append("\n/npchat enable/disable - Enable/disable the mod")
                .append("\n/npchat setkey <key> - Set OpenAI API key")
                .append("\n/npchat setmodel <model> - Set AI model")
                .append("\n/npchat setrange <range> - Set the range of the conversation")
                .append("\n/npchat setforgettime <time> - Set the time to forget the memory")
                .append("\n/npchat setlanguage <language> - Set the response language")
                .append("\n/npchat setmaxtokens <maxTokens> - Set the max tokens of a conversation")
                .append("\n/npchat setURL <url> - Set the AI Model URL")
                .append("\n/npchat reload - Reload the plugin")
                .append("\n --------------------------------")
                .append("\nOnce you are in a conversation, you can use /npchat npc to set the properties of the NPC.")
                .append("\n/npchat npc - NPC commands")
                .append("\n/npchat npc setCareer <career> - Set the career for the closest NPC.")
                .append("\n/npchat npc setGroup <group> - Set the group for the closest NPC.")
                .append("\n/npchat npc setBackground <prompt> - Set the background for the closest NPC.")
                .append("\n/npchat npc clearMemory - Clear the memory for the closest NPC.")
                .append("\n --------------------------------")
                .append("\n/npchat group <group> - Group commands")
                .append("\n/npchat group <group> setParent <parent> - Set the parent group for the group.")
                .append("\n/npchat group <group> addPermanentPrompt <prompt> - Add a permanent prompt to the group.")
                .append("\n/npchat group <group> popPermanentPrompt - Pop a permanent prompt from the group.")
                .append("\n/npchat group <group> addTempEvent <event> - Add a temporary event to the group.")
                .append("\n/npchat addGroup <newGroup> - Add a new group")
                .append("\n --------------------------------")
                .append("\nYou can start a conversation to mobs by shift-clicking on them!")
                .append("\nOnce you are in a conversation, you can reply to the NPC by typing in the chat.");
        context.getSource().sendFeedback(helpText, false);
        return 1;
    }
    public static int setAPIKey(CommandContext<ServerCommandSource> context) {
        String apiKey = context.getArgument("key", String.class);
        if (!apiKey.isEmpty()) {
            SettingManager.apiKey = apiKey;
            SettingManager.save();
            context.getSource().sendFeedback(Text.of("[chat-with-npc] API key set"), true);
            return 1;
        }
        return 0;
    }
    public static int setModel(CommandContext<ServerCommandSource> context) {
        String model = context.getArgument("model", String.class);
        if (!model.isEmpty()) {
            SettingManager.model = model;
            SettingManager.save();
            context.getSource().sendFeedback(Text.of("[chat-with-npc] Model set"), true);
            return 1;
        }
        return 0;
    }
}
