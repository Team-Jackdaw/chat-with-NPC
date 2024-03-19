package com.jackdaw.chatwithnpc.openaiapi;

import com.google.gson.Gson;
import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.jackdaw.chatwithnpc.openaiapi.prompt.GroupPrompt;
import com.jackdaw.chatwithnpc.openaiapi.prompt.NPCPrompt;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Assistant {

    private String id;
    private String name;
    private String instructions;
    private String description;
    private String model;

    private static String toJson(Map<String, String> map) {
        return new Gson().toJson(map);
    }

    private static Assistant fromJson(String json) {
        return new Gson().fromJson(json, Assistant.class);
    }

    public static void createAssistant(@NotNull NPCEntity npc) throws Exception {
        Map<String, String> createAssistantRequest = Map.of(
                "name", npc.getName(),
                "model", SettingManager.model,
                "instructions", NPCPrompt.instructions(npc),
                "description", NPCPrompt.description(npc) + GroupPrompt.getGroupsPrompt(npc.getGroup())
        );
        String res = Request.sendRequest(toJson(createAssistantRequest), "assistants", Header.buildBeta(), Request.Action.POST);
        String id = fromJson(res).id;
        if (id == null) {
            ChatWithNPCMod.LOGGER.error("[chat-with-npc] API error: " + res);
            throw new Exception("Assistant id is null");
        }
        npc.setAssistantId(id);
    }

    public static void modifyAssistant(@NotNull NPCEntity npc) throws Exception {
        if (npc.getAssistantId() == null) return;
        Map<String, String> modifyAssistantRequest = Map.of(
                "name", npc.getName(),
                "model", SettingManager.model,
                "instructions", NPCPrompt.instructions(npc),
                "description", NPCPrompt.description(npc) + GroupPrompt.getGroupsPrompt(npc.getGroup())
        );
        String res = Request.sendRequest(toJson(modifyAssistantRequest), "assistants/" + npc.getAssistantId(), Header.buildBeta(), Request.Action.POST);
        String id = fromJson(res).id;
        if (id == null) {
            ChatWithNPCMod.LOGGER.error("[chat-with-npc] API error: " + res);
            throw new Exception("Assistant id is null");
        }
    }
}
