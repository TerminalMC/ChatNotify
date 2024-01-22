package notryken.chatnotify.gui.component.listwidget;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.gui.screen.ConfigScreen;
import notryken.chatnotify.util.ColorUtil;

import java.util.List;

/**
 * {@code ConfigListWidget} containing controls for the specified
 * {@code Notification}, including references to other screens.
 */
public class NotifConfigListWidget extends ConfigListWidget {
    private final Notification notif;

    public NotifConfigListWidget(Minecraft minecraft, int width, int height,
                                 int top, int bottom, int itemHeight,
                                 int entryRelX, int entryWidth, int entryHeight,
                                 Notification notif) {
        super(minecraft, width, height, top, bottom, itemHeight,
                width / 2 + entryRelX, entryWidth, entryHeight);

        this.notif = notif;

        int eX = width / 2 - 120;
        int eW = 240;
        int eH = 20;

        addEntry(new ConfigListWidget.Entry.TextEntry(eX, eW, eH,
                Component.literal("Notification Trigger \u2139"),
                Tooltip.create(Component.literal("A trigger is a word or series of words " +
                        "that, if detected in a chat message, will activate the notification. " +
                        "NOT case-sensitive.")), -1));

        addEntry(new Entry.TriggerTypeEntry(eX, eW, eH, notif, this));

        if (notif.triggerIsKey) {
            addEntry(new ConfigListWidget.Entry.TextEntry(eX, eW, eH,
                    Component.literal("<< Key triggers may not work on some servers >>"),
                    null, -1));

            addEntry(new Entry.TriggerFieldEntry(eX, eW, eH, notif, this, 0));

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
                addEntry(new Entry.KeyTriggerEntry(eX, eW, eH, notif, this, keys.get(i), keys.get(i+1)));
                i++;
            }
        }
        else {
            int max = notif.getTriggers().size();
            for (int i = 0; i < max; i++) {
                addEntry(new Entry.TriggerFieldEntry(eX, eW, eH, notif, this, i));
            }
            addEntry(new ConfigListWidget.Entry.ActionButtonEntry(eX, 0, eW, eH,
                    Component.literal("+"), null, -1,
                    (button) -> {
                        notif.addTrigger("");
                        reload();
                    }));
        }

        addEntry(new ConfigListWidget.Entry.TextEntry(eX, eW, eH,
                Component.literal("Notification Options"), null, -1));

        addEntry(new Entry.SoundConfigEntry(eX, eW, eH, notif, this));
        addEntry(new Entry.ColorConfigEntry(eX, eW, eH, notif, this));
        addEntry(new Entry.FormatConfigEntry1(eX, eW, eH, notif, this));
        addEntry(new Entry.FormatConfigEntry2(eX, eW, eH, notif, this));

        addEntry(new ConfigListWidget.Entry.TextEntry(eX, eW, eH,
                Component.literal("Advanced Settings"), null, -1));

        addEntry(new Entry.AdvancedConfigButton(eX, eW, eH, this));
    }

    @Override
    public NotifConfigListWidget resize(int width, int height, int top, int bottom, int itemHeight) {
        return new NotifConfigListWidget(minecraft, width, height, top, bottom, itemHeight,
                entryX, entryWidth, entryHeight, notif);
    }

    private void openColorConfig() {
        minecraft.setScreen(new ConfigScreen(minecraft.screen,
                Component.translatable("screen.chatnotify.title.color"),
                new ColorConfigListWidget(minecraft, screen.width, screen.height,
                        y0, y1, itemHeight, entryX, entryWidth, entryHeight, notif)));
    }

    private void openSoundConfig() {
        minecraft.setScreen(new ConfigScreen(minecraft.screen,
                Component.translatable("screen.chatnotify.title.sound"),
                new SoundConfigListWidget(minecraft, screen.width, screen.height,
                        y0, y1, itemHeight, entryX, entryWidth, entryHeight, notif)));
    }

    private void openAdvancedConfig() {
        minecraft.setScreen(new ConfigScreen(minecraft.screen,
                Component.translatable("screen.chatnotify.title.advanced"),
                new AdvancedConfigListWidget(minecraft, screen.width, screen.height,
                        y0, y1, itemHeight, entryX, entryWidth, entryHeight, notif)));
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
                elements.add(triggerEditBox);

                if (index > 0) {
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

                elements.add(CycleButton.onOffBuilder()
                        .displayOnlyValue()
                        .withInitialValue(notif.getControl(2))
                        .create(x + width - statusButtonWidth, 0, statusButtonWidth, height,
                                Component.empty(),
                                (button, status) -> {
                                    notif.setControl(2, status);
                                    listWidget.reload();
                                }));
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

                MutableComponent message = Component.literal("Text Color");
                if (notif.getColor() != null) message.setStyle(Style.EMPTY.withColor(notif.getColor()));
                elements.add(Button.builder(message, (button) -> listWidget.openColorConfig())
                        .pos(x, 0)
                        .size(mainButtonWidth, height)
                        .build());

                EditBox colorEditBox = new EditBox(activeFont, x + mainButtonWidth + spacing, 0,
                        colorFieldWidth, height, Component.literal("Hex Color"));
                colorEditBox.setMaxLength(7);
                colorEditBox.setResponder(color -> notif.setColor(ColorUtil.parseColor(color)));
                if (notif.getColor() != null) {
                    colorEditBox.setValue(notif.getColor().formatValue());
                }
                elements.add(colorEditBox);

                elements.add(Button.builder(Component.literal("\ud83d\uddd8"),
                                (button) -> listWidget.reload())
                        .tooltip(Tooltip.create(Component.literal("Check Value")))
                        .pos(x + mainButtonWidth + spacing + colorFieldWidth, 0)
                        .size(reloadButtonWidth, height)
                        .build());

                elements.add(CycleButton.onOffBuilder()
                        .displayOnlyValue()
                        .withInitialValue(notif.getControl(0))
                        .create(x + width - statusButtonWidth, 0,
                                statusButtonWidth, height, Component.empty(),
                                (button, status) -> {
                                    notif.setControl(0, status);
                                    listWidget.reload();
                                }));
            }
        }

        private static class FormatConfigEntry1 extends Entry {
            FormatConfigEntry1(int x, int width, int height, Notification notif,
                               NotifConfigListWidget listWidget) {
                super();

                int spacing = 5;
                int buttonWidth = (width - spacing * 2) / 3;

                elements.add(CycleButton.onOffBuilder()
                        .withInitialValue(notif.getFormatControl(0))
                        .create(x, 0, buttonWidth, height, Component.literal("Bold")
                                .withStyle(Style.EMPTY.withBold(
                                        notif.getFormatControl(0))),
                                (button, status) -> {
                                    notif.setFormatControl(0, status);
                                    listWidget.reload();
                                }));

                elements.add(CycleButton.onOffBuilder()
                        .withInitialValue(notif.getFormatControl(1))
                        .create(x + width / 2 - buttonWidth / 2, 0, buttonWidth, height,
                                Component.literal("Italic")
                                        .withStyle(Style.EMPTY.withItalic(
                                                notif.getFormatControl(1))),
                                (button, status) -> {
                                    notif.setFormatControl(1, status);
                                    listWidget.reload();
                                }));

                elements.add(CycleButton.onOffBuilder()
                        .withInitialValue(notif.getFormatControl(2))
                        .create(x + width - buttonWidth, 0, buttonWidth, height,
                                Component.literal("Underline")
                                        .withStyle(Style.EMPTY.withUnderlined(
                                                notif.getFormatControl(2))),
                                (button, status) -> {
                                    notif.setFormatControl(2, status);
                                    listWidget.reload();
                                }));
            }
        }

        private static class FormatConfigEntry2 extends Entry {
            FormatConfigEntry2(int x, int width, int height, Notification notif,
                               NotifConfigListWidget listWidget) {
                super();

                int spacing = 6;
                int buttonWidth = (width - spacing) / 2;

                elements.add(CycleButton.onOffBuilder()
                        .withInitialValue(notif.getFormatControl(3))
                        .create(x, 0, buttonWidth, height,
                                Component.literal("Strikethrough")
                                        .withStyle(Style.EMPTY.withStrikethrough(
                                                notif.getFormatControl(3))),
                                (button, status) -> {
                                    notif.setFormatControl(3, status);
                                    listWidget.reload();
                                }));

                elements.add(CycleButton.onOffBuilder()
                        .withInitialValue(notif.getFormatControl(4))
                        .create(x + width - buttonWidth, 0, buttonWidth, height,
                                Component.literal("Obfuscate")
                                        .withStyle(Style.EMPTY.withObfuscated(
                                                notif.getFormatControl(4))),
                                (button, status) -> {
                                    notif.setFormatControl(4, status);
                                    listWidget.reload();
                                }));
            }
        }

        private static class AdvancedConfigButton extends Entry {
            AdvancedConfigButton(int x, int width, int height, NotifConfigListWidget listWidget) {
                super();
                elements.add(Button.builder(
                        Component.literal("Here be Dragons!"),
                                (button) -> listWidget.openAdvancedConfig())
                        .pos(x, 0)
                        .size(width, height)
                        .build());
            }
        }
    }
}