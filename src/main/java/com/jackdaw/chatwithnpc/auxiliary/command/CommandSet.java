package com.jackdaw.chatwithnpc.auxiliary.command;

import com.jackdaw.chatwithnpc.UpdateStaticData;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import com.jackdaw.chatwithnpc.conversation.ConversationManager;
import com.jackdaw.chatwithnpc.group.Group;
import com.jackdaw.chatwithnpc.group.GroupManager;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
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
                .then(literal("help").executes(CommandSet::help))
                .then(literal("setkey")
                        .then(argument("key", StringArgumentType.string())
                                .executes(CommandSet::setAPIKey)
                        ))
                .then(literal("setmodel")
                        .then(argument("model", StringArgumentType.string())
                                .executes(CommandSet::setModel)
                        ))
                .then(literal("settemp")
                        .then(argument("temperature", FloatArgumentType.floatArg(0,1))
                                .executes(CommandSet::setTemp)
                        ))
                .then(literal("enable").executes(context -> setEnabled(context, true)))
                .then(literal("disable").executes(context -> setEnabled(context, false)))
                .then(literal("npc")
                        .then(literal("setCareer")
                                .then(argument("career", StringArgumentType.greedyString())
                                        .executes(CommandSet::setNPCCareer)
                                ))
                        .then(literal("setGroup")
                                .then(argument("group", StringArgumentType.greedyString())
                                        .suggests(groupSuggestionProvider)
                                        .executes(CommandSet::setNPCGroup)
                                ))
                        .then(literal("setBackground")
                                .then(argument("prompt", StringArgumentType.greedyString())
                                        .executes(CommandSet::setNPCPrompt)
                                ))
                        .executes(CommandSet::npcStatus))
                .then(literal("group")
                        .then(argument("group", StringArgumentType.greedyString())
                                .suggests(groupSuggestionProvider)
                                .then(literal("setParent")
                                        .then(argument("parent", StringArgumentType.greedyString())
                                                .executes(CommandSet::setGroupParent)
                                        ))
                                .then(literal("addPermanentPrompt")
                                        .then(argument("prompt", StringArgumentType.greedyString())
                                                .executes(CommandSet::addGroupPermanentPrompt)
                                        ))
                                .then(literal("addTempEvent")
                                        .then(argument("event", StringArgumentType.greedyString())
                                                        .executes(CommandSet::addGroupTempEvent)
                                        ))
                                .executes(CommandSet::groupStatus)
                        ))
                .then(literal("reload")
                        .executes(context -> {
                            UpdateStaticData.close();
                            context.getSource().sendFeedback(Text.of("[chat-with-npc] Reloaded"), true);
                            return 1;
                        })
                )
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
                .append("\nLast Load Time: ").append(Text.literal(String.valueOf(g.getLastLoadTime())).formatted(Formatting.GRAY))
                .append("\nUse ").append(Text.literal("/npchat help").formatted(Formatting.GRAY)).append(" for help");
        context.getSource().sendFeedback(statusText, false);
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
                .append("\nLast Message Time: ").append(Text.literal(String.valueOf(npc.getLastMessageTime())).formatted(Formatting.GRAY))
                .append("\nUse ").append(Text.literal("/npchat help").formatted(Formatting.GRAY)).append(" for help");
        context.getSource().sendFeedback(statusText, false);
        return 1;
    }

    private static int setNPCPrompt(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        String prompt = context.getArgument("prompt", String.class);
        if (player != null && ConversationManager.getConversation(player) != null){
            ConversationManager.getConversation(player).getNpc().setBasicPrompt(prompt);
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
            ConversationManager.getConversation(player).getNpc().setGroup(group);
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
            ConversationManager.getConversation(player).getNpc().setCareer(career);
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
        boolean hasKey = !SettingManager.apiKey.isEmpty();
        Text yes = Text.literal("Yes").formatted(Formatting.GREEN);
        Text no = Text.literal("No").formatted(Formatting.RED);
        Text helpText = Text.literal("")
                .append(Text.literal("[chat-with-npc] ChatWithNPC").formatted(Formatting.UNDERLINE))
                .append("").formatted(Formatting.RESET)
                .append("\nEnabled: ").append(SettingManager.enabled ? yes : no)
                .append("\nAPI Key: ").append(hasKey ? yes : no)
                .append("\nModel: ").append(SettingManager.model)
                .append("\nTemp: ").append(String.valueOf(SettingManager.temperature))
                .append("\n\nUse ").append(Text.literal("/npchat help").formatted(Formatting.GRAY)).append(" for help");
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
                .append("\n/npchat settemp <temperature> - Set model temperature")
                .append("\nYou can talk to mobs by shift-clicking on them!");
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
    public static int setTemp(CommandContext<ServerCommandSource> context) {
        SettingManager.temperature = context.getArgument("temperature", float.class);
        SettingManager.save();
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Temperature set"), true);
        return 1;
    }
}
