package notryken.chatnotify.gui.component.listwidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.gui.screen.NotifConfigScreen;
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

        addEntry(new ConfigListWidget.Entry.Header(this.width, this.client,
                Component.literal("Notification Trigger \u2139"),
                Component.literal("A trigger is a word or series of words that, " +
                        "if detected in a chat message, will activate the notification. " +
                        "NOT case-sensitive.")));
        addEntry(new Entry.TriggerConfigType(this.width, this.notif, this));

        if (this.notif.triggerIsKey) {
            addEntry(new ConfigListWidget.Entry.Header(this.width, this.client,
                    Component.literal("May not work on some servers.")));
            addEntry(new Entry.TriggerField(this.width, this.notif, this.client, this, 0));

            String[][] keys = {
                            {".", "Any Message"},
                            {"commands.message.display", "Private Message"},
                            {"multiplayer.player.joined", "Player Joined"},
                            {"multiplayer.player.left", "Player Left"},
                            {"chat.type.advancement", "Advancement"},
                            {"death.", "Player/Pet Died"}
                    };
            for (int i = 0; i < keys.length; i++) {
                // Requires an even number of keys
                addEntry(new Entry.KeyTriggerButton(this.width, this.notif, this, keys[i], keys[i+1]));
                i++;
            }
        }
        else {
            for (int i = 0; i < this.notif.getTriggers().size(); i++) {
                addEntry(new Entry.TriggerField(this.width, this.notif, this.client, this, i));
            }
            addEntry(new Entry.TriggerField(this.width, this.notif, this.client, this, -1));
        }

        addEntry(new ConfigListWidget.Entry.Header(this.width, this.client,
                Component.literal("Notification Options")));

        addEntry(new Entry.SoundConfigOption(this.width, this.notif, this));
        addEntry(new Entry.ColorConfigOption(this.width, this.notif, this.client, this));
        addEntry(new Entry.FormatOptionPrimary(this.width, this.notif, this));
        addEntry(new Entry.FormatOptionSecondary(this.width, this.notif, this));

        addEntry(new ConfigListWidget.Entry.Header(this.width, this.client,
                Component.literal("Advanced Settings")));
        addEntry(new Entry.AdvancedConfigButton(this.width, this));
    }

    @Override
    public NotificationConfigListWidget resize(int width, int height, int top, int bottom) {
        NotificationConfigListWidget listWidget = new NotificationConfigListWidget(
                client, width, height, top, bottom, itemHeight, parentScreen, title, notif);
        listWidget.setScrollAmount(getScrollAmount());
        return listWidget;
    }

    @Override
    protected void reloadScreen() {
        reloadScreen(this);
    }

    private void openColorConfig() {
        assert client.screen != null;
        Component title = Component.translatable("screen.chatnotify.title.color");
        client.setScreen(new NotifConfigScreen(client.screen, title,
                new ColorConfigListWidget(client,
                        client.screen.width, client.screen.height,
                        32, client.screen.height - 32, 25,
                        client.screen, title, notif)));
    }

    private void openSoundConfig() {
        assert client.screen != null;
        Component title = Component.translatable("screen.chatnotify.title.sound");
        client.setScreen(new NotifConfigScreen(client.screen, title,
                new SoundConfigListWidget(client,
                        client.screen.width, client.screen.height,
                        32, client.screen.height - 32, 21,
                        client.screen, title, notif)));
    }

    private void openAdvancedConfig() {
        assert client.screen != null;
        Component title = Component.translatable("screen.chatnotify.title.advanced");
        client.setScreen(new NotifConfigScreen(client.screen, title,
                new AdvancedConfigListWidget(client,
                        client.screen.width, client.screen.height,
                        32, client.screen.height - 32, 25,
                        client.screen, title, notif)));
    }

    private abstract static class Entry extends ConfigListWidget.Entry {

        private static class TriggerConfigType extends Entry {
            TriggerConfigType(int width, Notification notif,
                              NotificationConfigListWidget listWidget) {
                super();
                options.add(CycleButton.booleanBuilder(
                        Component.literal("Event Key"), Component.literal("Word/Phrase"))
                                .withInitialValue(notif.triggerIsKey)
                                .create(width / 2 - 120, 0, 240, 20,
                                        Component.literal("Type"),
                                        (button, status) -> {
                                    notif.triggerIsKey = status;
                                    listWidget.reloadScreen();
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
                super();
                this.index = index;

                if (index == -1) {
                    options.add(Button.builder(Component.literal("+"),
                                    (button) -> {
                                        notif.addTrigger("");
                                        listWidget.reloadScreen();
                                    })
                            .size(240, 20)
                            .pos(width / 2 - 120, 0)
                            .build());
                }
                else if (index >= 0) {
                    EditBox triggerEdit = new EditBox(
                            client.font, width / 2 - 120, 0, 240,
                            20, Component.literal("Notification Trigger"));
                    triggerEdit.setMaxLength(120);
                    triggerEdit.setValue(notif.getTrigger(index));
                    triggerEdit.setResponder((trigger) ->
                            notif.setTrigger(index, trigger.strip()));
                    this.options.add(triggerEdit);

                    if (index != 0) {
                        options.add(Button.builder(Component.literal("X"),
                                        (button) -> {
                                            notif.removeTrigger(index);
                                            listWidget.reloadScreen();
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
                             NotificationConfigListWidget listWidget,
                             String[] keyLeft, String[] keyRight) {
                super();
                options.add(Button.builder(Component.literal(keyLeft[1]),
                        (button) -> {
                            notif.setTrigger(keyLeft[0]);
                            listWidget.reloadScreen();
                        })
                        .size(117, 20)
                        .pos(width / 2 - 120, 0)
                        .build());
                options.add(Button.builder(Component.literal(keyRight[1]),
                                (button) -> {
                                    notif.setTrigger(keyRight[0]);
                                    listWidget.reloadScreen();
                                })
                        .size(117, 20)
                        .pos(width / 2 + 3, 0)
                        .build());
            }
        }

        private static class SoundConfigOption extends Entry {
            SoundConfigOption(int width, Notification notif,
                              NotificationConfigListWidget listWidget) {
                super();
                options.add(Button.builder(
                                Component.literal("Sound: " + notif.getSound().toString()),
                                (button) -> listWidget.openSoundConfig())
                        .size(210, 20)
                        .pos(width / 2 - 120, 0)
                        .build());
                options.add(CycleButton.onOffBuilder()
                        .displayOnlyValue()
                        .withInitialValue(notif.getControl(2))
                        .create(width / 2 + 95, 0, 25, 20, Component.empty(),
                                (button, status) -> {
                                    notif.setControl(2, status);
                                    listWidget.reloadScreen();
                                }));
            }
        }

        private static class ColorConfigOption extends Entry {
            ColorConfigOption(int width, Notification notif, Minecraft client,
                              NotificationConfigListWidget listWidget) {
                super();

                MutableComponent message = Component.literal("Text Color");
                if (notif.getColor() != null) {
                    message.setStyle(Style.EMPTY.withColor(notif.getColor()));
                }
                options.add(Button.builder(message, (button) -> listWidget.openColorConfig())
                        .size(120, 20)
                        .pos(width / 2 - 120, 0)
                        .build());

                EditBox colorEdit = new EditBox(client.font, width / 2 + 6, 0, 64, 20,
                        Component.literal("Hex Color"));
                colorEdit.setMaxLength(7);
                colorEdit.setResponder(color -> notif.setColor(notif.parseColor(color)));
                if (notif.getColor() != null) {
                    colorEdit.setValue(notif.getColor().formatValue());
                }
                options.add(colorEdit);

                options.add(Button.builder(Component.literal("\ud83d\uddd8"),
                                (button) -> listWidget.reloadScreen())
                        .tooltip(Tooltip.create(Component.literal("Check Value")))
                        .size(20, 20)
                        .pos(width / 2 + 70, 0)
                        .build());

                options.add(CycleButton.onOffBuilder()
                        .displayOnlyValue()
                        .withInitialValue(notif.getControl(0))
                        .create(width / 2 + 95, 0, 25, 20, Component.empty(),
                                (button, status) -> {
                                    notif.setControl(0, status);
                                    listWidget.reloadScreen();
                                }));
            }
        }

        private static class FormatOptionPrimary extends Entry {
            FormatOptionPrimary(int width, Notification notif,
                                NotificationConfigListWidget listWidget) {
                super();
                options.add(CycleButton.onOffBuilder()
                        .withInitialValue(notif.getFormatControl(0))
                        .create(width / 2 - 120, 0, 76, 20, Component.literal("Bold")
                                .withStyle(Style.EMPTY.withBold(
                                        notif.getFormatControl(0))),
                                (button, status) -> {
                                    notif.setFormatControl(0, status);
                                    listWidget.reloadScreen();
                                }));
                options.add(CycleButton.onOffBuilder()
                        .withInitialValue(notif.getFormatControl(1))
                        .create(width / 2 - 38, 0, 76, 20, Component.literal("Italic")
                                        .withStyle(Style.EMPTY.withItalic(
                                                notif.getFormatControl(1))),
                                (button, status) -> {
                                    notif.setFormatControl(1, status);
                                    listWidget.reloadScreen();
                                }));
                options.add(CycleButton.onOffBuilder()
                        .withInitialValue(notif.getFormatControl(2))
                        .create(width / 2 + 44, 0, 76, 20, Component.literal("Underline")
                                        .withStyle(Style.EMPTY.withUnderlined(
                                                notif.getFormatControl(2))),
                                (button, status) -> {
                                    notif.setFormatControl(2, status);
                                    listWidget.reloadScreen();
                                }));
            }
        }

        private static class FormatOptionSecondary extends Entry {
            FormatOptionSecondary(int width, Notification notif,
                                  NotificationConfigListWidget listWidget) {
                super();
                options.add(CycleButton.onOffBuilder()
                        .withInitialValue(notif.getFormatControl(3))
                        .create(width / 2 - 120, 0, 117, 20, Component.literal("Strikethrough")
                                        .withStyle(Style.EMPTY.withStrikethrough(
                                                notif.getFormatControl(3))),
                                (button, status) -> {
                                    notif.setFormatControl(3, status);
                                    listWidget.reloadScreen();
                                }));
                options.add(CycleButton.onOffBuilder()
                        .withInitialValue(notif.getFormatControl(4))
                        .create(width / 2 + 3, 0, 117, 20, Component.literal("Obfuscate")
                                        .withStyle(Style.EMPTY.withObfuscated(
                                                notif.getFormatControl(4))),
                                (button, status) -> {
                                    notif.setFormatControl(4, status);
                                    listWidget.reloadScreen();
                                }));
            }
        }

        private static class AdvancedConfigButton extends Entry {
            AdvancedConfigButton(int width, NotificationConfigListWidget listWidget) {
                super();
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