package com.jackdaw.chatwithnpc.openaiapi;

import com.google.gson.Gson;
import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.SettingManager;
import com.jackdaw.chatwithnpc.openaiapi.function.FunctionManager;
import com.jackdaw.chatwithnpc.npc.NPCEntity;
import com.jackdaw.chatwithnpc.openaiapi.prompt.NPCPrompt;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

public class Assistant {

    /**
     * Create an assistant for the NPC from the OpenAI API
     * @param npc The NPC to create the assistant for
     * @throws Exception If the assistant id is null
     */
    public static void createAssistant(@NotNull NPCEntity npc) throws Exception {
        String id = assistantRequest(npc, Do.CREATE);
        npc.setAssistantId(id);
    }

    /**
     * Modify the assistant for the NPC from the OpenAI API
     * @param npc The NPC to modify the assistant for
     * @throws Exception If the assistant id is null
     */
    public static void modifyAssistant(@NotNull NPCEntity npc) throws Exception {
        assistantRequest(npc, Do.MODIFY);
    }
    private enum Do {
        CREATE, MODIFY
    }

    private static @NotNull String assistantRequest(@NotNull NPCEntity npc, Do what) throws Exception {
        Map assistantRequest;
        if (npc.getFunctions() == null || npc.getFunctions().isEmpty()) {
            assistantRequest = Map.of(
                    "name", npc.getName(),
                    "model", SettingManager.model,
                    "instructions", NPCPrompt.instructions(npc)
            );
        } else {
            ArrayList<String> functions = npc.getFunctions();
            ArrayList<Map> functionsJsonList = new ArrayList<>();
            for (String function: functions) {
                functionsJsonList.add(new Gson().fromJson(FunctionManager.getFunctionJson(function), Map.class));
            }
            assistantRequest = Map.of(
                    "name", npc.getName(),
                    "model", SettingManager.model,
                    "instructions", NPCPrompt.instructions(npc),
                    "tools", functionsJsonList
            );
        }
        String res;
        if (what == Do.CREATE) {
            res = Request.sendRequest(AssistantClass.toJson(assistantRequest), "assistants", Header.buildBeta(), Request.Action.POST);
        } else {
            res = Request.sendRequest(AssistantClass.toJson(assistantRequest), "assistants/" + npc.getAssistantId(), Header.buildBeta(), Request.Action.POST);
        }
        String id = AssistantClass.fromJson(res).id;
        if (id == null) {
            ChatWithNPCMod.LOGGER.error("[chat-with-npc] API error: " + res);
            throw new Exception("Assistant id is null");
        }
        return id;
    }

    private static class AssistantClass {
        private String id;
        private String name;
        private String instructions;
        private String model;
        private ArrayList<Map> tools;

        private static String toJson(Map map) {
            return new Gson().toJson(map);
        }

        private static AssistantClass fromJson(String json) {
            return new Gson().fromJson(json, AssistantClass.class);
        }
    }
}
