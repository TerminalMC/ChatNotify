/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.gui.widget.list;

import dev.terminalmc.chatnotify.ChatNotify;
import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.TriState;
import dev.terminalmc.chatnotify.gui.screen.OptionsScreen;
import dev.terminalmc.chatnotify.gui.widget.HsvColorPicker;
import dev.terminalmc.chatnotify.util.ColorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.options.SoundOptionsScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundSource;

import java.util.Locale;

import static dev.terminalmc.chatnotify.util.Localization.localized;

/**
 * Contains global configuration options.
 */
public class GlobalOptionsList extends OptionsList {
    public GlobalOptionsList(Minecraft mc, int width, int height, int y, int rowWidth,
                             int itemHeight, int entryWidth, int entryHeight) {
        super(mc, width, height, y, rowWidth, itemHeight, entryWidth, entryHeight);

        addEntry(new Entry.MixinAndKeyDebugEntry(entryX, entryWidth, entryHeight));
        addEntry(new Entry.SelfCheckAndRegexEntry(entryX, entryWidth, entryHeight));
        addEntry(new Entry.DefaultColorEntry(entryX, entryWidth, entryHeight, this));
        addEntry(new Entry.DefaultSoundEntry(entryX, entryWidth, entryHeight, this));
        addEntry(new Entry.SoundSourceEntry(entryX, entryWidth, entryHeight, this));

        addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "global.prefixes", "\u2139"),
                Tooltip.create(localized("option", "global.prefixes.tooltip")), -1));

        int max = Config.get().prefixes.size();
        for (int i = 0; i < max; i++) {
            addEntry(new Entry.PrefixFieldEntry(entryX, entryWidth, entryHeight, this, i));
        }
        addEntry(new OptionsList.Entry.ActionButtonEntry(entryX, entryWidth, entryHeight,
                Component.literal("+"), null, -1,
                (button) -> {
                    Config.get().prefixes.add("");
                    reload();
                }));
    }

    @Override
    public GlobalOptionsList reload(int width, int height, double scrollAmount) {
        GlobalOptionsList newListWidget = new GlobalOptionsList(minecraft, width, height, 
                getY(), getRowWidth(), itemHeight, entryWidth, entryHeight);
        newListWidget.setScrollAmount(scrollAmount);
        return newListWidget;
    }

    private void openSoundConfig() {
        minecraft.setScreen(new OptionsScreen(minecraft.screen, localized("option", "sound"),
                new SoundOptionsList(minecraft, width, height, getY(),
                        getRowWidth(), itemHeight, entryWidth, entryHeight, Config.get().defaultSound)));
    }

    private abstract static class Entry extends OptionsList.Entry {

        private static class MixinAndKeyDebugEntry extends MainOptionsList.Entry {
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
                        .withTooltip((status) -> Tooltip.create(localized("option", "global.mixin.tooltip")))
                        .create(x, 0, buttonWidth, height, localized("option", "global.mixin"),
                                (button, status) -> Config.get().mixinEarly.state = status));

                elements.add(CycleButton.<TriState.State>builder((status) -> switch(status) {
                    case ON -> localized("option", "global.debug.key").withStyle(ChatFormatting.GREEN);
                    case OFF -> localized("option", "global.debug.raw").withStyle(ChatFormatting.GREEN);
                    case DISABLED -> localized("option", "global.debug.off").withStyle(ChatFormatting.RED);
                })
                        .withValues(TriState.State.values())
                        .withInitialValue(Config.get().debugShowKey.state)
                        .withTooltip((status) -> Tooltip.create(switch(status) {
                            case ON -> localized("option", "global.debug.key.tooltip");
                            case OFF -> localized("option", "global.debug.raw.tooltip");
                            case DISABLED -> localized("option", "global.debug.off.tooltip");
                        }))
                        .create(x + width - buttonWidth, 0, buttonWidth, height, localized("option", "global.debug"),
                                (button, status) -> Config.get().debugShowKey.state = status));
            }
        }

        private static class SelfCheckAndRegexEntry extends MainOptionsList.Entry {
            SelfCheckAndRegexEntry(int x, int width, int height) {
                super();
                int buttonWidth = (width - SPACING) / 2;

                elements.add(CycleButton.booleanBuilder(
                        CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                                CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED))
                        .withInitialValue(Config.get().checkOwnMessages)
                        .withTooltip((status) -> Tooltip.create(
                                localized("option", "global.self_notify.tooltip")))
                        .create(x, 0, buttonWidth, height, localized("option", "global.self_notify"),
                                (button, status) -> Config.get().checkOwnMessages = status));

                elements.add(CycleButton.booleanBuilder(
                        CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                                CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED))
                        .withInitialValue(Config.get().allowRegex)
                        .withTooltip((status) -> Tooltip.create(
                                localized("option", "global.regex.tooltip")))
                        .create(x + width - buttonWidth, 0, buttonWidth, height,
                                localized("option", "global.regex"),
                                (button, status) -> Config.get().allowRegex = status));
            }
        }

        private static class DefaultColorEntry extends MainOptionsList.Entry {
            DefaultColorEntry(int x, int width, int height, GlobalOptionsList listWidget) {
                super();
                Font font = Minecraft.getInstance().font;
                int colorFieldWidth = font.width("#FFAAFF+++");
                int mainButtonWidth = width - colorFieldWidth - SPACING;

                Button mainButton = Button.builder(localized("option", "global.default_color")
                                        .setStyle(Style.EMPTY.withColor(Config.get().defaultColor)),
                        (button) -> {
                            int cpHeight = 80;
                            int cpWidth = Math.max(cpHeight, width);
                            listWidget.screen.setOverlayWidget(new HsvColorPicker(
                                    x, listWidget.screen.height / 2 - cpHeight / 2, cpWidth, cpHeight,
                                    Component.empty(), () -> Config.get().defaultColor,
                                    (val) -> Config.get().defaultColor = val,
                                    (widget) -> {
                                        listWidget.screen.removeOverlayWidget();
                                        listWidget.reload();
                                    }));
                        })
                        .pos(x, 0)
                        .size(mainButtonWidth, height)
                        .build();
                elements.add(mainButton);

                EditBox colorField = new EditBox(font, x + mainButtonWidth + SPACING, 0,
                        colorFieldWidth, height, Component.empty());
                colorField.setMaxLength(7);
                colorField.setResponder((val) -> {
                    TextColor textColor = ColorUtil.parseColor(val);
                    if (textColor != null) {
                        int color = textColor.getValue();
                        Config.get().defaultColor = color;
                        // Update color of main button
                        mainButton.setMessage(localized("option", "global.default_color")
                                .setStyle(Style.EMPTY.withColor(textColor)));
                        colorField.setTextColor(color);
                    } else {
                        colorField.setTextColor(16711680);
                    }
                });
                colorField.setValue(TextColor.fromRgb(Config.get().defaultColor).formatValue());
                elements.add(colorField);
            }
        }

        private static class DefaultSoundEntry extends MainOptionsList.Entry {
            DefaultSoundEntry(int x, int width, int height, GlobalOptionsList listWidget) {
                super();
                elements.add(Button.builder(localized("option", "global.default_sound",
                                        Config.get().defaultSound.getId()),
                                (button) -> listWidget.openSoundConfig())
                        .pos(x, 0)
                        .size(width, height)
                        .build());
            }
        }

        private static class SoundSourceEntry extends MainOptionsList.Entry {
            SoundSourceEntry(int x, int width, int height, GlobalOptionsList listWidget) {
                super();
                int volumeButtonWidth = height;
                int mainButtonWidth = width - volumeButtonWidth - SPACING;

                elements.add(CycleButton.<SoundSource>builder(source -> Component.translatable(
                        "soundCategory." + source.getName()))
                        .withValues(SoundSource.values())
                        .withInitialValue(Config.get().soundSource)
                        .withTooltip((status) -> Tooltip.create(
                                localized("option", "global.sound_source.tooltip")))
                        .create(x, 0, mainButtonWidth, height, localized("option", "global.sound_source"),
                                (button, status) -> Config.get().soundSource = status));

                elements.add(Button.builder(Component.literal("\uD83D\uDD0A"),
                                (button) -> Minecraft.getInstance().setScreen(new SoundOptionsScreen(
                                        listWidget.screen, Minecraft.getInstance().options)))
                        .tooltip(Tooltip.create(
                                localized("option", "global.sound_source.minecraft_volume")))
                        .pos(x + width - volumeButtonWidth, 0)
                        .size(volumeButtonWidth, height)
                        .build());
            }
        }

        private static class PrefixFieldEntry extends Entry {
            PrefixFieldEntry(int x, int width, int height, GlobalOptionsList listWidget, int index) {
                super();
                int removeButtonWidth = Math.max(16, height);

                EditBox prefixField = new EditBox(Minecraft.getInstance().font, x, 0,
                        width, height, Component.empty());
                prefixField.setMaxLength(30);
                prefixField.setValue(Config.get().prefixes.get(index));
                prefixField.setResponder((prefix) -> Config.get().prefixes.set(
                        index, prefix.strip().toLowerCase(Locale.ROOT)));
                elements.add(prefixField);

                elements.add(Button.builder(Component.literal("\u274C")
                                        .withStyle(ChatFormatting.RED),
                                (button) -> {
                                    Config.get().prefixes.remove(index);
                                    listWidget.reload();
                                })
                        .pos(x + width + SPACING, 0)
                        .size(removeButtonWidth, height)
                        .build());
            }
        }
    }
}
