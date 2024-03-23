package com.jackdaw.chatwithnpc.group;

import com.google.gson.Gson;
import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

/**
 * A serializer used to read or write the data from the files.
 * <p>
 * This data is related to the Group's content.
 *
 * <p>Read or Write the data file with some information, each file just record one relative information.</p>
 */
public class GroupDataManager {

    private static final Logger logger = ChatWithNPCMod.LOGGER;
    private final File theFile;
    private final Group group;

    /**
     * Create a new GroupDataManager.
     * @param group The group to be managed.
     */
    GroupDataManager(@NotNull Group group) {
        this.group = group;
        mkdir();
        this.theFile = new File(ChatWithNPCMod.workingDirectory.toFile(), "group/" + group.getName() + ".json");
    }

    private static void mkdir() {
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

    /**
     * Check if the file is existed.
     * @return true if the file is existed, otherwise false.
     */
    public boolean isExist() {
        return theFile.exists();
    }

    /**
     * Read the data from the file and set the group's content.
     */
    public void sync() {
        if (!isExist()) {
            save();
            return;
        }
        try {
            Gson gson = new Gson();
            String json = new String(Files.readAllBytes(theFile.toPath()));
            GroupData data = gson.fromJson(json, GroupData.class);
            data.set(group);
        } catch (IOException e) {
            logger.error("[chat-with-npc] Can't open the data file.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Write the group's content to the file.
     */
    public void save() {
        try {
            if (!isExist()) {
                if (!theFile.createNewFile()) {
                    logger.error("[chat-with-npc] Can't create the data file.");
                    return;
                }
            }
            Gson gson = new Gson();
            GroupData data = new GroupData(group);
            String json = gson.toJson(data);
            Files.write(theFile.toPath(), json.getBytes());
        } catch (IOException e) {
            logger.error("[chat-with-npc] Can't write the data file.");
        }
    }

    /**
     * Delete the data file. It would not delete the group's content.
     */
    public void delete() {
        if (!isExist()) {
            logger.warn("[chat-with-npc] The data file doesn't exist.");
            return;
        }
        if (!theFile.delete()) {
            logger.error("[chat-with-npc] Can't delete the data file.");
        }
    }

    private static final class GroupData {
        private final String name;

        private final String parentGroup;

        private final ArrayList<String> permanentPrompt;

        private final ArrayList<Map<Long, String>> tempEvent;

        private GroupData(Group group) {
            this.name = group.getName();
            this.parentGroup = group.getParentGroup();
            permanentPrompt = new ArrayList<>(group.getPermanentPrompt());
            tempEvent = new ArrayList<>(group.getTempEvent());
        }

        private void set(Group group) {
            group.setParentGroup(parentGroup);
            group.setPermanentPrompt(permanentPrompt);
            group.setTempEvent(tempEvent);
        }
    }
}
