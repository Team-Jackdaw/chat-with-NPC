# 注册 NPC

## 1. NPC 的基本命令

- `/npchat npc` - 最近的 NPC 的状态。
- `/npchat npc setCareer <career>` - 为最近的 NPC 设置职业。
- `/npchat npc setGroup <group>` - 为最近的 NPC 设置组。
- `/npchat npc setBackground <prompt>` - 为最近的 NPC 设置背景。
- `/npchat npc clearMemory` - 清除最近的 NPC 的记忆。

## 2. 注册 NPC 的步骤

1. **创建 NPC**：放置一个新的 `生物实体` 并用 `命名牌` 命名。
2. **注册 NPC**：如果您是 OP，您可以按住 `Shift` 键并点击 NPC 以注册它。然后 NPC 将向您打招呼。现在，NPC 已使用默认设置注册。
3. **设置职业**：您可以使用命令 `/npchat npc setCareer <career>` 为 NPC 设置 `职业`。
4. **设置组**：您可以使用命令 `/npchat npc setGroup <group>` 为 NPC 设置 `组`。要注册新组，请阅读 [RegisterGroup](RegisterGroup_zh.md)。
5. **设置背景**：您可以使用命令 `/npchat npc setBackground <prompt>` 为 NPC 设置 `BasicPrompt`。

## 3. NPC 设置规则

1. **NPC的名称**：NPC的名称需要满足正则`^[a-zA-Z0-9_-]{1,64}$`，即仅限字母数字下划线和横杠。
2. **NPC的职业**：无限制，但推荐英文。
3. **NPC的组**：组名必须符合正则`^[a-zA-Z0-9_-]{1,64}$`，即仅限字母数字下划线和横杠。详情请看[RegisterGroup](RegisterGroup_zh.md)。
4. **NPC的基础设定**: 无限制，但推荐英文。