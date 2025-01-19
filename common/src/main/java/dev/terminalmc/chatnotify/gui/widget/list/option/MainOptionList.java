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

import com.mojang.blaze3d.platform.InputConstants;
import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.Notification;
import dev.terminalmc.chatnotify.config.TextStyle;
import dev.terminalmc.chatnotify.config.Trigger;
import dev.terminalmc.chatnotify.gui.screen.OptionsScreen;
import dev.terminalmc.chatnotify.gui.widget.HsvColorPicker;
import dev.terminalmc.chatnotify.gui.widget.RightClickableButton;
import dev.terminalmc.chatnotify.gui.widget.field.FakeTextField;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import dev.terminalmc.chatnotify.util.ColorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.NotNull;
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
public class MainOptionList extends OptionList {
    private int dragSourceSlot = -1;

    public MainOptionList(Minecraft mc, int width, int height, int y, int itemHeight,
                          int entryWidth, int entryHeight) {
        super(mc, width, height, y, itemHeight, entryWidth, entryHeight);

        addEntry(new OptionList.Entry.ActionButtonEntry(entryX, entryWidth, entryHeight,
                localized("option", "main.global"), null, -1, (button -> openGlobalConfig())));

        addEntry(new OptionList.Entry.TextEntry(entryX, entryWidth, entryHeight,
                localized("option", "main.notifs", "\u2139"),
                Tooltip.create(localized("option", "main.notifs.tooltip")), -1));

        List<Notification> notifs = Config.get().getNotifs();
        for (int i = 0; i < notifs.size(); i++) {
            addEntry(new Entry.NotifConfigEntry(dynEntryX, dynEntryWidth, entryHeight, this, notifs, i));
        }
        addEntry(new OptionList.Entry.ActionButtonEntry(entryX, entryWidth, entryHeight,
                Component.literal("+"), null, -1,
                (button) -> {
                    Config.get().addNotif();
                    reload();
                }));
    }

    @Override
    public MainOptionList reload(int width, int height, double scrollAmount) {
        MainOptionList newList = new MainOptionList(minecraft, width, height,
                getY(), itemHeight, entryWidth, entryHeight);
        newList.setScrollAmount(scrollAmount);
        return newList;
    }

    private void openGlobalConfig() {
        minecraft.setScreen(new OptionsScreen(minecraft.screen, localized("option", "global"),
                new GlobalOptionList(minecraft, width, height, getY(), itemHeight,
                        entryWidth, entryHeight)));
    }

    private void openNotificationConfig(int index) {
        Notification notif = Config.get().getNotifs().get(index);
        notif.editing = true;
        minecraft.setScreen(new OptionsScreen(minecraft.screen, localized("option", "notif"),
                new NotifOptionList(minecraft, width, height, getY(), itemHeight,
                        entryWidth, entryHeight, notif, () -> notif.editing = false)));
    }

    private void openTriggerConfig(Notification notif, Trigger trigger) {
        notif.editing = true;
        minecraft.setScreen(new OptionsScreen(minecraft.screen, localized("option", "trigger"),
                new TriggerOptionList(minecraft, width, height, getY(), itemHeight,
                        entryWidth, entryHeight, trigger, notif.textStyle, "", "", 
                        false, true, () -> notif.editing = false)));
    }

    private void openKeyConfig(Notification notif, Trigger trigger, TextStyle textStyle) {
        notif.editing = true;
        minecraft.setScreen(new OptionsScreen(minecraft.screen, localized("option", "key"),
                new KeyOptionList(minecraft, width, height, getY(), entryWidth, entryHeight, 
                        trigger, textStyle, () -> notif.editing = false)));
    }

    private void openSoundConfig(Notification notif) {
        notif.editing = true;
        minecraft.setScreen(new OptionsScreen(minecraft.screen, localized("option", "sound"),
                new SoundOptionList(minecraft, width, height, getY(), entryWidth, entryHeight, 
                        notif.sound, () -> notif.editing = false)));
    }

    // Notification button dragging

    @Override
    public void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.renderWidget(graphics, mouseX, mouseY, delta);
        if (dragSourceSlot != -1) {
            super.renderItem(graphics, mouseX, mouseY, delta, dragSourceSlot,
                    mouseX, mouseY, entryWidth, entryHeight);
        }
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        if (dragSourceSlot != -1 && button == InputConstants.MOUSE_BUTTON_LEFT) {
            dropDragged(x, y);
            return true;
        }
        return super.mouseReleased(x, y, button);
    }

    /**
     * A dragged entry, when dropped, will be placed below the hovered entry.
     * Therefore, the move operation will only be executed if the hovered entry
     * is below the dragged entry, or more than one slot above.
     */
    private void dropDragged(double mouseX, double mouseY) {
        OptionList.Entry hoveredEntry = getEntryAtPosition(mouseX, mouseY);
        // Check whether the drop location is valid
        if (hoveredEntry instanceof Entry.NotifConfigEntry) {
            int hoveredSlot = children().indexOf(hoveredEntry);
            // Check whether the move operation would actually change anything
            if (hoveredSlot > dragSourceSlot || hoveredSlot < dragSourceSlot - 1) {
                // Account for the list not starting at slot 0
                int offset = notifListOffset();
                int sourceIndex = dragSourceSlot - offset;
                int destIndex = hoveredSlot - offset;
                // I can't really explain why
                if (sourceIndex > destIndex) destIndex += 1;
                // Move
                if (Config.get().changeNotifPriority(sourceIndex, destIndex)) {
                    reload();
                }
            }
        }
        this.dragSourceSlot = -1;
    }

    /**
     * @return The index of the first {@link Entry.NotifConfigEntry} in the
     * {@link OptionList}.
     */
    private int notifListOffset() {
        int i = 0;
        for (OptionList.Entry entry : children()) {
            if (entry instanceof Entry.NotifConfigEntry) return i;
            i++;
        }
        throw new IllegalStateException("Notification list not found");
    }

    public static class Entry extends OptionList.Entry {

        private static class NotifConfigEntry extends Entry {
            NotifConfigEntry(int x, int width, int height, MainOptionList list,
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
                
                if (singleTrig) {
                    // Type button
                    CycleButton<Trigger.Type> typeButton = CycleButton.<Trigger.Type>builder(
                                    (type) -> Component.literal(type.icon))
                            .withValues(Trigger.Type.values())
                            .displayOnlyValue()
                            .withInitialValue(trigger.type)
                            .withTooltip((type) -> Tooltip.create(
                                    localized("option", "notif.trigger.type." + type + ".tooltip")))
                            .create(movingX, 0, list.tinyWidgetWidth, height, Component.empty(),
                                    (button, type) -> {
                                        trigger.type = type;
                                        list.reload();
                                    });
                    typeButton.setTooltipDelay(Duration.ofMillis(200));
                    elements.add(typeButton);
                    movingX += list.tinyWidgetWidth;
                }

                // Trigger field
                TextField triggerField = singleTrig
                        ? new TextField(movingX, 0, triggerFieldWidth, height)
                        : new FakeTextField(movingX, 0, triggerFieldWidth, height,
                        () -> list.openNotificationConfig(index));
                if (singleTrig && trigger.type == Trigger.Type.REGEX) triggerField.regexValidator();
                triggerField.setMaxLength(240);
                if (singleTrig) triggerField.setResponder((str) -> trigger.string = str.strip());
                triggerField.setValue(singleTrig 
                        ? trigger.string 
                        : createLabel(notif, triggerFieldWidth - 10).getString());
                triggerField.setTooltip(Tooltip.create(
                        localized("option", "main.trigger.tooltip")));
                triggerField.setTooltipDelay(Duration.ofMillis(500));
                elements.add(triggerField);
                movingX += triggerFieldWidth + (singleTrig ? 0 : SPACING_NARROW);

                if (keyTrig) {
                    // Key selection button
                    Button keySelectButton = Button.builder(Component.literal("\uD83D\uDD0D"),
                                    (button) -> list.openKeyConfig(notif, trigger, notif.textStyle))
                            .pos(movingX, 0)
                            .size(list.tinyWidgetWidth, height)
                            .build();
                    keySelectButton.setTooltip(Tooltip.create(
                            localized("option", "notif.trigger.key.tooltip")));
                    keySelectButton.setTooltipDelay(Duration.ofMillis(200));
                    elements.add(keySelectButton);
                    movingX += list.tinyWidgetWidth;
                }
                
                if (singleTrig) {
                    // Trigger editor button
                    Button editorButton = Button.builder(Component.literal("\u270e"),
                                    (button) -> list.openTriggerConfig(notif, trigger))
                            .pos(movingX, 0)
                            .size(list.tinyWidgetWidth, height)
                            .build();
                    editorButton.setTooltip(Tooltip.create(
                            localized("option", "notif.trigger.editor.tooltip")));
                    editorButton.setTooltipDelay(Duration.ofMillis(200));
                    elements.add(editorButton);
                    movingX += list.tinyWidgetWidth + SPACING_NARROW;
                }

                // Options button

                ImageButton editButton = new ImageButton(movingX, 0,
                        list.smallWidgetWidth, height, OPTION_SPRITES,
                        (button) -> {
                            list.openNotificationConfig(index);
                            list.reload();
                        });
                editButton.setTooltip(Tooltip.create(localized(
                        "option", "main.options.tooltip")));
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
                            int cpWidth = Math.min(HsvColorPicker.MAX_WIDTH, 
                                    Math.max(HsvColorPicker.MIN_WIDTH, width));
                            list.screen.setOverlayWidget(new HsvColorPicker(
                                    x, list.screen.height / 2 - cpHeight / 2,
                                    cpWidth, cpHeight,
                                    () -> notif.textStyle.color,
                                    (color) -> notif.textStyle.color = color,
                                    (widget) -> {
                                        list.screen.removeOverlayWidget();
                                        list.reload();
                                    }));
                        }, (button) -> {
                            // Toggle color
                            notif.textStyle.doColor = !notif.textStyle.doColor;
                            list.reload();
                        });
                colorEditButton.setTooltip(Tooltip.create(localized(
                        "option", "main.color.status.tooltip." 
                                + (notif.textStyle.doColor ? "enabled" : "disabled"))
                        .append("\n")
                        .append(localized("option", "main.edit.click"))));
                colorEditButton.setTooltipDelay(Duration.ofMillis(200));
                if (showColorField) {
                    TextField colorField = new TextField(movingX, 0, colorFieldWidth, height);
                    colorField.hexColorValidator();
                    colorField.setMaxLength(7);
                    colorField.setResponder((val) -> {
                        TextColor textColor = ColorUtil.parseColor(val);
                        if (textColor != null) {
                            int color = textColor.getValue();
                            notif.textStyle.color = color;
                            float[] hsv = new float[3];
                            Color.RGBtoHSB(FastColor.ARGB32.red(color), FastColor.ARGB32.green(color),
                                    FastColor.ARGB32.blue(color), hsv);
                            if (hsv[2] < 0.1) colorField.setTextColor(16777215);
                            else colorField.setTextColor(color);
                            // Update status button color
                            colorEditButton.setMessage(
                                    colorEditButton.getMessage().copy().withColor(color));
                        }
                    });
                    colorField.setValue(TextColor.fromRgb(notif.textStyle.color).formatValue());
                    colorField.setTooltip(Tooltip.create(
                            localized("option", "main.color.tooltip")));
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
                    soundField.setTooltip(Tooltip.create(
                            localized("option", "main.sound.tooltip")));
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
                            list.reload();
                        });
                soundEditButton.setTooltip(Tooltip.create(localized(
                        "option", "main.sound.status.tooltip." 
                                + (notif.sound.isEnabled() ? "enabled" : "disabled"))
                        .append("\n")
                        .append(localized("option", "main.edit.click"))));
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
                    // Drag reorder button (left-side extension)
                    elements.add(Button.builder(Component.literal("\u2191\u2193"),
                                    (button) -> {
                                        this.setDragging(true);
                                        list.dragSourceSlot = list.children().indexOf(this);
                                    })
                            .pos(x - list.smallWidgetWidth - SPACING, 0)
                            .size(list.smallWidgetWidth, height)
                            .build());

                    // Delete button (right-side extension)
                    elements.add(Button.builder(Component.literal("\u274C")
                                            .withStyle(ChatFormatting.RED),
                                    (button) -> {
                                        if (Config.get().removeNotif(index)) {
                                            list.reload();
                                        }
                                    })
                            .pos(x + width + SPACING, 0)
                            .size(list.smallWidgetWidth, height)
                            .build());
                }
            }

            private MutableComponent createLabel(Notification notif, int maxWidth) {
                MutableComponent label;
                Font font = Minecraft.getInstance().font;
                String separator = ", ";
                String plusNumFormat = " [+%d]";
                Pattern plusNumPattern = Pattern.compile(" \\[\\+\\d+]");

                if (notif.triggers.isEmpty() || notif.triggers.getFirst().string.isBlank()) {
                    label = Component.literal("> ").withStyle(ChatFormatting.YELLOW).append(
                            localized("option", "main.notifs.configure")
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

            private String getString(Trigger trigger) {
                if (trigger.type == Trigger.Type.KEY) {
                    String key = translationKey("option", "key.id") + "." + trigger.string;
                    return localized("option", "main.label.key", 
                            I18n.exists(key) ? I18n.get(key) : trigger.string).getString();
                } else {
                    return trigger.string;
                }
            }

            private String compileLabel(List<String> list) {
                StringBuilder builder = new StringBuilder();
                for (String s : list) {
                    builder.append(s);
                }
                return builder.toString();
            }
        }
    }
}
