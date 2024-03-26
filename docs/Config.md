# Settings

## 1. Configuration File

The configuration file of the mod is stored in the `chat-with-npc` folder in the plugin directory. The file name is
the `config.json`. The content of the file is as follows:

```json
{
  "enabled": true,
  "range": 10.0,
  "language": "Chinese",
  "apiKey": "sk-XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX",
  "model": "gpt-3.5-turbo",
  "apiURL": "api.openai.com",
  "isBubble": true,
  "isChatBar": true,
  "wordLimit": 30
}
```

You can set the parameters below:

- `range`: how far the players can see and talk to the NPC.
- `language`: which language the NPC will use to talk to the player.
- `model`: the model of OpenAI
- `apiURL`: you can use the proxy to access the OpenAI API.
- `isBubble`: whether to show the bubble above the NPC.
- `isChatBar`: whether to show the chat of the NPC in the chat bar of the player.
- `wordLimit`: the maximum number of words in each reply of the NPC.

## 2. Manage you Assistant in OpenAI platform

Once you create a new NPC, you will see the Assistant in your OpenAI platform as below:

![Assistant](images/assistant.png)

You can access to the OpenAI platform to manage, use and test your NPCs in
the [OpenAI platform Assistant](https://platform.openai.com/assistants).

**Important: The settings in your configuration file will override the settings on the OpenAI platform, so remember to
modify them in the configuration file after testing.

## 3. Introduction of the structure of the mod

This document is about some basic settings of the plugin, which can help users to use the plugin well, or to develop the
plugin within the scope of the license. The plugin has the following parts:

1. Conversation manager
2. Group manager
3. NPCEntity manager
4. Lifecycle manager

And some basic settings:

- All times of this plugin are in **milliseconds**, recorded by `Long` type.

## 4. Conversation manager

The Conversation manager is the core part of the plugin, which is responsible for managing all conversations. The main
settings of the Conversation are:

- `ConversationManager` stores all `ConversationHandler`, which is the current conversation of an NPC, and provides
  methods to activate, extract, and delete conversations.
- `ConversationHandler` contains an `NPCEntity` member, the time `updateTime` of the last conversation, and whether the
  current NPC is talking `isTalking`.
- When the NPC is asynchronously requesting information from the model, `isTalking` will be set to `true` until the
  model returns the result, and `updateTime` will be updated.
- `ConversationHandler` is only activated when the player is talking to the NPC, and unloaded 5 minutes after the player
    stops talking to the NPC.
- The lifecycle of `ConversationHandler` is managed by the plugin lifecycle manager.

## 5. Group manager

The Group manager is responsible for managing all Groups, and the main settings of the Group are:

- `GroupManager` stores all `Group` and provides methods to activate, extract, and delete Groups.
- `Group` contains a last load time `lastLoadTime`, and some settings `parentGroup`, `instructions`, `event`.
- `Group` is only activated when the `ConversationHandle` requests information from the model, or when an OP uses a
  command to view and modify a Group.
- The lifecycle of `Group` is managed by the plugin lifecycle manager.

Other settings:

- The `parentGroup` of `Group` is a parent Group, and in general, all `Group` will eventually point to `Global`.

## 6. NPCEntity manager

The NPCEntity manager is responsible for managing all NPCEntity, and the main settings of NPCEntity are:

- `NPCEntityManager` stores all `NPCEntity` and provides methods to activate, extract, and delete NPCEntity.
- `NPCEntity` contains a `TextBubbleEntity`, and some settings `Career`, `instructions`, `Group`, `longTermMemory`.
- `NPCEntity` is only activated when the player is talking to the NPC, or when an OP uses a command to view and modify an
  NPC.

Other settings:

- The `TextBubbleEntity` of `NPCEntity` is a text bubble, managed by `NPCEntity`, which will be loaded when the NPC is
  activated, and unloaded when the NPC is unloaded.
- The `TextBubbleEntity` will be displayed **0.55 blocks** above the NPC, and when the NPC replies to the player, the
  bubble will be displayed normally for **10 seconds**, and then become transparent, but will not be unloaded (to be
  modified).

## 7. Lifecycle manager

The lifecycle manager is responsible for managing the lifecycle of all `ConversationHandler`, `NPCEntity` and `Group`, and the main
settings are:

- `ConversationHandler` and `Group` are managed by the lifecycle manager, and the lifecycle manager provides methods to
  activate, extract, and discard `ConversationHandler`, `NPCEntity` and `Group`.
- The lifecycle manager will automatically discard the `ConversationHandler`, `NPCEntity` and `Group` that have not been activated for
  a 5 minutes period every 30 seconds after the plugin is loaded.
- All `ConversationHandlers`, `NPCEntity` and `Groups` can be unloaded asynchronously with the command `/npc saveAll`.
