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

package dev.terminalmc.chatnotify.gui.widget.list.root;

import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.gui.screen.OptionScreen;
import dev.terminalmc.chatnotify.gui.widget.list.OptionList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;

import static dev.terminalmc.chatnotify.util.Localization.localized;

public class ControlList extends OptionList {

    public ControlList(
            Minecraft mc,
            OptionScreen screen,
            int width,
            int height,
            int y,
            int entryWidth,
            int entryHeight,
            int entrySpacing
    ) {
        super(mc, screen, width, height, y, entryWidth, entryHeight, entrySpacing);
    }

    @Override
    protected void addEntries() {
        addEntry(new Entry.Controls1(dynEntryX, dynEntryWidth, entryHeight));
        addEntry(new Entry.Controls2(dynEntryX, dynEntryWidth, entryHeight));
        addEntry(new Entry.Controls3(dynEntryX, dynEntryWidth, entryHeight));
        addEntry(new Entry.Controls4(dynEntryX, dynEntryWidth, entryHeight));
    }

    // Custom entries

    private abstract static class Entry extends OptionList.Entry {

        private static class Controls1 extends Entry {

            Controls1(int x, int width, int height) {
                super();
                int buttonWidth = (width - SPACE) / 2;

                elements.add(CycleButton.<Config.DetectionMode>builder((mode) -> localized(
                                "option",
                                "control.detection_mode.status." + mode.name()
                        ))
                        .withValues(Config.DetectionMode.values())
                        .withInitialValue(Config.get().detectionMode)
                        .withTooltip((status) -> Tooltip.create(localized(
                                "option",
                                "control.detection_mode.status." + status.name() + ".tooltip"
                        )
                                .append("\n\n")
                                .append(localized("option", "control.detection_mode.tooltip"))))
                        .create(
                                x,
                                0,
                                buttonWidth,
                                height,
                                localized("option", "control.detection_mode"),
                                (button, mode) -> Config.get().detectionMode = mode
                        ));

                elements.add(CycleButton.<Config.DebugMode>builder((mode) -> localized(
                                "option",
                                "control.debug_mode.status." + mode.name()
                        ))
                        .withValues(Config.DebugMode.values())
                        .withInitialValue(Config.get().debugMode)
                        .withTooltip((status) -> Tooltip.create(localized(
                                "option",
                                "control.debug_mode.status." + status.name() + ".tooltip"
                        )))
                        .create(
                                x + width - buttonWidth,
                                0,
                                buttonWidth,
                                height,
                                localized("option", "control.debug_mode"),
                                (button, mode) -> Config.get().debugMode = mode
                        ));
            }
        }

        private static class Controls2 extends Entry {

            Controls2(int x, int width, int height) {
                super();
                int buttonWidth = (width - SPACE) / 2;

                elements.add(CycleButton.booleanBuilder(
                                CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                                CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED)
                        )
                        .withInitialValue(Config.get().checkOwnMessages)
                        .withTooltip((status) -> Tooltip.create(localized(
                                "option",
                                "control.self_notify.tooltip"
                        )))
                        .create(
                                x,
                                0,
                                buttonWidth,
                                height,
                                localized("option", "control.self_notify"),
                                (button, status) -> Config.get().checkOwnMessages = status
                        ));

                elements.add(CycleButton.<Config.SendMode>builder((status) -> localized(
                                "option",
                                "control.send_mode.status." + status.name()
                        ))
                        .withValues(Config.SendMode.values())
                        .withInitialValue(Config.get().sendMode)
                        .withTooltip((mode) -> Tooltip.create(localized(
                                "option",
                                "control.send_mode.status." + mode.name() + ".tooltip"
                        )
                                .append("\n\n")
                                .append(localized("option", "control.send_mode.tooltip"))))
                        .create(
                                x + width - buttonWidth,
                                0,
                                buttonWidth,
                                height,
                                localized("option", "control.send_mode"),
                                (button, status) -> Config.get().sendMode = status
                        ));
            }
        }

        private static class Controls3 extends Entry {

            Controls3(int x, int width, int height) {
                super();
                int buttonWidth = (width - SPACE) / 2;

                elements.add(CycleButton.<Config.NotifMode>builder((status) -> localized(
                                "option",
                                "control.notif_mode.status." + status.name()
                        ))
                        .withValues(Config.NotifMode.values())
                        .withInitialValue(Config.get().notifMode)
                        .withTooltip((mode) -> Tooltip.create(localized(
                                "option",
                                "control.notif_mode.status." + mode.name() + ".tooltip"
                        )))
                        .create(
                                x,
                                0,
                                buttonWidth,
                                height,
                                localized("option", "control.notif_mode"),
                                (button, status) -> Config.get().notifMode = status
                        ));

                elements.add(CycleButton.<Config.RestyleMode>builder((status) -> localized(
                                "option",
                                "control.restyle_mode.status." + status.name()
                        ))
                        .withValues(Config.RestyleMode.values())
                        .withInitialValue(Config.get().restyleMode)
                        .withTooltip((mode) -> Tooltip.create(localized(
                                "option",
                                "control.restyle_mode.status." + mode.name() + ".tooltip"
                        )))
                        .create(
                                x + width - buttonWidth,
                                0,
                                buttonWidth,
                                height,
                                localized("option", "control.restyle_mode"),
                                (button, status) -> Config.get().restyleMode = status
                        ));
            }
        }

        private static class Controls4 extends Entry {

            Controls4(int x, int width, int height) {
                super();

                elements.add(CycleButton.<Config.SenderDetectionMode>builder((status) -> localized(
                                "option",
                                "control.sender_detection_mode.status." + status.name()
                        ))
                        .withValues(Config.SenderDetectionMode.values())
                        .withInitialValue(Config.get().senderDetectionMode)
                        .withTooltip((mode) -> Tooltip.create(localized(
                                "option",
                                "control.sender_detection_mode.status." + mode.name() + ".tooltip"
                        )))
                        .create(
                                x,
                                0,
                                width,
                                height,
                                localized("option", "control.sender_detection_mode"),
                                (button, status) -> Config.get().senderDetectionMode = status
                        ));
            }
        }
    }
}
