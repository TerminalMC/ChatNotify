# Chat Notify
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

This mod requires [Mod Menu](https://modrinth.com/mod/modmenu) for 
configuration (if you aren't using it already, now's a great time to start).

With Mod Menu installed, simply go the mods list, find Chat Notify, click
on the icon, and it's fairly self-explanatory from there. If you get stuck 
please let me know, and I'll see if I can make it more clear.

![](https://i.postimg.cc/Hk7pSQqT/chatnotify-modmenu-1.png)

![](https://i.postimg.cc/fbZ6xmZ2/chatnotify-modmenu-2.png)

![](https://i.postimg.cc/tJ1fXztT/chatnotify-modmenu-3.png)

### Performance
I have tried to reduce the workload wherever possible, but ultimately it still
has to process every single chat message. That said, it is unlikely you will be
able to notice any performance impact unless your chat is being spammed really
fast (e.g. by multiple repeating command-blocks).

## Contact

[Linktree](https://linktr.ee/notryken)