package com.jackdaw.chatwithnpc.openaiapi.function;

import com.jackdaw.chatwithnpc.conversation.ConversationHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * <h2>Custom Function Constructor</h2>
 * <p>
 * First, you need to expend this class and implement the execute method.
 * <p>
 * You will also need to add the description and properties fields.
 * <p>
 * The description field is a string that describes what the function does.
 * <p>
 * The properties field is a map of strings that describe the parameters of the function.
 * <p>
 * The key is the name of the parameter and the value is the description of the parameter.
 * <p>
 * <h2>For example:</h2>
 * <pre>
 *     public class MyFunction extends CustomFunction {
 *         public MyFunction() {
 *             description = "This function does something";
 *             properties = Map.of(
 *                 "param1", Map.of(
 *                      "description", "This is the first parameter",
 *                      "type", "string"
 *                 ),
 *                 "param2", Map.of(
 *                      "description", "This is the second parameter"
 *                      "type", "string"
 *                 )
 *             );
 *             required = new String[] { "param1", "param2" };
 *         }
 *
 *     public Map< String, String > execute(@NotNull ConversationHandler conversation, @NotNull Map args) {
 *         // Do something with the arguments
 *         }
 *     }
 * </pre>
 * Then register the function in the FunctionManager class.
 * <pre>
 *     FunctionManager.registerFunction("myFunction", new MyFunction());
 * </pre>
 * Finally, the function will be automatically called by the OpenAI Assistant (If you register the function for an NPC).
 */
public abstract class CustomFunction {
    public String description;
    public Map<String, Map<String, Object>> properties;
    @Nullable
    public String[] required;

    /**
     * Execute the function. This method will be called by the OpenAI Assistant in a conversation.
     * @param conversation The conversation handler
     * @param args The arguments
     * @return The result you want to tell the OpenAI assistant
     */
    public abstract Map<String, String> execute(@NotNull ConversationHandler conversation, @NotNull Map<String, Object> args);
}

