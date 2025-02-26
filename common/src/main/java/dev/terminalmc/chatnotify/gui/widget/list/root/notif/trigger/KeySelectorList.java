/*
 * Copyright 2025 TerminalMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.terminalmc.chatnotify.gui.widget.list.root.notif.trigger;

import dev.terminalmc.chatnotify.config.Trigger;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import dev.terminalmc.chatnotify.gui.widget.list.OptionList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static dev.terminalmc.chatnotify.util.Localization.localized;

public class KeySelectorList extends OptionList {
    public static final String[] CHAT_KEYS = {
            ".",
            "chat.type",
            "chat.type.text",
            "chat.type.announcement",
            "chat.type.admin",
            "chat.type.emote",
            "chat.type.team.sent",
            "chat.type.team.text",
    };
    public static final String[] PLAYER_KEYS = new String[]{
            "multiplayer.player.joined",
            "multiplayer.player.left",
            "death.",
    };
    public static final String[] ADVANCEMENT_KEYS = new String[]{
            "chat.type.advancement",
            "chat.type.advancement.task",
            "chat.type.advancement.goal",
            "chat.type.advancement.challenge",
    };
    public static final String[] COMMAND_KEYS = new String[]{
            "commands.",
            "commands.message.display",
            "commands.message.display.incoming",
            "commands.message.display.outgoing",
    };

    private final Trigger trigger;

    public KeySelectorList(Minecraft mc, int width, int height, int top, int bottom, int entryWidth,
                           int entryHeight, Trigger trigger) {
        super(mc, width, height, top, bottom, entryWidth, entryHeight, 1);
        this.trigger = trigger;
    }

    @Override
    protected void addEntries() {
        addEntry(new OptionList.Entry.Text(entryX, entryWidth, entryHeight,
                localized("option", "notif.trigger.selector.list", "\u2139"),
                Tooltip.create(localized("option", "notif.trigger.selector.list.tooltip")), -1));

        addEntry(new Entry.TriggerOption(dynWideEntryX, dynWideEntryWidth, entryHeight, trigger));

        addEntry(new OptionList.Entry.Text(entryX, entryWidth, entryHeight,
                localized("key", "group.chat"), null, -1));
        addKeyEntries(CHAT_KEYS);

        addEntry(new OptionList.Entry.Text(entryX, entryWidth, entryHeight,
                localized("key", "group.player"), null, -1));
        addKeyEntries(PLAYER_KEYS);

        addEntry(new OptionList.Entry.Text(entryX, entryWidth, entryHeight,
                localized("key", "group.advancement"), null, -1));
        addKeyEntries(ADVANCEMENT_KEYS);

        addEntry(new OptionList.Entry.Text(entryX, entryWidth, entryHeight,
                localized("key", "group.command"), null, -1));
        addKeyEntries(COMMAND_KEYS);
    }

    private void addKeyEntries(String[] keys) {
        for (int i = 0; i < keys.length; i++) {
            addEntry(new Entry.KeyOption(entryX, entryWidth, entryHeight, this, trigger,
                    keys[i], i < keys.length - 1 ? keys[++i] : null));
        }
    }

    // Custom entries

    private abstract static class Entry extends OptionList.Entry {

        private static class TriggerOption extends Entry {
            TriggerOption(int x, int width, int height, Trigger trigger) {
                super();
                TextField triggerField = new TextField(x, 0, width, height);
                if (trigger.type == Trigger.Type.REGEX) triggerField.regexValidator();
                triggerField.setMaxLength(240);
                triggerField.setValue(trigger.string);
                triggerField.setResponder((str) -> trigger.string = str.strip());
                triggerField.setHint(localized("option", "notif.trigger.field.hint"));
                elements.add(triggerField);
            }
        }

        private static class KeyOption extends Entry {
            KeyOption(int x, int width, int height, KeySelectorList list, Trigger trigger,
                      @NotNull String key1, @Nullable String key2) {
                super();
                int buttonWidth = (width - SPACE_TINY) / 2;

                elements.add(Button.builder(localized("key", "id." + key1),
                                (button) -> {
                                    trigger.string = key1;
                                    list.setScrollAmount(0);
                                    list.init();
                                })
                        .tooltip(Tooltip.create(Component.literal(key1)))
                        .pos(x, 0)
                        .size(buttonWidth, height)
                        .build());

                if (key2 != null) {
                    elements.add(Button.builder(localized("key", "id." + key2),
                                    (button) -> {
                                        trigger.string = key2;
                                        list.setScrollAmount(0);
                                        list.init();
                                    })
                            .tooltip(Tooltip.create(Component.literal(key2)))
                            .pos(x + width - buttonWidth, 0)
                            .size(buttonWidth, height)
                            .build());
                }
            }
        }
    }
}
