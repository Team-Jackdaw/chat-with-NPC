# Register Group

## 1. Basic Commands for Group

- `/npchat group <group>` - Group commands
- `/npchat group <group> setParent <parent>` - Set the parent group for the group.
- `/npchat group <group> addPermanentPrompt <prompt>` - Add a permanent prompt to the group.
- `/npchat group <group> popPermanentPrompt` - Pop a permanent prompt from the group.
- `/npchat group <group> addTempEvent <event>` - Add a temporary event to the group.
- `/npchat addGroup <newGroup>` - Add a new group


## 2. Steps to Register Group

1. **Create a Group**: Use the command `/npchat addGroup <newGroup>` to create a new group.
2. **Set the Parent Group**: Use the command `/npchat group <group> setParent <parent>` to set the parent group for the group.
3. **Add Permanent Prompt**: Use the command `/npchat group <group> addPermanentPrompt <prompt>` to add a permanent prompt to the group.
4. **Add Temporary Event**: Use the command `/npchat group <group> addTempEvent <event>` to add a temporary event to the group.
5. **Add Group to an NPC**: Use the command `/npchat npc setGroup <group>` to add this group to an NPC. And then they will know everything about this group.

## 3. Rules for Group Setting

1. **Group Name**: The group name needs to meet the regular pattern `^[a-zA-Z0-9_-]{1,64}$`, that is, only alphanumeric, underscores, and dashes are allowed.
2. **Parent Group**: The parent group name must be registered.
3. **Permanent Prompt**: No restrictions, but English is recommended.
4. **Temporary Event**: No restrictions, but English is recommended.

