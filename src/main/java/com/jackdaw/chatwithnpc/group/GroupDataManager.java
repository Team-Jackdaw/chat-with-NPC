package com.jackdaw.chatwithnpc.group;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.auxiliary.yaml.YamlUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A serializer used to read or write the data from the files.
 * <p>
 * This data is related to the Group's content.
 *
 * <p>Read or Write the data file with some information, each file just record one relative information.</p>
 */
public class GroupDataManager{

    private static final Logger logger = ChatWithNPCMod.LOGGER;
    private final File theFile;

    private final Group group;

    public GroupDataManager(Group group) {
        this.group = group;
        mkdir();
        this.theFile = new File(ChatWithNPCMod.workingDirectory.toFile(), "group/" + group.getName() + ".yml");
    }

    public boolean isExist() {
        return theFile.exists();
    }

    public void sync() {
        if (!isExist()) {
            save();
            return;
        }
        try {
            HashMap data = YamlUtils.readFile(theFile);
            ArrayList<String> permanentPrompt = new ArrayList<>();
            for (Object s : (Iterable) data.get("permanentPrompt")) {
                permanentPrompt.add((String) s);
            }
            group.setPermanentPrompt(permanentPrompt);
            HashMap tempEnvironmentPrompt = (HashMap) data.get("tempEvent");
            for (Object key : tempEnvironmentPrompt.keySet()) {
                group.addTempEvent((String) tempEnvironmentPrompt.get(key), (long) key);
            }
        } catch (FileNotFoundException e) {
            logger.error("[chat-with-npc] Can't open the data file.");
        }
    }

    public void save() {
        try {
            if (!isExist()) {
                if (!theFile.createNewFile()) {
                    logger.error("[chat-with-npc] Can't create the data file.");
                    return;
                }
            }
            HashMap<String, Object> data = new HashMap<>();
            ArrayList<String> permanentPrompt = new ArrayList<>(group.getPermanentPrompt());
            data.put("permanentPrompt", permanentPrompt);
            HashMap<Long, String> tempEvent = new HashMap<>();
            for (GroupEvent record : group.getTempEvent()) {
                // combine the end time and event
                String event = record.getEndTime() + ": " + record.getEvent();
                tempEvent.put(record.getStartTime(), event);
            }
            data.put("tempEvent", tempEvent);
            YamlUtils.writeFile(theFile, data);
        } catch (IOException e) {
            logger.error("[chat-with-npc] Can't write the data file.");
        }
    }

    public void delete() {
        if (!isExist()) {
            logger.warn("[chat-with-npc] The data file doesn't exist.");
            return;
        }
        if (!theFile.delete()) {
            logger.error("[chat-with-npc] Can't delete the data file.");
        }
    }

    /**
     * Create the directory.
     */
    static void mkdir() {
        Path workingDirectory = ChatWithNPCMod.workingDirectory.resolve("group");
        if (!Files.exists(workingDirectory)) {
            try {
                Files.createDirectories(workingDirectory);
            } catch (IOException e) {
                ChatWithNPCMod.LOGGER.error("[chat-with-npc] Failed to create the npc directory");
                ChatWithNPCMod.LOGGER.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }
}
