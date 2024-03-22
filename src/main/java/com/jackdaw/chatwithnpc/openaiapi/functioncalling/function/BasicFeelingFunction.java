package com.jackdaw.chatwithnpc.openaiapi.functioncalling.function;

import com.jackdaw.chatwithnpc.ChatWithNPCMod;
import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import com.jackdaw.chatwithnpc.npc.npcentity.Actions;
import com.jackdaw.chatwithnpc.openaiapi.functioncalling.CustomFunction;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class BasicFeelingFunction extends CustomFunction {
    public BasicFeelingFunction() {
        description = "This function is used to express your feelings. You can `shake head` if you feel bad or disagree with something, or `feel happy` if you feel happy or agree with something.";
        properties = Map.of(
                "feeling", Map.of(
                        "description", "This is your feeling.",
                        "enum", List.of("SHAKE_HEAD", "FEEL_HAPPY")
                )
        );
    }

    @Override
    public Map<String, String> execute(@NotNull ConversationHandler conversation, @NotNull Map args) {
        String feeling = (String) args.get("feeling");
        try {
            conversation.getNpc().doAction(Actions.valueOf(feeling), null);
        } catch (Exception e) {
            ChatWithNPCMod.LOGGER.error("Failed to execute the action: " + feeling + e);
            return Map.of("status", "failed");
        }
        return Map.of("status", "success, you `" + feeling + "`");
    }
}
