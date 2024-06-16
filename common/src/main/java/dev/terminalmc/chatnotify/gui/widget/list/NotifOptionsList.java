/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.gui.widget.list;

import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.Notification;
import dev.terminalmc.chatnotify.config.TriState;
import dev.terminalmc.chatnotify.config.Trigger;
import dev.terminalmc.chatnotify.gui.screen.OptionsScreen;
import dev.terminalmc.chatnotify.util.ColorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.time.Duration;

import static dev.terminalmc.chatnotify.util.Localization.localized;

/**
 * Contains controls for options of a {@link Notification}, and buttons linking
 * to other screens.
 */
public class NotifOptionsList extends OptionsList {
    private final Notification notif;
    private final boolean isUsername;

    public NotifOptionsList(Minecraft mc, int width, int height, int y,
                            int itemHeight, int entryRelX, int entryWidth, int entryHeight,
                            int scrollWidth, Notification notif, boolean isUsername) {
        super(mc, width, height, y, itemHeight, entryRelX, entryWidth, entryHeight, scrollWidth);
        notif.editing = true;
        this.notif = notif;
        this.isUsername = isUsername;

        addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Notification Triggers"), null, -1));

        for (int i = 0; i < notif.triggers.size(); i++) {
            addEntry(new Entry.TriggerFieldEntry(entryX, entryWidth, entryHeight,
                    this, notif, notif.triggers.get(i), i));
        }
        addEntry(new OptionsList.Entry.ActionButtonEntry(entryX, 0, entryWidth, entryHeight,
                Component.literal("+"), null, -1,
                (button) -> {
                    notif.triggers.add(new Trigger());
                    reload();
                }));


        addEntry(new OptionsList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Notification Options"), null, -1));

        addEntry(new Entry.SoundConfigEntry(entryX, entryWidth, entryHeight, notif, this));
        addEntry(new Entry.ColorConfigEntry(entryX, entryWidth, entryHeight, notif, this));
        addEntry(new Entry.FormatConfigEntry1(entryX, entryWidth, entryHeight, notif));
        addEntry(new Entry.FormatConfigEntry2(entryX, entryWidth, entryHeight, notif));

        addEntry(new OptionsList.Entry.ActionButtonEntry(entryX, 0, entryWidth, entryHeight,
                Component.literal("Advanced Settings"),
                Tooltip.create(Component.literal("Here be Dragons!")), 500,
                (button) -> openAdvancedConfig()));
    }

    @Override
    public NotifOptionsList resize(int width, int height, int y,
                                   int itemHeight, double scrollAmount) {
        NotifOptionsList newListWidget = new NotifOptionsList(
                minecraft, width, height, y, itemHeight,
                entryRelX, entryWidth, entryHeight, scrollWidth, notif, isUsername);
        newListWidget.setScrollAmount(scrollAmount);
        return newListWidget;
    }

    @Override
    public void onClose() {
        notif.editing = false;
        notif.autoDisable();
    }

    private void openKeyConfig(Trigger trigger) {
        minecraft.setScreen(new OptionsScreen(minecraft.screen,
                localized("screen", "key"),
                new KeyOptionsList(minecraft, screen.width, screen.height, getY(),
                        itemHeight, entryRelX, entryWidth, entryHeight, scrollWidth, trigger)));
    }

    private void openColorConfig() {
        minecraft.setScreen(new OptionsScreen(minecraft.screen,
                localized("screen", "color"),
                new ColorOptionsList(minecraft, screen.width, screen.height, getY(),
                        itemHeight, entryRelX, entryWidth, entryHeight, scrollWidth,
                        () -> notif.textStyle.color, (color) -> notif.textStyle.color = color)));
    }

    private void openSoundConfig() {
        minecraft.setScreen(new OptionsScreen(minecraft.screen,
                localized("screen", "sound"),
                new SoundOptionsList(minecraft, screen.width, screen.height, getY(),
                        itemHeight, entryRelX, entryWidth, entryHeight, scrollWidth, notif.sound)));
    }

    private void openAdvancedConfig() {
        minecraft.setScreen(new OptionsScreen(minecraft.screen,
                localized("screen", "advanced"),
                new AdvancedOptionsList(minecraft, screen.width, screen.height, getY(),
                        itemHeight, entryRelX, entryWidth, entryHeight, scrollWidth, notif)));
    }

    private abstract static class Entry extends OptionsList.Entry {

        private static class TriggerFieldEntry extends Entry {

            TriggerFieldEntry(int x, int width, int height, NotifOptionsList listWidget,
                              Notification notif, Trigger trigger, int index) {
                super();

                int spacing = 5;
                int smallButtonWidth = 15;
                int removeButtonWidth = 20;

                EditBox triggerField = new EditBox(Minecraft.getInstance().font,
                        x, 0, width, height, Component.literal("Notification Trigger"));
                triggerField.setMaxLength(120);
                triggerField.setValue(trigger.string);
                triggerField.setResponder((string) -> trigger.string = string.strip());

                if (listWidget.isUsername && index <= 1) {
                    triggerField.setEditable(false);
                    triggerField.active = false;
                    triggerField.setTooltip(Tooltip.create(Component.literal(
                            (index == 0 ? "Profile name" : "Display name") +
                            "\n(updated automatically)")));
                    triggerField.setTooltipDelay(Duration.ofMillis(500));
                    elements.add(triggerField);
                }
                else {
                    if (Config.get().allowRegex) {
                        CycleButton<Boolean> regexButton = CycleButton.booleanBuilder(
                                Component.literal(".*").withStyle(ChatFormatting.GREEN),
                                        Component.literal(".*").withStyle(ChatFormatting.RED))
                                .displayOnlyValue()
                                .withInitialValue(trigger.isRegex)
                                .withTooltip((status) -> Tooltip.create(Component.literal(
                                        status ? "Regex Enabled" : "Regex Disabled")))
                                .create(x - spacing - smallButtonWidth * 2, 0, smallButtonWidth, height,
                                        Component.empty(), (button, status) -> trigger.isRegex = status);
                        regexButton.setTooltipDelay(Duration.ofMillis(500));
                        if (trigger.isKey) {
                            regexButton.setMessage(Component.literal(".*"));
                            regexButton.setTooltip(Tooltip.create(Component.literal(
                                    "Regex Disabled for Key-Type Trigger")));
                            regexButton.active = false;
                        }
                        elements.add(regexButton);
                    }

                    CycleButton<Boolean> keyButton = CycleButton.booleanBuilder(
                            Component.literal("\uD83D\uDD11").withStyle(ChatFormatting.GREEN),
                                    Component.literal("\uD83D\uDD11").withStyle(ChatFormatting.RED))
                            .withInitialValue(trigger.isKey)
                            .displayOnlyValue()
                            .withTooltip((status) -> Tooltip.create(Component.literal(
                                    status ? "Translation Key Trigger" : "Normal Trigger")))
                            .create(x - spacing - smallButtonWidth, 0, smallButtonWidth, height, Component.empty(),
                                    (button, status) -> {
                                        if (Screen.hasShiftDown()) {
                                            trigger.isKey = status;
                                            listWidget.reload();
                                        } else {
                                            listWidget.openKeyConfig(trigger);
                                        }});
                    keyButton.setTooltipDelay(Duration.ofMillis(500));
                    elements.add(keyButton);

                    elements.add(keyButton);
                    elements.add(triggerField);
                    elements.add(Button.builder(Component.literal("\u274C"),
                                    (button) -> {
                                        notif.triggers.remove(index);
                                        listWidget.reload();
                                    })
                            .pos(x + width + spacing, 0)
                            .size(removeButtonWidth, height)
                            .build());
                }
            }
        }

        private static class SoundConfigEntry extends Entry {
            SoundConfigEntry(int x, int width, int height, Notification notif,
                             NotifOptionsList listWidget) {
                super();

                int spacing = 5;
                int statusButtonWidth = 25;
                int mainButtonWidth = width - statusButtonWidth - spacing;

                elements.add(Button.builder(
                                Component.literal("Sound: " + notif.sound.getId()),
                                (button) -> listWidget.openSoundConfig())
                        .pos(x, 0)
                        .size(mainButtonWidth, height)
                        .build());

                elements.add(CycleButton.booleanBuilder(
                        Component.translatable("options.on").withStyle(ChatFormatting.GREEN),
                                Component.translatable("options.off").withStyle(ChatFormatting.RED))
                        .displayOnlyValue()
                        .withInitialValue(notif.sound.isEnabled())
                        .create(x + width - statusButtonWidth, 0, statusButtonWidth, height,
                                Component.empty(),
                                (button, status) -> notif.sound.setEnabled(status)));
            }
        }

        private static class ColorConfigEntry extends Entry {
            ColorConfigEntry(int x, int width, int height, Notification notif, NotifOptionsList listWidget) {
                super();

                Font activeFont = Minecraft.getInstance().font;
                int spacing = 5;
                int statusButtonWidth = 25;
                int reloadButtonWidth = 20;
                int colorFieldWidth = activeFont.width("#FFAAFF+++");
                int mainButtonWidth = width - statusButtonWidth - reloadButtonWidth -
                        colorFieldWidth - spacing * 2;

                String mainButtonMessage = "Text Color";
                Button mainButton = Button.builder(Component.literal(mainButtonMessage)
                                        .setStyle(Style.EMPTY.withColor(notif.textStyle.getTextColor())),
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
                        notif.textStyle.color = color.getValue();
                        // Update color of main button
                        mainButton.setMessage(Component.literal(mainButtonMessage)
                                .setStyle(Style.EMPTY.withColor(notif.textStyle.getTextColor())));
                    }
                });
                colorField.setValue(notif.textStyle.getTextColor().formatValue());
                elements.add(colorField);

                elements.add(Button.builder(Component.literal("\ud83d\uddd8"),
                                (button) -> listWidget.reload())
                        .tooltip(Tooltip.create(Component.literal("Check Value")))
                        .pos(x + mainButtonWidth + spacing + colorFieldWidth, 0)
                        .size(reloadButtonWidth, height)
                        .build());

                elements.add(CycleButton.booleanBuilder(
                        Component.translatable("options.on").withStyle(ChatFormatting.GREEN),
                                Component.translatable("options.off").withStyle(ChatFormatting.RED))
                        .displayOnlyValue()
                        .withInitialValue(notif.textStyle.doColor)
                        .create(x + width - statusButtonWidth, 0,
                                statusButtonWidth, height, Component.empty(),
                                (button, status) -> notif.textStyle.doColor = status));
            }
        }

        private static class FormatConfigEntry1 extends Entry {
            FormatConfigEntry1(int x, int width, int height, Notification notif) {
                super();

                int spacing = 5;
                int buttonWidth = (width - spacing * 2) / 3;

                CycleButton<TriState.State> boldButton = CycleButton.<TriState.State>builder(
                        (state) -> getMessage(state, ChatFormatting.BOLD))
                        .withValues(TriState.State.values())
                        .withInitialValue(notif.textStyle.bold.getState())
                        .withTooltip(this::getTooltip)
                        .create(x, 0, buttonWidth, height,
                                Component.literal("Bold"),
                                (button, state) -> notif.textStyle.bold.state = state);
                boldButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(boldButton);

                CycleButton<TriState.State> italicButton = CycleButton.<TriState.State>builder(
                        (state) -> getMessage(state, ChatFormatting.ITALIC))
                        .withValues(TriState.State.values())
                        .withInitialValue(notif.textStyle.italic.getState())
                        .withTooltip(this::getTooltip)
                        .create(x + width / 2 - buttonWidth / 2, 0, buttonWidth, height,
                                Component.literal("Italic"),
                                (button, state) -> notif.textStyle.italic.state = state);
                italicButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(italicButton);

                CycleButton<TriState.State> underlineButton = CycleButton.<TriState.State>builder(
                        (state) -> getMessage(state, ChatFormatting.UNDERLINE))
                        .withValues(TriState.State.values())
                        .withInitialValue(notif.textStyle.underlined.getState())
                        .withTooltip(this::getTooltip)
                        .create(x + width - buttonWidth, 0, buttonWidth, height,
                                Component.literal("Underline"),
                                (button, state) -> notif.textStyle.underlined.state = state);
                underlineButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(underlineButton);
            }

            private Component getMessage(TriState.State state, ChatFormatting format) {
                return switch(state) {
                    case OFF -> Component.translatable("options.off").withStyle(ChatFormatting.RED);
                    case ON -> Component.translatable("options.on").withStyle(ChatFormatting.GREEN)
                            .withStyle(format);
                    default -> Component.literal("/").withStyle(ChatFormatting.GRAY);
                };
            }

            private Tooltip getTooltip(TriState.State state) {
                if (state.equals(TriState.State.DISABLED)) return
                        Tooltip.create(Component.literal("Use existing format"));
                return null;
            }
        }

        private static class FormatConfigEntry2 extends Entry {
            FormatConfigEntry2(int x, int width, int height, Notification notif) {
                super();

                int spacing = 6;
                int buttonWidth = (width - spacing) / 2;

                CycleButton<TriState.State> strikethroughButton = CycleButton.<TriState.State>builder(
                        (state) -> getMessage(state, ChatFormatting.STRIKETHROUGH))
                        .withValues(TriState.State.values())
                        .withInitialValue(notif.textStyle.strikethrough.getState())
                        .withTooltip(this::getTooltip)
                        .create(x, 0, buttonWidth, height,
                                Component.literal("Strikethrough"),
                                (button, state) -> notif.textStyle.strikethrough.state = state);
                strikethroughButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(strikethroughButton);

                CycleButton<TriState.State> obfuscateButton = CycleButton.<TriState.State>builder(
                        (state) -> getMessage(state, ChatFormatting.OBFUSCATED))
                        .withValues(TriState.State.values())
                        .withInitialValue(notif.textStyle.obfuscated.getState())
                        .withTooltip(this::getTooltip)
                        .create(x + width - buttonWidth, 0, buttonWidth, height,
                                Component.literal("Obfuscate"),
                                (button, state) -> notif.textStyle.obfuscated.state = state);
                obfuscateButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(obfuscateButton);
            }

            private Component getMessage(TriState.State state, ChatFormatting format) {
                return switch(state) {
                    case OFF -> Component.translatable("options.off").withStyle(ChatFormatting.RED);
                    case ON -> Component.translatable("options.on").withStyle(ChatFormatting.GREEN)
                            .withStyle(format);
                    default -> Component.literal("/").withStyle(ChatFormatting.GRAY);
                };
            }

            private Tooltip getTooltip(TriState.State state) {
                if (state.equals(TriState.State.DISABLED))
                    return Tooltip.create(Component.literal("Keep existing format"));
                return null;
            }
        }
    }
}