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
import dev.terminalmc.chatnotify.config.Notification;
import dev.terminalmc.chatnotify.config.Trigger;
import dev.terminalmc.chatnotify.gui.screen.OptionScreen;
import dev.terminalmc.chatnotify.gui.widget.ConfirmButton;
import dev.terminalmc.chatnotify.gui.widget.HsvColorPicker;
import dev.terminalmc.chatnotify.gui.widget.RightClickableButton;
import dev.terminalmc.chatnotify.gui.widget.field.FakeTextField;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import dev.terminalmc.chatnotify.util.ColorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import static dev.terminalmc.chatnotify.util.Localization.localized;
import static dev.terminalmc.chatnotify.util.Localization.translationKey;

/**
 * Contains a button linking to global options, and a dynamic list of buttons
 * linked to {@link Notification} instances.
 */
public class MainOptionList extends DragReorderList {
    private String filterString = "";
    private @Nullable Pattern filterPattern = null;
    private OptionList.Entry.ActionButton addNotifEntry;

    public MainOptionList(Minecraft mc, int width, int height, int y, int entryWidth,
                          int entryHeight, int entrySpacing) {
        super(mc, width, height, y, entryWidth, entryHeight, entrySpacing, () -> {}, 
                new HashMap<>(Map.of(Entry.NotifOptions.class, (source, dest) ->
                        Config.get().changeNotifPriority(++source, ++dest))));
        
        addNotifEntry = new OptionList.Entry.ActionButton(
                entryX, entryWidth, entryHeight, Component.literal("+"), null, -1,
                (button) -> {
                    Config.get().addNotif();
                    filterString = "";
                    filterPattern = null;
                    init();
                    ensureVisible(addNotifEntry);
                });
    }

    @Override
    protected void addEntries() {
        addEntry(new OptionList.Entry.ActionButton(entryX, entryWidth, entryHeight,
                localized("option", "global"), null, -1,
                (button -> openGlobalConfig())));

        addEntry(new Entry.NotifListHeader(entryX, entryWidth, entryHeight, this));

        refreshNotifSubList();
        addNotifEntry.setBounds(entryX, entryWidth, entryHeight);
        addEntry(addNotifEntry);
    }

    protected void refreshNotifSubList() {
        children().removeIf((entry) -> entry instanceof Entry.NotifOptions);
        // Add in reverse order at index 2 (entry 0 is global options, entry 1
        // is title/search)
        int start = 2;
        List<Notification> notifs = Config.get().getNotifs();
        for (int i = notifs.size() - 1; i >= 0; i--) {
            if (filterPattern == null || notifs.get(i).triggers.stream().anyMatch(
                    (trigger) -> filterPattern.matcher(trigger.string).find())) {
                if (i == 0) {
                    children().add(start, new Entry.LockedNotifOptions(
                            dynWideEntryX, dynWideEntryWidth, entryHeight, this, notifs, 0));
                } else {
                    children().add(start, new Entry.NotifOptions(
                            dynWideEntryX, dynWideEntryWidth, entryHeight, this, notifs, i));
                }
            }
        }
        refreshScrollAmount();
    }
    
    // Sub-screen opening

    private void openGlobalConfig() {
        mc.setScreen(new OptionScreen(mc.screen, localized("option", "global"), 
                new GlobalOptionList(mc, width, height, getY(), entryWidth, entryHeight,
                        entrySpacing)));
    }

    private void openNotificationConfig(int index) {
        Notification notif = Config.get().getNotifs().get(index);
        notif.editing = true;
        mc.setScreen(new OptionScreen(mc.screen, localized("option", "notif"),
                new NotifOptionList(mc, width, height, getY(), entryWidth, entryHeight,
                        entrySpacing, notif)));
    }

    private void openTriggerConfig(Notification notif, Trigger trigger) {
        notif.editing = true;
        Runnable onClose = () -> notif.editing = false;
        mc.setScreen(new OptionScreen(mc.screen, localized("option", "trigger"),
                new TriggerOptionList(mc, width, height, getY(), entryWidth, entryHeight,
                        entrySpacing, onClose, trigger, notif.textStyle)));
    }

    private void openKeyConfig(Notification notif, Trigger trigger) {
        notif.editing = true;
        Runnable onClose = () -> notif.editing = false;
        mc.setScreen(new OptionScreen(mc.screen, localized("option", "key"),
                new KeyOptionList(mc, width, height, getY(), entryWidth, entryHeight,
                        onClose, trigger)));
    }

    private void openSoundConfig(Notification notif) {
        notif.editing = true;
        Runnable onClose = () -> notif.editing = false;
        mc.setScreen(new OptionScreen(mc.screen, localized("option", "sound"),
                new SoundOptionList(mc, width, height, getY(), entryWidth, entryHeight,
                        onClose, notif.sound)));
    }

    // Custom entries

    public static class Entry extends OptionList.Entry {

        private static class NotifListHeader extends Entry {
            NotifListHeader(int x, int width, int height, MainOptionList list) {
                super();
                int searchFieldWidth = 100;
                int titleWidth = width - searchFieldWidth - SPACE;

                StringWidget titleWidget = new StringWidget(x, 0, titleWidth, height,
                        localized("option", "main.notifs", "\u2139"), list.mc.font);
                titleWidget.setTooltip(Tooltip.create(localized(
                        "option", "main.notifs.tooltip")));
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
                    list.refreshNotifSubList();
                });
                elements.add(searchField);
            }
        }

        private static class NotifOptions extends Entry {
            NotifOptions(int x, int width, int height, MainOptionList list,
                         List<Notification> notifs, int index) {
                super();
                Notification notif = notifs.get(index);
                int SPACING_NARROW = 2;
                
                @Nullable Trigger trigger = notif.triggers.size() == 1 
                        ? notif.triggers.getFirst() : null;
                boolean singleTrig = trigger != null;
                boolean keyTrig = singleTrig && trigger.type == Trigger.Type.KEY;
                
                int baseFieldWidth = Minecraft.getInstance().font.width("#FFAAFF++"); // ~54
                int colorFieldWidth = baseFieldWidth;
                int soundFieldWidth = baseFieldWidth;
                int statusButtonWidth = Math.max(24, height);
                
                boolean showColorField = false;
                boolean showColorFieldNominal = notif.textStyle.doColor;
                boolean showSoundField = false;
                boolean showSoundFieldNominal = notif.sound.isEnabled();
                
                int triggerWidth = width
                        - SPACING_NARROW
                        - list.smallWidgetWidth
                        - SPACING_NARROW
                        - list.tinyWidgetWidth
                        - SPACING_NARROW
                        - list.tinyWidgetWidth
                        - SPACING_NARROW
                        - statusButtonWidth;
                // Must be updated if any calculation constants are changed
                boolean canShowAllFields = triggerWidth >= 335;
                if (canShowAllFields) {
                    showColorField = showColorFieldNominal;
                    showSoundField = true;
                }
                
                // Add a field if trigger will still have 200 space
                if (triggerWidth >= (200 + baseFieldWidth)) {
                    triggerWidth -= baseFieldWidth;
                    // If color is enabled and sound is disabled, show color.
                    // Otherwise, show sound
                    if (showColorFieldNominal && !showSoundFieldNominal) {
                        showColorField = true;
                    } else {
                        showSoundField = true;
                    }
                }
                
                // If sound field is enabled, split the trigger's excess over 
                // 200 between trigger and sound
                if (showSoundField) {
                    int excess = triggerWidth - 200;
                    triggerWidth -= excess;
                    
                    // Up to 120, sound takes 70%
                    int soundBonus = (int)(excess * 0.7);
                    // Above 120, sound takes 35% (return 50% of extra)
                    int soundMargin = Math.max(0, soundFieldWidth + soundBonus - 120);
                    soundBonus -= (int)(soundMargin * 0.5);
                    // Above 140, sound takes nothing (return 100% of extra)
                    soundMargin = Math.max(0, soundFieldWidth + soundBonus - 140);
                    soundBonus -= soundMargin;
                    
                    soundFieldWidth += soundBonus;
                    triggerWidth += (excess - soundBonus);

                    // If trigger space is still at least 225 and color is 
                    // enabled, add color
                    if (triggerWidth >= 225 && (showColorFieldNominal || showColorField)) {
                        triggerWidth -= colorFieldWidth;
                        showColorField = true;
                    }
                }
                
                int triggerFieldWidth = triggerWidth;
                if (singleTrig) triggerFieldWidth -= (list.tinyWidgetWidth * 2);
                if (keyTrig) triggerFieldWidth -= list.tinyWidgetWidth;
                int movingX = x;

                if (index != 0) {
                    // Index indicator
                    Button indicatorButton = Button.builder(
                                    Component.literal(String.valueOf(index + 1)), (button) -> {})
                            .pos(x - list.smallWidgetWidth - SPACE - list.tinyWidgetWidth, 0)
                            .size(list.tinyWidgetWidth, height)
                            .build();
                    indicatorButton.active = false;
                    elements.add(indicatorButton);

                    // Drag reorder button (left-side extension)
                    Button dragButton = Button.builder(Component.literal("\u2191\u2193"),
                                    (button) -> {
                                        this.setDragging(true);
                                        list.startDragging(this, null, false);
                                    })
                            .pos(x - list.smallWidgetWidth - SPACE, 0)
                            .size(list.smallWidgetWidth, height)
                            .build();
                    dragButton.active = list.filterPattern == null;
                    elements.add(dragButton);
                }
                
                if (singleTrig) {
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
                    typeButton.setTooltipDelay(Duration.ofMillis(200));
                    elements.add(typeButton);
                    movingX += list.tinyWidgetWidth;
                }

                if (keyTrig) {
                    // Key selection button
                    Button keySelectButton = Button.builder(Component.literal("\uD83D\uDD0D"),
                                    (button) -> list.openKeyConfig(notif, trigger))
                            .pos(movingX, 0)
                            .size(list.tinyWidgetWidth, height)
                            .build();
                    keySelectButton.setTooltip(Tooltip.create(localized(
                            "option", "trigger.open.key_selector.tooltip")));
                    keySelectButton.setTooltipDelay(Duration.ofMillis(200));
                    elements.add(keySelectButton);
                    movingX += list.tinyWidgetWidth;
                }

                // Trigger field
                TextField triggerField;
                if (singleTrig) {
                    triggerField = new TextField(movingX, 0, triggerFieldWidth, height);
                    if (trigger.type == Trigger.Type.REGEX) triggerField.regexValidator();
                    triggerField.withValidator(new TextField.Validator.UniqueTrigger(
                            () -> Config.get().getNotifs(), (n) -> n.triggers, notif, trigger));
                    triggerField.setMaxLength(240);
                    triggerField.setValue(trigger.string);
                    triggerField.setResponder((str) -> trigger.string = str.strip());
                    triggerField.setTooltip(Tooltip.create(localized(
                            "option", "main.trigger.field.tooltip")));
                    triggerField.setTooltipDelay(Duration.ofMillis(500));
                } else {
                    triggerField = new FakeTextField(movingX, 0,
                            triggerFieldWidth, height, () -> list.openNotificationConfig(index));
                    triggerField.setMaxLength(240);
                    triggerField.setValue(createLabel(notif, triggerFieldWidth - 10).getString());
                }
                elements.add(triggerField);
                movingX += triggerFieldWidth + (singleTrig ? 0 : SPACING_NARROW);
                
                if (singleTrig) {
                    // Trigger editor button
                    Button editorButton = Button.builder(Component.literal("\u270e"),
                                    (button) -> list.openTriggerConfig(notif, trigger))
                            .pos(movingX, 0)
                            .size(list.tinyWidgetWidth, height)
                            .build();
                    editorButton.setTooltip(Tooltip.create(localized(
                            "option", "notif.open.trigger_editor.tooltip")));
                    editorButton.setTooltipDelay(Duration.ofMillis(200));
                    elements.add(editorButton);
                    movingX += list.tinyWidgetWidth + SPACING_NARROW;
                }

                // Options button

                ImageButton editButton = new ImageButton(movingX, 0,
                        list.smallWidgetWidth, height, OPTION_SPRITES,
                        (button) -> {
                            list.openNotificationConfig(index);
                            list.init();
                        });
                editButton.setTooltip(Tooltip.create(localized(
                        "option", "main.notif.options.tooltip")));
                editButton.setTooltipDelay(Duration.ofMillis(200));
                elements.add(editButton);
                movingX += list.smallWidgetWidth + SPACING_NARROW;
                
                // Color
                
                RightClickableButton colorEditButton = new RightClickableButton(
                        movingX, 0, list.tinyWidgetWidth, height,
                        Component.literal("\uD83C\uDF22").withColor(notif.textStyle.doColor 
                                ? notif.textStyle.color
                                : 0xffffff
                        ), (button) -> {
                            // Open color picker overlay widget
                            int cpHeight = HsvColorPicker.MIN_HEIGHT;
                            int cpWidth = HsvColorPicker.MIN_WIDTH;
                            list.screen.setOverlay(new HsvColorPicker(
                                    x + width / 2 - cpWidth / 2,
                                    list.screen.height / 2 - cpHeight / 2,
                                    cpWidth, cpHeight,
                                    () -> notif.textStyle.color,
                                    (color) -> notif.textStyle.color = color,
                                    (widget) -> {
                                        list.screen.removeOverlayWidget();
                                        list.init();
                                    }));
                        }, (button) -> {
                            // Toggle color
                            notif.textStyle.doColor = !notif.textStyle.doColor;
                            list.init();
                        });
                colorEditButton.setTooltip(Tooltip.create(localized(
                        "option", "main.color.status.tooltip." 
                                + (notif.textStyle.doColor ? "enabled" : "disabled"))
                        .append("\n")
                        .append(localized("option", "main.click_edit"))));
                colorEditButton.setTooltipDelay(Duration.ofMillis(200));
                if (showColorField) {
                    TextField colorField = new TextField(movingX, 0, colorFieldWidth, height);
                    colorField.hexColorValidator().strict();
                    colorField.setMaxLength(7);
                    colorField.setResponder((val) -> {
                        TextColor textColor = ColorUtil.parseColor(val);
                        if (textColor != null) {
                            int color = textColor.getValue();
                            notif.textStyle.color = color;
                            float[] hsv = new float[3];
                            Color.RGBtoHSB(ARGB.red(color), ARGB.green(color),
                                    ARGB.blue(color), hsv);
                            if (hsv[2] < 0.1) colorField.setTextColor(0xFFFFFF);
                            else colorField.setTextColor(color);
                            // Update status button color
                            colorEditButton.setMessage(
                                    colorEditButton.getMessage().copy().withColor(color));
                        }
                    });
                    colorField.setValue(TextColor.fromRgb(notif.textStyle.color).formatValue());
                    colorField.setTooltip(Tooltip.create(localized(
                            "option", "main.color.field.tooltip")));
                    colorField.setTooltipDelay(Duration.ofMillis(500));
                    elements.add(colorField);
                    movingX += colorFieldWidth;
                }
                colorEditButton.setPosition(movingX, 0);
                elements.add(colorEditButton);
                movingX += list.tinyWidgetWidth + SPACING_NARROW;
                
                // Sound

                if (showSoundField) {
                    TextField soundField = new TextField(movingX, 0, soundFieldWidth, height);
                    soundField.soundValidator();
                    soundField.setMaxLength(240);
                    soundField.setResponder(notif.sound::setId);
                    soundField.setValue(notif.sound.getId());
                    soundField.setTooltip(Tooltip.create(localized(
                            "option", "main.sound.field.tooltip")));
                    soundField.setTooltipDelay(Duration.ofMillis(500));
                    elements.add(soundField);
                    movingX += soundFieldWidth;
                }
                RightClickableButton soundEditButton = new RightClickableButton(
                        movingX, 0, list.tinyWidgetWidth, height, 
                        Component.literal("\uD83D\uDD0A").withStyle(notif.sound.isEnabled() 
                                ? ChatFormatting.WHITE
                                : ChatFormatting.RED
                        ), (button) -> {
                            // Open sound selection screen
                            list.openSoundConfig(notif);
                        }, (button) -> {
                            // Toggle sound
                            notif.sound.setEnabled(!notif.sound.isEnabled());
                            list.init();
                        });
                soundEditButton.setTooltip(Tooltip.create(localized(
                        "option", "main.sound.status.tooltip." 
                                + (notif.sound.isEnabled() ? "enabled" : "disabled"))
                        .append("\n")
                        .append(localized("option", "main.click_edit"))));
                soundEditButton.setTooltipDelay(Duration.ofMillis(200));
                elements.add(soundEditButton);

                // On/off button
                elements.add(CycleButton.booleanBuilder(
                                CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                                CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED))
                        .displayOnlyValue()
                        .withInitialValue(notif.enabled)
                        .create(x + width - statusButtonWidth, 0, statusButtonWidth, height,
                                Component.empty(), (button, status) -> notif.enabled = status));
                
                if (index != 0) {
                    // Delete button (right-side extension)
                    elements.add(new ConfirmButton(
                            x + width + SPACE, 0,
                            list.smallWidgetWidth, height,
                            Component.literal("\u274C"),
                            Component.literal("\u274C").withStyle(ChatFormatting.RED), 
                            (button) -> {
                                if (Config.get().removeNotif(index)) {
                                    list.init();
                                }
                            }));
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
                    label = Component.literal("> ").withStyle(ChatFormatting.YELLOW).append(
                            localized("option", "main.notif.label.configure")
                                    .withStyle(ChatFormatting.WHITE)).append(" <");
                }
                else {
                    Set<String> usedStrings = new TreeSet<>();
                    List<String> strList = new ArrayList<>();
                    boolean first = true;

                    // Compile all trigger strings, ignoring duplicates
                    for (Trigger trig : notif.triggers) {
                        String str = StringUtil.stripColor(getString(trig));
                        if (!usedStrings.contains(str)) {
                            strList.add(first ? str : separator + str);
                            usedStrings.add(str);
                        }
                        first = false;
                    }

                    // Delete trigger strings until label is small enough
                    // Not the most efficient approach, but simple is nice
                    while(font.width(compileLabel(strList)) > maxWidth) {
                        if (strList.size() == 1 || (strList.size() == 2 
                                && plusNumPattern.matcher(strList.getLast()).matches())) {
                            break;
                        }
                        if (plusNumPattern.matcher(strList.removeLast()).matches()) {
                            strList.removeLast();
                        }
                        strList.add(String.format(plusNumFormat, usedStrings.size() - strList.size()));
                    }

                    // Only one trigger (and possibly a number indicator)
                    // but if the first trigger is too long we trim it
                    while(font.width(compileLabel(strList)) > maxWidth) {
                        String str = strList.getFirst();
                        if (str.length() < 3) break;
                        strList.set(0, str.substring(0, str.length() - 5) + " ...");
                    }

                    label = Component.literal(compileLabel(strList));
                    if (notif.textStyle.isEnabled()) {
                        label.withColor(notif.textStyle.color);
                    }
                }
                return label;
            }

            private static String getString(Trigger trigger) {
                if (trigger.type == Trigger.Type.KEY) {
                    String key = translationKey("option", "key.id") + "." + trigger.string;
                    return localized("option", "main.notif.label.key", 
                            I18n.exists(key) ? I18n.get(key) : trigger.string).getString();
                } else {
                    return trigger.string;
                }
            }

            private static String compileLabel(List<String> list) {
                StringBuilder builder = new StringBuilder();
                for (String s : list) {
                    builder.append(s);
                }
                return builder.toString();
            }
        }

        private static class LockedNotifOptions extends NotifOptions {
            LockedNotifOptions(int x, int width, int height, MainOptionList list,
                               List<Notification> notifs, int index) {
                super(x, width, height, list, notifs, index);
            }
        }
    }
}
