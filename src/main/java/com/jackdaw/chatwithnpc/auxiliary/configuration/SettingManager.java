package com.jackdaw.chatwithnpc.auxiliary.configuration;

import com.google.gson.Gson;
import com.jackdaw.chatwithnpc.ChatWithNPCMod;
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
 * @author WDRshadow
 * @version v1.0
 */
public class SettingManager {
    private static final Logger logger = ChatWithNPCMod.LOGGER;
    private static final File configFile = ChatWithNPCMod.workingDirectory.resolve("config.json").toFile();

    // use for confirming the setting version is the same with the plugin
    private static final String lastVersion = "v2.1";

    public static boolean enabled = true;
    public static double range = 10.0;
    public static String language = "Chinese";
    public static String apiKey = "";
    public static String model = "gpt-3.5-turbo";

    private static final class Config {
        private String lastVersion = "v2.1";
        private boolean enabled = true;
        private double range = 10.0;
        private String language = "Chinese";
        private String apiKey = "";
        private String model = "gpt-3.5-turbo";

        private static String toJson() {
            Config config = new Config();
            config.enabled = SettingManager.enabled;
            config.range = SettingManager.range;
            config.language = SettingManager.language;
            config.apiKey = SettingManager.apiKey;
            config.model = SettingManager.model;
            Gson gson = new Gson();
            return gson.toJson(config);
        }

        private void set() {
            if (!SettingManager.lastVersion.equals(lastVersion)) {
                logger.warn("[chat-with-npc] The config file is not the same version with the plugin.");
                save();
                return;
            }
            SettingManager.enabled = enabled;
            SettingManager.language = language;
            SettingManager.apiKey = apiKey;
            SettingManager.model = model;
        }

    }


    /**
     * Load the setting from the config file.
     */
    public static void sync(){
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
}
