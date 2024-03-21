# Advance Features

## 1. Functions Calling

Function calling is an advanced feature of the OpenAI API that allows ChatGPT to call functions that may affect game balance as an NPC at the appropriate time. The functions are defined by the server administrator, and the effects that can be achieved are determined by you. For a detailed introduction to this feature, please refer to the [OpenAI API documentation](https://beta.openai.com/docs/api-reference/function-calls/create-function-call).

For example, you can define a function named `give_diamond`. When a player talks to an NPC and expresses a desire for diamonds, the NPC can call the `give_diamond` function to implement this feature. For example, you can define some more complex functions to implement "bargaining" between players and NPCs, and so on.

The following commands can be used to add or remove functions that NPCs can call in the game:

- `/npchat npc addFunction <function>` - Add a function to the NPC closest to you
- `/npchat npc deleteFunction <function>` - Remove a function from the NPC closest to you

**_!! Please note:_** These functions must be registered (in the code or in the configuration file) to take effect, otherwise it will affect the operation of the entire mod.

Function calls can be implemented in two ways:

### 1. Implemented through code based on this mod's API

This method requires you to write some mod code on the server side to implement the functions you define. The advantage of this method is that it can implement very complex functions, but the disadvantage is that it requires you to have some mod development experience.

>We will open the mod API documentation later so that you can better develop secondary based on this mod API. At present, you can clone the source code of this mod, view the comments of the [`src/main/java/com/jackdaw/chatwithnpc/openaiapi/functioncalling/CustomFunction`](../src/main/java/com/jackdaw/chatwithnpc/openaiapi/functioncalling/CustomFunction.java) class, inherit this class, register the function, and compile this plugin to obtain the function.

Here is a simple example:

```java
public class GiveDiamondFunction extends CustomFunction {

    public GiveDiamondFunction() {
        description = "This function is used to give player a diamond. If the player make a request to the NPC to give them some diamonds, this function will be called.";
        properties = Map.of(
                "number", "the number of diamonds to give to the player."
        );
    }

    @Override
    public Map<String, String> execute(@NotNull ConversationHandler conversation, @NotNull Map<String, String> args) {
        int number;
        try{
            number = Integer.parseInt(args.get("number"));
        } catch (NumberFormatException e) {
            number = 1;
        }
        ItemStack diamond = new ItemStack(Items.DIAMOND, number);
        conversation.getNpc().findNearbyPlayers(10).forEach(player -> player.giveItemStack(diamond));
        return Map.of("status", "success");
    }
}
```

In this example, we define a function named `give_diamond`, which is used to give the player a certain number of diamonds. This function accepts a parameter `number`, which represents how many diamonds to give to the player. In the `execute` method, this function is implemented. To teach ChatGPT to call this function, we need to write the function's purpose in the `description` and explain the `properties` of the parameters in the constructor (the parameters are stored in the form of a Map).

Don't forget to register this function in the static initialization method of the mod:

```java
public class ChatWithNPCMod implements ModInitializer {
    @Override
    public void onInitialize() {
        // ...
        FunctionManager.registerFunction("give_diamond", new GiveDiamondFunction());
        // ...
    }
}
```

**_Effect picture:_**

![give_diamond](images/give_diamond.png)

### 2. Implemented through the configuration file

This method does not require you to write any code, just define the function's purpose and parameters in the configuration file. The advantage of this method is that it is simple and easy to use, but the disadvantage is that this method itself cannot be implemented and executed, but information can be passed to the NBT data of the NPC entity through parameters. Thus, function calls can be implemented in the game through "modify map data packet", "command block", and other methods (not shown here).

Here is a simple example:

Create a file named `setNPCHappy.json` in the `config/chatwithnpc/functions` directory with the following content:

```json
{
  "type": "function",
  "function": {
    "name": "setNPCHappy",
    "description": "This function is used to indicate that the NPC is happy now.",
    "parameters": {
      "type": "object",
      "properties": {
        "level": {
          "type": "string",
          "description": "How happy is the NPC, choose a floating point number from 0 to 1."
        }
      },
      "required": ["level"]
    }
  }
}
```

In this example, we define a function named `setNPCHappy`, which is used to make the NPC happy (in fact, happiness is just a key-value pair recorded in the NPC's NBT data). This function accepts a parameter `level`, which represents how happy the NPC is. To teach ChatGPT to call this function, we need to write the function's purpose in the `description` and explain the `properties` of the parameters.

All functions in the `config/chatwithnpc/functions` directory will be automatically registered when the server starts.

**_Effect picture:_**

![setNPCHappy](images/setNPCHappy.png)