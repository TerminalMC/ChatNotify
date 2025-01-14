<div align="center"><center>

<img alt="Icon" width=100 src="https://raw.githubusercontent.com/TerminalMC/ChatNotify/HEAD/common/src/main/resources/assets/chatnotify/icon.png">

## ChatNotify

Plays a ping sound when your name is mentioned, with options to create custom alerts.

[![Environment](https://img.shields.io/badge/Environment-Client-blue?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAABhWlDQ1BJQ0MgcHJvZmlsZQAAKJF9kT1Iw0AYht+malUqDnYQEclQneyiIo6likWwUNoKrTqYXPoHTRqSFBdHwbXg4M9i1cHFWVcHV0EQ/AFxdnBSdJESv0sKLWI8uLuH97735e47QGhUmGp2RQFVs4xUPCZmc6ti4BU9CKCP1jGJmXoivZiB5/i6h4/vdxGe5V335xhQ8iYDfCJxlOmGRbxBPLtp6Zz3iUOsJCnE58STBl2Q+JHrsstvnIsOCzwzZGRS88QhYrHYwXIHs5KhEs8QhxVVo3wh67LCeYuzWqmx1j35C4N5bSXNdZqjiGMJCSQhQkYNZVRgIUK7RoqJFJ3HPPwjjj9JLplcZTByLKAKFZLjB/+D3701C9NTblIwBnS/2PbHOBDYBZp12/4+tu3mCeB/Bq60tr/aAOY+Sa+3tfARMLgNXFy3NXkPuNwBhp90yZAcyU9TKBSA9zP6phwwdAv0r7l9a53j9AHIUK+Wb4CDQ2CiSNnrHu/u7ezbvzWt/v0ATphymIBZ6aQAAAAGYktHRAAKAAwAGd6C8noAAAAJcEhZcwAADdcAAA3XAUIom3gAAAAHdElNRQfoBgcOHRYlcgoRAAABRklEQVR42u2YMUoDQRRAX0axUzCteIZ4hKn0FDmFhalSWKkgnkHt9AQWwhzBNr2tBGNno82ACwm6EZvxvwdTzP8s7P8zu8w8EJHIDABKKfvAFXAIbP/zmt+AR2CSc54NavFPwDDY4s+BUaorPwy4+3eBy1S3fVSOUoBv/jt2UmMv/A6cAHt1TGqsb36JzcYaMM05X3Tm56UUgLOe+SVa2wE3K2LXa+Sbb8BgRWxjjXzzDRj/EBv3fOarY6WUj8Z+glPgtlPcKbDVM998A/6cVM/GUXlN9WIQlYdUDwvzgMW/AMcp5zwDRsA9sAhQ+AK4Aw5yzs8aEZHY6AR1gjpBnaBOsLHrsE6wM9cJohPUCeoE0Qn+Hp0gOkGdoE5QRMKiE9QJ6gR1gjrBxq7DOsHOXCeITlAnqBNEJ/h7dILoBHWCOkERCcsncuextWq5TzoAAAAASUVORK5CYII=)]()
[![Latest Minecraft](https://img.shields.io/modrinth/game-versions/Iudurxl8?label=Latest%20Minecraft&color=%2300AF5C&logo=data:image/png;base64,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)](https://modrinth.com/mod/Iudurxl8/versions)

[![Loader](https://img.shields.io/badge/Available%20for-Fabric-dbd0b4?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABoAAAAcBAMAAACNPbLgAAABhGlDQ1BJQ0MgcHJvZmlsZQAAKJF9kT1Iw0AcxV9TpX5UHMwgIpihOtlFRRxLFYtgobQVWnUwufQLmjQkKS6OgmvBwY/FqoOLs64OroIg+AHi6uKk6CIl/i8ptIj14Lgf7+497t4BQr3MNKsrAmi6bSZjUSmTXZUCr+iHiADG0Cszy4inFtPoOL7u4ePrXZhndT735xhQcxYDfBJxhBmmTbxBPLtpG5z3iUVWlFXic+JJky5I/Mh1xeM3zgWXBZ4pmunkPLFILBXaWGljVjQ14hnikKrplC9kPFY5b3HWylXWvCd/YTCnr6S4TnMUMSwhjgQkKKiihDJshGnVSbGQpP1oB/+I60+QSyFXCYwcC6hAg+z6wf/gd7dWfnrKSwpGge4Xx/kYBwK7QKPmON/HjtM4AfzPwJXe8lfqwNwn6bWWFjoCBreBi+uWpuwBlzvA8JMhm7Ir+WkK+TzwfkbflAWGboG+Na+35j5OH4A0dbV8AxwcAhMFyl7v8O6e9t7+PdPs7wd+dXKrd9SjeQAAAAlwSFlzAAAuIwAALiMBeKU/dgAAAAd0SU1FB+cLFAcgIbOcUjoAAAAbUExURQAAAB0tQTg0KoB6bZqSfq6mlLyynMa8pdvQtJRJT6UAAAABdFJOUwBA5thmAAAAAWJLR0QB/wIt3gAAAF5JREFUGNN10FENwCAMhOFqOQuzMAtYOAtYqGw6mkEvhL59yR9Ca5YDqyOC465eKYqQm6LoCkVwnwQOBYKdeA5l51zhFtrsnPmg6m3Z2akk15dFH1lWFQVxlUFv+2sAJlA9O7NwQRQAAAAASUVORK5CYII=)](https://fabricmc.net/)
[![Loader](https://img.shields.io/badge/Available%20for-NeoForge-f16436?logo=data:image/png;base64,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)](https://neoforged.net/)

[![Download on Modrinth](https://img.shields.io/modrinth/dt/Iudurxl8?label=Download%20on%20Modrinth&logo=modrinth&logoColor=%2300AF5C)](https://modrinth.com/mod/Iudurxl8)
[![Download on GitHub](https://img.shields.io/github/downloads/TerminalMC/ChatNotify/total?label=Download%20on%20GitHub&logo=github&logoColor=white)](https://github.com/TerminalMC/ChatNotify)

</center></div>

### Features

- Easy access to all Minecraft sound effects, plus sounds from resourcepacks.
- Customizable volume and pitch for alert sounds.
- Text highlighting with full RGB color and format control.
- Complete control over alert triggers with optional regex support and anti-triggers.
- Automatic response messages with optional delay.
- Fully custom options GUI for efficient configuration.

### Overview

### Setup

<details open>
<summary><b>How it Works</b></summary>

- ChatNotify has a list of `Notifications`, which you can view on the options screen.
- Each notification has one or more `Triggers`, and options to control what happens when the notification is activated.

1. When a new message arrives in chat, ChatNotify starts checking the triggers of each notification.
2. If a trigger matches the chat message, that notification will be activated.
3. When a notification is activated, two things will happen;
   1. The message will be restyled, to highlight the trigger that activated the notification.
   2. A sound will be played.

- You can create and customize your own notifications via the options screen, which can be opened using 
  [ModMenu](https://modrinth.com/mod/mOgUt4GM) on Fabric, or the mod list on NeoForge.
- See below for basic guides on setting up your own custom notifications.
</details>

<details open>
<summary><b>Level 0: "Just ping me when my name is mentioned"</b></summary>

- If you only want to get pinged when someone says your name, the mod works automatically - no setup is required.
</details>

<details>
<summary><b>Level 1A (Normal): "Ping me when someone says hello"</b></summary>

1. Access the options screen.
2. Click the `+` button to add a new notification.
3. Type your custom notification trigger (e.g. "hello") in the field on the left.
4. To change the highlight color, click the `🌢` button.
5. To change the notification sound, click the `🔊` button.
    1. Select a common sound by clicking one of the buttons, or
    2. Click the field at the top to search for other sounds.
6. To disable highlighting or sound, right-click the `🌢` or `🔊` button.
7. When you're finished, click `Done` to exit.
</details>

<details>
<summary><b>Level 1B (Normal): "Also ping me when someone says hi"</b></summary>

1. Access the options screen and find your notification from `Level 1A`.
2. Click the square `'More Options'` button on the right of the trigger field.
3. On the new screen, click the large `+` button below the existing trigger.
4. Type your custom notification trigger (e.g. "hi") into the new field.
5. Click `Done` to return to the main screen, then `Done` again to exit.
</details>

<details>
<summary><b>Level 2A (Key): "Ping me when someone gets an advancement"</b></summary>

1. Access the options screen and click the `+` button to add a new notification.
2. Click the `~` button on the left twice, so it shows a key. A new `🔍` button will appear.
3. Click the `🔍` button.
4. On the new screen, click the `Any Advancement` button, then click `Done`.
5. Change the color and sound if you want (as in `Level 1A`), then click `Done` to exit.
- **Note**: Some servers remove keys from messages, which prevents this type of trigger from working.
- To check whether a message has a key, follow `Level 3` and look at the `Key` field after clicking the message.
- If the message does not have a key (or uses a generic key), you must use a normal (`~`) trigger instead.
</details>

<details>
<summary><b>Level 2B (Key): "Play a sound for every new message"</b></summary>

1. Access the options screen and add a new notification (`+`).
2. Click the `~` button on the left twice, so it shows a key. A new `🔍` button will appear.
3. Click the `🔍` button.
4. On the new screen, click the `Any Message` button, then click `Done`.
5. Change the color and sound if you want (as in `Level 1A`), then click `Done` again.
- **Note**: Because this notification will activate for every message, you probably want it to be last.
- On the options screen, click and drag the button on the far left to change the notification order.
</details>

<details>
<summary><b>Level 3 (Normal): "Ping me when a specific server message appears"</b></summary>

1. Join the server and wait for the message to appear in chat.
2. Access the options screen and add a new notification (`+`).
3. Click the `✎` button on the right of the trigger field to open the trigger editor.
4. Chat messages will be displayed in a list, most recent first.
5. Find your message, and click on it.
6. The message text will be placed into the `Text` field.
7. Use the message text to create a custom trigger in the top field.
8. To test your trigger, toggle the `Filter` button.
9. When the filter is on, only messages that match the trigger will be shown.
10. Once you're satisfied, click `Done` to return to the options screen.
</details>

<details>
<summary><b>Level 4 (Regex): "I want more control"</b></summary>

1. If the message might contain special characters, follow `Level 3` to copy the message text.
2. Optionally use a tool like [regex101](https://regex101.com) to help build a proper regex pattern for the message.
3. Access the options screen and add a new notification (`+`).
4. Click the `~` button on the left, so it shows `.*`.
5. Enter your regex pattern into the trigger field.

</details>

### Other Options

#### Global Options

To access, click the `Global Options` button from the options screen.

<details>
<summary><b>Detection Mode</b></summary>

- Controls where and how incoming messages are intercepted.
- Useful if you have client-side mods sending messages that you want to detect or ignore.
- Default is `HUD (Tags)`.
</details>

<details>
<summary><b>Send Mode</b></summary>

- Controls where and how response messages are sent.
- Useful if you want other client-side to detect or ignore response messages.
- Default is `Packet`.
</details>

<details>
<summary><b>Activation Mode</b></summary>

- Controls how many notifications can be triggered by a single message.
- Preference only.
- Default is `Single Sound`.
</details>

<details>
<summary><b>Restyle Mode</b></summary>

- Controls how many triggers (or instances of triggers) will be restyled, per notification.
- Preference only.
- Default is `All Instances`.
</details>

#### Notification Options

To access, click the `'More Options'` button for the notification you want to edit.

<details>
<summary><b>Restyle String</b></summary>

- This feature provides finer control over which part of a message is highlighted (restyled).
- For example, if you have a regex trigger that matches "correct horse battery staple" but you only want to highlight
  "battery", you can specify "battery" as a restyle string.
- To set a restyle string, access the `Notification Options` screen then press the small `+` button to the right
  of the trigger field.
</details>

#### Advanced Options

To access, first go to the `Notification Options` screen, then click the `Advanced Options` button.

<details>
<summary><b>Custom Messages</b></summary>

- This feature allows you to send messages to yourself when a notification is activated. See below for the different 
  types.

- All custom messages support color and format codes using `$` instead of `§`. For more information on codes, refer to 
  [the Minecraft Wiki](https://minecraft.wiki/w/Formatting_codes).

- If the notification has a regex trigger, you can access capturing groups from the match using `(1)`, `(2)` etc. in the
  custom message. For example, if the trigger is `(\d+) stacks`, and you have a replacement message `(1)x64`, the message "23 stacks"
  will be replaced by "23x64".

<details>
<summary><b>Replacement Messages</b></summary>

- This feature allows you to replace the triggering chat message with a custom message.
- **Note**: If you switch this to `ON` and leave the field blank, the message will be blocked.
  - If a message is blocked, any subsequent notifications cannot be activated, but previous ones that have already
    activated may still play sounds or send response messages, so use this feature with caution.
</details>

<details>
<summary><b>Status Bar Messages</b></summary>

- This feature allows you to send a custom message to the status bar (above the hotbar).
- **Note**: If you switch this to `ON` and leave the field blank, the entire message will be forwarded.
</details>

<details>
<summary><b>Title Messages</b></summary>

- This feature allows you to display a custom message as a title (large text in the middle of the screen).
- **Note**: If you switch this to `ON` and leave the field blank, the entire message will be forwarded.
</details>
</details>

<details>
<summary><b>Exclusion Triggers</b></summary>

- Exclusion triggers allow you to restrict the activation conditions of notifications.
- For example, if you want a notification to activate a message contains "shark", but only if the message does not also
  contain "fish", create a trigger for "shark" and an exclusion trigger for "fish".
</details>

<details>
<summary><b>Response Messages</b></summary>

- Response messages are sent in chat when a notification is activated.
- Regex (`.*`) response messages can access capturing groups from a regex trigger using `(1)`, `(2)` etc., like custom
  messages.
- **Note**: Response messages allow you to do things like spamming chat and creating infinite loops of notifications
  and responses, so you should exercise caution when using this feature.
</details>

<table style="width:100%;">
  <tr>
    <td style="width:50%;"><img src="https://raw.githubusercontent.com/TerminalMC/ChatNotify/HEAD/assets/images/chat_01.png" style="width:100%;"></td>
    <td style="width:50%;"><img src="https://raw.githubusercontent.com/TerminalMC/ChatNotify/HEAD/assets/images/config_01.png" style="width:100%;"></td>
  </tr>
</table>

#### GUI Tweaks

<details>
<summary><b>Special Widgets</b></summary>

- ChatNotify uses several custom GUI widgets to behave differently to normal Minecraft. Some are listed below.

1. Color (`🌢`) and sound (`🔊`) status buttons on the options screen support right-click to toggle status.
2. Single-line text fields have been modified to support double-clicking or clicking and dragging to select text.
3. Fullscreen overlay widgets such as the color picker and drop-down text field support clicking outside to cancel.

</details>

<details>
<summary><b>Chat Height Slider</b></summary>

- ChatNotify modifies the chat height slider (in `Chat Settings`) to increase the maximum value to `500px`.
- The set value is not affected, so your existing setting will stay the same unless you decide to change it.

</details>

### Dependencies

Fabric: [Fabric API](https://modrinth.com/mod/P7dR8mSH), [ModMenu](https://modrinth.com/mod/mOgUt4GM)

NeoForge: None

### Compatibility

If you encounter issues, please report on Discord or GitHub.

### Contact

[![Discord Server](https://img.shields.io/discord/1103153365216669797?logo=discord&label=Discord%20Server&color=%235865F2)](https://discord.terminalmc.dev)

[![GitHub Issues](https://img.shields.io/github/issues/TerminalMC/ChatNotify?logo=github&label=GitHub%20Issues)](https://github.com/TerminalMC/ChatNotify/issues)

[![License](https://img.shields.io/github/license/TerminalMC/ChatNotify?label=License&logo=github&logoColor=white)](https://github.com/TerminalMC/ChatNotify/blob/HEAD/LICENSE.txt)
