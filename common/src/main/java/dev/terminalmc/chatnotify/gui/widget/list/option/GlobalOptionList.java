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

import dev.terminalmc.chatnotify.ChatNotify;
import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.TriState;
import dev.terminalmc.chatnotify.gui.screen.OptionsScreen;
import dev.terminalmc.chatnotify.gui.widget.HsvColorPicker;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import dev.terminalmc.chatnotify.util.MiscUtil;
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

        addEntry(new Entry.MixinAndKeyDebugEntry(entryX, entryWidth, entryHeight));
        addEntry(new Entry.SelfCheckAndSendModeEntry(entryX, entryWidth, entryHeight));
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
                new SoundOptionList(minecraft, width, height, getY(), itemHeight,
                        entryWidth, entryHeight, Config.get().defaultSound)));
    }

    private abstract static class Entry extends OptionList.Entry {

        private static class MixinAndKeyDebugEntry extends MainOptionList.Entry {
            MixinAndKeyDebugEntry(int x, int width, int height) {
                super();
                int buttonWidth = (width - SPACING) / 2;

                elements.add(CycleButton.<TriState.State>builder((status) -> switch(status) {
                    case ON -> CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN);
                    case OFF -> CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED);
                    case DISABLED -> localized("option", "global.mixin.auto").append(" ")
                            .append(ChatNotify.hasChatHistoryMod
                                    ? CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN)
                                    : CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED));
                })
                        .withValues(TriState.State.values())
                        .withInitialValue(Config.get().mixinEarly.state)
                        .withTooltip((status) -> Tooltip.create(
                                localized("option", "global.mixin.tooltip")))
                        .create(x, 0, buttonWidth, height, 
                                localized("option", "global.mixin"),
                                (button, status) -> Config.get().mixinEarly.state = status));

                elements.add(CycleButton.<TriState.State>builder((status) -> switch(status) {
                    case ON -> localized("option", "global.debug.key")
                            .withStyle(ChatFormatting.GREEN);
                    case OFF -> localized("option", "global.debug.raw")
                            .withStyle(ChatFormatting.GREEN);
                    case DISABLED -> localized("option", "global.debug.off")
                            .withStyle(ChatFormatting.RED);
                })
                        .withValues(TriState.State.values())
                        .withInitialValue(Config.get().debugShowKey.state)
                        .withTooltip((status) -> Tooltip.create(switch(status) {
                            case ON -> localized("option", "global.debug.key.tooltip");
                            case OFF -> localized("option", "global.debug.raw.tooltip");
                            case DISABLED -> localized("option", "global.debug.off.tooltip");
                        }))
                        .create(x + width - buttonWidth, 0, buttonWidth, height, 
                                localized("option", "global.debug"),
                                (button, status) -> Config.get().debugShowKey.state = status));
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

                elements.add(CycleButton.booleanBuilder(
                                CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                                CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED))
                        .withInitialValue(Config.get().compatSendMode)
                        .withTooltip((status) -> Tooltip.create(
                                localized("option", "global.compat_send.tooltip")))
                        .create(x + width - buttonWidth, 0, buttonWidth, height, 
                                localized("option", "global.compat_send"),
                                (button, status) -> Config.get().compatSendMode = status));
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
                                    Component.empty(), () -> Config.get().defaultColor,
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
                    TextColor textColor = MiscUtil.parseColor(val);
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
                                localized("option", "global.sound_source.tooltip")))
                        .create(x, 0, width - list.smallWidgetWidth - SPACING, height,
                                localized("option", "global.sound_source"),
                                (button, status) -> Config.get().soundSource = status));

                elements.add(Button.builder(Component.literal("\uD83D\uDD0A"),
                                (button) -> Minecraft.getInstance().setScreen(new SoundOptionsScreen(
                                        list.screen, Minecraft.getInstance().options)))
                        .tooltip(Tooltip.create(
                                localized("option", "global.sound_source.minecraft_volume")))
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
