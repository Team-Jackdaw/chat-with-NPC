package com.jackdaw.chatwithnpc;

import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import com.jackdaw.chatwithnpc.conversation.ConversationManager;
import com.jackdaw.chatwithnpc.group.Group;
import com.jackdaw.chatwithnpc.group.GroupManager;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.jackdaw.chatwithnpc.npc.NPCEntityManager;
import com.jackdaw.chatwithnpc.npc.TextBubbleEntity;
import com.jackdaw.chatwithnpc.openaiapi.Threads;
import com.jackdaw.chatwithnpc.openaiapi.function.FunctionManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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

    private static final SuggestionProvider<ServerCommandSource> bubbleColorProvider = (context, builder) -> {
        for (TextBubbleEntity.TextBackgroundColor color : TextBubbleEntity.TextBackgroundColor.values()) {
            builder.suggest(color.name());
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
        return source.hasPermissionLevel(2);
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
                .then(literal("setWordLimit")
                        .requires(CommandSet::hasOPPermission)
                        .then(argument("wordLimit", IntegerArgumentType.integer(10))
                                .executes(CommandSet::setWordLimit)))
                .then(literal("setURL")
                        .requires(CommandSet::hasOPPermission)
                        .then(argument("url", StringArgumentType.string())
                                .executes(CommandSet::setURL)))
                .then(literal("setBubble")
                        .requires(CommandSet::hasOPPermission)
                        .then(argument("isBubble", BoolArgumentType.bool())
                                .executes(CommandSet::setBubble)))
                .then(literal("setBubbleStyle")
                        .requires(CommandSet::hasOPPermission)
                        .then(literal("Color")
                                .then(argument("color", StringArgumentType.word())
                                        .suggests(bubbleColorProvider)
                                        .executes(CommandSet::setBubbleColor))))
                .then(literal("setBubbleStyle")
                        .requires(CommandSet::hasOPPermission)
                        .then(literal("timeLastingPerChar")
                                .then(argument("time lasting per character in second", FloatArgumentType.floatArg(0.0f))
                                        .executes(CommandSet::setBubbleTimeLastingPerChar))))
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
                                .then(literal("setInstruction")
                                        .then(argument("instruction", StringArgumentType.greedyString())
                                                .executes(CommandSet::setGroupInstruction)))
                                .then(literal("addEvent")
                                        .then(argument("event", StringArgumentType.greedyString())
                                                .executes(CommandSet::addGroupEvent)))
                                .then(literal("popEvent")
                                        .executes(CommandSet::popGroupEvent))
                                .executes(CommandSet::groupStatus))
                        .executes(CommandSet::allGroupStatus))
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
                .append("\n/npchat setLanguage <language> - Set the response language")
                .append("\n/npchat setWordLimit <wordLimit> - Set the word limit of the response")
                .append("\n/npchat setURL <url> - Set the AI Model URL")
                .append("\n/npchat saveAll - Discard all instances, save all the data and reload the mod.")
                .append("\n --------------------------------")
                .append("\nOnce you are in a conversation, you can use /npchat npc to set the properties of the NPC.")
                .append("\n/npchat npc - The closest NPC status")
                .append("\n/npchat npc setCareer <career> - Set the career for the closest NPC.")
                .append("\n/npchat npc setGroup <group> - Set the group for the closest NPC.")
                .append("\n/npchat npc setInstructions <instructions> - Set the instructions for the closest NPC.")
                .append("\n/npchat npc isNeedMemory <isNeedMemory> - Set the need memory for the closest NPC.")
                .append("\n/npchat npc addFunction <function> - Add a function to the closest NPC.")
                .append("\n/npchat npc deleteFunction <function> - Delete a function from the closest NPC.")
                .append("\n/npchat npc clearMemory - Clear the memory for the closest NPC.")
                .append("\n --------------------------------")
                .append("\n/npchat group <group> - The group status")
                .append("\n/npchat group <group> setParent <parent> - Set the parent group for the group.")
                .append("\n/npchat group <group> setInstruction <instruction> - Set the instruction for the group.")
                .append("\n/npchat group <group> addEvent <event> - Add a temporary event to the group.")
                .append("\n/npchat group <group> popEvent - Pop a temporary event from the group.")
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
        if (!context.getSource().hasPermissionLevel(2)) {
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
                    .append("\nAPI URL: ").append(SettingManager.apiURL)
                    .append("\nUse ").append(Text.literal("/npchat help").formatted(Formatting.GRAY)).append(" for help");
            context.getSource().sendFeedback(helpText, false);
        }
        return 1;
    }

    private static int allGroupStatus(@NotNull CommandContext<ServerCommandSource> context) {
        Text statusText = Text.literal("")
                .append(Text.literal("[chat-with-npc] Group List:").formatted(Formatting.UNDERLINE))
                .append("").formatted(Formatting.RESET)
                .append("\n").append(Text.literal(String.join(", ", GroupManager.getGroupList())).formatted(Formatting.GOLD))
                .append("\n").append(Text.literal(GroupManager.getGroupTree("Global")).formatted(Formatting.BLUE))
                .append("\nUse ").append(Text.literal("/npchat help").formatted(Formatting.GRAY)).append(" for help");
        context.getSource().sendFeedback(statusText, false);
        return 1;
    }

    private static int npcStatus(@NotNull CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        NPCEntity npc = NPCEntityManager.getNPCEntity(player);
        if (npc == null) {
            if (player != null) {
                player.sendMessage(Text.of("[chat-with-npc] No NPC near you."), true);
            }
            return 0;
        }
        ConversationHandler conversation = ConversationManager.getConversation(player);
        String lastMessageTime = conversation != null ? conversation.getUpdateTimeString() : "N/A";
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
                .append("\nLast Message Time: ").append(Text.literal(String.valueOf(lastMessageTime)).formatted(Formatting.GRAY))
                .append("\nUse ").append(Text.literal("/npchat help").formatted(Formatting.GRAY)).append(" for help");
        context.getSource().sendFeedback(statusText, false);
        return 1;
    }

    private static int groupStatus(@NotNull CommandContext<ServerCommandSource> context) {
        String group = context.getArgument("group", String.class);
        Group g = GroupManager.getGroup(group);
        if (g == null) {
            context.getSource().sendFeedback(Text.of("[chat-with-npc] Group not found."), false);
            return 0;
        }
        Text statusText = Text.literal("")
                .append(Text.literal("[chat-with-npc] Group Status:").formatted(Formatting.UNDERLINE))
                .append("").formatted(Formatting.RESET)
                .append("\nName: ").append(Text.literal(g.getName()).formatted(Formatting.GOLD))
                .append("\nParent Groups: ").append(Text.literal(
                        String.join("->", GroupManager.getParentGroups(g.getName()).stream().map(Group::getName).toList())
                ).formatted(Formatting.GOLD))
                .append("\nInstruction: ").append(Text.literal(g.getInstruction()).formatted(Formatting.AQUA))
                .append("\nTemp Events: ").append(Text.literal(
                        String.join(", ", g.getEvent())
                ).formatted(Formatting.BLUE))
                .append("\n Member: ").append(Text.literal(String.join(", ", g.getMemberList())).formatted(Formatting.DARK_PURPLE))
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

    private static int setBubbleColor(@NotNull CommandContext<ServerCommandSource> context) {
        SettingManager.bubbleColor = TextBubbleEntity.TextBackgroundColor.valueOf(context.getArgument("color", String.class));
        SettingManager.save();
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Bubble color set"), true);
        return 1;
    }

    private static int setBubbleTimeLastingPerChar(@NotNull CommandContext<ServerCommandSource> context) {
        // unit in seconds to user side, while in milliseconds for inside implementation.
        SettingManager.timeLastingPerChar = (long) (1000L * context.getArgument("time lasting per character in second", Float.class));
        SettingManager.save();
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Bubble time lasting per character set"), true);
        return 1;
    }

    private static int setURL(@NotNull CommandContext<ServerCommandSource> context) {
        SettingManager.apiURL = context.getArgument("url", String.class);
        SettingManager.save();
        context.getSource().sendFeedback(Text.of("[chat-with-npc] URL set"), true);
        return 1;
    }

    private static int setLanguage(@NotNull CommandContext<ServerCommandSource> context) {
        SettingManager.language = context.getArgument("language", String.class);
        SettingManager.save();
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Language set"), true);
        return 1;
    }

    private static int setWordLimit(@NotNull CommandContext<ServerCommandSource> context) {
        SettingManager.wordLimit = context.getArgument("wordLimit", Integer.class);
        SettingManager.save();
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Word limit set"), true);
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
        GroupManager.loadGroup(group, true);
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Group added"), true);
        return 1;
    }

    private static int addGroupEvent(@NotNull CommandContext<ServerCommandSource> context) {
        String group = context.getArgument("group", String.class);
        Group g = GroupManager.getGroup(group);
        if (g == null) {
            context.getSource().sendFeedback(Text.of("[chat-with-npc] Group not found."), false);
            return 0;
        }
        String event = context.getArgument("event", String.class);
        g.addEvent(event);
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Event added"), true);
        return 1;
    }

    private static int popGroupEvent(@NotNull CommandContext<ServerCommandSource> context) {
        String group = context.getArgument("group", String.class);
        Group g = GroupManager.getGroup(group);
        if (g == null) {
            context.getSource().sendFeedback(Text.of("[chat-with-npc] Group not found."), false);
            return 0;
        }
        g.popEvent();
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Event popped"), true);
        return 1;
    }

    private static int setGroupInstruction(@NotNull CommandContext<ServerCommandSource> context) {
        String group = context.getArgument("group", String.class);
        Group g = GroupManager.getGroup(group);
        if (g == null) {
            context.getSource().sendFeedback(Text.of("[chat-with-npc] Group not found."), false);
            return 0;
        }
        String instruction = context.getArgument("instruction", String.class);
        g.setInstruction(instruction);
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Group Instruction set."), true);
        return 1;
    }

    private static int setGroupParent(@NotNull CommandContext<ServerCommandSource> context) {
        String group = context.getArgument("group", String.class);
        String parent = context.getArgument("parent", String.class);
        GroupManager.setGroupParent(group, parent);
        context.getSource().sendFeedback(Text.of("[chat-with-npc] Group parent set"), true);
        return 1;
    }

    private static int setNeedMemory(@NotNull CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        NPCEntity npc = NPCEntityManager.getNPCEntity(player);
        if (player != null && npc != null) {
            boolean isNeedMemory = context.getArgument("isNeedMemory", Boolean.class);
            npc.setNeedMemory(isNeedMemory);
            player.sendMessage(Text.of("[chat-with-npc] Need memory set."), true);
            return 1;
        }
        if (player != null) {
            player.sendMessage(Text.of("[chat-with-npc] No NPC near you."), true);
        }
        return 0;
    }

    private static int clearNPCMemory(@NotNull CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        NPCEntity npc = NPCEntityManager.getNPCEntity(player);
        if (player != null && npc != null) {
            if (npc.getThreadId() != null) {
                AsyncTask.call(() -> {
                    try {
                        Threads.discardThread(npc.getThreadId());
                        npc.setThreadId(null);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    return AsyncTask.nothingToDo();
                });
            }
            player.sendMessage(Text.of("[chat-with-npc] Memory clear."), true);
            return 1;
        }
        if (player != null) {
            player.sendMessage(Text.of("[chat-with-npc] No NPC near you."), true);
        }
        return 0;
    }

    private static int setNPCInstructions(@NotNull CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        NPCEntity npc = NPCEntityManager.getNPCEntity(player);
        if (player != null && npc != null) {
            String instructions = context.getArgument("instructions", String.class);
            npc.setInstructions(instructions);
            player.sendMessage(Text.of("[chat-with-npc] Instructions set."), true);
            return 1;
        }
        if (player != null) {
            player.sendMessage(Text.of("[chat-with-npc] No NPC near you."), true);
        }
        return 0;
    }

    private static int setNPCGroup(@NotNull CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        NPCEntity npc = NPCEntityManager.getNPCEntity(player);
        if (player != null && npc != null) {
            String group = context.getArgument("group", String.class);
            GroupManager.removeGroupMember(npc.getGroup(), npc.getName());
            npc.setGroup(group);
            GroupManager.addGroupMember(group, npc.getName());
            player.sendMessage(Text.of("[chat-with-npc] Group set."), true);
            return 1;
        }
        if (player != null) {
            player.sendMessage(Text.of("[chat-with-npc] No NPC near you."), true);
        }
        return 0;
    }

    private static int setNPCCareer(@NotNull CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        NPCEntity npc = NPCEntityManager.getNPCEntity(player);
        if (player != null && npc != null) {
            String career = context.getArgument("career", String.class);
            npc.setCareer(career);
            player.sendMessage(Text.of("[chat-with-npc] Career set."), true);
            return 1;
        }
        if (player != null) {
            player.sendMessage(Text.of("[chat-with-npc] No NPC near you."), true);
        }
        return 0;
    }

    private static int addNPCFunction(@NotNull CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        NPCEntity npc = NPCEntityManager.getNPCEntity(player);
        if (player != null && npc != null) {
            String function = context.getArgument("function", String.class);
            npc.addFunction(function);
            player.sendMessage(Text.of("[chat-with-npc] Function added."), true);
            return 1;
        }
        if (player != null) {
            player.sendMessage(Text.of("[chat-with-npc] No NPC near you."), true);
        }
        return 0;
    }

    private static int deleteNPCFunction(@NotNull CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        NPCEntity npc = NPCEntityManager.getNPCEntity(player);
        if (player != null && npc != null) {
            String function = context.getArgument("function", String.class);
            npc.removeFunction(function);
            player.sendMessage(Text.of("[chat-with-npc] Function removed."), true);
            return 1;
        }
        if (player != null) {
            player.sendMessage(Text.of("[chat-with-npc] No NPC near you."), true);
        }
        return 0;
    }
}
