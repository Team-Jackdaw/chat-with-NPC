# 注册Group

## 1. 群组的基本命令

- `/npchat group <group>` - 群组命令
- `/npchat group <group> setParent <parent>` - 设置群组的父级群组。
- `/npchat group <group> addPermanentPrompt <prompt>` - 向群组添加永久提示。
- `/npchat group <group> popPermanentPrompt` - 从群组中删除最近一条永久提示。
- `/npchat group <group> addTempEvent <event>` - 向群组添加一条临时事件。
- `/npchat addGroup <newGroup>` - 添加一个新的群组


## 2. 注册群组的步骤

1. **创建群组**：使用命令 `/npchat addGroup <newGroup>` 创建一个新的群组。
2. **设置父级群组**：使用命令 `/npchat group <group> setParent <parent>` 为群组设置父级群组。
3. **添加永久提示**：使用命令 `/npchat group <group> addPermanentPrompt <prompt>` 向群组添加永久提示。
4. **添加临时事件**：使用命令 `/npchat group <group> addTempEvent <event>` 向群组添加临时事件。
5. **将群组添加到NPC**：使用命令 `/npchat npc setGroup <group>` 将该群组添加到NPC中。然后他们将了解有关该群组的一切。

## 3. 群组设置规则

1. **群组名称**：群组名称需要符合正则模式 `^[a-zA-Z0-9_-]{1,64}$`，即只允许使用字母、数字、下划线和破折号。
2. **父级群组**：父级群组名称必须已注册。
3. **永久提示**：没有限制，但建议使用英文。
4. **临时事件**：没有限制，但建议使用英文。

