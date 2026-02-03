<!--suppress HtmlDeprecatedAttribute, HtmlDeprecatedTag, XmlDeprecatedElement, HtmlRequiredAltAttribute -->
<div align="center"><center>

<img alt="Icon" width=100 src="https://raw.githubusercontent.com/TerminalMC/ChatNotify/HEAD/common/src/main/resources/assets/chatnotify/icon.png">

## ChatNotify

Plays a ping sound when your name is mentioned, with options to create custom alerts.

[![Environment](https://img.shields.io/badge/Environment-Client-blue?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAABhWlDQ1BJQ0MgcHJvZmlsZQAAKJF9kT1Iw0AYht+malUqDnYQEclQneyiIo6likWwUNoKrTqYXPoHTRqSFBdHwbXg4M9i1cHFWVcHV0EQ/AFxdnBSdJESv0sKLWI8uLuH97735e47QGhUmGp2RQFVs4xUPCZmc6ti4BU9CKCP1jGJmXoivZiB5/i6h4/vdxGe5V335xhQ8iYDfCJxlOmGRbxBPLtp6Zz3iUOsJCnE58STBl2Q+JHrsstvnIsOCzwzZGRS88QhYrHYwXIHs5KhEs8QhxVVo3wh67LCeYuzWqmx1j35C4N5bSXNdZqjiGMJCSQhQkYNZVRgIUK7RoqJFJ3HPPwjjj9JLplcZTByLKAKFZLjB/+D3701C9NTblIwBnS/2PbHOBDYBZp12/4+tu3mCeB/Bq60tr/aAOY+Sa+3tfARMLgNXFy3NXkPuNwBhp90yZAcyU9TKBSA9zP6phwwdAv0r7l9a53j9AHIUK+Wb4CDQ2CiSNnrHu/u7ezbvzWt/v0ATphymIBZ6aQAAAAGYktHRAAKAAwAGd6C8noAAAAJcEhZcwAADdcAAA3XAUIom3gAAAAHdElNRQfoBgcOHRYlcgoRAAABRklEQVR42u2YMUoDQRRAX0axUzCteIZ4hKn0FDmFhalSWKkgnkHt9AQWwhzBNr2tBGNno82ACwm6EZvxvwdTzP8s7P8zu8w8EJHIDABKKfvAFXAIbP/zmt+AR2CSc54NavFPwDDY4s+BUaorPwy4+3eBy1S3fVSOUoBv/jt2UmMv/A6cAHt1TGqsb36JzcYaMM05X3Tm56UUgLOe+SVa2wE3K2LXa+Sbb8BgRWxjjXzzDRj/EBv3fOarY6WUj8Z+glPgtlPcKbDVM998A/6cVM/GUXlN9WIQlYdUDwvzgMW/AMcp5zwDRsA9sAhQ+AK4Aw5yzs8aEZHY6AR1gjpBnaBOsLHrsE6wM9cJohPUCeoE0Qn+Hp0gOkGdoE5QRMKiE9QJ6gR1gjrBxq7DOsHOXCeITlAnqBNEJ/h7dILoBHWCOkERCcsncuextWq5TzoAAAAASUVORK5CYII=)]()
[![Latest Minecraft](https://img.shields.io/modrinth/game-versions/Iudurxl8?label=Latest%20Minecraft&color=%2300AF5C&logo=data:image/png;base64,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)](https://modrinth.com/project/Iudurxl8/versions)

[![Loader](https://img.shields.io/badge/Available%20for-Fabric-dbd0b4?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABoAAAAcBAMAAACNPbLgAAABhGlDQ1BJQ0MgcHJvZmlsZQAAKJF9kT1Iw0AcxV9TpX5UHMwgIpihOtlFRRxLFYtgobQVWnUwufQLmjQkKS6OgmvBwY/FqoOLs64OroIg+AHi6uKk6CIl/i8ptIj14Lgf7+497t4BQr3MNKsrAmi6bSZjUSmTXZUCr+iHiADG0Cszy4inFtPoOL7u4ePrXZhndT735xhQcxYDfBJxhBmmTbxBPLtpG5z3iUVWlFXic+JJky5I/Mh1xeM3zgWXBZ4pmunkPLFILBXaWGljVjQ14hnikKrplC9kPFY5b3HWylXWvCd/YTCnr6S4TnMUMSwhjgQkKKiihDJshGnVSbGQpP1oB/+I60+QSyFXCYwcC6hAg+z6wf/gd7dWfnrKSwpGge4Xx/kYBwK7QKPmON/HjtM4AfzPwJXe8lfqwNwn6bWWFjoCBreBi+uWpuwBlzvA8JMhm7Ir+WkK+TzwfkbflAWGboG+Na+35j5OH4A0dbV8AxwcAhMFyl7v8O6e9t7+PdPs7wd+dXKrd9SjeQAAAAlwSFlzAAAuIwAALiMBeKU/dgAAAAd0SU1FB+cLFAcgIbOcUjoAAAAbUExURQAAAB0tQTg0KoB6bZqSfq6mlLyynMa8pdvQtJRJT6UAAAABdFJOUwBA5thmAAAAAWJLR0QB/wIt3gAAAF5JREFUGNN10FENwCAMhOFqOQuzMAtYOAtYqGw6mkEvhL59yR9Ca5YDqyOC465eKYqQm6LoCkVwnwQOBYKdeA5l51zhFtrsnPmg6m3Z2akk15dFH1lWFQVxlUFv+2sAJlA9O7NwQRQAAAAASUVORK5CYII=)](https://fabricmc.net/)
[![Loader](https://img.shields.io/badge/Available%20for-Forge%201.20.1-1d2d41?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAMAAABEpIrGAAAABGdBTUEAALGPC/xhBQAAACBjSFJNAAB6JgAAgIQAAPoAAACA6AAAdTAAAOpgAAA6mAAAF3CculE8AAABvFBMVEUdLUEcLEEcLEAbK0AaKj4lNUguPVAuPU8vPVAnNkkoN0o5R1g3RVc0QlQ5R1lPXGuCi5ba3ODY2t7W2d3U19vS1trR1NjQ09jQ09fP09fP0tfS1dnT1tqBipUbKz8bLEA4Rlivtbzk5uji5Ofi5OadpK3d4OOxt77////8/Pz09fbq7O7h4+bX2t3N0dXDyM27wMa0ucChqLB9hpJkb3xDUGE7SVqyt775+fq/w8nw8fLKzdL+/v7v8PKqsLhaZnQpOUsZKj6HkJrs7u/4+fno6evX2d1hbHohMUVXY3LFyc77/Pz5+vr9/f5HVGQaKj8tO052gIy9wcfa3eDo6uz19vb29/hmcX4ZKT4jMkYwP1E9S1xZZXTO0dYqOUwXJzxoc4C5vsQgMERqdIIxQFIZKT01Q1VncX+pr7dQXWz7+/zb3uHz9PXb3eD3+PjV2NtVYXA6SFrV2Nz6+vqfpq+8wcfP0taTm6Tx8vM2RVZYZHP3+Pnn6Ot8hZGwtr2Kkp1yfIlweoeEjZivtLuAiZT19vdTX25HVGW6vsXBxcqrsblFUmMkM0chMESco6zBxsu4vcNEUWIeLkIiMkV8HzjGAAAAAWJLR0QovbC1sgAAAAd0SU1FB+AJFRIdHqqGUp8AAAE3SURBVBgZ7cGxS1RxAMDx7/d3j3fvhEDhdDFeBIJQECgGxtHgENVQS0O74Oq/4B/Q1tLWFI2hRksctDQaQbjJDR04mC/KGg6i3svf455df4N+PlyoyQTlfyVIkFplBBWN4AgSMiVyEpYtR1MOSa4YQXUyo+IE/AASVvxRAdPqV2rdYlZ9f3swBCEvVj2E7pfcUUEt135ZEgmE8r4OWPg+c0DtuPfzHWMJUJLqNT8uS+2G7tJIiHbCgynLtukvTrV9wRlpBOfXPB4At3zOmUCj/CMtor2wEWgE/rmb7Qy6wO8kWachjfzqp16ufaZv2r/jqyG1hLHWI48W9TXdouPD7W/rLz8TJYzZkY4+Nnp2adNDai3GLvfaS1mWbc0V+9eP7q2k6f4JkTSeypu3VcWp8CSl2uQ8+QuWsUtIT20mIQAAACV0RVh0ZGF0ZTpjcmVhdGUAMjAxNi0wOS0yMVQxODoyOTozMCswMjowMOts9rwAAAAldEVYdGRhdGU6bW9kaWZ5ADIwMTYtMDktMjFUMTg6Mjk6MzArMDI6MDCaMU4AAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAFd6VFh0UmF3IHByb2ZpbGUgdHlwZSBpcHRjAAB4nOPyDAhxVigoyk/LzEnlUgADIwsuYwsTIxNLkxQDEyBEgDTDZAMjs1Qgy9jUyMTMxBzEB8uASKBKLgDqFxF08kI1lQAAAABJRU5ErkJggg==)](https://files.minecraftforge.net)
[![Loader](https://img.shields.io/badge/Available%20for-NeoForge%201.21%2B-f16436?logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAACAAAAAgCAMAAABEpIrGAAABhGlDQ1BJQ0MgcHJvZmlsZQAAKJF9kT1Iw0AcxV/TiqIVBwuKOASsTnZREcdSxSJYKG2FVh1MLv2CJg1Jiouj4Fpw8GOx6uDirKuDqyAIfoC4ujgpukiJ/0sKLWI8OO7Hu3uPu3eA0Kgw1QxEAVWzjFQ8JmZzq2L3K/oQQgBjGJKYqSfSixl4jq97+Ph6F+FZ3uf+HP1K3mSATySOMt2wiDeIZzctnfM+cYiVJIX4nHjSoAsSP3JddvmNc9FhgWeGjExqnjhELBY7WO5gVjJU4hnisKJqlC9kXVY4b3FWKzXWuid/YTCvraS5TnMUcSwhgSREyKihjAosRGjVSDGRov2Yh3/E8SfJJZOrDEaOBVShQnL84H/wu1uzMD3lJgVjQNeLbX+MA927QLNu29/Htt08AfzPwJXW9lcbwNwn6fW2Fj4CBraBi+u2Ju8BlzvA8JMuGZIj+WkKhQLwfkbflAMGb4HeNbe31j5OH4AMdbV8AxwcAhNFyl73eHdPZ2//nmn19wOjxHK68ogHXgAAAAlwSFlzAAALEwAACxMBAJqcGAAAAAd0SU1FB+cLFAQpNXrCg1cAAAHsUExURQAAAIuOlHV1gIuOlH6AiYuOlJ6jpxMVGh4hKSYqM2ZTTXFcVXV1gHlSSHtjXIGDjIJtZ4OFjYSGjoVqYoWHj4aIj4dudYeEhYqNlIuOlIyPlo1xaI15c42Jho2QlpCUmZOWnJSSj5SXnJWZnpaboJdPPZeboJidoZqfo5taQpyhpZ5VJp9XLJ+kqKBZMaClqaFTO6KMh6Koq6OprKRON6WqrqWrrqZoW6ZxaaatsKeKiKetsKitsaiusamfn6mjpKqUjaqws6tTNqyzsqyztq6pp6+2uLGalrKjobK5u7NZNbS8vbW7u7W9vrW9v7afnbaoora+v7a/wLeBjLehnbeqqLi/v7jAwbjCwrldNbnBw7vExb1mK73Fx73Gxr3Hx76Zjr9hNL+Ecr+7ub/HxsBjM8DIysDKysF3a8GJd8GcksHLy8LMzMOHi8PLysPNzcPOzcTOzsVmM8XPz8bR0MejucfR0cjT08nU08qwrMtrMsttLcvU1cvV1cy/uszAu83X2M5tMc90Nc/a2c/a2tFwMNKgjNNxMNRyMNR5NtSMatS6t9TMytXg39d0L9nCu9nl5NuWd9zY2N3Iw96+tt+wnuCCNODNzeKHNeLu7eaMN+by8efZ0+ja2Ozg3O/o5/Dn5PXu7Pn09P///+RBO4EAAAAHdFJOUwAQQEBwgJ+al5Z5AAAAAWJLR0Sjx9rvGgAAAkNJREFUGBkFwT9vG3UAANB3dz/7bNfnxO61UkAhiSBCFGWhHcqGxMrKxtfgMyCVL8HYHRbKyNCJobJYIEoqkWIwSWPHzdkX3x/ei0QxAAAAmjaIHwIAAGBeh070hN0+YFkAAD/HId/7gmEMuK0BgDezIB3OXh12M+Y/8uXe6u+XfP4e1vNsshGEuFrM0pazf/lncn11xVUf5eIuaiRZ3bSHqtvbH16Pq+bowXBxHnZOf/+UsDPwpozF404dehcXUed4pLy6Ko2OO9HFBaqyFfT2/5vlyUvj429+evH62tKTr/z50tcsiq2gLiqVxGa5bZN7I1XSbpebBNJtJMnCoLApmnxWHizDx/nuOJr4tfgkf0iazcoA4GC0eTDZT5XDZHN0A8pVIwB46t1ePog1w8vZI1NYFZUAOPYRCTC6xwYgAHr6ALroaYAAkvZE3m7LknekaSc6MY1qCFWFJl7T/JX2GliWh8laL6oTdRWvC7T16anVs+c2BZ4/Wzk9zYY7Q7frgHJVt481d21jVUqzpr1r9vwRda4RsCrWVa66aVvLa+OsbW92cy9CH5Ju+mGxTToXZx8k+dNBOp4Mw8H7+80vZ22ImFcBxObq8Bkp3L+vnsuAAGDK49G3C7vf3/wGgBgAAAAgADJTHo2+a8TWUzKAoCnLy1GXwNtJUQmDtwHc3fSGRMPeyfnlUQ7KO9BNweV5fjTdBAwmXSAAAehOBggYdvpAkgBAf5wiiuPdukliAAA0dZwsmkg8AAAAQNEAAAAA+B8LzexYIpdh2QAAAABJRU5ErkJggg==)](https://neoforged.net/)

[![Download on Modrinth](https://img.shields.io/modrinth/dt/Iudurxl8?label=Download%20on%20Modrinth&logo=modrinth&logoColor=%2300AF5C)](https://modrinth.com/project/Iudurxl8)
[![Download on CurseForge](https://img.shields.io/curseforge/dt/855056?label=Download%20on%20CurseForge&logo=curseforge)](https://curseforge.com/minecraft/mc-mods/chatnotify)
[![Download on GitHub](https://img.shields.io/github/downloads/TerminalMC/ChatNotify/total?label=Download%20on%20GitHub&logo=github&logoColor=white)](https://github.com/TerminalMC/ChatNotify)

</center></div>

### Features

- Custom triggers - never worry about missing a message again!
- Choose notification sounds from the full range of built-in and resourcepack sound effects.
- Adjust sound volume and pitch per-notification.
- Customize message highlighting with a color picker and format controls.
- Use regex patterns, inclusion and exclusion triggers for fine-grained control.
- Add automatic response messages, trigger [CommandKeys](https://modrinth.com/project/commandkeys)
  macros, or send notifications to Discord webhooks.

<img src="https://raw.githubusercontent.com/TerminalMC/ChatNotify/HEAD/assets/images/chat_cropped.png" width="500px">

### How it Works

1. ChatNotify has a list of `Notifications`, each of which has one or more `Triggers`.
2. When a new message arrives in chat, ChatNotify starts checking the triggers of each notification.
3. If a trigger matches the chat message, the corresponding notification will be activated. By
   default;
    1. The message will be edited to highlight the trigger that activated the notification.
    2. A sound will be played.

- You can create and customize your own notifications via the options screen, which can be opened
  via [ModMenu](https://modrinth.com/project/mOgUt4GM) on Fabric, or the mod list on NeoForge.

### Setup

When you open the options screen, you'll see a list of notifications. You can set the notification
trigger, color and sound here.

<img src="https://raw.githubusercontent.com/TerminalMC/ChatNotify/HEAD/assets/images/options/root/notifications.png" width="500px">

#### Trigger types

To change the trigger type, click the `~` button to the left of the text field.

- Normal
  - This is what you probably want to use at first. Normal triggers aren't case-sensitive (so a
    trigger `hello` will match messages containing `HELLO`), and they don't match partial words (so
    a trigger `rock` will match `rock!` but not `rocket` or `rock932`).
- Key
  - In vanilla, each type of chat message has a different translation key (such as `chat.type.text`
    or `chat.type.advancement.task`). You can use a key-type trigger to match all messages of that
    type.
  - Note: Many servers remove keys from messages, which will prevent this type of key from working.
    To check if a message has a key, use the trigger editor (see below).
- Regex
  - If you want more precise control, you can switch to this type and enter a regex pattern in the
    text field.
  - Consider using the trigger editor (see below) and/or a tool such
    as [regex101](https://regex101.com) to help create regex patterns.

#### Trigger editor

Click the `✎` button to the left of the text field to open the trigger editor.

<img src="https://raw.githubusercontent.com/TerminalMC/ChatNotify/HEAD/assets/images/options/trigger/editor.png" width="500px">

In the trigger editor, recent chat messages will be displayed in a list (most recent first). Use the
`Filter` and `Restyle` buttons to preview the effect of your trigger.

Click on a message to view the text and translation key (if any) so you can copy them.

Additionally, some common keys are available for selection in the `Key Selector`.

<img src="https://raw.githubusercontent.com/TerminalMC/ChatNotify/HEAD/assets/images/options/trigger/selector.png" width="500px">

#### Trigger List

If you want to add more triggers, go back to `Notifications` and click on the options button to the
left of the color button. You'll now see a list of triggers, which you can edit as desired.

<img src="https://raw.githubusercontent.com/TerminalMC/ChatNotify/HEAD/assets/images/options/notif/triggers.png" width="500px">

#### Style target

Sometimes for key-type and regex-type triggers you may want to highlight a different part of the
message to what the trigger matches.

Adding a style target allows you to specify what part of the message should be highlighted. If the
style target doesn't match the message, the trigger will be used as normal.

#### Format

<img src="https://raw.githubusercontent.com/TerminalMC/ChatNotify/HEAD/assets/images/options/notif/format.png" width="500px">

#### Sound

All built-in and resourcepack sounds can be used (click on the text field). Additionally, some
sounds are available for quick selection.

The sound source controls which of Minecraft's volume control sliders will affect the notification
sound, in addition to the volume control slider provided here.

<img src="https://raw.githubusercontent.com/TerminalMC/ChatNotify/HEAD/assets/images/options/notif/sound.png" width="500px">

#### Inclusion

Inclusion triggers do nothing by themselves, but if a trigger from the `Triggers` list matches a
message, ChatNotify will check all the triggers in this list and only activate the notification if
they all match.

<img src="https://raw.githubusercontent.com/TerminalMC/ChatNotify/HEAD/assets/images/options/notif/inclusion.png" width="500px">

#### Exclusion

Exclusion triggers are the reverse of `Inclusion` triggers; before activating a notification,
ChatNotify will check all the triggers in this list and only proceed with activation if none of them
match the message.

<img src="https://raw.githubusercontent.com/TerminalMC/ChatNotify/HEAD/assets/images/options/notif/exclusion.png" width="500px">

#### Response

Response messages can be sent when the notification is activated. ChatNotify supports multiple response types:

- **Normal**: Sends a message directly in chat
- **Regex**: Uses regex capture groups from the trigger in the response message
- **CommandKeys**: Triggers [CommandKeys](https://modrinth.com/project/commandkeys) macros
- **Discord**: Sends a message to a Discord webhook (useful for monitoring chat events remotely)

For Discord webhooks, you'll need to:
1. Create a webhook in your Discord server (Server Settings > Integrations > Webhooks)
2. Copy the webhook URL
3. In ChatNotify, open Detection > Sender Detection and enable Discord Webhook
4. Paste the webhook URL into the Webhook URL field

Use with caution, as you can easily make a notification send a response which triggers the
notification again in a loop, which will spam chat and then crash the game.

<img src="https://raw.githubusercontent.com/TerminalMC/ChatNotify/HEAD/assets/images/options/notif/response.png" width="500px">

#### Custom Messages

These are special ways of showing notifications, as an alternative to the normal 'play a sound and
highlight the message'.

<img src="https://raw.githubusercontent.com/TerminalMC/ChatNotify/HEAD/assets/images/options/notif/misc.png" width="500px">

#### Controls

These options apply to all notifications, and control various aspects of ChatNotify's behavior. You
should not generally need to change them (you can if you want, of course), but the `Detect` and
`Send Mode` options can be useful in certain cases of conflict with other mods.

<img src="https://raw.githubusercontent.com/TerminalMC/ChatNotify/HEAD/assets/images/options/root/controls.png" width="500px">

#### Defaults

If you want new notifications to have a particular color or sound by default, you can set that here.

<img src="https://raw.githubusercontent.com/TerminalMC/ChatNotify/HEAD/assets/images/options/root/defaults.png" width="500px">

#### Prefixes

When ChatNotify is not using Chat Heads to detect the message sender, it will store your sent
messages and compare them to incoming messages to determine which messages were sent by you. If
you're on a server that modifies your messages, that system may not work properly, so prefixes can
help.

If you're still being notified on every message that you send (because it contains your name) try
installing [Chat Heads](https://modrinth.com/project/chat-heads) and setting `Sender Detection Mode`
to `Combined` in the `Controls` tab.

<img src="https://raw.githubusercontent.com/TerminalMC/ChatNotify/HEAD/assets/images/options/root/prefixes.png" width="500px">

### GUI Tweaks

<details>
<summary><b>Special Widgets</b></summary>

ChatNotify uses a range of custom GUI widgets which behave differently to normal Minecraft.

1. Color (`🌢`) and sound (`🔊`) status buttons on the options screen support right-click to toggle
   status.
2. Text fields have been modified to support double-clicking, triple-clicking or clicking and
   dragging to select text.
3. Text fields have also been modified to support `CTRL+Z` to undo and `CTRL+Y` to redo edits. Note
   that edit history will be erased whenever the GUI is refreshed, which often happens when you
   click on a different widget.
4. Fullscreen overlay widgets such as the color picker and drop-down text field support clicking
   outside to cancel and close.

</details>

<details>
<summary><b>Chat Height Slider</b></summary>

- ChatNotify modifies the chat height slider (in Minecraft's `Chat Settings`) to increase the
  maximum value to `500px`.
- Your existing setting will stay the same unless you decide to change it.

</details>

### Dependencies

Fabric: [Fabric API](https://modrinth.com/project/P7dR8mSH), [ModMenu](https://modrinth.com/project/mOgUt4GM)

NeoForge: None

### Compatibility

If you encounter issues, please report on Discord or GitHub.

### Contact

[![Discord Server](https://img.shields.io/discord/1103153365216669797?logo=discord&label=Discord%20Server&color=%235865F2)](https://discord.terminalmc.dev)

[![GitHub Issues](https://img.shields.io/github/issues/TerminalMC/ChatNotify?logo=github&label=GitHub%20Issues)](https://github.com/TerminalMC/ChatNotify/issues)

[![License](https://img.shields.io/github/license/TerminalMC/ChatNotify?label=License&logo=github&logoColor=white)](https://github.com/TerminalMC/ChatNotify/blob/HEAD/LICENSE.txt)
