/*
 * Copyright 2024 TerminalMC
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

package dev.terminalmc.chatnotify.gui.widget.list.option;

import dev.terminalmc.chatnotify.config.TextStyle;
import dev.terminalmc.chatnotify.config.Trigger;
import dev.terminalmc.chatnotify.gui.screen.OptionsScreen;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.time.Duration;

import static dev.terminalmc.chatnotify.util.Localization.localized;

public class KeyOptionList extends OptionList {
    private final Trigger trigger;
    private final TextStyle textStyle;

    public KeyOptionList(Minecraft mc, int width, int height, int y, int itemHeight,
                         int entryWidth, int entryHeight, Trigger trigger, TextStyle textStyle) {
        super(mc, width, height, y, itemHeight, entryWidth, entryHeight);
        this.trigger = trigger;
        this.textStyle = textStyle;

        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "key.trigger", "\u2139"),
                Tooltip.create(localized("option", "key.trigger.tooltip")), -1));

        addEntry(new Entry.TriggerFieldEntry(dynEntryX, dynEntryWidth, entryHeight, this, trigger));

        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "key.group.chat"), null, -1));
        String[] chatKeys = { 
                ".",
                "chat.type",
                "chat.type.text",
                "chat.type.announcement",
                "chat.type.admin",
                "chat.type.emote",
                "chat.type.team.sent",
                "chat.type.team.text",
        };
        for (String s : chatKeys) {
            addEntry(new Entry.KeyOption(entryX, entryWidth, entryHeight, this, trigger, s));
        }

        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "key.group.player"), null, -1));
        String[] playerKeys = new String[]{
                "multiplayer.player.joined",
                "multiplayer.player.left",
                "death.",
        };
        for (String s : playerKeys) {
            addEntry(new Entry.KeyOption(entryX, entryWidth, entryHeight, this, trigger, s));
        }

        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "key.group.advancement"), null, -1));
        String[] advancementKeys = new String[]{
                "chat.type.advancement",
                "chat.type.advancement.task",
                "chat.type.advancement.goal",
                "chat.type.advancement.challenge",
        };
        for (String s : advancementKeys) {
            addEntry(new Entry.KeyOption(entryX, entryWidth, entryHeight, this, trigger, s));
        }

        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "key.group.command"), null, -1));
        String[] commandKeys = new String[]{
                "commands.",
                "commands.message.display",
                "commands.message.display.incoming",
                "commands.message.display.outgoing",
        };
        for (String s : commandKeys) {
            addEntry(new Entry.KeyOption(entryX, entryWidth, entryHeight, this, trigger, s));
        }
    }

    @Override
    public KeyOptionList reload(int width, int height, double scrollAmount) {
        KeyOptionList newList = new KeyOptionList(minecraft, width, height,
                getY(), itemHeight, entryWidth, entryHeight, trigger, textStyle);
        newList.setScrollAmount(scrollAmount);
        return newList;
    }

    private void openTriggerConfig(Trigger trigger) {
        minecraft.setScreen(new OptionsScreen(minecraft.screen, localized("option", "trigger"),
                new TriggerOptionList(minecraft, width, height, getY(), itemHeight,
                        entryWidth, entryHeight, trigger, textStyle, "", "", false, true)));
    }

    private abstract static class Entry extends OptionList.Entry {

        private static class TriggerFieldEntry extends Entry {
            TriggerFieldEntry(int x, int width, int height, KeyOptionList list,
                              Trigger trigger) {
                super();
                int fieldSpacing = 1;
                int triggerFieldWidth = width - list.tinyWidgetWidth - fieldSpacing;
                TextField triggerField = new TextField(0, 0, triggerFieldWidth, height);

                // Trigger field
                triggerField.setPosition(x, 0);
                if (trigger.type == Trigger.Type.REGEX) triggerField.regexValidator();
                triggerField.setMaxLength(240);
                triggerField.setResponder((str) -> trigger.string = str.strip());
                triggerField.setValue(trigger.string);
                triggerField.setTooltip(Tooltip.create(
                        localized("option", "notif.trigger.field.tooltip")));
                triggerField.setTooltipDelay(Duration.ofMillis(500));
                elements.add(triggerField);

                // Trigger editor button
                Button editorButton = Button.builder(Component.literal("\u270e"),
                                (button) -> list.openTriggerConfig(trigger))
                        .pos(x + width - list.tinyWidgetWidth, 0)
                        .size(list.tinyWidgetWidth, height)
                        .build();
                editorButton.setTooltip(Tooltip.create(
                        localized("option", "notif.trigger_editor.tooltip")));
                editorButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(editorButton);
            }
        }

        private static class KeyOption extends Entry {
            KeyOption(int x, int width, int height, KeyOptionList list, Trigger trigger, String key) {
                super();
                elements.add(Button.builder(localized("option", "key." + key), 
                        (button) -> {
                            trigger.string = key;
                            list.setScrollAmount(0);
                            list.reload();
                        })
                        .tooltip(Tooltip.create(Component.literal(key)))
                        .pos(x, 0)
                        .size(width, height)
                        .build());
            }
        }
    }
}
