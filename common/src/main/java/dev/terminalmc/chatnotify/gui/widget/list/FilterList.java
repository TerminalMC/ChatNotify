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
import dev.terminalmc.chatnotify.gui.screen.NotifScreen;
import dev.terminalmc.chatnotify.gui.screen.OptionScreen;
import dev.terminalmc.chatnotify.gui.screen.TriggerScreen;
import dev.terminalmc.chatnotify.gui.widget.ConfirmButton;
import dev.terminalmc.chatnotify.gui.widget.HsvColorPicker;
import dev.terminalmc.chatnotify.gui.widget.RightClickableButton;
import dev.terminalmc.chatnotify.gui.widget.field.DropdownTextField;
import dev.terminalmc.chatnotify.gui.widget.field.FakeTextField;
import dev.terminalmc.chatnotify.gui.widget.field.MultiLineTextField;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import dev.terminalmc.chatnotify.mixin.accessor.KeyAccessor;
import dev.terminalmc.chatnotify.mixin.accessor.TextColorAccessor;
import dev.terminalmc.chatnotify.util.Unicode;
import dev.terminalmc.chatnotify.util.functional.StringSupplier;
import dev.terminalmc.chatnotify.util.text.ColorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static dev.terminalmc.chatnotify.util.Localization.localized;

public class FilterList<E extends StringSupplier> extends DragReorderList {

    private String filterString = "";
    private @Nullable Pattern filterPattern = null;
    private OptionList.Entry.ActionButton addButtonEntry;

    private final Runnable tabNameUpdate;
    private final Component title;
    private final Component titleTooltip;
    private final @Nullable Supplier<Boolean> statusSupplier;
    private final @Nullable Consumer<Boolean> statusConsumer;
    private final Supplier<List<E>> listSupplier;
    private final EntrySupplier<E> entrySupplier;
    private final @Nullable TrailerSupplier<E> trailerSupplier;

    public FilterList(
            Minecraft mc,
            OptionScreen screen,
            int width,
            int height,
            int y,
            int entryWidth,
            int entryHeight,
            int entrySpacing,
            Class<? extends Entry.ListEntry> entryClass,
            BiFunction<Integer, Integer, Boolean> moveFunction,
            Runnable tabNameUpdate,
            Component title,
            Component titleTooltip,
            @Nullable Supplier<Boolean> statusSupplier,
            @Nullable Consumer<Boolean> statusConsumer,
            Supplier<List<E>> listSupplier,
            EntrySupplier<E> entrySupplier,
            @Nullable TrailerSupplier<E> trailerSupplier,
            Runnable addRunnable
    ) {
        super(
                mc,
                screen,
                width,
                height,
                y,
                entryWidth,
                entryHeight,
                entrySpacing,
                new HashMap<>(Map.of(entryClass, moveFunction))
        );
        this.tabNameUpdate = tabNameUpdate;
        this.title = title;
        this.titleTooltip = titleTooltip;
        this.statusSupplier = statusSupplier;
        this.statusConsumer = statusConsumer;
        this.listSupplier = listSupplier;
        this.entrySupplier = entrySupplier;
        this.trailerSupplier = trailerSupplier;

        addButtonEntry = new OptionList.Entry.ActionButton(
                entryX, entryWidth, entryHeight, Component.literal("+"), null, -1, (button) -> {
            addRunnable.run();
            tabNameUpdate.run();
            filterString = "";
            filterPattern = null;
            init();
            scrollToEntry(addButtonEntry);
        }
        );
    }

    @Override
    protected void addEntries() {
        addEntry(new Entry.ListHeader(dynEntryX, dynEntryWidth, entryHeight, this));

        refreshSubList();
        addButtonEntry.setBounds(entryX, entryWidth, entryHeight);
        addEntry(addButtonEntry);
    }

    protected void refreshSubList() {
        List<OptionList.Entry> entries = new ArrayList<>(children());
        Iterator<OptionList.Entry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            OptionList.Entry entry = iterator.next();
            if (entry instanceof Entry.SpacedListEntry) {
                iterator.remove();
                iterator.next();
                iterator.remove();
            } else if (entry instanceof Entry.ListEntry
                    || entry instanceof Entry.ListEntryTrailer) {
                iterator.remove();
            }
        }
        // Get list start index
        int start = entries.indexOf(addButtonEntry);
        if (start == -1)
            start = entries.size();
        // Add in reverse order
        List<E> list = listSupplier.get();
        for (int i = list.size() - 1; i >= 0; i--) {
            E e = list.get(i);
            if (filterPattern == null || filterPattern.matcher(e.getString()).find()) {
                Entry entry = entrySupplier.get(
                        dynWideEntryX,
                        dynWideEntryWidth,
                        entryHeight,
                        this,
                        e,
                        list.indexOf(e)
                );
                if (entry instanceof Entry.SpacedListEntry) {
                    entries.add(start, new OptionList.Entry.Space(entry));
                } else if (trailerSupplier != null) {
                    Entry trailer = trailerSupplier.get(
                            dynWideEntryX,
                            dynWideEntryWidth,
                            entryHeight,
                            this,
                            e
                    );
                    if (trailer != null)
                        entries.add(start, trailer);
                }
                entries.add(start, entry);
            }
        }
        replaceEntries(entries);
        tabNameUpdate.run();
        refreshScrollAmount();
    }

    @FunctionalInterface
    public interface EntrySupplier<E extends StringSupplier> {

        Entry.ListEntry get(int x, int width, int height, FilterList<?> list, E e, int index);
    }

    @FunctionalInterface
    public interface TrailerSupplier<E extends StringSupplier> {

        @Nullable Entry.ListEntryTrailer get(int x, int width, int height, FilterList<?> list, E e);
    }

    public abstract static class Entry extends OptionList.Entry {

        private static class ListHeader extends Entry {

            ListHeader(int x, int width, int height, FilterList<?> list) {
                super();
                boolean hasStatus = list.statusSupplier != null && list.statusConsumer != null;

                int cappedWidth = Math.min(width, OptionScreen.BASE_ROW_WIDTH);
                if (cappedWidth < width) {
                    x += (width - cappedWidth) / 2;
                    width = cappedWidth;
                }

                int titleWidth = Minecraft.getInstance().font.width(list.title) + 8;
                int statusButtonWidth = 25;
                int searchFieldMinWidth = 50;
                int searchFieldWidth = width - titleWidth - SPACE;
                if (hasStatus)
                    searchFieldWidth -= statusButtonWidth + SPACE;
                if (searchFieldWidth < searchFieldMinWidth) {
                    int diff = searchFieldMinWidth - searchFieldWidth;
                    searchFieldWidth += diff;
                    titleWidth -= diff;
                }
                int movingX = x;

                StringWidget titleWidget =
                        new StringWidget(movingX, 0, titleWidth, height, list.title, list.mc.font);
                titleWidget.setTooltip(Tooltip.create(list.titleTooltip));
                elements.add(titleWidget);
                movingX += titleWidth + SPACE;

                if (hasStatus) {
                    elements.add(CycleButton.booleanBuilder(
                                    CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                                    CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED)
                            )
                            .displayOnlyValue()
                            .withInitialValue(list.statusSupplier.get())
                            .create(
                                    movingX,
                                    0,
                                    statusButtonWidth,
                                    height,
                                    Component.empty(),
                                    (button, status) -> {
                                        list.statusConsumer.accept(status);
                                        list.tabNameUpdate.run();
                                    }
                            ));
                    movingX += statusButtonWidth + SPACE;
                }

                TextField searchField = new TextField(movingX, 0, searchFieldWidth, height);
                searchField.setMaxLength(64);
                searchField.setHint(localized("common", "search"));
                searchField.setValue(list.filterString);
                searchField.setResponder((str) -> {
                    list.filterString = str;
                    if (str.isBlank()) {
                        list.filterPattern = null;
                    } else {
                        list.filterPattern = Pattern.compile("(?iU)" + Pattern.quote(str));
                    }
                    list.refreshSubList();
                });
                elements.add(searchField);
            }
        }

        public abstract static class ListEntry extends Entry {

        }

        public abstract static class SpacedListEntry extends ListEntry {

        }

        public abstract static class ListEntryTrailer extends Entry {

        }

        /**
         * A text field and buttons for configuration of a {@link Trigger}.
         */
        public static class TriggerOptions extends ListEntry {

            public TriggerOptions(
                    int x,
                    int width,
                    int height,
                    FilterList<?> list,
                    Trigger trigger,
                    TextStyle textStyle,
                    int index,
                    Consumer<Integer> removeFunction,
                    TextField.Validator validator,
                    boolean canUseStyleTarget
            ) {
                super();
                Minecraft mc = Minecraft.getInstance();
                int triggerFieldWidth = width - list.tinyWidgetWidth * 2;
                if (canUseStyleTarget)
                    triggerFieldWidth -= list.tinyWidgetWidth;
                int movingX = x;

                // Index indicator
                Button indicatorButton = Button.builder(
                                Component.literal(String.valueOf(index + 1)), (button) -> {
                                }
                        )
                        .pos(x - list.smallWidgetWidth - SPACE - list.tinyWidgetWidth, 0)
                        .size(list.tinyWidgetWidth, height)
                        .build();
                indicatorButton.active = false;
                elements.add(indicatorButton);

                // Drag reorder button
                Button dragButton = Button.builder(
                                Component.literal(Unicode.UP_DOWN.str), (button) -> {
                                    this.setDragging(true);
                                    list.startDragging(
                                            this,
                                            StyleTargetOptions.class,
                                            trigger.styleTarget.enabled
                                    );
                                }
                        )
                        .pos(x - list.smallWidgetWidth - SPACE, 0)
                        .size(list.smallWidgetWidth, height)
                        .build();
                dragButton.active = list.filterPattern == null;
                elements.add(dragButton);

                // Type button
                CycleButton<Trigger.Type> typeButton =
                        CycleButton.<Trigger.Type>builder((type) -> Component.literal(type.icon))
                                .withValues(Trigger.Type.values())
                                .displayOnlyValue()
                                .withInitialValue(trigger.type)
                                .withTooltip((type) -> Tooltip.create(localized(
                                        "option",
                                        "notif.trigger.type." + type + ".tooltip"
                                )))
                                .create(
                                        movingX,
                                        0,
                                        list.tinyWidgetWidth,
                                        height,
                                        Component.empty(),
                                        (button, type) -> {
                                            trigger.type = type;
                                            list.init();
                                        }
                                );
                typeButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(typeButton);
                movingX += list.tinyWidgetWidth;

                // Trigger editor button
                Button editorButton = Button.builder(
                                Component.literal(Unicode.EDIT.str),
                                (button) -> mc.setScreen(new TriggerScreen(
                                        mc.screen,
                                        trigger,
                                        textStyle,
                                        () -> {
                                        },
                                        TriggerScreen.TabKey.TRIGGER_EDITOR.key
                                ))
                        )
                        .pos(movingX, 0)
                        .size(list.tinyWidgetWidth, height)
                        .build();
                editorButton.setTooltip(Tooltip.create(localized(
                        "option",
                        "notif.trigger.open.trigger_editor.tooltip"
                )));
                editorButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(editorButton);
                movingX += list.tinyWidgetWidth;

                // Trigger field
                TextField triggerField = new TextField(movingX, 0, triggerFieldWidth, height);
                triggerField.withValidator(validator);
                if (trigger.type == Trigger.Type.REGEX)
                    triggerField.regexValidator();
                triggerField.setMaxLength(240);
                triggerField.setResponder((str) -> trigger.string = str.strip());
                triggerField.setValue(trigger.string);
                triggerField.setHint(localized("option", "notif.trigger.field.hint"));
                elements.add(triggerField);
                movingX += triggerFieldWidth;

                if (canUseStyleTarget) {
                    // Style string add button
                    Button styleButton = Button.builder(
                                    Component.literal("+"), (button) -> {
                                        trigger.styleTarget.enabled = true;
                                        list.init();
                                    }
                            )
                            .pos(movingX, 0)
                            .size(list.tinyWidgetWidth, height)
                            .build();
                    if (!trigger.styleTarget.enabled) {
                        styleButton.setTooltip(Tooltip.create(localized(
                                "option",
                                "notif.trigger.style_target.add.tooltip"
                        )));
                        styleButton.setTooltipDelay(Duration.ofMillis(500));
                    } else {
                        styleButton.active = false;
                    }
                    elements.add(styleButton);
                }

                // Delete button
                elements.add(Button.builder(
                                Component.literal(Unicode.CROSS.str).withStyle(ChatFormatting.RED),
                                (button) -> {
                                    removeFunction.accept(index);
                                    list.init();
                                }
                        )
                        .pos(x + width + SPACE, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());
            }
        }

        /**
         * A non-editable text field for display of a locked {@link Trigger}.
         */
        public static class LockedTriggerOptions extends ListEntry {

            public LockedTriggerOptions(
                    int x,
                    int width,
                    int height,
                    Trigger trigger,
                    Component tooltip
            ) {
                super();
                TextField displayField = new TextField(x, 0, width, height);
                displayField.setValue(trigger.string);
                displayField.setTooltip(Tooltip.create(tooltip));
                displayField.setTooltipDelay(Duration.ofMillis(500));
                displayField.setEditable(false);
                displayField.active = false;
                elements.add(displayField);
            }
        }

        /**
         * A text field and buttons for configuration of a {@link StyleTarget}.
         */
        public static class StyleTargetOptions extends ListEntryTrailer {

            public StyleTargetOptions(
                    int x,
                    int width,
                    int height,
                    FilterList<?> list,
                    StyleTarget styleTarget
            ) {
                super();
                int stringFieldWidth = width - (list.tinyWidgetWidth * 4);
                int movingX = x + list.tinyWidgetWidth;

                // Info icon
                StringWidget infoIcon = new StringWidget(
                        movingX,
                        0,
                        list.tinyWidgetWidth,
                        height,
                        Component.literal(Unicode.INFO.str),
                        Minecraft.getInstance().font
                );
                infoIcon.setTooltip(Tooltip.create(localized(
                        "option",
                        "notif.trigger.style_target.tooltip"
                )));
                infoIcon.setTooltipDelay(Duration.ofMillis(500));
                elements.add(infoIcon);
                movingX += list.tinyWidgetWidth;

                // Type button
                CycleButton<StyleTarget.Type> typeButton =
                        CycleButton.<StyleTarget.Type>builder((type) -> Component.literal(type.icon))
                                .withValues(StyleTarget.Type.values())
                                .displayOnlyValue()
                                .withInitialValue(styleTarget.type)
                                .withTooltip((type) -> Tooltip.create(localized(
                                        "option",
                                        "notif.trigger.style_target.type." + type + ".tooltip"
                                )))
                                .create(
                                        movingX,
                                        0,
                                        list.tinyWidgetWidth,
                                        height,
                                        Component.empty(),
                                        (button, type) -> {
                                            styleTarget.type = type;
                                            list.init();
                                        }
                                );
                typeButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(typeButton);
                movingX += list.tinyWidgetWidth;

                // Style string field
                TextField stringField = new TextField(movingX, 0, stringFieldWidth, height);
                if (styleTarget.type == StyleTarget.Type.REGEX)
                    stringField.regexValidator();
                stringField.setMaxLength(240);
                stringField.setValue(styleTarget.string);
                stringField.setResponder((string) -> styleTarget.string = string.strip());
                stringField.setHint(localized("option", "notif.trigger.style_target.field.hint"));
                elements.add(stringField);
                movingX = x + width - list.tinyWidgetWidth;

                // Delete button
                elements.add(Button.builder(
                                Component.literal(Unicode.CROSS.str).withStyle(ChatFormatting.RED),
                                (button) -> {
                                    styleTarget.enabled = false;
                                    list.init();
                                }
                        )
                        .pos(movingX, 0)
                        .size(list.tinyWidgetWidth, height)
                        .build());
            }
        }

        /**
         * Text fields for configuration of a {@link Response}.
         */
        public static class ResponseOptions extends SpacedListEntry {

            public ResponseOptions(
                    int x,
                    int width,
                    int height,
                    FilterList<?> list,
                    Response message,
                    int index,
                    Consumer<Integer> removeFunction
            ) {
                super();
                int fieldSpacing = 1;
                int timeFieldWidth = Minecraft.getInstance().font.width("00000");
                int msgFieldWidth =
                        width - timeFieldWidth * 2 - list.tinyWidgetWidth - fieldSpacing * 2;
                int movingX = x;

                // Drag reorder button
                elements.add(Button.builder(
                                Component.literal(Unicode.UP_DOWN.str), (button) -> {
                                    this.setDragging(true);
                                    list.startDragging(this, null, false);
                                }
                        )
                        .pos(x - list.smallWidgetWidth - SPACE, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());

                // Type button
                CycleButton<Response.Type> typeButton =
                        CycleButton.<Response.Type>builder((type) -> Component.literal(type.icon))
                                .withValues(Response.Type.values())
                                .displayOnlyValue()
                                .withInitialValue(message.type)
                                .withTooltip((type) -> Tooltip.create(localized(
                                        "option",
                                        "notif.response.type." + type.name() + ".tooltip"
                                )))
                                .create(
                                        movingX,
                                        0,
                                        list.tinyWidgetWidth,
                                        height,
                                        Component.empty(),
                                        (button, type) -> {
                                            message.type = type;
                                            list.init();
                                        }
                                );
                typeButton.setTooltipDelay(Duration.ofMillis(500));
                elements.add(typeButton);
                movingX += list.tinyWidgetWidth + fieldSpacing;

                if (message.type.equals(Response.Type.COMMANDKEYS)) {
                    int keyFieldWidth = msgFieldWidth / 2;
                    List<String> keys =
                            KeyAccessor.chatnotify$getNameMap().keySet().stream().sorted().toList();
                    FakeTextField keyField1 = new FakeTextField(
                            movingX, 0, keyFieldWidth, height, () -> {
                        int wHeight = Math.max(DropdownTextField.MIN_HEIGHT, list.height);
                        int wWidth = Math.max(DropdownTextField.MIN_WIDTH, list.dynWideEntryWidth);
                        int wX = x + (width / 2) - (wWidth / 2);
                        int wY = list.getY();
                        list.screen.setOverlayWidget(new DropdownTextField(
                                wX,
                                wY,
                                wWidth,
                                wHeight,
                                Component.empty(),
                                () -> message.string.matches(".+-.+")
                                        ? message.string.split("-")[0]
                                        : "",
                                (val) -> message.string =
                                        val + "-" + (message.string.matches(".+-.+")
                                                ? message.string.split("-")[1]
                                                : "key.keyboard.unknown"),
                                (widget) -> list.init(),
                                keys
                        ));
                    }
                    );
                    MutableComponent label1 =
                            localized("option", "notif.response.commandkeys.limit_key");
                    keyField1.setHint(label1.copy());
                    keyField1.setTooltip(Tooltip.create(label1.append("\n\n")
                            .append(localized(
                                    "option",
                                    "notif.response.commandkeys.limit_key.tooltip"
                            ))));
                    keyField1.setMaxLength(240);
                    keyField1.withValidator(new TextField.Validator.InputKey(keys));
                    keyField1.setValue(message.string.matches(".+-.+")
                            ? message.string.split("-")[0]
                            : "");
                    elements.add(keyField1);
                    movingX += keyFieldWidth;
                    FakeTextField keyField2 = new FakeTextField(
                            movingX, 0, keyFieldWidth, height, () -> {
                        int wHeight = Math.max(DropdownTextField.MIN_HEIGHT, list.height);
                        int wWidth = Math.max(DropdownTextField.MIN_WIDTH, list.dynWideEntryWidth);
                        int wX = x + (width / 2) - (wWidth / 2);
                        int wY = list.getY();
                        list.screen.setOverlayWidget(new DropdownTextField(
                                wX,
                                wY,
                                wWidth,
                                wHeight,
                                Component.empty(),
                                () -> message.string.matches(".+-.+")
                                        ? message.string.split("-")[1]
                                        : "",
                                (val) -> message.string = (message.string.matches(".+-.+")
                                        ? message.string.split("-")[0] + "-" + val
                                        : "key.keyboard.unknown"),
                                (widget) -> list.init(),
                                keys
                        ));
                    }
                    );
                    MutableComponent label2 = localized("option", "notif.response.commandkeys.key");
                    keyField2.setHint(label2.copy());
                    keyField2.setTooltip(Tooltip.create(label2.append("\n\n")
                            .append(localized(
                                    "option",
                                    "notif.response.commandkeys.key.tooltip"
                            ))));
                    keyField2.setMaxLength(240);
                    keyField2.withValidator(new TextField.Validator.InputKey(keys));
                    keyField2.setValue(message.string.matches(".+-.+")
                            ? message.string.split("-")[1]
                            : "");
                    elements.add(keyField2);
                } else {
                    // Response field
                    MultiLineTextField msgField =
                            new MultiLineTextField(movingX, 0, msgFieldWidth, height * 2);
                    msgField.setCharacterLimit(256);
                    msgField.setValue(message.string);
                    msgField.setValueListener((val) -> message.string = val.strip());
                    elements.add(msgField);
                }

                // Delay field
                TextField timeField =
                        new TextField(x + width - timeFieldWidth * 2, 0, timeFieldWidth, height);
                timeField.posIntValidator().strict();
                timeField.setTooltip(Tooltip.create(localized(
                        "option",
                        "notif.response.time.tooltip"
                )));
                timeField.setTooltipDelay(Duration.ofMillis(500));
                timeField.setMaxLength(5);
                timeField.setResponder((s) -> message.delayTicks = Integer.parseInt(s.strip()));
                timeField.setValue(String.valueOf(message.delayTicks));
                elements.add(timeField);

                // Cooldown field
                TextField cooldownField =
                        new TextField(x + width - timeFieldWidth, 0, timeFieldWidth, height);
                cooldownField.posIntValidator().strict();
                cooldownField.setTooltip(Tooltip.create(localized(
                        "option",
                        "notif.response.cooldown.tooltip"
                )));
                cooldownField.setTooltipDelay(Duration.ofMillis(500));
                cooldownField.setMaxLength(5);
                cooldownField.setResponder((s) ->
                        message.cooldownTicks = Integer.parseInt(s.strip()));
                cooldownField.setValue(String.valueOf(message.cooldownTicks));
                elements.add(cooldownField);

                // Delete button
                elements.add(Button.builder(
                                Component.literal(Unicode.CROSS.str).withStyle(ChatFormatting.RED),
                                (button) -> {
                                    removeFunction.accept(index);
                                    list.init();
                                }
                        )
                        .pos(x + width + SPACE, 0)
                        .size(list.smallWidgetWidth, height)
                        .build());
            }
        }

        /**
         * A set of widgets for superficial configuration of a {@link Notification}.
         */
        public static class NotifOptions extends ListEntry {

            public NotifOptions(
                    int x,
                    int width,
                    int height,
                    FilterList<?> list,
                    Notification notif,
                    int index
            ) {
                super();
                Minecraft mc = Minecraft.getInstance();

                @Nullable Trigger trigger =
                        notif.triggers.size() == 1 ? notif.triggers.getFirst() : null;
                boolean singleTrig = trigger != null;

                int statusButtonWidth = 24;
                int minFieldWidth = mc.font.width("#FFAAFF++"); // default 54
                int soundFieldWidth = minFieldWidth;
                int soundFieldWidthMax = mc.font.width("block.note_block.chime++"); // default 122

                boolean showColorField = false;
                boolean showColorFieldNominal = notif.textStyle.doColor;
                boolean showSoundField = false;
                boolean showSoundFieldNominal = notif.sound.isEnabled();

                int triggerWidth = width
                        - SPACE_SMALL
                        - list.smallWidgetWidth // More options
                        - SPACE_SMALL
                        - list.tinyWidgetWidth // Color toggle
                        - SPACE_SMALL
                        - list.tinyWidgetWidth // Sound toggle
                        - SPACE_SMALL
                        - statusButtonWidth; // Status toggle

                // Other fields should not be shown if trigger would be less
                int triggerWidthNominalMin = 220;
                int excess = triggerWidth - triggerWidthNominalMin;
                if (excess > minFieldWidth) {
                    // Preference sound field
                    if (showSoundFieldNominal) {
                        showSoundField = true;
                        triggerWidth -= soundFieldWidth;
                    }
                }
                excess = triggerWidth - triggerWidthNominalMin;
                if (excess > minFieldWidth) {
                    // Color field
                    if (showColorFieldNominal) {
                        showColorField = true;
                        triggerWidth -= minFieldWidth;
                    }
                }

                // If sound field is enabled, split the trigger's excess over
                // threshold between trigger and sound
                if (showSoundField) {
                    excess = triggerWidth - triggerWidthNominalMin;
                    triggerWidth -= excess;

                    // Up to 100, sound takes 70%
                    int soundBonus = (int) (excess * 0.7);
                    // Above 100, sound takes 35% (return 50% of extra)
                    int soundMargin = Math.max(0, soundFieldWidth + soundBonus - 100);
                    soundBonus -= (int) (soundMargin * 0.5);
                    // Above sound max, sound takes nothing (return 100% of extra)
                    soundMargin = Math.max(0, soundFieldWidth + soundBonus - soundFieldWidthMax);
                    soundBonus -= soundMargin;

                    soundFieldWidth += soundBonus;
                    triggerWidth += (excess - soundBonus);
                }

                int triggerFieldWidth = triggerWidth;
                if (singleTrig)
                    triggerFieldWidth -= (list.tinyWidgetWidth * 2);
                int movingX = x;

                if (index != 0) {
                    // Index indicator
                    Button indicatorButton = Button.builder(
                                    Component.literal(String.valueOf(index + 1)), (button) -> {
                                    }
                            )
                            .pos(x - list.smallWidgetWidth - SPACE - list.tinyWidgetWidth, 0)
                            .size(list.tinyWidgetWidth, height)
                            .build();
                    indicatorButton.active = false;
                    elements.add(indicatorButton);

                    // Drag reorder button (left-side extension)
                    Button dragButton = Button.builder(
                                    Component.literal(Unicode.UP_DOWN.str), (button) -> {
                                        this.setDragging(true);
                                        list.startDragging(this, null, false);
                                    }
                            )
                            .pos(x - list.smallWidgetWidth - SPACE, 0)
                            .size(list.smallWidgetWidth, height)
                            .build();
                    dragButton.active = list.filterPattern == null;
                    elements.add(dragButton);
                }

                if (singleTrig) {
                    // Type button
                    CycleButton<Trigger.Type> typeButton =
                            CycleButton.<Trigger.Type>builder((type) -> Component.literal(type.icon))
                                    .withValues(Trigger.Type.values())
                                    .displayOnlyValue()
                                    .withInitialValue(trigger.type)
                                    .withTooltip((type) -> Tooltip.create(localized(
                                            "option",
                                            "notif.trigger.type." + type + ".tooltip"
                                    )))
                                    .create(
                                            movingX,
                                            0,
                                            list.tinyWidgetWidth,
                                            height,
                                            Component.empty(),
                                            (button, type) -> {
                                                trigger.type = type;
                                                list.init();
                                            }
                                    );
                    typeButton.setTooltipDelay(Duration.ofMillis(200));
                    elements.add(typeButton);
                    movingX += list.tinyWidgetWidth;

                    // Trigger editor button
                    Button editorButton = Button.builder(
                                    Component.literal(Unicode.EDIT.str), (button) -> {
                                        notif.editing = true;
                                        mc.setScreen(new TriggerScreen(
                                                mc.screen,
                                                trigger,
                                                notif.textStyle,
                                                () -> notif.editing = false,
                                                TriggerScreen.TabKey.TRIGGER_EDITOR.key
                                        ));
                                    }
                            )
                            .pos(movingX, 0)
                            .size(list.tinyWidgetWidth, height)
                            .build();
                    editorButton.setTooltip(Tooltip.create(localized(
                            "option",
                            "notif.trigger.open.trigger_editor.tooltip"
                    )));
                    editorButton.setTooltipDelay(Duration.ofMillis(200));
                    elements.add(editorButton);
                    movingX += list.tinyWidgetWidth;
                }

                // Trigger field
                TextField triggerField;
                if (singleTrig) {
                    triggerField = new TextField(movingX, 0, triggerFieldWidth, height);
                    if (trigger.type == Trigger.Type.REGEX)
                        triggerField.regexValidator();
                    triggerField.withValidator(new TextField.Validator.UniqueTrigger(
                            notif,
                            trigger
                    ));
                    triggerField.setMaxLength(240);
                    triggerField.setResponder((str) -> trigger.string = str.strip());
                    triggerField.setValue(trigger.string);
                    triggerField.setHint(localized("option", "notif.trigger.field.hint"));
                } else {
                    triggerField = new FakeTextField(
                            movingX,
                            0,
                            triggerFieldWidth,
                            height,
                            () -> mc.setScreen(new NotifScreen(mc.screen, notif))
                    );
                    triggerField.setMaxLength(240);
                    triggerField.setValue(createLabel(notif, triggerFieldWidth - 10).getString());
                }
                elements.add(triggerField);
                movingX += triggerFieldWidth + SPACE_SMALL;

                // Options button

                ImageButton editButton = new ImageButton(
                        movingX,
                        0,
                        list.smallWidgetWidth,
                        height,
                        OPTION_SPRITES,
                        (button) -> mc.setScreen(new NotifScreen(mc.screen, notif))
                );
                editButton.setTooltip(Tooltip.create(localized(
                        "option",
                        "notif.open.options.tooltip"
                )));
                editButton.setTooltipDelay(Duration.ofMillis(200));
                elements.add(editButton);
                movingX += list.smallWidgetWidth + SPACE_SMALL;

                // Color

                RightClickableButton colorEditButton = new RightClickableButton(
                        movingX,
                        0,
                        list.tinyWidgetWidth,
                        height,
                        Component.literal(Unicode.PAINT.str)
                                .withColor(notif.textStyle.doColor
                                        ? notif.textStyle.color
                                        : 0xFFffffff),
                        (button) -> {
                            // Open color picker overlay widget
                            int cpHeight = HsvColorPicker.MIN_HEIGHT;
                            int cpWidth = HsvColorPicker.MIN_WIDTH;
                            list.screen.setOverlayWidget(new HsvColorPicker(
                                    x + width / 2 - cpWidth / 2,
                                    list.screen.height / 2 - cpHeight / 2,
                                    cpWidth,
                                    cpHeight,
                                    () -> notif.textStyle.color,
                                    (color) -> notif.textStyle.color = color,
                                    (widget) -> list.init()
                            ));
                        },
                        (button) -> {
                            // Toggle color
                            notif.textStyle.doColor = !notif.textStyle.doColor;
                            list.init();
                        }
                );
                colorEditButton.setTooltip(Tooltip.create(localized(
                        "option",
                        "notif.color.status.tooltip." + (notif.textStyle.doColor
                                ? "enabled"
                                : "disabled")
                )
                        .append("\n")
                        .append(localized("option", "notif.click_edit"))));
                colorEditButton.setTooltipDelay(Duration.ofMillis(200));
                if (showColorField) {
                    TextField colorField = new TextField(movingX, 0, minFieldWidth, height);
                    colorField.hexColorValidator().strict();
                    colorField.setMaxLength(7);
                    colorField.setResponder((val) -> {
                        TextColor textColor = ColorUtil.parseColor(val);
                        if (textColor != null) {
                            int color = textColor.getValue();
                            notif.textStyle.color = color;
                            float[] hsv = new float[3];
                            Color.RGBtoHSB(
                                    ARGB.red(color),
                                    ARGB.green(color),
                                    ARGB.blue(color),
                                    hsv
                            );
                            if (hsv[2] < 0.1)
                                colorField.setTextColor(0xFFFFFFFF);
                            else
                                colorField.setTextColor((0xFF << 24) | color);
                            // Update status button color
                            colorEditButton.setMessage(colorEditButton.getMessage()
                                    .copy()
                                    .withColor(color));
                        }
                    });
                    colorField.setValue(((TextColorAccessor) (Object) TextColor.fromRgb(notif.textStyle.color)).chatnotify$formatValue());
                    colorField.setTooltip(Tooltip.create(localized(
                            "option",
                            "notif.color.field.tooltip"
                    )));
                    colorField.setTooltipDelay(Duration.ofMillis(500));
                    elements.add(colorField);
                    movingX += minFieldWidth;
                }
                colorEditButton.setPosition(movingX, 0);
                elements.add(colorEditButton);
                movingX += list.tinyWidgetWidth + SPACE_SMALL;

                // Sound

                if (showSoundField) {
                    TextField soundField = new TextField(movingX, 0, soundFieldWidth, height);
                    soundField.soundValidator();
                    soundField.setMaxLength(240);
                    soundField.setResponder(notif.sound::setId);
                    soundField.setValue(notif.sound.getId());
                    soundField.setTooltip(Tooltip.create(localized(
                            "option",
                            "notif.sound.field.tooltip"
                    )));
                    soundField.setTooltipDelay(Duration.ofMillis(500));
                    elements.add(soundField);
                    movingX += soundFieldWidth;
                }
                RightClickableButton soundEditButton = new RightClickableButton(
                        movingX,
                        0,
                        list.tinyWidgetWidth,
                        height,
                        Component.literal(Unicode.SOUND.str)
                                .withStyle(notif.sound.isEnabled()
                                        ? ChatFormatting.WHITE
                                        : ChatFormatting.RED),
                        (button) -> mc.setScreen(new NotifScreen(
                                mc.screen,
                                notif,
                                NotifScreen.TabKey.SOUND.key
                        )),
                        (button) -> {
                            // Toggle sound
                            notif.sound.setEnabled(!notif.sound.isEnabled());
                            list.init();
                        }
                );
                soundEditButton.setTooltip(Tooltip.create(localized(
                        "option",
                        "notif.sound.status.tooltip." + (notif.sound.isEnabled()
                                ? "enabled"
                                : "disabled")
                )
                        .append("\n")
                        .append(localized("option", "notif.click_edit"))));
                soundEditButton.setTooltipDelay(Duration.ofMillis(200));
                elements.add(soundEditButton);

                // On/off button
                elements.add(CycleButton.booleanBuilder(
                        CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                        CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED)
                ).displayOnlyValue().withInitialValue(notif.enabled).create(
                        x + width - statusButtonWidth,
                        0,
                        statusButtonWidth,
                        height,
                        Component.empty(),
                        (button, status) -> {
                            notif.enabled = status;
                            // Update trigger duplicate indicators
                            list.refreshSubList();
                        }
                ));

                if (index != 0) {
                    // Delete button (right-side extension)
                    elements.add(new ConfirmButton(
                            x + width + SPACE,
                            0,
                            list.smallWidgetWidth,
                            height,
                            Component.literal(Unicode.CROSS.str),
                            Component.literal(Unicode.CROSS.str).withStyle(ChatFormatting.RED),
                            (button) -> {
                                if (Config.get().removeNotif(index)) {
                                    list.init();
                                }
                            }
                    ));
                }
            }

            // Utility methods to create a preview label for notifications with
            // multiple triggers

            private static MutableComponent createLabel(Notification notif, int maxWidth) {
                MutableComponent label;
                Font font = Minecraft.getInstance().font;
                String separator = ", ";
                String plusNumFormat = " [+%d]";
                Pattern plusNumPattern = Pattern.compile(" \\[\\+\\d+]");

                if (notif.triggers.isEmpty() || notif.triggers.getFirst().string.isBlank()) {
                    label = Component.literal("> ")
                            .withStyle(ChatFormatting.YELLOW)
                            .append(localized("option", "notif.label.configure")
                                    .withStyle(ChatFormatting.WHITE))
                            .append(" <");
                } else {
                    Set<String> usedStrings = new TreeSet<>();
                    List<String> strList = new ArrayList<>();

                    // Compile the first few triggers, ignoring duplicates
                    int cap = 10;
                    int i = 0;
                    for (Trigger trig : notif.triggers) {
                        String str = StringUtil.stripColor(trig.string);
                        if (!usedStrings.contains(str)) {
                            strList.add(i == 0 ? str : separator + str);
                            usedStrings.add(str);
                        }
                        if (++i >= cap)
                            break;
                    }
                    // If we have an excess of triggers, append the number
                    if (notif.triggers.size() > cap) {
                        strList.add(String.format(plusNumFormat, notif.triggers.size() - cap));
                    }

                    // Trim label by deleting triggers from the end until it's
                    // small enough. Not the most efficient, but we're capped.
                    while (font.width(compileLabel(strList)) > maxWidth && (strList.size() != 1
                            && !(strList.size() == 2 && plusNumPattern.matcher(strList.getLast())
                            .matches()))) {
                        // Remove the number (if any) and the last trigger
                        if (plusNumPattern.matcher(strList.removeLast()).matches()) {
                            strList.removeLast();
                        }
                        // Add the new number
                        strList.add(String.format(
                                plusNumFormat,
                                usedStrings.size() - strList.size()
                        ));
                    }

                    // Only one trigger (and possibly a number indicator)
                    // but if the first trigger is too long we trim it
                    while (font.width(compileLabel(strList)) > maxWidth) {
                        String str = strList.getFirst();
                        if (str.length() < 3)
                            break;
                        strList.set(0, str.substring(0, str.length() - 5) + " ...");
                    }

                    label = Component.literal(compileLabel(strList));
                    if (notif.textStyle.isEnabled()) {
                        label.withColor(notif.textStyle.color);
                    }
                }
                return label;
            }

            private static String compileLabel(List<String> list) {
                StringBuilder builder = new StringBuilder();
                for (String s : list) {
                    builder.append(s);
                }
                return builder.toString();
            }

            public static class Locked extends NotifOptions {

                public Locked(
                        int x,
                        int width,
                        int height,
                        FilterList<?> list,
                        Notification notif
                ) {
                    super(x, width, height, list, notif, 0);
                }
            }
        }
    }
}
