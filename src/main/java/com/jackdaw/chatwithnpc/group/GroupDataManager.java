package com.jackdaw.chatwithnpc.group;

import com.google.gson.Gson;
import com.jackdaw.chatwithnpc.ChatWithNPCMod;
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
public class GroupDataManager{

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

    private static final Logger logger = ChatWithNPCMod.LOGGER;
    private final File theFile;

    private final Group group;

    public GroupDataManager(Group group) {
        this.group = group;
        mkdir();
        this.theFile = new File(ChatWithNPCMod.workingDirectory.toFile(), "group/" + group.getName() + ".json");
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
            Gson gson = new Gson();
            String json = new String(Files.readAllBytes(theFile.toPath()));
            GroupData data = gson.fromJson(json, GroupData.class);
            data.set(group);
        } catch (IOException e) {
            logger.error("[chat-with-npc] Can't open the data file.");
            throw new RuntimeException(e);
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
            Gson gson = new Gson();
            GroupData data = new GroupData(group);
            String json = gson.toJson(data);
            Files.write(theFile.toPath(), json.getBytes());
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
}
