package com.jackdaw.chatwithnpc.npc;

import com.google.gson.Gson;
import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 * A serializer used to read or write the data from the files.
 * <p>
 * This data is related to the NPC's chat content.
 *
 * <p>Read or Write the data file with some information, each file just record one relative information.</p>
 */
public class NPCDataManager {

    private static final Logger logger = ChatWithNPCMod.LOGGER;
    private final File theFile;
    private final NPCEntity npc;

    NPCDataManager(@NotNull NPCEntity npc) {
        this.npc = npc;
        mkdir();
        this.theFile = new File(ChatWithNPCMod.workingDirectory.toFile(), "npc/" + npc.getUUID().toString() + ".json");
    }

    private static void mkdir() {
        Path workingDirectory = ChatWithNPCMod.workingDirectory.resolve("npc");
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
     * Synchronize the data from the file to the NPC.
     */
    public void sync() {
        if (!isExist()) {
            save();
            return;
        }
        try {
            String json = new String(Files.readAllBytes(theFile.toPath()));
            NPCData data = NPCData.fromJson(json);
            data.set(npc);
        } catch (IOException e) {
            logger.error("[chat-with-npc] Can't open the data file.");
        }
    }

    /**
     * Save the data from the NPC to the file.
     */
    public void save() {
        try {
            if (!isExist()) {
                if (!theFile.createNewFile()) {
                    logger.error("[chat-with-npc] Can't create the data file.");
                    return;
                }
            }
            NPCData data = new NPCData(npc);
            String json = data.toJson();
            Files.write(theFile.toPath(), json.getBytes());
        } catch (IOException e) {
            logger.error("[chat-with-npc] Can't write the data file.");
        }
    }

    private static final class NPCData {
        private final String name;
        private final String assistantID;
        private final String threadID;
        private final String careers;
        private final String localGroup;
        private final boolean needMemory;
        private final String instructions;
        private final ArrayList<String> functions;

        private NPCData(NPCEntity npc) {
            this.name = npc.getName();
            this.assistantID = npc.getAssistantId();
            this.threadID = npc.getThreadId();
            this.careers = npc.getCareer();
            this.localGroup = npc.getGroup();
            this.instructions = npc.getInstructions();
            this.needMemory = npc.isNeedMemory();
            this.functions = npc.getFunctions();
        }

        private void set(NPCEntity npc) {
            npc.setAssistantId(assistantID);
            npc.setThreadId(threadID);
            npc.setCareer(careers);
            npc.setGroup(localGroup);
            npc.setInstructions(instructions);
            npc.setNeedMemory(needMemory);
            npc.setFunctions(functions);
        }

        private String toJson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }

        private static NPCData fromJson(String json) {
            Gson gson = new Gson();
            return gson.fromJson(json, NPCData.class);
        }
    }
}
