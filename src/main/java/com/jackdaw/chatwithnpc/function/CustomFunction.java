package com.jackdaw.chatwithnpc.function;

import com.jackdaw.chatwithnpc.conversation.ConversationHandler;

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
 *                      "description", "This is the second parameter",
 *                      "type", "string"
 *                 )
 *             );
 *             required = new String[] { "param1", "param2" };
 *         }
 *
 *     public Map< String, String > execute(ConversationWindow conversation, Map args) {
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
    /**
     * The simplest success response.
     */
    protected static final Map<String, String> SUCCESS = Map.of("status", "success");
    /**
     * The simplest failure response.
     */
    protected static final Map<String, String> FAILURE = Map.of("status", "failure");
    public String description;
    public Map<String, Map<String, Object>> properties;
    public String[] required;
    /**
     * The permission level of the function. The default is 1. Usually, 1 means the function can be called by any Agents.
     * <p>
     * The permission level 2 means the function can be called by high level Agent like Assistant and Master.
     * <p>
     * The permission level 3 means the function can be called by the Master only.
     */
    protected int permissionLevel = 1;

    /**
     * Execute the function. This method will be called by the OpenAI Assistant in a conversation.
     * @param conversation The conversation handler
     * @param args The arguments
     * @return The result you want to tell the OpenAI assistant
     */
    public abstract Map<String, String> execute(ConversationHandler conversation, Map<String, Object> args);

    /**
     * Get the permission level of the function. The default is 1.
     * <p>
     * Usually, 1 means the function can be called by any Agents.
     * <p>
     * The permission level 2 means the function can be called by high level Agent like Assistant and Master.
     * <p>
     * The permission level 3 means the function can be called by the Master only.
     * @return The permission level
     */
    public int getPermissionLevel() {
        return permissionLevel;
    }

}

