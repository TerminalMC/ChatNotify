/*
 * Copyright 2026 TerminalMC
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

package dev.terminalmc.chatnotify.gui.widget.list.root;

import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.Config.ChatDetectionMode;
import dev.terminalmc.chatnotify.config.Config.CommonDetectionMode;
import dev.terminalmc.chatnotify.gui.screen.OptionScreen;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import dev.terminalmc.chatnotify.gui.widget.list.OptionList;
import dev.terminalmc.chatnotify.util.Unicode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import static dev.terminalmc.chatnotify.util.Localization.localized;

public class DetectionList extends OptionList {

    public DetectionList(
            Minecraft mc,
            OptionScreen screen,
            int width,
            int height,
            int top,
            int bottom,
            int entryWidth,
            int entryHeight,
            int entrySpacing
    ) {
        super(mc, screen, width, height, top, bottom, entryWidth, entryHeight, entrySpacing);
    }

    @Override
    protected void addEntries() {
        addEntry(new OptionList.Entry.Text(
                entryX,
                entryWidth,
                entryHeight,
                localized("option", "detection.message", Unicode.INFO.str),
                Tooltip.create(localized("option", "detection.message.tooltip")),
                -1
        ));

        addEntry(new Entry.ChatDetection(dynEntryX, dynEntryWidth, entryHeight));
        addEntry(new Entry.ActionBarDetection(dynEntryX, dynEntryWidth, entryHeight));
        addEntry(new Entry.TitleDetection(dynEntryX, dynEntryWidth, entryHeight));
        addEntry(new Entry.SubtitleDetection(dynEntryX, dynEntryWidth, entryHeight));

        addEntry(new OptionList.Entry.Text(
                entryX,
                entryWidth,
                entryHeight,
                localized("option", "detection.sender", Unicode.INFO.str),
                Tooltip.create(localized("option", "detection.sender.tooltip")),
                -1
        ));

        addEntry(new Entry.SelfNotify(dynEntryX, dynEntryWidth, entryHeight));
        addEntry(new Entry.SenderDetection(dynEntryX, dynEntryWidth, entryHeight));

        addEntry(new OptionList.Entry.Text(
                entryX,
                entryWidth,
                entryHeight,
                localized("option", "detection.prefix.list", Unicode.INFO.str),
                Tooltip.create(localized("option", "detection.prefix.list.tooltip")),
                -1
        ));

        int max = Config.get().prefixes.size();
        for (int i = 0; i < max; i++) {
            addEntry(new Entry.PrefixFieldEntry(entryX, entryWidth, entryHeight, this, i));
        }
        addEntry(new OptionList.Entry.ActionButton(
                entryX,
                entryWidth,
                entryHeight,
                Component.literal("+"),
                null,
                -1,
                (button) -> {
                    Config.get().prefixes.add("");
                    init();
                }
        ));
    }

    // Custom entries

    private abstract static class Entry extends OptionList.Entry {

        private static class ChatDetection extends Entry {

            ChatDetection(int x, int width, int height) {
                super();

                elements.add(CycleButton.<ChatDetectionMode>builder((mode) -> localized(
                                "option",
                                "detection.message.chat.mode.status." + mode.name()
                        ))
                        .withValues(ChatDetectionMode.values())
                        .withInitialValue(Config.get().detectionMode)
                        .withTooltip((status) -> Tooltip.create(localized(
                                "option",
                                "detection.message.chat.mode.status." + status.name() + ".tooltip"
                        )))
                        .create(
                                x,
                                0,
                                width,
                                height,
                                localized("option", "detection.message.chat.mode"),
                                (button, mode) -> Config.get().detectionMode = mode
                        ));
            }
        }

        private static class ActionBarDetection extends Entry {

            ActionBarDetection(int x, int width, int height) {
                super();

                elements.add(CycleButton.<CommonDetectionMode>builder((mode) -> localized(
                                "option",
                                "detection.message.common.mode.status." + mode.name()
                        ))
                        .withValues(CommonDetectionMode.values())
                        .withInitialValue(Config.get().actionBarDetectionMode)
                        .withTooltip((status) -> Tooltip.create(localized(
                                "option",
                                "detection.message.common.mode.status." + status.name()
                                        + ".tooltip"
                        )))
                        .create(
                                x,
                                0,
                                width,
                                height,
                                localized("option", "detection.message.action_bar.mode"),
                                (button, mode) -> Config.get().actionBarDetectionMode = mode
                        ));
            }
        }

        private static class TitleDetection extends Entry {

            TitleDetection(int x, int width, int height) {
                super();

                elements.add(CycleButton.<Config.CommonDetectionMode>builder((mode) -> localized(
                                "option",
                                "detection.message.common.mode.status." + mode.name()
                        ))
                        .withValues(Config.CommonDetectionMode.values())
                        .withInitialValue(Config.get().titleDetectionMode)
                        .withTooltip((status) -> Tooltip.create(localized(
                                "option",
                                "detection.message.common.mode.status." + status.name()
                                        + ".tooltip"
                        )))
                        .create(
                                x,
                                0,
                                width,
                                height,
                                localized("option", "detection.message.title.mode"),
                                (button, mode) -> Config.get().titleDetectionMode = mode
                        ));
            }
        }

        private static class SubtitleDetection extends Entry {

            SubtitleDetection(int x, int width, int height) {
                super();

                elements.add(CycleButton.<Config.CommonDetectionMode>builder((mode) -> localized(
                                "option",
                                "detection.message.common.mode.status." + mode.name()
                        ))
                        .withValues(Config.CommonDetectionMode.values())
                        .withInitialValue(Config.get().subtitleDetectionMode)
                        .withTooltip((status) -> Tooltip.create(localized(
                                "option",
                                "detection.message.common.mode.status." + status.name()
                                        + ".tooltip"
                        )))
                        .create(
                                x,
                                0,
                                width,
                                height,
                                localized("option", "detection.message.subtitle.mode"),
                                (button, mode) -> Config.get().subtitleDetectionMode = mode
                        ));
            }
        }

        private static class SelfNotify extends Entry {

            SelfNotify(int x, int width, int height) {
                super();

                elements.add(CycleButton.booleanBuilder(
                                CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                                CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED)
                        )
                        .withInitialValue(Config.get().checkOwnMessages)
                        .withTooltip((status) -> Tooltip.create(localized(
                                "option",
                                "detection.self_notify.tooltip"
                        )))
                        .create(
                                x,
                                0,
                                width,
                                height,
                                localized("option", "detection.self_notify"),
                                (button, status) -> Config.get().checkOwnMessages = status
                        ));
            }
        }

        private static class SenderDetection extends Entry {

            SenderDetection(int x, int width, int height) {
                super();

                elements.add(CycleButton.<Config.SenderDetectionMode>builder((status) -> localized(
                                "option",
                                "detection.sender.mode.status." + status.name()
                        ))
                        .withValues(Config.SenderDetectionMode.values())
                        .withInitialValue(Config.get().senderDetectionMode)
                        .withTooltip((mode) -> Tooltip.create(localized(
                                "option",
                                "detection.sender.mode.status." + mode.name() + ".tooltip"
                        )))
                        .create(
                                x,
                                0,
                                width,
                                height,
                                localized("option", "detection.sender.mode"),
                                (button, status) -> Config.get().senderDetectionMode = status
                        ));
            }
        }

        private static class PrefixFieldEntry extends Entry {

            PrefixFieldEntry(int x, int width, int height, DetectionList list, int index) {
                super();

                TextField prefixField = new TextField(x, 0, width, height);
                prefixField.setMaxLength(30);
                prefixField.setResponder((prefix) -> Config.get().prefixes.set(
                        index,
                        prefix.strip()
                ));
                prefixField.setValue(Config.get().prefixes.get(index));
                elements.add(prefixField);

                elements.add(Button.builder(
                                Component.literal(Unicode.CROSS.str).withStyle(ChatFormatting.RED),
                                (button) -> {
                                    Config.get().prefixes.remove(index);
                                    list.init();
                                }
                        )
                        .pos(x + width + SPACE, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());
            }
        }
    }
}
