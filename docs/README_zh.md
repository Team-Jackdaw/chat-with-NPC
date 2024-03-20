# Chat-with-NPC

![GitHub release](https://img.shields.io/github/v/release/Team-Jackdaw/chat-with-NPC?include_prereleases)
![GitHub license](https://img.shields.io/github/license/Team-Jackdaw/chat-with-NPC)
![test workflow](https://github.com/Team-Jackdaw/chat-with-NPC/actions/workflows/build.yml/badge.svg)

> 该mod仍在开发中，当前版本不稳定。如果您想使用mod，请自行编译源代码，参见[构建](#8-构建)。如果遇到任何问题，请提交问题。

[English Version](../README.md)

## 1. 介绍
**Chat With NPC** 允许玩家自由地与NPC进行对话，NPC将根据其设置（基本提示）和其组（国家、城市、城镇等）的设置来回答问题。适用于RPG地图或谜题地图。NPC不会改变游戏规则，即实体的行为不受聊天内容的影响（无论是在冒险模式还是生存模式中），这意味着即使在生存服务器上也可以使用它来使游戏更有趣。

在未来的版本中，我们将添加创新功能，例如NPC根据聊天内容执行动作，NPC彼此聊天，NPC对事件的观点等，以便同一组中的NPC可以相互交互。

## 2. 如何使用
任何玩家都可以通过按住`Shift`键并点击NPC来与其交谈，如果NPC已由OP注册，则NPC将向玩家打招呼。

NPC的对话可以被所有人看到（在他们头顶的**聊天气泡**中），或者在NPC周围的一定范围内的玩家（在**聊天栏**中）。

![image](images/greeting.png)

任何附近的玩家都可以直接在聊天栏中回复NPC（如果玩家附近有多个NPC，则回复最近的NPC）。

![image](images/reply.png)

如果您是管理员，请参阅[安装](#5-安装)以获取更多信息。

## 3. 功能

1. 每个`NPC`都有自己的`Instructions`，并且可以属于一个`Group`。
2. 每个`NPC`可以自动管理他们的上下文，包括他们的记忆，如果记忆过长，那么早前的记忆会被自动压缩。
3. 每个`Group`都有一些`PermanentPrompt`来描述它们，并且还可以记录一些`TempEvent`，这将在一段时间后结束。
4. 每个`Group`都可以记录一个`ParentGroup`，直到该组的父组是`Global`，`Global`不能有父组。
5. 每个`NPC`的对话将结合他自己的`BasicPrompt`，这次对话的上下文（如果设置了记住记忆，那么将包含所有上下文），他的`Group`及其所有`ParentGroup`的`PermanentPrompt`的内容，以及`TempEvent`的内容。

## 4. 要求
- Minecraft Server 1.19.4 或更高版本
- Fabric Loader 0.12.0 或更高版本
- 包含Fabric API

## 5. 安装
1. 在`mods`文件夹中安装mod后，从[此处](https://platform.openai.com/api-keys)获取您的OpenAI API密钥，并使用`/npchat setkey <key>`命令设置它。
2. 您可以使用[命令](#6-命令)来设置mod的基本配置。
3. 要注册NPC，请阅读[RegisterNPC](RegisterNPC_zh.md)。
4. 要注册组，请阅读[RegisterGroup](RegisterGroup_zh.md)。
5. 要了解有关mod的配置，请阅读[配置](Config_zh.md)。

## 6. 命令
- `/npchat` - 查看配置状态
- `/npchat help` - 查看命令帮助
- `/npchat enable/disable` - 启用/禁用mod
- `/npchat setKey <key>` - 设置OpenAI API密钥
- `/npchat setModel <model>` - 设置AI模型
- `/npchat setRange <range>` - 设置对话范围
- `/npchat setLanguage <language>` - 设置响应语言
- `/npchat setMaxTokens <maxTokens>` - 设置对话的最大标记
- `/npchat setURL <url>` - 设置OpenAI API代理URL
- `/npchat saveAll` - 将所有数据保存到磁盘

## 7. 待做列表

- [ ] 改进聊天气泡。
- [ ] NPC之间可以相互聊天，并交流观点。
- [ ] NPC会对他们所属的`Group`中的事件有自己的看法。
- [ ] NPC会对聊天内容作出动作反应。

## 8. 构建

1. 克隆存储库。
2. 在存储库的根目录中运行`./gradlew build`。
3. jar文件将生成在`build/libs`目录中。

## 9. 参考
- [AIMobs](https://github.com/rebane2001/aimobs) by [rebane2001](https://github.com/rebane2001) and [Eianex](https://github.com/Eianex)