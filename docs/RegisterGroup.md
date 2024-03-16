# Register Group

## 1. Basic Commands for Group

- `/npchat group <group>` - View the information of a group.
- `/npchat group <group> setParent <parent>` - Set the parent group for the group.
- `/npchat group <group> addPermanentPrompt <prompt>` - Add a permanent prompt to the group (adjective or sentence).
- `/npchat group <group> popPermanentPrompt` - Pop a permanent prompt from the group.
- `/npchat group <group> addTempEvent <event>` - Add a temporary event to the group (valid for 7 days).
- `/npchat addGroup <newGroup>` - Add a new group


## 2. Steps to Register Group

1. **Create a Group**: Use the command `/npchat addGroup <newGroup>` to create a new group.

    > You can use the command `/npchat group <group>` to view the information of the group.
    ![new Group](images/initgroup.png)

2. **Set the Parent Group**: Use the command `/npchat group <group> setParent <parent>` to set the parent group for the group.
3. **Add Permanent Prompt**: Use the command `/npchat group <group> addPermanentPrompt <prompt>` to add a permanent prompt to the group.
4. **Add Temporary Event**: Use the command `/npchat group <group> addTempEvent <event>` to add a temporary event to the group.

    > ![set Group](images/newgroup.png)

5. **Add Group to an NPC**: Use the command `/npchat npc setGroup <group>` to add this group to an NPC. And then they will know everything about this group.
6. **Reload the Plugin and Save Data**: You may need to use `/npchat reload` to reload the plugin immediately and save this new NPC data (note that this will also unload all other conversations).

## 3. Rules for Group Setting

1. **Group Name**: The group name needs to meet the regular pattern `^[a-zA-Z0-9_-]{1,64}$`, that is, only alphanumeric, underscores, and dashes are allowed.
2. **Parent Group**: The parent group name must be registered.
3. **Permanent Prompt**: No restrictions, but English is recommended. It should be an adjective or descriptive sentence.
4. **Temporary Event**: No restrictions, but English is recommended. It should be a noun or noun phrase and sentence.

## 4. Group Example

- `name`: `imperialPalace`
- `parent`: `SkeyCity`
- `permanentPrompt`: `Majestic`, `ancient`, `opulent`, `grandiose`, `fortified`, `sprawling`, `ornate`, `regal`, `awe-inspiring`, `very save`
- `tempEvent`: `enthronement ceremony`

