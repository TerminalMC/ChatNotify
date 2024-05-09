/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package com.notryken.chatnotify.gui.widget.list;

import com.notryken.chatnotify.config.Config;
import com.notryken.chatnotify.gui.screen.OptionsScreen;
import com.notryken.chatnotify.util.ColorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundSource;

import java.util.Locale;

/**
 * Contains global configuration options.
 */
public class GlobalOptionsList extends OptionsList {
    public GlobalOptionsList(Minecraft mc, int width, int height, int y,
                             int itemHeight, int entryRelX, int entryWidth, int entryHeight,
                             int scrollWidth) {
        super(mc, width, height, y, itemHeight, entryRelX, entryWidth, entryHeight, scrollWidth);

        addEntry(new Entry.MixinAndKeyDebugEntry(entryX, entryWidth, entryHeight));
        addEntry(new Entry.SelfCheckAndRegexEntry(entryX, entryWidth, entryHeight));
        addEntry(new Entry.DefaultColorEntry(entryX, entryWidth, entryHeight, this));
        addEntry(new Entry.DefaultSoundEntry(entryX, entryWidth, entryHeight, this));
        addEntry(new Entry.SoundSourceEntry(entryX, entryWidth, entryHeight));

        addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Message Modifier Prefixes \u2139"),
                Tooltip.create(Component.literal("A message prefix is a character or " +
                        "sequence of characters that you type before a message to modify it. " +
                        "For example, '!' or '/shout' may be used on some servers to communicate " +
                        "in global chat. This may be useful for preventing spurious notifications.")), -1));

        int max = Config.get().prefixes.size();
        for (int i = 0; i < max; i++) {
            addEntry(new Entry.PrefixFieldEntry(entryX, entryWidth, entryHeight, this, i));
        }
        addEntry(new OptionsList.Entry.ActionButtonEntry(entryX, 0, entryWidth, entryHeight,
                Component.literal("+"), null, -1,
                (button) -> {
                    Config.get().prefixes.add("");
                    reload();
                }));
    }

    @Override
    public GlobalOptionsList resize(int width, int height, int y,
                                    int itemHeight, double scrollAmount) {
        GlobalOptionsList newListWidget = new GlobalOptionsList(
                minecraft, width, height, y, itemHeight,
                entryRelX, entryWidth, entryHeight, scrollWidth);
        newListWidget.setScrollAmount(scrollAmount);
        return newListWidget;
    }

    private void openColorConfig() {
        minecraft.setScreen(new OptionsScreen(minecraft.screen,
                Component.translatable("screen.chatnotify.title.color"),
                new ColorOptionsList(minecraft, screen.width, screen.height, getY(),
                        itemHeight, entryRelX, entryWidth, entryHeight, scrollWidth,
                        () -> Config.get().defaultColor, (color) -> Config.get().defaultColor = color)));
    }

    private void openSoundConfig() {
        minecraft.setScreen(new OptionsScreen(minecraft.screen,
                Component.translatable("screen.chatnotify.title.sound"),
                new SoundOptionsList(minecraft, screen.width, screen.height, getY(),
                        itemHeight, entryRelX, entryWidth, entryHeight, scrollWidth, Config.get().defaultSound)));
    }

    private abstract static class Entry extends OptionsList.Entry {

        private static class MixinAndKeyDebugEntry extends MainOptionsList.Entry {
            MixinAndKeyDebugEntry(int x, int width, int height) {
                super();

                int spacing = 4;
                int buttonWidth = (width - spacing) / 2;

                elements.add(CycleButton.booleanBuilder(
                                Component.translatable("options.on").withStyle(ChatFormatting.GREEN),
                                Component.translatable("options.off").withStyle(ChatFormatting.RED))
                        .withInitialValue(Config.get().mixinEarly)
                        .withTooltip((status) -> Tooltip.create(Component.literal(
                                "If ChatNotify is not detecting incoming messages, try changing this.")))
                        .create(x, 0, buttonWidth, height, Component.literal("Early Mixin"),
                                (button, status) -> Config.get().mixinEarly = status));

                elements.add(CycleButton.booleanBuilder(
                                Component.translatable("options.on").withStyle(ChatFormatting.GREEN),
                                Component.translatable("options.off").withStyle(ChatFormatting.RED))
                        .withInitialValue(Config.get().debugShowKey)
                        .withTooltip((value) -> Tooltip.create(Component.literal(("If ON, translation key " +
                                "info of new chat messages will be shown when you hover over them." +
                                "\nTurn OFF if not in use."))))
                        .create(x + width - buttonWidth, 0, buttonWidth, height, Component.literal("Debug Keys"),
                                (button, status) -> Config.get().debugShowKey = status));
            }
        }

        private static class SelfCheckAndRegexEntry extends MainOptionsList.Entry {
            SelfCheckAndRegexEntry(int x, int width, int height) {
                super();

                int spacing = 4;
                int buttonWidth = (width - spacing) / 2;

                elements.add(CycleButton.booleanBuilder(
                                Component.translatable("options.on").withStyle(ChatFormatting.GREEN),
                                Component.translatable("options.off").withStyle(ChatFormatting.RED))
                        .withInitialValue(Config.get().checkOwnMessages)
                        .withTooltip((status) -> Tooltip.create(Component.literal(
                                "If ON, messages that you send will trigger notifications." +
                                        "\n\nNote: ChatNotify will only detect a message as being sent " +
                                        "by you if it matches a trigger of the first notification.")))
                        .create(x, 0, buttonWidth, height, Component.literal("Self Notify"),
                                (button, status) -> Config.get().checkOwnMessages = status));

                elements.add(CycleButton.booleanBuilder(
                                Component.translatable("options.on").withStyle(ChatFormatting.GREEN),
                                Component.translatable("options.off").withStyle(ChatFormatting.RED))
                        .withInitialValue(Config.get().allowRegex)
                        .withTooltip((status) -> Tooltip.create(Component.literal(
                                "If ON, you can set any trigger to be interpreted " +
                                        "as regex by using the [.*] button next to the trigger.")))
                        .create(x + width - buttonWidth, 0, buttonWidth, height, Component.literal("Allow Regex"),
                                (button, status) -> Config.get().allowRegex = status));
            }
        }

        private static class DefaultColorEntry extends MainOptionsList.Entry {
            DefaultColorEntry(int x, int width, int height, GlobalOptionsList listWidget) {
                super();

                Font activeFont = Minecraft.getInstance().font;
                int spacing = 5;
                int reloadButtonWidth = 20;
                int colorFieldWidth = activeFont.width("#FFAAFF+++");
                int mainButtonWidth = width - reloadButtonWidth - colorFieldWidth - spacing;

                String mainButtonMessage = "Default Color";
                Button mainButton = Button.builder(Component.literal(mainButtonMessage)
                                        .setStyle(Style.EMPTY.withColor(Config.get().defaultColor)),
                                (button) -> listWidget.openColorConfig())
                        .pos(x, 0)
                        .size(mainButtonWidth, height)
                        .build();
                elements.add(mainButton);

                EditBox colorField = new EditBox(activeFont, x + mainButtonWidth + spacing, 0,
                        colorFieldWidth, height, Component.literal("Hex Color"));
                colorField.setMaxLength(7);
                colorField.setResponder(strColor -> {
                    TextColor color = ColorUtil.parseColor(strColor);
                    if (color != null) {
                        Config.get().defaultColor = color.getValue();
                        // Update color of main button
                        mainButton.setMessage(Component.literal(mainButtonMessage)
                                .setStyle(Style.EMPTY.withColor(color)));
                    }
                });
                colorField.setValue(TextColor.fromRgb(Config.get().defaultColor).formatValue());
                elements.add(colorField);

                elements.add(Button.builder(Component.literal("\ud83d\uddd8"),
                                (button) -> listWidget.reload())
                        .tooltip(Tooltip.create(Component.literal("Check Value")))
                        .pos(x + mainButtonWidth + spacing + colorFieldWidth, 0)
                        .size(reloadButtonWidth, height)
                        .build());
            }
        }

        private static class DefaultSoundEntry extends MainOptionsList.Entry {
            DefaultSoundEntry(int x, int width, int height, GlobalOptionsList listWidget) {
                super();
                elements.add(Button.builder(
                        Component.literal("Default Sound: " + Config.get().defaultSound.getId()),
                                (button) -> listWidget.openSoundConfig())
                        .pos(x, 0)
                        .size(width, height)
                        .build());
            }
        }

        private static class SoundSourceEntry extends MainOptionsList.Entry {
            SoundSourceEntry(int x, int width, int height) {
                super();
                elements.add(CycleButton.<SoundSource>builder(source -> Component.translatable(
                                "soundCategory." + source.getName()))
                        .withValues(SoundSource.values())
                        .withInitialValue(Config.get().soundSource)
                        .withTooltip((status) -> Tooltip.create(Component.literal(
                                "The sound category determines which of Minecraft's volume control " +
                                        "sliders will affect the notification sound.")))
                        .create(x, 0, width, height, Component.literal("Sound Category"),
                                (button, status) -> Config.get().soundSource = status));
            }
        }

        private static class PrefixFieldEntry extends Entry {
            PrefixFieldEntry(int x, int width, int height, GlobalOptionsList listWidget, int index) {
                super();

                int spacing = 5;
                int removeButtonWidth = 24;

                EditBox prefixField = new EditBox(
                        Minecraft.getInstance().font, x, 0, width, height,
                        Component.literal("Message Prefix"));
                prefixField.setMaxLength(20);
                prefixField.setValue(Config.get().prefixes.get(index));
                prefixField.setResponder(
                        (prefix) -> Config.get().prefixes.set(
                                index, prefix.strip().toLowerCase(Locale.ROOT)));
                elements.add(prefixField);

                elements.add(Button.builder(Component.literal("\u274C"),
                                (button) -> {
                                    Config.get().prefixes.remove(index);
                                    listWidget.reload();
                                })
                        .pos(x + width + spacing, 0)
                        .size(removeButtonWidth, height)
                        .build());
            }
        }
    }
}