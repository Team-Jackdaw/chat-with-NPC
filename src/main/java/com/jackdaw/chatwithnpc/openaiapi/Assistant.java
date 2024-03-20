package com.jackdaw.chatwithnpc.openaiapi;

import com.google.gson.Gson;
import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.auxiliary.configuration.SettingManager;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.jackdaw.chatwithnpc.openaiapi.prompt.NPCPrompt;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Assistant {

    /**
     * Create an assistant for the NPC from the OpenAI API
     * @param npc The NPC to create the assistant for
     * @throws Exception If the assistant id is null
     */
    public static void createAssistant(@NotNull NPCEntity npc) throws Exception {
        Map<String, String> createAssistantRequest = Map.of(
                "name", npc.getName(),
                "model", SettingManager.model,
                "instructions", NPCPrompt.instructions(npc)
        );
        String res = Request.sendRequest(AssistantClass.toJson(createAssistantRequest), "assistants", Header.buildBeta(), Request.Action.POST);
        String id = AssistantClass.fromJson(res).id;
        if (id == null) {
            ChatWithNPCMod.LOGGER.error("[chat-with-npc] API error: " + res);
            throw new Exception("Assistant id is null");
        }
        npc.setAssistantId(id);
    }

    /**
     * Modify the assistant for the NPC from the OpenAI API
     * @param npc The NPC to modify the assistant for
     * @throws Exception If the assistant id is null
     */
    public static void modifyAssistant(@NotNull NPCEntity npc) throws Exception {
        if (npc.getAssistantId() == null) return;
        Map<String, String> modifyAssistantRequest = Map.of(
                "name", npc.getName(),
                "model", SettingManager.model,
                "instructions", NPCPrompt.instructions(npc)
        );
        String res = Request.sendRequest(AssistantClass.toJson(modifyAssistantRequest), "assistants/" + npc.getAssistantId(), Header.buildBeta(), Request.Action.POST);
        String id = AssistantClass.fromJson(res).id;
        if (id == null) {
            ChatWithNPCMod.LOGGER.error("[chat-with-npc] API error: " + res);
            throw new Exception("Assistant id is null");
        }
    }

    private static class AssistantClass {
        private String id;
        private String name;
        private String instructions;
        private String description;
        private String model;

        private static String toJson(Map<String, String> map) {
            return new Gson().toJson(map);
        }

        private static AssistantClass fromJson(String json) {
            return new Gson().fromJson(json, AssistantClass.class);
        }
    }
}
