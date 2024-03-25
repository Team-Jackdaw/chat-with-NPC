# 高级功能

## 1. 函数调用

函数调用是OpenAI
API的一个高级功能，它允许ChatGPT在适当的时候以NPC的身份调用这些可能影响游戏平衡的函数，函数由服务器管理员定义，它可以达到的效果由您来决定。详细关于该功能的介绍请参见[OpenAI API文档](https://beta.openai.com/docs/api-reference/function-calls/create-function-call)。

比如说，您可以定义一个名为`give_diamond`的函数，当玩家向NPC对话时表现出他想要钻石时，NPC可以调用`give_diamond`
函数来实现这个功能。再比如，您可以定义一些比较复杂的函数来实现玩家与NPC之间的“讨价还价”功能，等等。

在游戏中可以通过一下命令增减NPC可以调用的函数：

- `/npchat npc addFunction <function>` - 为离你最近的NPC添加一个函数
- `/npchat npc deleteFunction <function>` - 为离你最近的NPC移除一个函数

**_！！请注意：_**这些函数都必须已经注册（在代码中或者在配置文件中）才能生效，否则会影响整个mod的运行。

函数调用可以有两种方式实现：

### 1. 通过基于本mod api的代码实现

这种方式需要您在服务器端编写一些mod代码，以实现您定义的函数。这种方式的优点是可以实现非常复杂的功能，缺点是需要您有一定的mod开发经验。

> 我们会在晚些时候开放mod api的文档，以便您可以更好地基于本mod
> api进行二次开发。目前，您可以克隆本mod的源代码，查看[`src/main/java/com/jackdaw/chatwithnpc/openaiapi/function/CustomFunction`](../src/main/java/com/jackdaw/chatwithnpc/openaiapi/function/CustomFunction.java)
> 类的注释，继承该类并注册函数，在编译本插件来获得该函数功能。

你可以复刻摸板仓库[NPCBasicFunction](https://github.com/Team-Jackdaw/NPCBasicFunction)来查看示例代码。

以下是一个简单的示例：

```java
public class GiveDiamondFunction extends CustomFunction {

    public GiveDiamondFunction() {
        description = "This function is used to give player a diamond. You can give player diamonds if you want.";
        properties = Map.of(
                "number", Map.of(
                        "type", "integer",
                        "description", "the number of diamonds to give to the player."
                )
        );
    }

    @Override
    public Map<String, String> execute(@NotNull ConversationHandler conversation, @NotNull Map<String, Object> args) {
        int number;
        try {
            number = (int) args.get("number");
        } catch (ClassCastException ignore) {
            try {
                double doubleNumber = Double.parseDouble(args.get("number").toString());
                number = (int) doubleNumber;
            } catch (NumberFormatException ignore2) {
                number = 1;
            }
        }
        ItemStack diamond = new ItemStack(Items.DIAMOND, number);
        conversation.getNpc().findNearbyPlayers(10).forEach(player -> player.giveItemStack(diamond));
        return Map.of("status", "success");
    }
}
```

在这个例子中，我们定义了一个名为`give_diamond`的函数，它的作用是给玩家一定数量的钻石。这个函数接受一个参数`number`
，表示要给玩家多少个钻石。在`execute`
方法中，实现了这个功能。为了让ChatGPT学会调用这个函数，我们需要在构造函数中的`description`
中写明这个函数的作用，以及它接受的参数`properties`的解释（参数以Map表的形式储存）。

不要忘记在mod的静态初始化方法中注册这个函数：

```java
public class NPCBasicFunction implements ModInitializer {
    @Override
    public void onInitialize() {
        // ...
        FunctionManager.registerFunction("give_diamond", new GiveDiamondFunction());
        // ...
    }
}
```

**_效果图：_**

![give_diamond](images/give_diamond.png)

### 2. 通过配置文件实现

这种方式不需要您编写任何Java代码，只需要在配置文件中定义函数的作用和参数，并且在地图数据包中写入Minecraft指令相关的函数即可。这种方式的优点是简单易用，缺点是这种方法不能向OpenAI返回复杂的结果。

以下是一个简单的示例：

在`config/chatwithnpc/functions`目录下创建一个名为`open_door.json`的文件（文件名一定要与函数名对应），内容如下：

```json
{
  "type": "function",
  "function": {
    "name": "open_door",
    "description": "This function is used to let you open the door or close the door.",
    "parameters": {
      "type": "object",
      "properties": {
        "willingness": {
          "type": "integer",
          "description": "If this parameter is 0, you will close the door. If it is 1, you will open the door.",
          "enum": [0, 1]
        }
      },
      "required": [
        "willingness"
      ]
    }
  },
  "call": "npchat:open_door"
}
```

在这个例子中，我们定义了一个名为`open_door`的函数，它的作用是让NPC打开或关闭门。这个函数接受一个参数`willingness`
，表示NPC是希望开门(`1`)还是关门(`0`)。为了让ChatGPT学会调用这个函数，我们需要`description`
中写明这个函数的作用，以及它接受的参数properties的解释。

当NPC调用该函数后，它会首先在离NPC最近的玩家身上建立一个或数个记分板`npc_<function_name>_<arg>`
用来记录NPC调用的函数参数，然后根据参数的值赋予到该玩家身上，最后将读取结果记分板`npc_<function_name>_result`
，如果该记分板被创建切最近的一位玩家的结果为0则表示失败，该结果会影响NPC的回答。如果没有调用函数或没有创建该记分板或者该记分板为1则返回结果为成功。

可以接受的参数：

- `type`：函数的类型，固定为`function`
- `function`
    - `name`：函数的名称
    - `description`：函数的作用
    - `parameters`
        - `type`：参数的类型，固定为`object`
        - `properties`
            - `key`：参数的名称（自定义）
                - `type`：参数的类型，固定为`integer`，并在记分板中记录
                - `description`：参数的作用
                - `enum`：参数的可选值（可选）
        - `required`：让NPC必须填的参数（可选）
    - `call`：函数调用的地图数据包Function（可选）

另外，我们还需要在`call`中写明这个函数调用的Minecraft地图数据包Function，结构为`<name_space>:<function_name>`
。具体如何写入地图数据包请参考Minecraft相关文档。

下面是一个简单的示例：

```mcfunction
# （在目录/server/world/datapacks/npchat/data/npchat/functions/open_door.mcfunction下）
# 参数为 'npc_open_door_willingness'，0为关门，1为开门

# 重制玩家的结果
execute run scoreboard players set @s npc_open_door_result 0

# 创建结果记分板，该结果将被gpt读取
execute run scoreboard objectives add npc_open_door_result dummy

# 一些其他步骤，比如检查玩家是否有权限，是否有钥匙等等
# ...

# 检查参数，如果为1将开门（通过移除某一块红石块）
execute if entity @p[scores={npc_open_door_willingness=1}] run setblock -3812 125 2831 air

# 检查参数，如果为0将关门（通过移除放置一块红石块）
execute if entity @p[scores={npc_open_door_willingness=0}] run setblock -3812 125 2831 redstone_block

# 成功执行后，将结果设为为1
execute run scoreboard players set @s npc_open_door_result 1
```

服务器启动时将自动注册在`config/chatwithnpc/functions`
目录下的所有函数，如果需要在服务器启动期间热加载，可以使用`/npchat saveAll`重新加载。

不要忘记为NPC添加这个函数。

**_效果图：_**

![open_door](images/open_door.png)
![close_door](images/close_door.png)