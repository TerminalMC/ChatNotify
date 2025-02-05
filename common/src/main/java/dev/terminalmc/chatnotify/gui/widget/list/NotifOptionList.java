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

import dev.terminalmc.chatnotify.config.*;
import dev.terminalmc.chatnotify.gui.screen.OptionScreen;
import dev.terminalmc.chatnotify.gui.widget.HsvColorPicker;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import dev.terminalmc.chatnotify.util.ColorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static dev.terminalmc.chatnotify.util.Localization.localized;

/**
 * Contains controls for options of a {@link Notification}, and buttons linking
 * to other screens.
 */
public class NotifOptionList extends DragReorderList {
    private final Notification notif;
    private String filterString = "";
    private @Nullable Pattern filterPattern = null;
    private OptionList.Entry.ActionButtonEntry addTriggerEntry;

    public NotifOptionList(Minecraft mc, int width, int height, int y, int entryWidth,
                           int entryHeight, int entrySpacing, Notification notif) {
        super(mc, width, height, y, entryWidth, entryHeight, entrySpacing, 
                () -> notif.editing = false, new HashMap<>(Map.of(
                        Entry.TriggerFieldEntry.class, (source, dest) ->
                                moveTrigger(notif, source, dest))));
        this.notif = notif;
        notif.editing = true;
        
        addTriggerEntry = new OptionList.Entry.ActionButtonEntry(
                entryX, entryWidth, entryHeight, Component.literal("+"), null, -1,
                (button) -> {
                    notif.triggers.add(new Trigger());
                    filterString = "";
                    filterPattern = null;
                    init();
                    ensureVisible(addTriggerEntry);
                });
    }

    @Override
    protected void addEntries() {
        addEntry(new Entry.TitleAndSearchEntry(entryX, entryWidth, entryHeight, this));

        refreshTriggerSubList();
        addTriggerEntry.setBounds(entryX, entryWidth, entryHeight);
        addEntry(addTriggerEntry);

        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "notif"), null, -1));

        addEntry(new Entry.SoundConfigEntry(entryX, entryWidth, entryHeight, notif, this));
        addEntry(new Entry.ColorConfigEntry(entryX, entryWidth, entryHeight, this,
                () -> notif.textStyle.color, (val) -> notif.textStyle.color = val,
                () -> notif.textStyle.doColor, (val) -> notif.textStyle.doColor = val,
                localized("option", "notif.color")));
        addEntry(new Entry.FormatConfigEntry(entryX, entryWidth, entryHeight, notif, true));
        addEntry(new Entry.FormatConfigEntry(entryX, entryWidth, entryHeight, notif, false));

        addEntry(new OptionList.Entry.ActionButtonEntry(entryX, entryWidth, entryHeight,
                localized("option", "advanced"),
                Tooltip.create(localized("option", "advanced.tooltip")), 500,
                (button) -> openAdvancedConfig()));
    }
    
    protected void refreshTriggerSubList() {
        children().removeIf((entry) -> entry instanceof Entry.TriggerFieldEntry 
                || entry instanceof Entry.StyleTargetFieldEntry);
        // Add in reverse order at index 1 (entry 0 is title/search)
        int start = 1;
        boolean isUser = notif.equals(Config.get().getUserNotif());
        for (int i = notif.triggers.size() - 1; i >= 0; i--) {
            Trigger trigger = notif.triggers.get(i);
            if (filterPattern == null || filterPattern.matcher(trigger.string).find()) {
                if (isUser && i <= 1) {
                    children().add(start, new Entry.TriggerDisplayFieldEntry(
                            dynEntryX, dynEntryWidth, entryHeight, trigger.string, i == 0));
                } else {
                    if (trigger.styleTarget.enabled) {
                        children().add(start, new Entry.StyleTargetFieldEntry(
                                dynEntryX, dynEntryWidth, entryHeight, this, trigger.styleTarget));
                    }
                    children().add(start, new Entry.TriggerFieldEntry(
                            dynEntryX, dynEntryWidth, entryHeight, this, notif, trigger, i));
                }
            }
        }
        clampScrollAmount();
    }
    
    // Sub-screen opening

    private void openTriggerConfig(Trigger trigger) {
        mc.setScreen(new OptionScreen(mc.screen, localized("option", "trigger"),
                new TriggerOptionList(mc, width, height, getY(), entryWidth, entryHeight,
                        entrySpacing, () -> {}, trigger, notif.textStyle)));
    }

    private void openKeyConfig(Trigger trigger) {
        mc.setScreen(new OptionScreen(mc.screen, localized("option", "key"),
                new KeyOptionList(mc, width, height, getY(), entryWidth, entryHeight,
                        () -> {}, trigger)));
    }

    private void openSoundConfig() {
        mc.setScreen(new OptionScreen(mc.screen, localized("option", "sound"),
                new SoundOptionList(mc, width, height, getY(), entryWidth, entryHeight,
                        () -> {}, notif.sound)));
    }

    private void openAdvancedConfig() {
        mc.setScreen(new OptionScreen(mc.screen, localized("option", "advanced"),
                new AdvancedOptionList(mc, width, height, getY(), entryWidth, entryHeight,
                        entrySpacing, notif)));
    }
    
    // Re-ordering util
    
    private static boolean moveTrigger(Notification notif, int source, int dest) {
        if (notif.equals(Config.get().getUserNotif())) {
            // Offset to compensate for username triggers
            source += 2;
            dest += 2;
        }
        return notif.moveTrigger(source, dest);
    }
    
    // Custom entries

    abstract static class Entry extends OptionList.Entry {
        
        private static class TitleAndSearchEntry extends Entry {
            TitleAndSearchEntry(int x, int width, int height, NotifOptionList list) {
                super();
                int searchFieldWidth = 100;
                int titleWidth = width - searchFieldWidth - SPACE;

                StringWidget titleWidget = new StringWidget(x, 0, titleWidth, height,
                        localized("option", "notif.triggers", "\u2139"), list.mc.font);
                titleWidget.setTooltip(Tooltip.create(localized(
                        "option", "notif.triggers.tooltip")));
                elements.add(titleWidget);
                
                TextField searchField = new TextField(x + width - searchFieldWidth, 0,
                        searchFieldWidth, height);
                searchField.setMaxLength(64);
                searchField.setHint(localized("option", "notif.triggers.search.hint")
                        .withColor(TextField.TEXT_COLOR_HINT));
                searchField.setValue(list.filterString);
                searchField.setResponder((str) -> {
                    list.filterString = str;
                    if (str.isBlank()) {
                        list.filterPattern = null;
                    } else {
                        list.filterPattern = Pattern.compile("(?iU)" + Pattern.quote(str));
                    }
                    list.refreshTriggerSubList();
                });
                elements.add(searchField);
            }
        }

        private static class TriggerDisplayFieldEntry extends Entry {
            TriggerDisplayFieldEntry(int x, int width, int height, String value, boolean first) {
                super();

                TextField displayField = new TextField(x, 0, width, height);
                displayField.setValue(value);
                displayField.setTooltip(Tooltip.create(first
                        ? localized("option", "notif.trigger.special.profile_name.tooltip")
                        : localized("option", "notif.trigger.special.display_name.tooltip")));
                displayField.setTooltipDelay(Duration.ofMillis(500));
                displayField.setEditable(false);
                displayField.active = false;
                elements.add(displayField);
            }
        }

        private static class TriggerFieldEntry extends Entry {
            TriggerFieldEntry(int x, int width, int height, NotifOptionList list,
                              Notification notif, Trigger trigger, int index) {
                super();
                int triggerFieldWidth = width - (list.tinyWidgetWidth * 3);
                boolean keyTrigger = trigger.type == Trigger.Type.KEY;
                if (keyTrigger) triggerFieldWidth -= list.tinyWidgetWidth;
                int movingX = x;

                // Index indicator
                Button indicatorButton = Button.builder(
                        Component.literal(String.valueOf(index + 1)), (button) -> {})
                        .pos(x - list.smallWidgetWidth - SPACE - list.tinyWidgetWidth, 0)
                        .size(list.tinyWidgetWidth, height)
                        .build();
                indicatorButton.active = false;
                elements.add(indicatorButton);
                
                // Drag reorder button
                Button dragButton = Button.builder(Component.literal("\u2191\u2193"),
                                (button) -> {
                                    this.setDragging(true);
                                    list.startDragging(this, StyleTargetFieldEntry.class,
                                            trigger.styleTarget.enabled);
                                })
                        .pos(x - list.smallWidgetWidth - SPACE, 0)
                        .size(list.smallWidgetWidth, height)
                        .build();
                dragButton.active = list.filterPattern == null;
                elements.add(dragButton);

                // Type button
                CycleButton<Trigger.Type> typeButton = CycleButton.<Trigger.Type>builder(
                                (type) -> Component.literal(type.icon))
                        .withValues(Trigger.Type.values())
                        .displayOnlyValue()
                        .withInitialValue(trigger.type)
                        .withTooltip((type) -> Tooltip.create(localized(
                                "option", "trigger.type." + type + ".tooltip")))
                        .create(movingX, 0, list.tinyWidgetWidth, height, Component.empty(),
                                (button, type) -> {
                                    trigger.type = type;
                                    list.init();
                                });
                typeButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(typeButton);
                movingX += list.tinyWidgetWidth;

                if (keyTrigger) {
                    // Key selection button
                    Button keySelectButton = Button.builder(Component.literal("\uD83D\uDD0D"),
                                    (button) -> list.openKeyConfig(trigger))
                            .pos(movingX, 0)
                            .size(list.tinyWidgetWidth, height)
                            .build();
                    keySelectButton.setTooltip(Tooltip.create(localized(
                            "option", "trigger.open.key_selector.tooltip")));
                    keySelectButton.setTooltipDelay(Duration.ofMillis(500));
                    elements.add(keySelectButton);
                    movingX += list.tinyWidgetWidth;
                }

                // Trigger field
                TextField triggerField = new TextField(movingX, 0, triggerFieldWidth, height);
                if (trigger.type == Trigger.Type.REGEX) triggerField.regexValidator();
                triggerField.withValidator(new TextField.Validator.UniqueTrigger(
                        () -> Config.get().getNotifs(), (n) -> n.triggers, notif, trigger));
                triggerField.setMaxLength(240);
                triggerField.setResponder((str) -> trigger.string = str.strip());
                triggerField.setValue(trigger.string);
                triggerField.setTooltip(Tooltip.create(localized(
                        "option", "trigger.field.tooltip")));
                triggerField.setTooltipDelay(Duration.ofMillis(500));
                elements.add(triggerField);
                movingX += triggerFieldWidth;
                
                // Trigger editor button
                Button editorButton = Button.builder(Component.literal("\u270e"),
                                (button) -> list.openTriggerConfig(trigger))
                        .pos(movingX, 0)
                        .size(list.tinyWidgetWidth, height)
                        .build();
                editorButton.setTooltip(Tooltip.create(localized(
                        "option", "notif.open.trigger_editor.tooltip")));
                editorButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(editorButton);
                movingX += list.tinyWidgetWidth;

                // Style string add button
                Button styleButton = Button.builder(Component.literal("+"),
                                (button) -> {
                                    trigger.styleTarget.enabled = true;
                                    list.init();
                                })
                        .pos(movingX, 0)
                        .size(list.tinyWidgetWidth, height)
                        .build();
                if (!trigger.styleTarget.enabled) {
                    styleButton.setTooltip(Tooltip.create(localized(
                            "option", "trigger.style_target.add.tooltip")));
                    styleButton.setTooltipDelay(Duration.ofMillis(500));
                } else {
                    styleButton.active = false;
                }
                elements.add(styleButton);

                // Delete button
                elements.add(Button.builder(
                        Component.literal("\u274C").withStyle(ChatFormatting.RED),
                        (button) -> {
                            notif.triggers.remove(index);
                            list.init();
                        })
                        .pos(x + width + SPACE, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());
            }
        }

        private static class StyleTargetFieldEntry extends Entry {
            StyleTargetFieldEntry(int x, int width, int height, NotifOptionList list,
                                  StyleTarget styleTarget) {
                super();
                int stringFieldWidth = width - (list.tinyWidgetWidth * 4);
                int movingX = x + list.tinyWidgetWidth;

                // Info icon
                StringWidget infoIcon = new StringWidget(movingX, 0, list.tinyWidgetWidth, height,
                        Component.literal("\u2139"), Minecraft.getInstance().font);
                infoIcon.alignCenter();
                infoIcon.setTooltip(Tooltip.create(localized(
                        "option", "trigger.style_target.tooltip")));
                infoIcon.setTooltipDelay(Duration.ofMillis(500));
                elements.add(infoIcon);
                movingX += list.tinyWidgetWidth;

                // Type button
                CycleButton<StyleTarget.Type> typeButton = CycleButton.<StyleTarget.Type>builder(
                                (type) -> Component.literal(type.icon))
                        .withValues(StyleTarget.Type.values())
                        .displayOnlyValue()
                        .withInitialValue(styleTarget.type)
                        .withTooltip((type) -> Tooltip.create(localized(
                                "option", "trigger.style_target.type." + type + ".tooltip")))
                        .create(movingX, 0, list.tinyWidgetWidth, height, Component.empty(),
                                (button, type) -> {
                                    styleTarget.type = type;
                                    list.init();
                                });
                typeButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(typeButton);
                movingX += list.tinyWidgetWidth;

                // Style string field
                TextField stringField = new TextField(movingX, 0, stringFieldWidth, height);
                if (styleTarget.type == StyleTarget.Type.REGEX) stringField.regexValidator();
                stringField.setMaxLength(240);
                stringField.setValue(styleTarget.string);
                stringField.setResponder((string) -> styleTarget.string = string.strip());
                stringField.setTooltip(Tooltip.create(localized(
                        "option", "trigger.style_target.field.tooltip")));
                stringField.setTooltipDelay(Duration.ofMillis(500));
                elements.add(stringField);
                movingX = x + width - list.tinyWidgetWidth;

                // Delete button
                elements.add(Button.builder(
                        Component.literal("\u274C").withStyle(ChatFormatting.RED), 
                        (button) -> {
                            styleTarget.enabled = false;
                            list.init();
                        })
                        .pos(movingX, 0)
                        .size(list.tinyWidgetWidth, height)
                        .build());
            }
        }

        private static class SoundConfigEntry extends Entry {
            SoundConfigEntry(int x, int width, int height, Notification notif,
                             NotifOptionList list) {
                super();
                int statusButtonWidth = Math.max(24, height);
                int mainButtonWidth = width - statusButtonWidth - SPACE;

                // Sound GUI button
                elements.add(Button.builder(localized("option", "notif.sound", notif.sound.getId()),
                                (button) -> list.openSoundConfig())
                        .pos(x, 0)
                        .size(mainButtonWidth, height)
                        .build());

                // Status button
                elements.add(CycleButton.booleanBuilder(
                        CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                                CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED))
                        .displayOnlyValue()
                        .withInitialValue(notif.sound.isEnabled())
                        .create(x + width - statusButtonWidth, 0, statusButtonWidth, height,
                                Component.empty(), (button, status) -> notif.sound.setEnabled(status)));
            }
        }

        private static class FormatConfigEntry extends Entry {
            private FormatConfigEntry(int x, int width, int height, Notification notif, boolean first) {
                super();
                if (first) createFirst(x, width, height, notif);
                else createSecond(x, width, height, notif);
            }

            // Bold, italic, underline
            private void createFirst(int x, int width, int height, Notification notif) {
                int buttonWidth = (width - SPACE * 2) / 3;

                CycleButton<TextStyle.FormatMode> boldButton = 
                        CycleButton.<TextStyle.FormatMode>builder(
                                (state) -> getMessage(state, ChatFormatting.BOLD))
                                .withValues(TextStyle.FormatMode.values())
                                .withInitialValue(notif.textStyle.bold)
                                .withTooltip(this::getTooltip)
                                .create(x, 0, buttonWidth, height, 
                                        localized("option", "notif.format.bold"), 
                                        (button, state) -> notif.textStyle.bold = state);
                boldButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(boldButton);

                CycleButton<TextStyle.FormatMode> italicButton = 
                        CycleButton.<TextStyle.FormatMode>builder(
                                (state) -> getMessage(state, ChatFormatting.ITALIC))
                                .withValues(TextStyle.FormatMode.values())
                                .withInitialValue(notif.textStyle.italic)
                                .withTooltip(this::getTooltip)
                                .create(x + width / 2 - buttonWidth / 2, 0, buttonWidth, height,
                                        localized("option", "notif.format.italic"),
                                        (button, state) -> notif.textStyle.italic = state);
                italicButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(italicButton);

                CycleButton<TextStyle.FormatMode> underlineButton = 
                        CycleButton.<TextStyle.FormatMode>builder(
                                (state) -> getMessage(state, ChatFormatting.UNDERLINE))
                                .withValues(TextStyle.FormatMode.values())
                                .withInitialValue(notif.textStyle.underlined)
                                .withTooltip(this::getTooltip)
                                .create(x + width - buttonWidth, 0, buttonWidth, height,
                                        localized("option", "notif.format.underline"),
                                        (button, state) -> notif.textStyle.underlined = state);
                underlineButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(underlineButton);
            }

            // Strikethrough, obfuscate
            private void createSecond(int x, int width, int height, Notification notif) {
                int buttonWidth = (width - SPACE) / 2;

                CycleButton<TextStyle.FormatMode> strikethroughButton = 
                        CycleButton.<TextStyle.FormatMode>builder(
                                (state) -> getMessage(state, ChatFormatting.STRIKETHROUGH))
                                .withValues(TextStyle.FormatMode.values())
                                .withInitialValue(notif.textStyle.strikethrough)
                                .withTooltip(this::getTooltip)
                                .create(x, 0, buttonWidth, height,
                                        localized("option", "notif.format.strikethrough"),
                                        (button, state) -> notif.textStyle.strikethrough = state);
                strikethroughButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(strikethroughButton);

                CycleButton<TextStyle.FormatMode> obfuscateButton = 
                        CycleButton.<TextStyle.FormatMode>builder(
                                (state) -> getMessage(state, ChatFormatting.OBFUSCATED))
                                .withValues(TextStyle.FormatMode.values())
                                .withInitialValue(notif.textStyle.obfuscated)
                                .withTooltip(this::getTooltip)
                                .create(x + width - buttonWidth, 0, buttonWidth, height,
                                        localized("option", "notif.format.obfuscate"),
                                        (button, state) -> notif.textStyle.obfuscated = state);
                obfuscateButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(obfuscateButton);
            }

            private Component getMessage(TextStyle.FormatMode mode, ChatFormatting format) {
                return switch(mode) {
                    case ON -> CommonComponents.OPTION_ON.copy().withStyle(format)
                            .withStyle(ChatFormatting.GREEN);
                    case OFF -> CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED);
                    default -> Component.literal("/").withStyle(ChatFormatting.GRAY);
                };
            }

            private Tooltip getTooltip(TextStyle.FormatMode mode) {
                return mode.equals(TextStyle.FormatMode.DISABLED)
                        ? Tooltip.create(localized("option", "notif.format.disabled.tooltip"))
                        : null;
            }
        }

        static class ColorConfigEntry extends Entry {
            ColorConfigEntry(int x, int width, int height, OptionList list,
                             Supplier<Integer> supplier, Consumer<Integer> consumer,
                             Supplier<Boolean> statusSupplier, Consumer<Boolean> statusConsumer,
                             MutableComponent text) {
                this(x, width, height, list, supplier, consumer, statusSupplier, statusConsumer, text, true);
            }

            ColorConfigEntry(int x, int width, int height, OptionList list,
                             Supplier<Integer> supplier, Consumer<Integer> consumer,
                             Supplier<Boolean> statusSupplier, Consumer<Boolean> statusConsumer,
                             MutableComponent text, boolean showStatusButton) {
                super();
                int statusButtonWidth = Math.max(24, height);
                int colorFieldWidth = Minecraft.getInstance().font.width("#FFAAFF+++");
                int mainButtonWidth = width - colorFieldWidth - SPACE;
                if (showStatusButton) mainButtonWidth -= (statusButtonWidth + SPACE);

                // Color GUI button
                Button mainButton = Button.builder(text.withColor(supplier.get()),
                                (button) -> {
                                    int cpHeight = HsvColorPicker.MIN_HEIGHT;
                                    int cpWidth = HsvColorPicker.MIN_WIDTH;
                                    list.screen.setOverlay(new HsvColorPicker(
                                            x + width / 2 - cpWidth / 2,
                                            list.screen.height / 2 - cpHeight / 2,
                                            cpWidth, cpHeight,
                                            supplier, consumer,
                                            (widget) -> {
                                                list.screen.removeOverlayWidget();
                                                list.init();
                                            }));
                                })
                        .pos(x, 0)
                        .size(mainButtonWidth, height)
                        .build();
                elements.add(mainButton);

                // Hex code field
                TextField colorField = new TextField(x + mainButtonWidth + SPACE, 0,
                        colorFieldWidth, height);
                colorField.hexColorValidator().strict();
                colorField.setMaxLength(7);
                colorField.setResponder((val) -> {
                    TextColor textColor = ColorUtil.parseColor(val);
                    if (textColor != null) {
                        int color = textColor.getValue();
                        consumer.accept(color);
                        // Update color of main button and field
                        mainButton.setMessage(mainButton.getMessage().copy().withColor(color));
                        float[] hsv = new float[3];
                        Color.RGBtoHSB(ARGB.red(color), ARGB.green(color), ARGB.blue(color), hsv);
                        if (hsv[2] < 0.1) colorField.setTextColor(16777215);
                        else colorField.setTextColor(color);
                    }
                });
                colorField.setValue(TextColor.fromRgb(supplier.get()).formatValue());
                elements.add(colorField);

                if (showStatusButton) {
                    // Status button
                    elements.add(CycleButton.booleanBuilder(
                                    CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                                    CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED))
                            .displayOnlyValue()
                            .withInitialValue(statusSupplier.get())
                            .create(x + width - statusButtonWidth, 0, statusButtonWidth, height,
                                    Component.empty(), (button, status) -> statusConsumer.accept(status)));
                }
            }
        }
    }
}
