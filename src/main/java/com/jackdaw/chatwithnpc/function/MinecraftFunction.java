package com.jackdaw.chatwithnpc.function;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.NumberFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import com.jackdaw.chatwithnpc.SettingManager;
import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.jackdaw.chatwithnpc.api.json.Tool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class MinecraftFunction extends CustomFunction {
    private static final Logger logger = ChatWithNPCMod.LOGGER;
    private static final Path folder = ChatWithNPCMod.workingDirectory.resolve("functions");
    private final String name;
    private String call;

    private MinecraftFunction(String name, String description, Map<String, Map<String, Object>> properties) {
        this.name = name;
        this.description = description;
        this.properties = properties;
    }

    /**
     * Register a function from a JSON string. It will be called by the OpenAI Assistant and then call the function in Minecraft World data package.
     * <p>
     * The JSON string should be in the following format:
     * <pre>
     *     {
     *         "type": "function",
     *         "function": {
     *             "name": "functionName",
     *             "description": "This function does something",
     *             "parameters": {
     *                 "type": "object",
     *                 "properties": {
     *                     "param1": {
     *                         "type": "integer",
     *                         "description": "This is the first parameter",
     *                         "enum": [1, 2, 3]
     *                     },
     *                     "param2": {
     *                         "type": "integer",
     *                         "description": "This is the second parameter"
     *                     }
     *                 },
     *                 "required": ["param1", "param2"]
     *             }
     *         }
     *         "call": "NameSpace:function"
     *     }
     * </pre>
     *
     * <b>Note: The parameters must be integer as it will be stored in scoreboard.</b>
     * <p>
     * The parameters are stored in the nearby players' scoreboard with format "npc_UUID_paramName". The UUID is the UUID of the NPC.
     * <p>
     * The "call" field is optional, it is the function stored in Minecraft World data package with format "NameSpace:function". If it is not null, the function will be called as the Server in NPC's World with its position and with permission level 2.
     * <p>
     * This function will not be executed if "call" is null. It will only add the integer parameters to the scoreboard.
     *
     * @param json The JSON string
     */
    private static void registerFromJson(String json) {
        Tool tool = Tool.fromJson(json);
        MinecraftFunction function = new MinecraftFunction(tool.function.name, tool.function.description, tool.function.parameters.properties);
        if (tool.call != null) function.call = tool.call;
        function.required = tool.function.parameters.required;
        FunctionManager.getInstance().register(tool.function.name, function);
    }

    /**
     * Get the list of the functions, and register them.
     */
    public static void sync() {
        if (!Files.exists(folder)) {
            try {
                Files.createDirectories(folder);
            } catch (IOException e) {
                logger.error("[chat-with-npc] Failed to create the functions directory");
                logger.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }
        File workingDirectory = folder.toFile();
        File[] files = workingDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                if (name.endsWith(".json")) {
                    String json;
                    try {
                        json = Files.readString(file.toPath());
                    } catch (IOException e) {
                        logger.error("[chat-with-npc] Failed to read the function file: " + name);
                        logger.error(e.getMessage());
                        continue;
                    }
                    registerFromJson(json);
                }
            }
        }
    }

    public Map<String, String> execute(@NotNull ConversationHandler conversation, @NotNull Map<String, Object> args) {
        Map<String, String> failed = Map.of("status", "failed");
        Map<String, String> ok = Map.of("status", "success");
        NPCEntity npc = conversation.getNpc();
        Entity entity = npc.getEntity();
        PlayerEntity player = entity.getWorld().getClosestPlayer(entity, SettingManager.range);
        MinecraftServer server = entity.getServer();
        if (server == null) {
            return failed;
        } else {
            String playerName;
            if (player != null) {
                playerName = player.getName().getString();
                args.forEach((key, value) -> {
                    String scoreName = "npc_" + this.name + "_" + key;
                    Scoreboard scoreboard = server.getScoreboard();
                    ScoreboardObjective objective = scoreboard.getNullableObjective(scoreName);
                    if (objective == null) {
                        scoreboard.addObjective(scoreName, ScoreboardCriterion.DUMMY, Text.of(scoreName), ScoreboardCriterion.RenderType.INTEGER, false, (NumberFormat)null);
                        objective = scoreboard.getNullableObjective(scoreName);
                    }

                    int intValue;
                    if (value instanceof Integer) {
                        intValue = (Integer)value;
                    } else {
                        try {
                            double doubleValue = Double.parseDouble(value.toString());
                            intValue = (int)doubleValue;
                        } catch (NumberFormatException var11) {
                            ChatWithNPCMod.LOGGER.error("[chat-with-npc] Failed to parse the value of " + key + " in function " + this.name);
                            return;
                        }
                    }

                    scoreboard.getOrCreateScore(player, objective).setScore(intValue);
                });
            } else {
                playerName = "";
            }

            boolean var = false;
            if (this.call != null) {
                Vec3d pos = entity.getPos();
                ServerCommandSource source = server.getCommandSource();
                ServerCommandSource newSource = source.withPosition(pos).withWorld((ServerWorld)entity.getWorld()).withSilent().withLevel(2);
                server.getCommandManager().executeWithPrefix(newSource, "function " + this.call);
                if (!playerName.isEmpty()) {
                    String scoreName = "npc_" + this.name + "_result";
                    Scoreboard scoreboard = server.getScoreboard();
                    ScoreboardObjective objective = scoreboard.getNullableObjective(scoreName);
                    if (objective != null) {
                        ReadableScoreboardScore score = scoreboard.getScore(player, objective);
                        if (score != null && score.getScore() == 1) {
                            var = true;
                        }
                    }
                }
            }

            return var ? ok : failed;
        }
    }
}
