package com.jackdaw.chatwithnpc.npc;

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
 * This data is related to the NPC's chat content.
 *
 * <p>Read or Write the data file with some information, each file just record one relative information.</p>
 */
public class NPCDataManager{

    private static final class NPCData {
        private final String name;
        private final String careers;
        private final String localGroup;
        private final String basicPrompt;
        private final ArrayList<Map<Long, String>> longTermMemory;

        private NPCData(NPCEntity npc) {
            this.name = npc.getName();
            this.careers = npc.getCareer();
            this.localGroup = npc.getGroup();
            this.basicPrompt = npc.getBasicPrompt();
            this.longTermMemory = new ArrayList<>(npc.getLongTermMemory());
        }

        private String toJson() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }

        private void set(NPCEntity npc) {
            npc.setCareer(careers);
            npc.setGroup(localGroup);
            npc.setBasicPrompt(basicPrompt);
            npc.setLongTermMemory(longTermMemory);
        }
    }

    private static final Logger logger = ChatWithNPCMod.LOGGER;
    private final File theFile;

    private final NPCEntity npc;

    public NPCDataManager(NPCEntity npc) {
        this.npc = npc;
        mkdir();
        this.theFile = new File(ChatWithNPCMod.workingDirectory.toFile(), "npc/" + npc.getName() + ".json");
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
            String json = new String(Files.readAllBytes(theFile.toPath()));
            NPCData data = new Gson().fromJson(json, NPCData.class);
            data.set(npc);
        } catch (IOException e) {
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
            NPCData data = new NPCData(npc);
            String json = data.toJson();
            Files.write(theFile.toPath(), json.getBytes());
        } catch (IOException e) {
            logger.error("[chat-with-npc] Can't write the data file.");
        }
    }

    /**
     * Create the directory.
     */
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
}
