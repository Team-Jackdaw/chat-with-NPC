
# Register NPC

## 1. Basic Commands for NPC

- `/npchat npc` - NPC status of the closest NPC.
- `/npchat npc setCareer <career>` - Set the career for the closest NPC.
- `/npchat npc setGroup <group>` - Set the group for the closest NPC.
- `/npchat npc setBackground <prompt>` - Set the background for the closest NPC.
- `/npchat npc clearMemory` - Clear the memory for the closest NPC.

## 2. Steps to Register NPC

1. **Create a NPC**: Place a new `Villager` and name it with a `Name Tag`.
2. **Register the NPC**: If you are an OP, you can shift+clicking on the NPC to register it. Then the NPC will greet with you. Now, the NPC is registered with default settings.
3. **Set the Career**: You can use the command `/npchat npc setCareer <career>` to set the `Career` for the NPC. 
4. **Set the Group**: You can use the command `/npchat npc setGroup <group>` to set the `Group` for the NPC. To register a new group, please read [RegisterGroup](RegisterGroup.md).
5. **Set the Background**: You can use the command `/npchat npc setBackground <prompt>` to set the `BasicPrompt` for the NPC.

## 3. Rules for NPC Setting

1. **NPC's Name**: The NPC name needs to meet the regular pattern `^[a-zA-Z0-9_-]{1,64}$`, that is, only alphanumeric underscores and horizontal bars are allowed.
2. **NPC's Career**: No restrictions, but English is recommended.
3. **NPC's Group**: The group name must conform to the regular pattern `^[a-zA-Z0-9_-]{1,64}$`, that is, only alphanumeric underscores and dashes are allowed. Please see [RegisterGroup](RegisterGroup.md) for details.
4. **NPC's Background**: No restrictions, but English is recommended.
