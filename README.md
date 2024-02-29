# Chat-with-NPC
Chat-with-NPC allow players to interact and talk to specific entities under specific prompts (for use in RPG maps).

This mode is a fork of [AIMobs](https://github.com/rebane2001/aimobs) by [rebane2001](https://github.com/rebane2001) and [Eianex](https://github.com/Eianex).

AIMobs is a mod that lets you chat with Minecraft mobs and other entities by creating prompts and using the OpenAI API. This mod has the following changes:

- The mod is now a server-side mod, so it can be used in multiplayer servers.
- ...... (to be continued)

## Requirements
- Minecraft 1.19.*
- Fabric
- Fabric API

## Usage
After installing the mod, grab your OpenAI API key from [here](https://beta.openai.com/account/api-keys), and set it with the `/npchat setkey <key>` command.

You should now be able to **talk to mobs by shift+clicking** on them!

## Commands
- `/npchat` - View configuration status
- `/npchat help` - View commands help
- `/npchat enable/disable` - Enable/disable the mod
- `/npchat setkey <key>` - Set OpenAI API key
- `/npchat setmodel <model>` - Set AI model
- `/npchat settemp <temperature>` - Set model temperature

## 开发结构（临时章节）

### 软件包结构

1. api
    - 包含与OpenAI API交互的所有代码。
    - 类：`RequestHandler`
2. data
    - 用于存储和管理数据，如NPC状态和环境信息。
    - 类：`NPCDataManager`, `EnvironmentDataManager`
    - 接口：`DataManager`
3. npc
    - 与NPC相关的核心功能。
    - 类：`NPCEntityManager`, `VillagerNPCEntity`, `LivingNPCEntity`
    - 抽象类：`NPCEntity`
    - 枚举类：`Actions`
    - 接口：`NPCHandler`
4. event
    - 用于管理通话事件和处理程序。
    - 类：`ConversationManager`, `ConversationHandler`
5. group
    - 管理全局和本地环境设置。
    - 类：`EnvironmentManager`, `GlobalEnvironment`, `LocalEnvironment`
    - 接口：`Environment`
6. auxiliary
    - 通用工具和助手类。
    - 子包: `configuration`, `command`， `prompt`， `yaml`

### 类和接口
- RequestHandler
    - 负责与OpenAI API的交互，封装API请求和响应。
- DataManager
    - 管理数据存储，提供数据访问的接口。
- NpcManager
    - 管理NPC实体的创建和更新。
- NpcEntity
    - 表示一个NPC实体，包含状态和行为。
- ConversationManager
    - 管理与NPC交谈事件的创建和更新。
- ConversationHandler
    - 用于处理与NPC交谈的单一事件。
- EnvironmentManager
    - 管理全局和本地环境设置的创建和更新。
- GlobalEnvironment/LocalEnvironment
    - 存储和管理特定的环境信息。

### 抽象类
- NPCEntity
    - 为NPC提供一个共同的基类，定义了一些通用的属性和方法。

### 数据结构

- NPC管理
  - NPC实例 - （标识（key），组，职业，描述，聊天记录）
  - NPC文件管理 - （标识，组，职业，描述，聊天记录）
- 环境管理
  - 环境实例 - （标识（key），父组，描述）
  - 环境文件管理 - （标识，父组，描述）
- 会话管理
  - 会话实例 - （玩家（key），NPC）

### 运行流程

玩家与NPC对话：

1. 激活会话->加载（注册）NPC->启动会话->递归加载（注册）环境->生成prompt->OpenAI API->记录聊天信息，回应玩家

2. 玩家发送信息->（如果存在会话）加载会话->记录聊天信息，生成新prompt->OpenAI API->记录聊天信息，回应玩家

### 实例生命周期，卸载时更新文件：

1. 会话超5分钟无新内容，自动卸载。
2. 玩家与新的NPC对话，旧对话卸载。
3. NPC超5分钟无新对话内容，实例自动卸载。
4. 环境实例超5分钟未被读取，实例自动卸载。
5. 服务器关闭，会话，NPC实例，环境实例均自动卸载。

