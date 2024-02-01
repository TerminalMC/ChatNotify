package com.notryken.chatnotify.gui.component.listwidget;

import com.mojang.datafixers.util.Pair;
import com.notryken.chatnotify.config.Notification;
import com.notryken.chatnotify.gui.screen.ConfigScreen;
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

import java.util.List;

/**
 * {@code ConfigListWidget} containing controls for the specified
 * {@code Notification}, including references to sub-screens.
 */
public class NotifConfigListWidget extends ConfigListWidget {
    private final Notification notif;
    private final boolean isUsernameNotif;

    public NotifConfigListWidget(Minecraft minecraft, int width, int height,
                                 int top, int bottom, int itemHeight,
                                 int entryRelX, int entryWidth, int entryHeight,
                                 int scrollWidth, Notification notif,
                                 boolean isUsernameNotif) {
        super(minecraft, width, height, top, bottom, itemHeight, 
                entryRelX, entryWidth, entryHeight, scrollWidth);
        this.notif = notif;
        this.isUsernameNotif = isUsernameNotif;

        addEntry(new ConfigListWidget.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Notification Trigger \u2139"),
                Tooltip.create(Component.literal("A trigger is a word or series of words " +
                        "that, if detected in a chat message, will activate the notification. " +
                        "NOT case-sensitive.")), -1));

        // Username notification can't be key-triggered
        if (!this.isUsernameNotif) {
            addEntry(new Entry.TriggerTypeEntry(entryX, entryWidth, entryHeight, notif, this));
        }

        if (notif.triggerIsKey) {
            addEntry(new ConfigListWidget.Entry.TextEntry(entryX, entryWidth, entryHeight,
                    Component.literal("<< Key triggers may not work on some servers >>"),
                    null, -1));

            addEntry(new Entry.TriggerFieldEntry(entryX, entryWidth, entryHeight, notif, this, 0));

            List<Pair<String,String>> keys = List.of(
                    new Pair<>(".", "Any Message"),
                    new Pair<>("commands.message.display", "Private Message"),
                    new Pair<>("multiplayer.player.joined", "Player Joined"),
                    new Pair<>("multiplayer.player.left", "Player Left"),
                    new Pair<>("chat.type.advancement", "Advancement"),
                    new Pair<>("death.", "Player/Pet Died")
            );
            for (int i = 0; i < keys.size(); i++) {
                // Number of key-description pairs must be even
                addEntry(new Entry.KeyTriggerEntry(entryX, entryWidth, entryHeight, notif, this, keys.get(i), keys.get(i+1)));
                i++;
            }
        }
        else {
            int max = notif.getTriggers().size();
            for (int i = 0; i < max; i++) {
                addEntry(new Entry.TriggerFieldEntry(entryX, entryWidth, entryHeight, notif, this, i));
            }
            addEntry(new ConfigListWidget.Entry.ActionButtonEntry(entryX, 0, entryWidth, entryHeight,
                    Component.literal("+"), null, -1,
                    (button) -> {
                        notif.addTrigger("");
                        reload();
                    }));
        }

        addEntry(new ConfigListWidget.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Notification Options"), null, -1));

        addEntry(new Entry.SoundConfigEntry(entryX, entryWidth, entryHeight, notif, this));
        addEntry(new Entry.ColorConfigEntry(entryX, entryWidth, entryHeight, notif, this));
        addEntry(new Entry.FormatConfigEntry1(entryX, entryWidth, entryHeight, notif));
        addEntry(new Entry.FormatConfigEntry2(entryX, entryWidth, entryHeight, notif));

        addEntry(new ConfigListWidget.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Advanced Settings"), null, -1));

        addEntry(new ConfigListWidget.Entry.ActionButtonEntry(entryX, 0, entryWidth, entryHeight,
                Component.literal("Here be Dragons!"), null, -1,
                (button) -> openAdvancedConfig()));
    }

    @Override
    public NotifConfigListWidget resize(int width, int height, int top, int bottom,
                                        int itemHeight, double scrollAmount) {
        NotifConfigListWidget newListWidget = new NotifConfigListWidget(
                minecraft, width, height, top, bottom, itemHeight,
                entryRelX, entryWidth, entryHeight, scrollWidth, notif, isUsernameNotif);
        newListWidget.setScrollAmount(scrollAmount);
        return newListWidget;
    }

    @Override
    public void onClose() {
        notif.autoDisable();
    }

    private void openColorConfig() {
        minecraft.setScreen(new ConfigScreen(minecraft.screen,
                Component.translatable("screen.chatnotify.title.color"),
                new ColorConfigListWidget(minecraft, screen.width, screen.height, y0, y1, 
                        itemHeight, entryRelX, entryWidth, entryHeight, scrollWidth, notif)));
    }

    private void openSoundConfig() {
        minecraft.setScreen(new ConfigScreen(minecraft.screen,
                Component.translatable("screen.chatnotify.title.sound"),
                new SoundConfigListWidget(minecraft, screen.width, screen.height, y0, y1, 
                        itemHeight, entryRelX, entryWidth, entryHeight, scrollWidth, notif)));
    }

    private void openAdvancedConfig() {
        minecraft.setScreen(new ConfigScreen(minecraft.screen,
                Component.translatable("screen.chatnotify.title.advanced"),
                new AdvancedConfigListWidget(minecraft, screen.width, screen.height, y0, y1, 
                        itemHeight, entryRelX, entryWidth, entryHeight, scrollWidth, notif)));
    }

    private abstract static class Entry extends ConfigListWidget.Entry {

        private static class TriggerTypeEntry extends Entry {
            TriggerTypeEntry(int x, int width, int height, Notification notif,
                             NotifConfigListWidget listWidget) {
                super();
                elements.add(CycleButton.booleanBuilder(
                        Component.literal("Event Key"), Component.literal("Word/Phrase"))
                                .withInitialValue(notif.triggerIsKey)
                                .create(x, 0, width, height,
                                        Component.literal("Type"),
                                        (button, status) -> {
                                    notif.triggerIsKey = status;
                                    listWidget.reload();
                                }));
            }
        }

        private static class TriggerFieldEntry extends Entry {

            TriggerFieldEntry(int x, int width, int height, Notification notif,
                              NotifConfigListWidget listWidget, int index) {
                super();

                int spacing = 5;
                int removeButtonWidth = 20;

                EditBox triggerEditBox = new EditBox(Minecraft.getInstance().font,
                        x, 0, width, height, Component.literal("Notification Trigger"));
                triggerEditBox.setMaxLength(120);
                triggerEditBox.setValue(notif.getTrigger(index));
                triggerEditBox.setResponder(
                        (trigger) -> notif.setTrigger(index, trigger.strip()));
                // First two triggers of username notification are uneditable
                if (listWidget.isUsernameNotif && index <= 1) {
                    triggerEditBox.setEditable(false);
                    triggerEditBox.active = false;
                    triggerEditBox.setTooltip(Tooltip.create(Component.literal(
                            index == 0 ? "Profile name" : "Display name")));
                    triggerEditBox.setTooltipDelay(500);
                }
                elements.add(triggerEditBox);

                // Only show the delete button if it makes sense to delete.
                if (!notif.triggerIsKey && notif.getTriggers().size() > 1 &&
                        (index > 1 || (!listWidget.isUsernameNotif && index > 0))) {
                    elements.add(Button.builder(Component.literal("X"),
                                    (button) -> {
                                        notif.removeTrigger(index);
                                        listWidget.reload();
                                    })
                            .pos(x + width + spacing, 0)
                            .size(removeButtonWidth, height)
                            .build());
                }
            }
        }

        private static class KeyTriggerEntry extends Entry {
            KeyTriggerEntry(int x, int width, int height, Notification notif,
                            NotifConfigListWidget listWidget,
                            Pair<String,String> keyLeft, Pair<String,String>  keyRight) {
                super();

                int spacing = 6;
                int buttonWidth = width / 2 - spacing;

                elements.add(Button.builder(Component.literal(keyLeft.getSecond()),
                                (button) -> {
                                    notif.setTrigger(keyLeft.getFirst());
                                    listWidget.reload();
                                })
                        .pos(x, 0)
                        .size(buttonWidth, height)
                        .build());

                elements.add(Button.builder(Component.literal(keyRight.getSecond()),
                                (button) -> {
                                    notif.setTrigger(keyRight.getFirst());
                                    listWidget.reload();
                                })
                        .pos(x + buttonWidth + spacing, 0)
                        .size(buttonWidth, height)
                        .build());
            }
        }

        private static class SoundConfigEntry extends Entry {
            SoundConfigEntry(int x, int width, int height, Notification notif,
                             NotifConfigListWidget listWidget) {
                super();

                int spacing = 5;
                int statusButtonWidth = 25;
                int mainButtonWidth = width - statusButtonWidth - spacing;

                elements.add(Button.builder(
                                Component.literal("Sound: " + notif.getSound().toString()),
                                (button) -> listWidget.openSoundConfig())
                        .pos(x, 0)
                        .size(mainButtonWidth, height)
                        .build());

                elements.add(CycleButton.booleanBuilder(
                        Component.translatable("options.on").withStyle(ChatFormatting.GREEN),
                                Component.translatable("options.off").withStyle(ChatFormatting.RED))
                        .displayOnlyValue()
                        .withInitialValue(notif.getControl(2))
                        .create(x + width - statusButtonWidth, 0, statusButtonWidth, height,
                                Component.empty(),
                                (button, status) -> notif.setControl(2, status)));
            }
        }

        private static class ColorConfigEntry extends Entry {
            ColorConfigEntry(int x, int width, int height, Notification notif, NotifConfigListWidget listWidget) {
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
                                        .setStyle(Style.EMPTY.withColor(notif.getColor())),
                                (button) -> listWidget.openColorConfig())
                        .pos(x, 0)
                        .size(mainButtonWidth, height)
                        .build();
                elements.add(mainButton);

                EditBox colorEditBox = new EditBox(activeFont, x + mainButtonWidth + spacing, 0,
                        colorFieldWidth, height, Component.literal("Hex Color"));
                colorEditBox.setMaxLength(7);
                colorEditBox.setResponder(strColor -> {
                    TextColor color = ColorUtil.parseColor(strColor);
                    if (color != null) {
                        notif.setColor(color);
                        mainButton.setMessage(Component.literal(mainButtonMessage)
                                .setStyle(Style.EMPTY.withColor(notif.getColor())));
                    }
                });
                colorEditBox.setValue(notif.getColor().formatValue());
                elements.add(colorEditBox);

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
                        .withInitialValue(notif.getControl(0))
                        .create(x + width - statusButtonWidth, 0,
                                statusButtonWidth, height, Component.empty(),
                                (button, status) -> notif.setControl(0, status)));
            }
        }

        private static class FormatConfigEntry1 extends Entry {
            FormatConfigEntry1(int x, int width, int height, Notification notif) {
                super();

                int spacing = 5;
                int buttonWidth = (width - spacing * 2) / 3;

                elements.add(CycleButton.booleanBuilder(
                        Component.translatable("options.on")
                                .withStyle(ChatFormatting.BOLD)
                                .withStyle(ChatFormatting.GREEN),
                                Component.translatable("options.off")
                                        .withStyle(ChatFormatting.RED))
                        .withInitialValue(notif.getFormatControl(0))
                        .create(x, 0, buttonWidth, height,
                                Component.literal("Bold"),
                                (button, status) -> notif.setFormatControl(0, status)));

                elements.add(CycleButton.booleanBuilder(
                        Component.translatable("options.on")
                                .withStyle(ChatFormatting.ITALIC)
                                .withStyle(ChatFormatting.GREEN),
                                Component.translatable("options.off")
                                        .withStyle(ChatFormatting.RED))
                        .withInitialValue(notif.getFormatControl(1))
                        .create(x + width / 2 - buttonWidth / 2, 0, buttonWidth, height,
                                Component.literal("Italic"),
                                (button, status) -> notif.setFormatControl(1, status)));

                elements.add(CycleButton.booleanBuilder(
                        Component.translatable("options.on")
                                .withStyle(ChatFormatting.UNDERLINE)
                                .withStyle(ChatFormatting.GREEN),
                                Component.translatable("options.off")
                                        .withStyle(ChatFormatting.RED))
                        .withInitialValue(notif.getFormatControl(2))
                        .create(x + width - buttonWidth, 0, buttonWidth, height,
                                Component.literal("Underline"),
                                (button, status) -> notif.setFormatControl(2, status)));
            }
        }

        private static class FormatConfigEntry2 extends Entry {
            FormatConfigEntry2(int x, int width, int height, Notification notif) {
                super();

                int spacing = 6;
                int buttonWidth = (width - spacing) / 2;

                elements.add(CycleButton.booleanBuilder(
                        Component.translatable("options.on")
                                .withStyle(ChatFormatting.STRIKETHROUGH)
                                .withStyle(ChatFormatting.GREEN),
                                Component.translatable("options.off")
                                        .withStyle(ChatFormatting.RED))
                        .withInitialValue(notif.getFormatControl(3))
                        .create(x, 0, buttonWidth, height,
                                Component.literal("Strikethrough"),
                                (button, status) -> notif.setFormatControl(3, status)));

                elements.add(CycleButton.booleanBuilder(
                        Component.translatable("options.on")
                                .withStyle(ChatFormatting.OBFUSCATED)
                                .withStyle(ChatFormatting.GREEN),
                                Component.translatable("options.off")
                                        .withStyle(ChatFormatting.RED))
                        .withInitialValue(notif.getFormatControl(4))
                        .create(x + width - buttonWidth, 0, buttonWidth, height,
                                Component.literal("Obfuscate"),
                                (button, status) -> notif.setFormatControl(4, status)));
            }
        }
    }
}