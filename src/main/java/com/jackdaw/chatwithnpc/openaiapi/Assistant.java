package com.jackdaw.chatwithnpc.openaiapi;

import com.google.gson.Gson;
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
        String res = Request.sendRequest(toJson(createAssistantRequest), "assistants", Header.buildBeta());
        String id = fromJson(res).id;
        npc.setAssistantId(id);
    }

    public static void modifyAssistant(@NotNull NPCEntity npc) throws Exception {
        if (npc.getAssistantId() == null) return;
        Map<String, String> modifyAssistantRequest = Map.of(
                "name", npc.getName(),
                "model", SettingManager.model,
                "instructions", npc.getInstructions(),
                "description", npc.getBasicPrompt()
        );
        String ignore = Request.sendRequest(toJson(modifyAssistantRequest), "assistants/" + npc.getAssistantId(), Header.buildBeta());
    }
}
