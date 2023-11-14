package notryken.chatnotify.gui.listwidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.gui.screen.ConfigScreen;
import org.jetbrains.annotations.NotNull;

public class NotificationConfigListWidget extends ConfigListWidget
{
    private final Notification notif;

    public NotificationConfigListWidget(Minecraft client,
                                        int width, int height,
                                        int top, int bottom, int itemHeight,
                                        Screen parent, Component title,
                                        Notification notif)
    {
        super(client, width, height, top, bottom, itemHeight, parent, title);
        this.notif = notif;

        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Component.literal("Notification Trigger ℹ"),
                Component.literal("A trigger is a word or series of words that, " +
                        "if detected in a chat message, will activate the " +
                        "notification. NOT case-sensitive.")));
        this.addEntry(new Entry.TriggerConfigType(width, notif, this));

        if (notif.triggerIsKey) {
            this.addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                    Component.literal("May not work on some servers.")));
            this.addEntry(new Entry.TriggerField(width, notif, client, this,
                    0));
            this.addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                    Component.literal("Quick Keys")));

            String[][] keys =
                    {
                            {"chat.type", "Any Message"},
                            {"commands.message.display", "Private Message"},
                            {"multiplayer.player.joined", "Player Joined"},
                            {"multiplayer.player.left", "Player Left"},
                            {"chat.type.advancement", "Advancement"},
                            {"death.", "Player/Pet Died"}
                    };
            for (String[] key : keys) {
                this.addEntry(new Entry.TriggerOption(width, notif, this, key));
            }
        }
        else {
            for (int i = 0; i < notif.getTriggers().size(); i++) {
                this.addEntry(new Entry.TriggerField(width, notif, client, this,
                        i));
            }
            this.addEntry(new Entry.TriggerField(width, notif, client, this,
                    -1));
        }

        this.addEntry(new Entry.ControlConfigHeader(width, notif, client, this,
                "Notification Sound", 2));
        this.addEntry(new Entry.SoundConfigButton(width, notif, this));

        this.addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                Component.literal("Message Color")));
        this.addEntry(new Entry.ColorConfigButton(width, notif, this));

        this.addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                Component.literal("Message Format")));
        this.addEntry(new Entry.FormatConfigOption(
                width, notif, this, "Bold", 0));
        this.addEntry(new Entry.FormatConfigOption(
                width, notif, this, "Italic", 1));
        this.addEntry(new Entry.FormatConfigOption(
                width, notif, this, "Underlined", 2));
        this.addEntry(new Entry.FormatConfigOption(
                width, notif, this, "Strikethrough", 3));
        this.addEntry(new Entry.FormatConfigOption(
                width, notif, this, "Obfuscated", 4));

        this.addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                Component.literal("Advanced Settings")));
        this.addEntry(new Entry.AdvancedConfigButton(width, notif, this));
    }

    @Override
    public NotificationConfigListWidget resize(int width, int height,
                                               int top, int bottom)
    {
        NotificationConfigListWidget listWidget =
                new NotificationConfigListWidget(client, width, height, top,
                        bottom, itemHeight, parent, title, notif);
        listWidget.setScrollAmount(this.getScrollAmount());
        return listWidget;
    }

    @Override
    protected void refreshScreen()
    {
        refreshScreen(this);
    }

    private void openColorConfig()
    {
        assert client.screen != null;
        Component title = Component.literal("Notification Message Color");
        client.setScreen(new ConfigScreen(client.screen, client.options,
                title, new ColorConfigListWidget(client,
                client.screen.width, client.screen.height,
                32, client.screen.height - 32, 25,
                client.screen, title, this.notif)));
    }

    private void openSoundConfig()
    {
        assert client.screen != null;
        Component title = Component.literal("Notification Sound");
        client.setScreen(new ConfigScreen(client.screen, client.options,
                title, new SoundConfigListWidget(client,
                client.screen.width, client.screen.height,
                32, client.screen.height - 32, 25,
                client.screen, title, notif)));
    }

    private void openAdvancedConfig()
    {
        assert client.screen != null;
        Component title = Component.literal("Advanced Options");
        client.setScreen(new ConfigScreen(client.screen, client.options,
                title, new AdvancedConfigListWidget(client,
                client.screen.width, client.screen.height,
                32, client.screen.height - 32, 25,
                client.screen, title, notif)));
    }

    private abstract static class Entry extends ConfigListWidget.Entry
    {
        public final Notification notif;

        Entry(int width, Notification notif,
              NotificationConfigListWidget listWidget)
        {
            super(width, listWidget);
            this.notif = notif;
        }

        private static class TriggerConfigType extends Entry
        {
            TriggerConfigType(int width, Notification notif,
                              NotificationConfigListWidget listWidget)
            {
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

        private static class TriggerField extends Entry
        {
            final int index;

            TriggerField(int width, Notification notif,
                         @NotNull Minecraft client,
                         NotificationConfigListWidget listWidget, int index)
            {
                super(width, notif, listWidget);
                this.index = index;

                if (index >= 0) {
                    EditBox triggerEdit = new EditBox(
                            client.font, this.width / 2 - 120, 0, 240,
                            20, Component.literal("Notification Trigger"));
                    triggerEdit.setMaxLength(120);
                    triggerEdit.setValue(this.notif.getTrigger(index));
                    triggerEdit.setResponder(this::setTrigger);

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
                else {
                    options.add(Button.builder(Component.literal("+"),
                                    (button) -> {
                                        notif.addTrigger("");
                                        listWidget.refreshScreen();
                                    })
                            .size(240, 20)
                            .pos(width / 2 - 120, 0)
                            .build());
                }
            }

            private void setTrigger(String trigger)
            {
                notif.setTrigger(index, trigger.strip());
            }
        }

        private static class TriggerOption extends Entry
        {
            TriggerOption(int width, Notification notif,
                          NotificationConfigListWidget listWidget, String[] key)
            {
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

        private static class SoundConfigButton extends Entry
        {
            SoundConfigButton(int width, Notification notif,
                              NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                options.add(Button.builder(
                                Component.literal(notif.getSound().toString()),
                                (button) -> listWidget.openSoundConfig())
                        .size(240, 20)
                        .pos(width / 2 - 120, 0)
                        .build());
            }
        }

        private static class ControlConfigHeader extends Entry
        {
            ControlConfigHeader(int width, Notification notif,
                                @NotNull Minecraft client,
                                NotificationConfigListWidget listWidget,
                                String label, int index)
            {
                super(width, notif, listWidget);
                options.add(new StringWidget(width / 2 - 60, 0, 120, 20,
                        Component.literal(label),
                        client.font));
                options.add(CycleButton.onOffBuilder()
                        .displayOnlyValue()
                        .withInitialValue(notif.getControl(index))
                        .create(this.width / 2 + 60, 0, 25, 20, Component.empty(),
                                (button, status) -> {
                                    notif.setControl(index, status);
                                    listWidget.refreshScreen();
                                }));
            }
        }

        private static class ColorConfigButton extends Entry
        {
            ColorConfigButton(int width, Notification notif,
                              NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                MutableComponent message;

                if (notif.getColor() == null) {
                    message = Component.literal("[No Color]");
                }
                else {
                    message = Component.literal(notif.getColor().formatValue());
                    message.setStyle(Style.EMPTY.withColor(notif.getColor()));
                }
                options.add(Button.builder(message,
                                (button) -> listWidget.openColorConfig())
                        .size(240, 20)
                        .pos(width / 2 - 120, 0)
                        .build());
            }
        }

        private static class FormatConfigOption extends Entry
        {
            FormatConfigOption(int width, Notification notif,
                               NotificationConfigListWidget listWidget,
                               String label, int index)
            {
                super(width, notif, listWidget);
                options.add(CycleButton.onOffBuilder()
                        .withInitialValue(notif.getFormatControl(index))
                        .create(this.width / 2 - 120, 0, 240, 20,
                                Component.literal(label), (button, status) -> {
                                    notif.setFormatControl(index, status);
                                    listWidget.refreshScreen();
                                }));
            }
        }

        private static class AdvancedConfigButton extends Entry
        {
            AdvancedConfigButton(int width, Notification notif,
                                 NotificationConfigListWidget listWidget)
            {
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