package notryken.chatnotify.gui.components.listwidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.gui.screen.ConfigSubScreen;
import org.jetbrains.annotations.NotNull;

/**
 * {@code ConfigListWidget} containing controls for the specified
 * {@code Notification}, including references to other screens.
 */
public class NotificationConfigListWidget extends ConfigListWidget {
    private final Notification notif;

    public NotificationConfigListWidget(Minecraft client, int width, int height,
                                        int top, int bottom, int itemHeight, Screen parent,
                                        Component title, Notification notif) {
        super(client, width, height, top, bottom, itemHeight, parent, title);
        this.notif = notif;

        addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Component.literal("Notification Trigger \u2139"),
                Component.literal("A trigger is a word or series of words that, " +
                        "if detected in a chat message, will activate the notification. " +
                        "NOT case-sensitive.")));
        addEntry(new Entry.TriggerConfigType(width, notif, this));

        if (notif.triggerIsKey) {
            addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                    Component.literal("May not work on some servers.")));
            addEntry(new Entry.TriggerField(width, notif, client, this, 0));

            String[][] keys = {
                            {"chat.type", "Any Message"},
                            {"commands.message.display", "Private Message"},
                            {"multiplayer.player.joined", "Player Joined"},
                            {"multiplayer.player.left", "Player Left"},
                            {"chat.type.advancement", "Advancement"},
                            {"death.", "Player/Pet Died"}
                    };
            for (String[] key : keys) {
                addEntry(new Entry.KeyTriggerButton(width, notif, this, key));
            }
        }
        else {
            for (int i = 0; i < notif.getTriggers().size(); i++) {
                addEntry(new Entry.TriggerField(width, notif, client, this, i));
            }
            addEntry(new Entry.TriggerField(width, notif, client, this, -1));
        }

        addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                Component.literal("Notification Options")));

        addEntry(new Entry.SoundConfigOption(width, notif, this));
        addEntry(new Entry.ColorConfigOption(width, notif, client, this));
        addEntry(new Entry.FormatOptionPrimary(width, notif, this));
        addEntry(new Entry.FormatOptionSecondary(width, notif, this));

        addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                Component.literal("Advanced Settings")));
        addEntry(new Entry.AdvancedConfigButton(width, notif, this));
    }

    @Override
    public NotificationConfigListWidget resize(int width, int height, int top, int bottom) {
        NotificationConfigListWidget listWidget = new NotificationConfigListWidget(
                client, width, height, top, bottom, itemHeight, parentScreen, title, notif);
        listWidget.setScrollAmount(getScrollAmount());
        return listWidget;
    }

    @Override
    protected void refreshScreen() {
        refreshScreen(this);
    }

    private void openColorConfig() {
        assert client.screen != null;
        Component title = Component.literal("Notification Color Options");
        client.setScreen(new ConfigSubScreen(client.screen, client.options,
                title, new ColorConfigListWidget(client,
                client.screen.width, client.screen.height,
                32, client.screen.height - 32, 25,
                client.screen, title, notif)));
    }

    private void openSoundConfig() {
        assert client.screen != null;
        Component title = Component.literal("Notification Sound Options");
        client.setScreen(new ConfigSubScreen(client.screen, client.options,
                title, new SoundConfigListWidget(client,
                client.screen.width, client.screen.height,
                32, client.screen.height - 32, 25,
                client.screen, title, notif)));
    }

    private void openAdvancedConfig() {
        assert client.screen != null;
        Component title = Component.literal("Advanced Settings");
        client.setScreen(new ConfigSubScreen(client.screen, client.options,
                title, new AdvancedConfigListWidget(client,
                client.screen.width, client.screen.height,
                32, client.screen.height - 32, 25,
                client.screen, title, notif)));
    }

    private abstract static class Entry extends ConfigListWidget.Entry {
        public final Notification notif;

        Entry(int width, Notification notif, NotificationConfigListWidget listWidget) {
            super(width, listWidget);
            this.notif = notif;
        }

        private static class TriggerConfigType extends Entry {
            TriggerConfigType(int width, Notification notif,
                              NotificationConfigListWidget listWidget) {
                super(width, notif, listWidget);
                options.add(CycleButton.booleanBuilder(
                        Component.literal("Event Key"), Component.literal("Word/Phrase"))
                                .withInitialValue(notif.triggerIsKey)
                                .create(this.width / 2 - 120, 0, 240, 20,
                                        Component.literal("Type"),
                                        (button, status) -> {
                                    notif.triggerIsKey = status;
                                    listWidget.refreshScreen();
                                }));
            }
        }

        private static class TriggerField extends Entry {
            final int index;

            /**
             * @param index the index of the {@code Notification} trigger,
             *              or -1 for the 'add trigger' button.
             */
            TriggerField(int width, Notification notif, @NotNull Minecraft client,
                         NotificationConfigListWidget listWidget, int index) {
                super(width, notif, listWidget);
                this.index = index;

                if (index == -1) {
                    options.add(Button.builder(Component.literal("+"),
                                    (button) -> {
                                        notif.addTrigger("");
                                        listWidget.refreshScreen();
                                    })
                            .size(240, 20)
                            .pos(width / 2 - 120, 0)
                            .build());
                }
                else if (index >= 0) {
                    EditBox triggerEdit = new EditBox(
                            client.font, this.width / 2 - 120, 0, 240,
                            20, Component.literal("Notification Trigger"));
                    triggerEdit.setMaxLength(120);
                    triggerEdit.setValue(this.notif.getTrigger(index));
                    triggerEdit.setResponder((trigger) ->
                            notif.setTrigger(index, trigger.strip()));
                    this.options.add(triggerEdit);

                    if (index != 0) {
                        options.add(Button.builder(Component.literal("X"),
                                        (button) -> {
                                            notif.removeTrigger(index);
                                            listWidget.refreshScreen();
                                        })
                                .size(25, 20)
                                .pos(width / 2 + 120 + 5, 0)
                                .build());
                    }
                }
            }
        }

        private static class KeyTriggerButton extends Entry {
            KeyTriggerButton(int width, Notification notif,
                             NotificationConfigListWidget listWidget, String[] key) {
                super(width, notif, listWidget);

                options.add(Button.builder(Component.literal(key[1]),
                        (button) -> {
                            notif.setTrigger(key[0]);
                            listWidget.refreshScreen();
                        })
                        .size(240, 20)
                        .pos(width / 2 - 120, 0)
                        .build());
            }
        }

        private static class SoundConfigOption extends Entry {
            SoundConfigOption(int width, Notification notif,
                              NotificationConfigListWidget listWidget) {
                super(width, notif, listWidget);

                options.add(Button.builder(
                                Component.literal("Sound: " + notif.getSound().toString()),
                                (button) -> listWidget.openSoundConfig())
                        .size(210, 20)
                        .pos(width / 2 - 120, 0)
                        .build());

                options.add(CycleButton.onOffBuilder()
                        .displayOnlyValue()
                        .withInitialValue(notif.getControl(2))
                        .create(this.width / 2 + 95, 0, 25, 20, Component.empty(),
                                (button, status) -> {
                                    notif.setControl(2, status);
                                    listWidget.refreshScreen();
                                }));
            }
        }

        private static class ColorConfigOption extends Entry {
            ColorConfigOption(int width, Notification notif, Minecraft client,
                              NotificationConfigListWidget listWidget) {
                super(width, notif, listWidget);

                MutableComponent message = Component.literal("Text Color");
                if (notif.getColor() != null) {
                    message.setStyle(Style.EMPTY.withColor(notif.getColor()));
                }
                options.add(Button.builder(message, (button) -> listWidget.openColorConfig())
                        .size(120, 20)
                        .pos(width / 2 - 120, 0)
                        .build());

                EditBox colorEdit = new EditBox(client.font, this.width / 2 + 6, 0, 64, 20,
                        Component.literal("Hex Color"));
                colorEdit.setMaxLength(7);
                colorEdit.setResponder(color -> notif.setColor(notif.parseColor(color)));
                if (this.notif.getColor() != null) {
                    colorEdit.setValue(this.notif.getColor().formatValue());
                }
                options.add(colorEdit);

                options.add(Button.builder(Component.literal("\ud83d\uddd8"),
                                (button) -> listWidget.refreshScreen())
                        .size(20, 20)
                        .pos(width / 2 + 70, 0)
                        .build());

                options.add(CycleButton.onOffBuilder()
                        .displayOnlyValue()
                        .withInitialValue(notif.getControl(0))
                        .create(this.width / 2 + 95, 0, 25, 20, Component.empty(),
                                (button, status) -> {
                                    notif.setControl(0, status);
                                    listWidget.refreshScreen();
                                }));
            }
        }

        private static class FormatOptionPrimary extends Entry {
            FormatOptionPrimary(int width, Notification notif,
                                NotificationConfigListWidget listWidget) {
                super(width, notif, listWidget);

                options.add(CycleButton.onOffBuilder()
                        .withInitialValue(notif.getFormatControl(0))
                        .create(this.width / 2 - 120, 0, 76, 20, Component.literal("Bold")
                                .withStyle(Style.EMPTY.withBold(
                                        notif.getFormatControl(0))),
                                (button, status) -> {
                                    notif.setFormatControl(0, status);
                                    listWidget.refreshScreen();
                                }));

                options.add(CycleButton.onOffBuilder()
                        .withInitialValue(notif.getFormatControl(1))
                        .create(this.width / 2 - 38, 0, 76, 20, Component.literal("Italic")
                                        .withStyle(Style.EMPTY.withItalic(
                                                notif.getFormatControl(1))),
                                (button, status) -> {
                                    notif.setFormatControl(1, status);
                                    listWidget.refreshScreen();
                                }));

                options.add(CycleButton.onOffBuilder()
                        .withInitialValue(notif.getFormatControl(2))
                        .create(this.width / 2 + 44, 0, 76, 20, Component.literal("Underline")
                                        .withStyle(Style.EMPTY.withUnderlined(
                                                notif.getFormatControl(2))),
                                (button, status) -> {
                                    notif.setFormatControl(2, status);
                                    listWidget.refreshScreen();
                                }));
            }
        }

        private static class FormatOptionSecondary extends Entry {
            FormatOptionSecondary(int width, Notification notif,
                                  NotificationConfigListWidget listWidget) {
                super(width, notif, listWidget);

                options.add(CycleButton.onOffBuilder()
                        .withInitialValue(notif.getFormatControl(3))
                        .create(this.width / 2 - 120, 0, 117, 20, Component.literal("Strikethrough")
                                        .withStyle(Style.EMPTY.withStrikethrough(
                                                notif.getFormatControl(3))),
                                (button, status) -> {
                                    notif.setFormatControl(3, status);
                                    listWidget.refreshScreen();
                                }));

                options.add(CycleButton.onOffBuilder()
                        .withInitialValue(notif.getFormatControl(4))
                        .create(this.width / 2 + 3, 0, 117, 20, Component.literal("Obfuscate")
                                        .withStyle(Style.EMPTY.withObfuscated(
                                                notif.getFormatControl(4))),
                                (button, status) -> {
                                    notif.setFormatControl(4, status);
                                    listWidget.refreshScreen();
                                }));
            }
        }

        private static class AdvancedConfigButton extends Entry {
            AdvancedConfigButton(int width, Notification notif,
                                 NotificationConfigListWidget listWidget) {
                super(width, notif, listWidget);

                options.add(Button.builder(
                        Component.literal("Here be Dragons!"),
                                (button) -> listWidget.openAdvancedConfig())
                        .size(240, 20)
                        .pos(width / 2 - 120, 0)
                        .build());
            }
        }
    }
}