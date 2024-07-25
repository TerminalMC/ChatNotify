/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.gui.widget.list.option;

import com.mojang.blaze3d.platform.InputConstants;
import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.Notification;
import dev.terminalmc.chatnotify.config.Trigger;
import dev.terminalmc.chatnotify.gui.screen.OptionsScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;

import static dev.terminalmc.chatnotify.util.Localization.localized;

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
            addEntry(new Entry.NotifConfigEntry(entryX, entryWidth, entryHeight, this, notifs, i));
        }
        addEntry(new OptionList.Entry.ActionButtonEntry(entryX, entryWidth, entryHeight,
                Component.literal("+"), null, -1,
                (button) -> {
                    Config.get().addNotif();
                    openNotificationConfig(Config.get().getNotifs().size() - 1);
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
        minecraft.setScreen(new OptionsScreen(minecraft.screen, localized("option", "notif"),
                new NotifOptionList(minecraft, width, height, getY(), itemHeight,
                        entryWidth, entryHeight, Config.get().getNotifs().get(index))));
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
                Config.get().changeNotifPriority(sourceIndex, destIndex);
                reload();
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
                int statusButtonWidth = Math.max(24, height);
                int mainButtonWidth = width - statusButtonWidth - SPACING;

                if (index > 0) {
                    // Drag reorder button
                    elements.add(Button.builder(Component.literal("\u2191\u2193"),
                                    (button) -> {
                                        this.setDragging(true);
                                        list.dragSourceSlot = list.children().indexOf(this);
                                    })
                            .pos(x - list.smallWidgetWidth - SPACING, 0)
                            .size(list.smallWidgetWidth, height)
                            .build());
                }

                // Main button
                elements.add(Button.builder(createLabel(notif, mainButtonWidth - 10),
                                (button) -> list.openNotificationConfig(index))
                        .pos(x, 0)
                        .size(mainButtonWidth, height)
                        .build());

                // On/off button
                elements.add(CycleButton.booleanBuilder(
                        CommonComponents.OPTION_ON.copy().withStyle(ChatFormatting.GREEN),
                                CommonComponents.OPTION_OFF.copy().withStyle(ChatFormatting.RED))
                        .displayOnlyValue()
                        .withInitialValue(notif.isEnabled())
                        .create(x + mainButtonWidth + SPACING, 0, statusButtonWidth, height,
                                Component.empty(), (button, status) -> notif.setEnabled(status)));

                if (index > 0) {
                    // Delete button
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

                    label = Component.literal(compileLabel(strList));
                    if (notif.textStyle.isEnabled()) {
                        label.withColor(notif.textStyle.color);
                    }
                }
                return label;
            }

            private String getString(Trigger trigger) {
                if (trigger.isKey) {
                    return localized("option", "notif.label.key", (trigger.string.equals(".")
                            ? localized("option", "notif.trigger.key.any")
                            : trigger.string)).getString();
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
