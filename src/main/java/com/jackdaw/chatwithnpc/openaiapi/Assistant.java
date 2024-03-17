package com.jackdaw.chatwithnpc.openaiapi;

import com.google.gson.Gson;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.jackdaw.chatwithnpc.openaiapi.prompt.GroupPrompt;
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

    public static void createAssistant(@NotNull NPCEntity npc) {
        Map<String, String> createAssistantRequest = Map.of(
                "name", npc.getName(),
                "model", SettingManager.model,
                "instructions", npc.getInstructions(),
                "description", npc.getBasicPrompt() + GroupPrompt.getGroupsPrompt(npc.getGroup())
        );
        java.lang.Thread t = new java.lang.Thread(() -> {
            try {
                String res = Request.sendRequest(toJson(createAssistantRequest), "assistants", Header.buildBeta());
                String id = fromJson(res).id;
                npc.setAssistantId(id);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        t.start();
    }

    public static void modifyAssistant(@NotNull NPCEntity npc) {
        if (npc.getAssistantId() == null) return;
        Map<String, String> modifyAssistantRequest = Map.of(
                "name", npc.getName(),
                "model", SettingManager.model,
                "instructions", npc.getInstructions(),
                "description", npc.getBasicPrompt()
        );
        java.lang.Thread t = new java.lang.Thread(() -> {
            try {
                String ignore = Request.sendRequest(toJson(modifyAssistantRequest), "assistants/" + npc.getAssistantId(), Header.buildBeta());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        t.start();
    }
}
