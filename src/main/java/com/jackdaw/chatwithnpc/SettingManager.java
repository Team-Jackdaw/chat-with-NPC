package com.jackdaw.chatwithnpc;

import com.google.gson.Gson;
import com.jackdaw.chatwithnpc.npc.TextBubbleEntity.TextBackgroundColor;

import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Manages the config files for the modules provided by this plugin.
 *
 * <p>
 * Configure the setting if a specific module is enabled or disabled.
 *
 * @author WDRshadow, Lokeyli
 * @version v2.6
 */
public class SettingManager {
    private static final Logger logger = ChatWithNPCMod.LOGGER;
    private static final File configFile = ChatWithNPCMod.workingDirectory.resolve("config.json").toFile();

    public static boolean enabled = true;
    public static double range = 10.0;
    public static String language = "Chinese";
    public static int wordLimit = 30;
    public static String apiKey = "";
    public static String model = "gpt-3.5-turbo";
    public static String apiURL = "api.openai.com";
    public static boolean isBubble = true;
    public static boolean isChatBar = false;
    public static TextBackgroundColor bubbleColor = TextBackgroundColor.DEFAULT;
    public static long timeLastingPerChar = 500L;

    /**
     * Load the setting from the config file.
     */
    public static void sync() {
        if (configFile.exists()) {
            try {
                String json = new String(Files.readAllBytes(configFile.toPath()));
                Gson gson = new Gson();
                Config config = gson.fromJson(json, Config.class);
                config.set();
            } catch (IOException e) {
                logger.error("[chat-with-npc] Can't open the config file.");
            }
        } else {
            save();
        }
    }

    /**
     * Write the setting to the config file.
     */
    public static void save() {
        try {
            if (!configFile.exists()) {
                if (!configFile.createNewFile()) {
                    logger.error("[chat-with-npc] Can't create the config file.");
                    return;
                }
            }
            Files.write(configFile.toPath(), Config.toJson().getBytes());
        } catch (IOException e) {
            logger.error("[chat-with-npc] Can't write the config file.");
        }
    }

    private static final class Config {
        private boolean enabled = true;
        private double range = 10.0;
        private String language = "Chinese";
        private int wordLimit = 30;
        private String apiKey = "";
        private String model = "gpt-3.5-turbo";
        private String apiURL = "api.openai.com";
        private boolean isBubble = true;
        private boolean isChatBar = false;
        private String textBackgroundColor = TextBackgroundColor.DEFAULT.name();
        private long timeLastingPerChar = 500L;

        private static String toJson() {
            Config config = new Config();
            config.enabled = SettingManager.enabled;
            config.range = SettingManager.range;
            config.language = SettingManager.language;
            config.wordLimit = SettingManager.wordLimit;
            config.apiKey = SettingManager.apiKey;
            config.model = SettingManager.model;
            config.apiURL = SettingManager.apiURL;
            config.isBubble = SettingManager.isBubble;
            config.isChatBar = SettingManager.isChatBar;
            config.textBackgroundColor = SettingManager.bubbleColor.name();
            config.timeLastingPerChar = SettingManager.timeLastingPerChar;
            Gson gson = new Gson();
            return gson.toJson(config);
        }

        private void set() {
            SettingManager.enabled = enabled;
            SettingManager.range = range;
            SettingManager.language = language;
            SettingManager.wordLimit = wordLimit;
            SettingManager.apiKey = apiKey;
            SettingManager.model = model;
            SettingManager.apiURL = apiURL;
            SettingManager.isBubble = isBubble;
            SettingManager.isChatBar = isChatBar;
            SettingManager.bubbleColor = TextBackgroundColor.valueOf(textBackgroundColor);
            SettingManager.timeLastingPerChar = timeLastingPerChar;
        }

    }
    
}
