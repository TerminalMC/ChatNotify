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

package dev.terminalmc.chatnotify.gui.widget.list.option;

import dev.terminalmc.chatnotify.config.TextStyle;
import dev.terminalmc.chatnotify.config.Trigger;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

import static dev.terminalmc.chatnotify.util.Localization.localized;

public class KeyOptionList extends OptionList {
    private final Trigger trigger;
    private final TextStyle textStyle;
    private final Runnable closeRunnable;

    public KeyOptionList(Minecraft mc, int width, int height, int y,
                         int entryWidth, int entryHeight, Trigger trigger, TextStyle textStyle, 
                         Runnable closeRunnable) {
        super(mc, width, height, y, entryHeight + 1, entryWidth, entryHeight);
        this.trigger = trigger;
        this.textStyle = textStyle;
        this.closeRunnable = closeRunnable;

        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "key.trigger", "\u2139"),
                Tooltip.create(localized("option", "key.trigger.tooltip")), -1));

        addEntry(new Entry.TriggerFieldEntry(dynEntryX, dynEntryWidth, entryHeight, trigger));

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
        for (int i = 0; i < chatKeys.length; i++) {
            addEntry(new Entry.KeyOption(entryX, entryWidth, entryHeight, this, trigger,
                    chatKeys[i], i < chatKeys.length - 1 ? chatKeys[++i] : null));
        }

        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "key.group.player"), null, -1));
        String[] playerKeys = new String[]{
                "multiplayer.player.joined",
                "multiplayer.player.left",
                "death.",
        };
        for (int i = 0; i < playerKeys.length; i++) {
            addEntry(new Entry.KeyOption(entryX, entryWidth, entryHeight, this, trigger,
                    playerKeys[i], i < playerKeys.length - 1 ? playerKeys[++i] : null));
        }

        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "key.group.advancement"), null, -1));
        String[] advancementKeys = new String[]{
                "chat.type.advancement",
                "chat.type.advancement.task",
                "chat.type.advancement.goal",
                "chat.type.advancement.challenge",
        };
        for (int i = 0; i < advancementKeys.length; i++) {
            addEntry(new Entry.KeyOption(entryX, entryWidth, entryHeight, this, trigger,
                    advancementKeys[i], i < advancementKeys.length - 1 ? advancementKeys[++i] : null));
        }

        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "key.group.command"), null, -1));
        String[] commandKeys = new String[]{
                "commands.",
                "commands.message.display",
                "commands.message.display.incoming",
                "commands.message.display.outgoing",
        };
        for (int i = 0; i < commandKeys.length; i++) {
            addEntry(new Entry.KeyOption(entryX, entryWidth, entryHeight, this, trigger,
                    commandKeys[i], i < commandKeys.length - 1 ? commandKeys[++i] : null));
        }
    }

    @Override
    public KeyOptionList reload(int width, int height, double scrollAmount) {
        KeyOptionList newList = new KeyOptionList(minecraft, width, height,
                getY(), entryWidth, entryHeight, trigger, textStyle, closeRunnable);
        newList.setScrollAmount(scrollAmount);
        return newList;
    }

    @Override
    public void onClose() {
        closeRunnable.run();
    }

    private abstract static class Entry extends OptionList.Entry {

        private static class TriggerFieldEntry extends Entry {
            TriggerFieldEntry(int x, int width, int height, Trigger trigger) {
                super();
                TextField triggerField = new TextField(x, 0, width, height);
                if (trigger.type == Trigger.Type.REGEX) triggerField.regexValidator();
                triggerField.setMaxLength(240);
                triggerField.setResponder((str) -> trigger.string = str.strip());
                triggerField.setValue(trigger.string);
                triggerField.setTooltip(Tooltip.create(
                        localized("option", "trigger.field.tooltip")));
                triggerField.setTooltipDelay(Duration.ofMillis(500));
                elements.add(triggerField);
            }
        }

        private static class KeyOption extends Entry {
            KeyOption(int x, int width, int height, KeyOptionList list, Trigger trigger,
                      String key1, @Nullable String key2) {
                super();
                int buttonWidth = (width - 1) / 2;
                
                elements.add(Button.builder(localized("option", "key.id." + key1), 
                        (button) -> {
                            trigger.string = key1;
                            list.setScrollAmount(0);
                            list.reload();
                        })
                        .tooltip(Tooltip.create(Component.literal(key1)))
                        .pos(x, 0)
                        .size(buttonWidth, height)
                        .build());

                if (key2 != null) {
                    elements.add(Button.builder(localized("option", "key.id." + key2),
                                    (button) -> {
                                        trigger.string = key2;
                                        list.setScrollAmount(0);
                                        list.reload();
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
