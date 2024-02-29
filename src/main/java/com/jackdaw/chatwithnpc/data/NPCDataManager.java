package com.jackdaw.chatwithnpc.data;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.auxiliary.yaml.YamlUtils;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.jackdaw.chatwithnpc.npc.Record;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

/**
 * A serializer used to read or write the data from the files.
 * <p>
 * This data is related to the NPC's chat content.
 *
 * <p>Read or Write the data file with some information, each file just record one relative information.</p>
 */
public class NPCDataManager implements DataManager {

    private static final Logger logger = ChatWithNPCMod.LOGGER;
    private final File theFile;

    private final NPCEntity npc;

    public NPCDataManager(NPCEntity npc) {
        this.npc = npc;
        DataManager.mkdir("npc");
        this.theFile = new File(ChatWithNPCMod.workingDirectory.toFile(), "npc/" + npc.getName() + ".yml");
    }

    @Override
    public boolean isExist() {
        return theFile.exists();
    }

    @Override
    public void sync() {
        if (!isExist()) {
            save();
            return;
        }
        try {
            HashMap data = YamlUtils.readFile(theFile);
            // 读取储存在文件中的数据，然后将其赋值给npc
            npc.setCareer((String) data.get("careers"));
            npc.setGroup((String) data.get("localGroup"));
            npc.setBasicPrompt((String) data.get("basicPrompt"));
            // 在data中读取存在history中的数据，其中key为时间，value为消息
            HashMap messageRecord = (HashMap) data.get("messageRecord");
            for (Object key : messageRecord.keySet()) {
                String message = (String) messageRecord.get(key);
                // message的格式为"role:content"，所以需要分割
                String[] record = message.split(":");
                npc.addMessageRecord((long) key, Record.Role.valueOf(record[0]), record[1]);
            }
            if (!npc.getMessageRecord().isEmpty()) {
                npc.updateLastMessageTime(npc.getMessageRecord().lastMessageTime());
            } else {
                npc.updateLastMessageTime(System.currentTimeMillis());
            }
        } catch (FileNotFoundException e) {
            logger.error("[chat-with-npc] Can't open the data file.");
        }
    }

    @Override
    public void save() {
        try {
            if (!isExist()) {
                if (!theFile.createNewFile()) {
                    logger.error("[chat-with-npc] Can't create the data file.");
                    return;
                }
            }
            HashMap<String, Object> data = new HashMap<>();
            // 将npc的数据写入data中
            data.put("name", npc.getName());
            data.put("careers", npc.getCareer());
            data.put("localGroup", npc.getGroup().getName());
            data.put("basicPrompt", npc.getBasicPrompt());
            // 将npc的消息记录写入data中
            HashMap<Long, String> messageRecord = new HashMap<>();
            for (long key : npc.getMessageRecord().getTreeMap().keySet()) {
                // message的格式为"role:content"，所以需要拼接
                messageRecord.put(key,
                        npc.getMessageRecord().getTreeMap().get(key).getRole() + ":"
                                + npc.getMessageRecord().getTreeMap().get(key).getMessage());
            }
            data.put("messageRecord", messageRecord);
            YamlUtils.writeFile(theFile, data);
        } catch (IOException e) {
            logger.error("[chat-with-npc] Can't write the data file.");
        }
    }

    @Override
    public void delete() {
        if (!isExist()) {
            logger.warn("[chat-with-npc] The data file doesn't exist.");
            return;
        }
        if (!theFile.delete()) {
            logger.error("[chat-with-npc] Can't delete the data file.");
        }
    }
}
