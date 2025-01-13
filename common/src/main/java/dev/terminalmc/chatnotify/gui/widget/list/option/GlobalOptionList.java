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

import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.gui.screen.OptionsScreen;
import dev.terminalmc.chatnotify.gui.widget.HsvColorPicker;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import dev.terminalmc.chatnotify.util.ColorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor;

import java.awt.*;
import java.util.Locale;

import static dev.terminalmc.chatnotify.util.Localization.localized;

/**
 * Contains global configuration options.
 */
public class GlobalOptionList extends OptionList {
    public GlobalOptionList(Minecraft mc, int width, int height, int y, int itemHeight,
                            int entryWidth, int entryHeight) {
        super(mc, width, height, y, itemHeight, entryWidth, entryHeight);

        addEntry(new Entry.DetectAndDebugEntry(entryX, entryWidth, entryHeight));
        addEntry(new Entry.SelfCheckAndSendModeEntry(entryX, entryWidth, entryHeight));
        addEntry(new Entry.MultiModeEntry(entryX, entryWidth, entryHeight));
        addEntry(new Entry.DefaultColorEntry(entryX, entryWidth, entryHeight, this));
        addEntry(new Entry.DefaultSoundEntry(entryX, entryWidth, entryHeight, this));
        addEntry(new Entry.SoundSourceEntry(entryX, entryWidth, entryHeight, this));

        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "global.prefixes", "\u2139"),
                Tooltip.create(localized("option", "global.prefixes.tooltip")), -1));

        int max = Config.get().prefixes.size();
        for (int i = 0; i < max; i++) {
            addEntry(new Entry.PrefixFieldEntry(entryX, entryWidth, entryHeight, this, i));
        }
        addEntry(new OptionList.Entry.ActionButtonEntry(entryX, entryWidth, entryHeight,
                Component.literal("+"), null, -1,
                (button) -> {
                    Config.get().prefixes.add("");
                    reload();
                }));
    }

    @Override
    public GlobalOptionList reload(int width, int height, double scrollAmount) {
        GlobalOptionList newList = new GlobalOptionList(minecraft, width, height,
                getY(), itemHeight, entryWidth, entryHeight);
        newList.setScrollAmount(scrollAmount);
        return newList;
    }

    private void openSoundConfig() {
        minecraft.setScreen(new OptionsScreen(minecraft.screen, localized("option", "sound"),
                new SoundOptionList(minecraft, width, height, getY(),
                        entryWidth, entryHeight, Config.get().defaultSound)));
    }

    private abstract static class Entry extends OptionList.Entry {

        private static class DetectAndDebugEntry extends MainOptionList.Entry {
            DetectAndDebugEntry(int x, int width, int height) {
                super();
                int buttonWidth = (width - SPACING) / 2;

                elements.add(CycleButton.<Config.DetectionMode>builder((mode) ->
                                localized("option", "global.detection_mode." + mode.name()))
                        .withValues(Config.DetectionMode.values())
                        .withInitialValue(Config.get().detectionMode)
                        .withTooltip((mode) -> Tooltip.create(
                                localized("option", "global.detection_mode." + mode.name() + ".tooltip")
                                        .append("\n\n")
                                        .append(localized("option", "global.detection_mode.tooltip"))))
                        .create(x, 0, buttonWidth, height,
                                localized("option", "global.detection_mode"),
                                (button, mode) -> Config.get().detectionMode = mode));

                elements.add(CycleButton.<Config.DebugMode>builder((mode) ->
                                localized("option", "global.debug_mode." + mode.name()))
                        .withValues(Config.DebugMode.values())
                        .withInitialValue(Config.get().debugMode)
                        .withTooltip((mode) -> Tooltip.create(
                                localized("option", "global.debug_mode."
                                        + mode.name() + ".tooltip")))
                        .create(x + width - buttonWidth, 0, buttonWidth, height,
                                localized("option", "global.debug_mode"),
                                (button, mode) -> Config.get().debugMode = mode));
            }
        }

        private static class SelfCheckAndSendModeEntry extends MainOptionList.Entry {
            SelfCheckAndSendModeEntry(int x, int width, int height) {
                super();
                int buttonWidth = (width - SPACING) / 2;

                elements.add(CycleButton.booleanBuilder(
                        CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                                CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED))
                        .withInitialValue(Config.get().checkOwnMessages)
                        .withTooltip((status) -> Tooltip.create(
                                localized("option", "global.self_notify.tooltip")))
                        .create(x, 0, buttonWidth, height, 
                                localized("option", "global.self_notify"),
                                (button, status) -> Config.get().checkOwnMessages = status));

                elements.add(CycleButton.<Config.SendMode>builder((status) -> 
                                localized("option", "global.send_mode." + status.name()))
                        .withValues(Config.SendMode.values())
                        .withInitialValue(Config.get().sendMode)
                        .withTooltip((mode) -> Tooltip.create(
                                localized("option", "global.send_mode." + mode.name() + ".tooltip")
                                        .append("\n\n")
                                        .append(localized("option", "global.send_mode.tooltip"))))
                        .create(x + width - buttonWidth, 0, buttonWidth, height,
                                localized("option", "global.send_mode"),
                                (button, status) -> Config.get().sendMode = status));
            }
        }

        private static class MultiModeEntry extends MainOptionList.Entry {
            MultiModeEntry(int x, int width, int height) {
                super();
                int buttonWidth = (width - SPACING) / 2;

                elements.add(CycleButton.<Config.NotifMode>builder((status) -> 
                                localized("option", "global.notif_mode." + status.name()))
                        .withValues(Config.NotifMode.values())
                        .withInitialValue(Config.get().notifMode)
                        .withTooltip((status) -> Tooltip.create(
                                localized("option", "global.notif_mode." 
                                        + status.name() + ".tooltip")))
                        .create(x, 0, buttonWidth, height,
                                localized("option", "global.notif_mode"),
                                (button, status) -> Config.get().notifMode = status));

                elements.add(CycleButton.<Config.RestyleMode>builder((status) ->
                                localized("option", "global.restyle_mode." + status.name()))
                        .withValues(Config.RestyleMode.values())
                        .withInitialValue(Config.get().restyleMode)
                        .withTooltip((status) -> Tooltip.create(
                                localized("option", "global.restyle_mode."
                                        + status.name() + ".tooltip")))
                        .create(x + width - buttonWidth, 0, buttonWidth, height,
                                localized("option", "global.restyle_mode"),
                                (button, status) -> Config.get().restyleMode = status));
            }
        }

        private static class DefaultColorEntry extends MainOptionList.Entry {
            DefaultColorEntry(int x, int width, int height, GlobalOptionList list) {
                super();
                int colorFieldWidth = Minecraft.getInstance().font.width("#FFAAFF+++");

                Button mainButton = Button.builder(localized("option", "global.default_color")
                                        .setStyle(Style.EMPTY.withColor(Config.get().defaultColor)),
                        (button) -> {
                            int cpHeight = Math.max(HsvColorPicker.MIN_HEIGHT, list.height / 2);
                            int cpWidth = Math.max(HsvColorPicker.MIN_WIDTH, width);
                            list.screen.setOverlayWidget(new HsvColorPicker(
                                    x, list.screen.height / 2 - cpHeight / 2, cpWidth, cpHeight,
                                    () -> Config.get().defaultColor,
                                    (val) -> Config.get().defaultColor = val,
                                    (widget) -> {
                                        list.screen.removeOverlayWidget();
                                        list.reload();
                                    }));
                        })
                        .pos(x, 0)
                        .size(width - colorFieldWidth - SPACING, height)
                        .build();
                elements.add(mainButton);

                TextField colorField = new TextField(x + width - colorFieldWidth, 0,
                        colorFieldWidth, height);
                colorField.hexColorValidator();
                colorField.setMaxLength(7);
                colorField.setResponder((val) -> {
                    TextColor textColor = ColorUtil.parseColor(val);
                    if (textColor != null) {
                        int color = textColor.getValue();
                        Config.get().defaultColor = color;
                        // Update color of main button and field
                        mainButton.setMessage(localized("option", "global.default_color")
                                .setStyle(Style.EMPTY.withColor(textColor)));
                        float[] hsv = new float[3];
                        Color.RGBtoHSB(FastColor.ARGB32.red(color), FastColor.ARGB32.green(color),
                                FastColor.ARGB32.blue(color), hsv);
                        if (hsv[2] < 0.1) colorField.setTextColor(16777215);
                        else colorField.setTextColor(color);
                    }
                });
                colorField.setValue(TextColor.fromRgb(Config.get().defaultColor).formatValue());
                elements.add(colorField);
            }
        }

        private static class DefaultSoundEntry extends MainOptionList.Entry {
            DefaultSoundEntry(int x, int width, int height, GlobalOptionList list) {
                super();
                elements.add(Button.builder(localized("option", "global.default_sound",
                                        Config.get().defaultSound.getId()),
                                (button) -> list.openSoundConfig())
                        .pos(x, 0)
                        .size(width, height)
                        .build());
            }
        }

        private static class SoundSourceEntry extends MainOptionList.Entry {
            SoundSourceEntry(int x, int width, int height, GlobalOptionList list) {
                super();

                elements.add(CycleButton.<SoundSource>builder(source -> Component.translatable(
                        "soundCategory." + source.getName()))
                        .withValues(SoundSource.values())
                        .withInitialValue(Config.get().soundSource)
                        .withTooltip((status) -> Tooltip.create(
                                localized("option", "sound.source.tooltip")))
                        .create(x, 0, width - list.smallWidgetWidth - SPACING, height,
                                localized("option", "sound.source"),
                                (button, status) -> Config.get().soundSource = status));

                elements.add(Button.builder(Component.literal("\uD83D\uDD0A"),
                                (button) -> Minecraft.getInstance().setScreen(new SoundOptionsScreen(
                                        list.screen, Minecraft.getInstance().options)))
                        .tooltip(Tooltip.create(
                                localized("option", "sound.source.minecraft_volume")))
                        .pos(x + width - list.smallWidgetWidth, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());
            }
        }

        private static class PrefixFieldEntry extends Entry {
            PrefixFieldEntry(int x, int width, int height, GlobalOptionList list, int index) {
                super();

                EditBox prefixField = new TextField(x, 0, width, height);
                prefixField.setMaxLength(30);
                prefixField.setResponder((prefix) -> Config.get().prefixes.set(
                        index, prefix.strip().toLowerCase(Locale.ROOT)));
                prefixField.setValue(Config.get().prefixes.get(index));
                elements.add(prefixField);

                elements.add(Button.builder(Component.literal("\u274C")
                                        .withStyle(ChatFormatting.RED),
                                (button) -> {
                                    Config.get().prefixes.remove(index);
                                    list.reload();
                                })
                        .pos(x + width + SPACING, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());
            }
        }
    }
}
