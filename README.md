## Chat Notify
Chat Notify is a simple client-side Minecraft Fabric mod that allows the 
user to set up audio and visual notifications for when specified words or 
phrases appear in chat.

Note: Some servers (e.g. Hypixel) enforce a non-vanilla color scheme for all
chat messages, which overrides this mod's custom coloring. I'm looking into ways
around that, but in any case the sound should always work fine.
## Features

#### Notify by changing the message color:

![Example of a colored message.](https://i.postimg.cc/y8VwNcpk/chatnotify-keywordexample.png)

![Example of ignoring your own messages but still checking others.](https://i.postimg.cc/VL9x1DRx/chatnotify-ignoreownmessages.png)

#### Notify by changing the message formatting:

![Example of custom formatting.](https://i.postimg.cc/43xgsnBz/chatnotify-customformatting.png)

#### Notify by playing a Minecraft sound (see configuration options below).

## Usage and Configuration

### The config file:

This mod does not currently support an in-game GUI (I'm working on that), but
you can add your own custom notifications by editing the config file,
(located at .minecraft/config/chat-notify.json).

You can edit the json file using notepad, just be careful with the formatting
or you might lose your changes.

### General options:

- "reloadOnJoin": true or false, default is true. 

This controls whether the mod will re-load the config file every time you join
a world or server. Leaving it as true allows you to edit the file and apply
your changes without having to close and re-open Minecraft.

- "ignoreOwnMessages": true/false, default is false. 

Make this true if you only want to be notified of other players' messages. The
default is false because that makes it easier for you to test stuff.

### Notifications (what you're actually here for):

This is where you can add customized notifications. The first one (number 0) is
for your username. You can change the color, formatting and sound options, but
you can't remove it (if you try it will get added back automatically). If you
don't want it, just set the color to "#FFFFFF" (white) and "playSound" to false.
Add your own custom notifications below that.

The number at the start of each notification block indicates its priority 
(smaller number means higher priority). This is so that if a chat message 
matches two notifications, only the higher-priority (smaller number) one will 
control the message formatting and sound. You can't have duplicates.

#### Changing the notification trigger:
- "trigger": The word or phrase that will make the notification happen. 
Make sure it's in quotations "like this".

#### Changing the notification color:
- "color": The hex code of the color you want the chat message to be, e.g. 
"#FF0000" for red. Refer to [this site](https://www.color-hex.com).

#### Changing the formatting:
- "bold", "italic", "underlined", "strikethrough", "obfuscated": These are all 
the formatting controls. true for ON, false for OFF.

#### Changing the notification sound:
- "playSound": true/false. Whether to play the notification sound.
- "sound": See below.

The "sound" option is the Minecraft ID of the sound you want the notification
to play. To find a sound, [click here](https://github.com/NotRyken/ChatNotify/blob/master/src/main/resources/assets/chatnotify/SoundList.txt), or, in Minecraft with cheats on,
type the command /playsound, and look through the auto-complete options.

The "sound" option can handle both formats, so "minecraft:block.anvil.land" and
"BLOCK_ANVIL_LAND" will both work fine. If you get it wrong, it will default
to the XP sound.

### Performance
I have tried to reduce the workload wherever possible, but ultimately it still
has to process every single chat message. That said, it is unlikely you will be
able to notice any performance impact unless your chat is being spammed really
fast (e.g. by multiple repeating command-blocks).

## Contact

Discord: Ryken#8585

[Public issue tracker](https://github.com/NotRyken/ChatNotify/issues)