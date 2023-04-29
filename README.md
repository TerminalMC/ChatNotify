# Chat Notify
Chat Notify is a simple mod that allows you to set up audio and visual
notifications for when specified words or phrases appear in chat. 

# Features

#### Change the color of a message: 

![Example of a colored message.](https://i.postimg.cc/y8VwNcpk/chatnotify-keywordexample.png)

![Example of ignoring your own messages but still checking others.](https://i.postimg.cc/VL9x1DRx/chatnotify-ignoreownmessages.png)

#### Change the formatting of a message:

![Example of custom formatting.](https://i.postimg.cc/43xgsnBz/chatnotify-customformatting.png)

#### Play a Minecraft sound:

You can add sounds using the config file (see below). For the full list, see [insert link].

# Usage

#### The config file:

This mod does not currently support customization from within the game,
but you can change all available options using the config file, which
is located at [.minecraft/config/chatnotify.json]. You can edit this
file using notepad, just be careful with the formatting (you can't break
anything, but it will undo your changes if you get it wrong).

#### General options:

- "reloadOnJoin": true/false. This controls whether the config file is 
reloaded every time you join a world or server. It is true by default, 
and it makes it more convenient to edit the config file because you don't
have to close Minecraft every time you make a change, only leave and 
re-join the world or server.


- "playerName": Nothing - you can try changing it but it won't do anything.


- "ignoreOwnMessages": true/false. This is false by default, make it true
if you only want to be notified when other players say stuff.


#### Notification Options:

This is where you can add customized notifications. Note: The first one
(option 0) is required. You can change it, but you can't remove it. Add your
own options below (watch out for those commas and quotation marks).


The number at the start of each option indicates its priority (smaller number
means higher priority). This is so that if a chat message matches two options,
only the higher-priority (smaller number) one will control the formatting 
and sound. You can't have duplicates.

#### Adding trigger words:
- "word": The word or phrase that will trigger the notification. Make sure 
it's in quotations "like this".

#### Changing the notification color:
- "color": The hex code of the color you want the chat message to be, e.g. 
"FF0000" for red. Refer to [this site](https://www.rapidtables.com/web/color/RGB_Color.html).

#### Changing the formatting:
- "bold", "italic", "underlined", "strikethrough", "obfuscated": Formatting 
controls. true for ON, false for OFF.

#### Changing the notification sound:
- "playSound": true/false. Whether to play the specified sound.
- "sound": See below.

The "sound" control is the Minecraft ID of the sound you want the notification
to play. To find a sound, [click here](https://github.com/NotRyken/ChatNotify/blob/master/src/main/resources/assets/chatnotify/SoundList.txt), or, in Minecraft with cheats on,
type the command /playsound, and look through the auto-complete options.

The "sound" can handle both formats, so "minecraft:block.anvil.land" and
"BLOCK_ANVIL_LAND" will both work fine. If you get it wrong, it will default
to the XP sound.

### Performance
While significant effort has been put into making this mod lightweight, 
ultimately it still has to process every chat message and that does 
take work. However, it is unlikely that you will only notice a 
performance impact unless you have, for example, a couple of repeating 
command-blocks constantly spamming chat.

If you do think it is causing any lag, you can try minimizing the 
number of words that you have notification options for, and/or
set the config option "reloadOnJoin" to false. Note: If you do set
it to false and want to change the config file, you will have to
close Minecraft, edit the config file, and then start Minecraft
again.

# Contact

https://github.com/NotRyken/ChatNotify/issues