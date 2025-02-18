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

package dev.terminalmc.chatnotify.gui.widget.list;

import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.gui.screen.OptionScreen;
import dev.terminalmc.chatnotify.gui.widget.HsvColorPicker;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.SoundOptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FastColor;

import java.awt.*;

import static dev.terminalmc.chatnotify.util.Localization.localized;

/**
 * Contains global configuration options.
 */
public class GlobalOptionList extends OptionList {
    public GlobalOptionList(Minecraft mc, int width, int height, int top, int bottom, int entryWidth,
                            int entryHeight, int entrySpacing) {
        super(mc, width, height, top, bottom, entryWidth, entryHeight, entrySpacing, () -> {});
    }

    @Override
    protected void addEntries() {
        addEntry(new Entry.Controls1(dynEntryX, dynEntryWidth, entryHeight));
        addEntry(new Entry.Controls2(dynEntryX, dynEntryWidth, entryHeight));
        addEntry(new Entry.Controls3(dynEntryX, dynEntryWidth, entryHeight));
        addEntry(new Entry.Controls4(dynEntryX, dynEntryWidth, entryHeight));
        addEntry(new Entry.DefaultColor(dynEntryX, dynEntryWidth, entryHeight, this));
        addEntry(new Entry.DefaultSound(dynEntryX, dynEntryWidth, entryHeight, this));
        addEntry(new Entry.SoundSource(dynEntryX, dynEntryWidth, entryHeight, this));

        addEntry(new OptionList.Entry.Text(entryX, entryWidth, entryHeight,
                localized("option", "global.prefixes", "\u2139"),
                Tooltip.create(localized("option", "global.prefixes.tooltip")), -1));

        int max = Config.get().prefixes.size();
        for (int i = 0; i < max; i++) {
            addEntry(new Entry.PrefixFieldEntry(entryX, entryWidth, entryHeight, this, i));
        }
        addEntry(new OptionList.Entry.ActionButton(entryX, entryWidth, entryHeight,
                Component.literal("+"), null, -1,
                (button) -> {
                    Config.get().prefixes.add("");
                    init();
                }));
    }
    
    // Sub-screen opening

    private void openSoundConfig() {
        mc.setScreen(new OptionScreen(mc.screen, localized("option", "sound"),
                new SoundOptionList(mc, width, height, y0, y1, entryWidth, entryHeight,
                        () -> {}, Config.get().defaultSound)));
    }
    
    // Custom entries

    private abstract static class Entry extends OptionList.Entry {

        private static class Controls1 extends MainOptionList.Entry {
            Controls1(int x, int width, int height) {
                super();
                int buttonWidth = (width - SPACE) / 2;

                elements.add(CycleButton.<Config.DetectionMode>builder((mode) -> localized(
                        "option", "global.detection_mode." + mode.name()))
                        .withValues(Config.DetectionMode.values())
                        .withInitialValue(Config.get().detectionMode)
                        .withTooltip((mode) -> Tooltip.create(localized(
                                "option", "global.detection_mode." + mode.name() + ".tooltip")
                                .append("\n\n")
                                .append(localized("option", "global.detection_mode.tooltip"))))
                        .create(x, 0, buttonWidth, height,
                                localized("option", "global.detection_mode"), 
                                (button, mode) -> Config.get().detectionMode = mode));

                elements.add(CycleButton.<Config.DebugMode>builder((mode) -> localized(
                        "option", "global.debug_mode." + mode.name()))
                        .withValues(Config.DebugMode.values())
                        .withInitialValue(Config.get().debugMode)
                        .withTooltip((mode) -> Tooltip.create(localized(
                                "option", "global.debug_mode." + mode.name() + ".tooltip")))
                        .create(x + width - buttonWidth, 0, buttonWidth, height,
                                localized("option", "global.debug_mode"),
                                (button, mode) -> Config.get().debugMode = mode));
            }
        }

        private static class Controls2 extends MainOptionList.Entry {
            Controls2(int x, int width, int height) {
                super();
                int buttonWidth = (width - SPACE) / 2;

                elements.add(CycleButton.booleanBuilder(
                        CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                                CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED))
                        .withInitialValue(Config.get().checkOwnMessages)
                        .withTooltip((status) -> Tooltip.create(localized(
                                "option", "global.self_notify.tooltip")))
                        .create(x, 0, buttonWidth, height, 
                                localized("option", "global.self_notify"),
                                (button, status) -> Config.get().checkOwnMessages = status));

                elements.add(CycleButton.<Config.SendMode>builder((status) -> 
                                localized("option", "global.send_mode." + status.name()))
                        .withValues(Config.SendMode.values())
                        .withInitialValue(Config.get().sendMode)
                        .withTooltip((mode) -> Tooltip.create(localized(
                                "option", "global.send_mode." + mode.name() + ".tooltip")
                                .append("\n\n")
                                .append(localized("option", "global.send_mode.tooltip"))))
                        .create(x + width - buttonWidth, 0, buttonWidth, height,
                                localized("option", "global.send_mode"),
                                (button, status) -> Config.get().sendMode = status));
            }
        }

        private static class Controls3 extends MainOptionList.Entry {
            Controls3(int x, int width, int height) {
                super();
                int buttonWidth = (width - SPACE) / 2;

                elements.add(CycleButton.<Config.NotifMode>builder((status) -> 
                                localized("option", "global.notif_mode." + status.name()))
                        .withValues(Config.NotifMode.values())
                        .withInitialValue(Config.get().notifMode)
                        .withTooltip((mode) -> Tooltip.create(localized(
                                "option", "global.notif_mode." + mode.name() + ".tooltip")))
                        .create(x, 0, buttonWidth, height,
                                localized("option", "global.notif_mode"),
                                (button, status) -> Config.get().notifMode = status));

                elements.add(CycleButton.<Config.RestyleMode>builder((status) ->
                                localized("option", "global.restyle_mode." + status.name()))
                        .withValues(Config.RestyleMode.values())
                        .withInitialValue(Config.get().restyleMode)
                        .withTooltip((mode) -> Tooltip.create(localized(
                                "option", "global.restyle_mode." + mode.name() + ".tooltip")))
                        .create(x + width - buttonWidth, 0, buttonWidth, height,
                                localized("option", "global.restyle_mode"),
                                (button, status) -> Config.get().restyleMode = status));
            }
        }

        private static class Controls4 extends MainOptionList.Entry {
            Controls4(int x, int width, int height) {
                super();

                elements.add(CycleButton.<Config.SenderDetectionMode>builder((status) ->
                                localized("option", "global.sender_detection_mode." + status.name()))
                        .withValues(Config.SenderDetectionMode.values())
                        .withInitialValue(Config.get().senderDetectionMode)
                        .withTooltip((mode) -> Tooltip.create(localized(
                                "option", "global.sender_detection_mode."
                                        + mode.name() + ".tooltip")))
                        .create(x, 0, width, height,
                                localized("option", "global.sender_detection_mode"),
                                (button, status) -> 
                                        Config.get().senderDetectionMode = status));
            }
        }

        private static class DefaultColor extends MainOptionList.Entry {
            DefaultColor(int x, int width, int height, GlobalOptionList list) {
                super();
                int colorFieldWidth = Minecraft.getInstance().font.width("#FFAAFF+++");

                Button mainButton = Button.builder(localized("option", "global.default_color")
                                        .setStyle(Style.EMPTY.withColor(Config.get().defaultColor)),
                        (button) -> {
                            int cpHeight = HsvColorPicker.MIN_HEIGHT;
                            int cpWidth = HsvColorPicker.MIN_WIDTH;
                            list.screen.setOverlay(new HsvColorPicker(
                                    x + width / 2 - cpWidth / 2,
                                    list.screen.height / 2 - cpHeight / 2,
                                    cpWidth, cpHeight,
                                    () -> Config.get().defaultColor,
                                    (val) -> Config.get().defaultColor = val,
                                    (widget) -> {
                                        list.screen.removeOverlayWidget();
                                        list.init();
                                    }));
                        })
                        .pos(x, 0)
                        .size(width - colorFieldWidth - SPACE, height)
                        .build();
                elements.add(mainButton);

                TextField colorField = new TextField(x + width - colorFieldWidth, 0,
                        colorFieldWidth, height);
                colorField.hexColorValidator().strict();
                colorField.setMaxLength(7);
                colorField.setResponder((val) -> {
                    TextColor textColor = TextColor.parseColor(val);
                    if (textColor != null) {
                        int color = textColor.getValue();
                        Config.get().defaultColor = color;
                        // Update color of main button and field
                        mainButton.setMessage(localized("option", "global.default_color")
                                .setStyle(Style.EMPTY.withColor(textColor)));
                        float[] hsv = new float[3];
                        Color.RGBtoHSB(FastColor.ARGB32.red(color), FastColor.ARGB32.green(color),
                                FastColor.ARGB32.blue(color), hsv);
                        if (hsv[2] < 0.1) colorField.setTextColor(0xFFFFFF);
                        else colorField.setTextColor(color);
                    }
                });
                colorField.setValue(TextColor.fromRgb(Config.get().defaultColor).formatValue());
                elements.add(colorField);
            }
        }

        private static class DefaultSound extends MainOptionList.Entry {
            DefaultSound(int x, int width, int height, GlobalOptionList list) {
                super();
                elements.add(Button.builder(localized("option", "global.default_sound",
                                        Config.get().defaultSound.getId()),
                                (button) -> list.openSoundConfig())
                        .pos(x, 0)
                        .size(width, height)
                        .build());
            }
        }

        private static class SoundSource extends MainOptionList.Entry {
            SoundSource(int x, int width, int height, GlobalOptionList list) {
                super();

                elements.add(CycleButton.<net.minecraft.sounds.SoundSource>builder(source -> Component.translatable(
                        "soundCategory." + source.getName()))
                        .withValues(net.minecraft.sounds.SoundSource.values())
                        .withInitialValue(Config.get().soundSource)
                        .withTooltip((status) -> Tooltip.create(localized(
                                "option", "sound.source.tooltip")))
                        .create(x, 0, width - list.smallWidgetWidth - SPACE, height,
                                localized("option", "sound.source"),
                                (button, status) -> Config.get().soundSource = status));

                elements.add(Button.builder(Component.literal("\uD83D\uDD0A"),
                                (button) -> Minecraft.getInstance().setScreen(new SoundOptionsScreen(
                                        list.screen, Minecraft.getInstance().options)))
                        .tooltip(Tooltip.create(localized(
                                "option", "sound.source.minecraft_volume")))
                        .pos(x + width - list.smallWidgetWidth, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());
            }
        }

        private static class PrefixFieldEntry extends Entry {
            PrefixFieldEntry(int x, int width, int height, GlobalOptionList list, int index) {
                super();

                TextField prefixField = new TextField(x, 0, width, height);
                prefixField.setMaxLength(30);
                prefixField.setResponder((prefix) -> 
                        Config.get().prefixes.set(index, prefix.strip()));
                prefixField.setValue(Config.get().prefixes.get(index));
                elements.add(prefixField);

                elements.add(Button.builder(
                        Component.literal("\u274C").withStyle(ChatFormatting.RED),
                        (button) -> {
                            Config.get().prefixes.remove(index);
                            list.init();
                        })
                        .pos(x + width + SPACE, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());
            }
        }
    }
}
