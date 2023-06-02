package notryken.chatnotify.gui.listwidget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.gui.screen.ConfigScreen;
import org.jetbrains.annotations.NotNull;

public class NotificationConfigListWidget extends ConfigListWidget
{
    private final Notification notif;

    public NotificationConfigListWidget(MinecraftClient client,
                                        int width, int height,
                                        int top, int bottom, int itemHeight,
                                        Screen parent, Text title,
                                        Notification notif)
    {
        super(client, width, height, top, bottom, itemHeight, parent, title);
        this.notif = notif;

        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Text.literal("Notification Trigger"), Text.literal(
                        "A trigger is a case-insensitive word or series of " +
                                "words that, if detected in a chat message, " +
                                "will activate the notification.")));
        this.addEntry(new Entry.TriggerConfigType(width, notif, this));

        if (notif.triggerIsKey) {
            this.addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                    Text.literal("May not work on some servers.")));
            this.addEntry(new Entry.TriggerField(width, notif, client, this,
                    0));
            this.addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                    Text.literal("Quick Keys")));

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
                Text.literal("Message Color")));
        this.addEntry(new Entry.ColorConfigButton(width, notif, this));

        this.addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                Text.literal("Message Format")));
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
                Text.literal("Advanced Settings")));
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
        assert client.currentScreen != null;
        Text title = Text.literal("Notification Message Color");
        client.setScreen(new ConfigScreen(client.currentScreen, client.options,
                title, new ColorConfigListWidget(client,
                client.currentScreen.width, client.currentScreen.height,
                32, client.currentScreen.height - 32, 25,
                client.currentScreen, title, this.notif)));
    }

    private void openSoundConfig()
    {
        assert client.currentScreen != null;
        Text title = Text.literal("Notification Sound");
        client.setScreen(new ConfigScreen(client.currentScreen, client.options,
                title, new SoundConfigListWidget(client,
                client.currentScreen.width, client.currentScreen.height,
                32, client.currentScreen.height - 32, 25,
                client.currentScreen, title, notif)));
    }

    private void openAdvancedConfig()
    {
        assert client.currentScreen != null;
        Text title = Text.literal("Advanced Options");
        client.setScreen(new ConfigScreen(client.currentScreen, client.options,
                title, new AdvancedConfigListWidget(client,
                client.currentScreen.width, client.currentScreen.height,
                32, client.currentScreen.height - 32, 25,
                client.currentScreen, title, notif)));
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
                options.add(CyclingButtonWidget.onOffBuilder(
                        Text.literal("Event Key"), Text.literal("Word/Phrase"))
                                .initially(notif.triggerIsKey)
                                .build(this.width / 2 - 120, 0, 240, 20,
                                        Text.literal("Type"),
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
                         @NotNull MinecraftClient client,
                         NotificationConfigListWidget listWidget, int index)
            {
                super(width, notif, listWidget);
                this.index = index;

                if (index >= 0) {
                    TextFieldWidget triggerEdit = new TextFieldWidget(
                            client.textRenderer, this.width / 2 - 120, 0, 240,
                            20, Text.literal("Notification Trigger"));
                    triggerEdit.setMaxLength(120);
                    triggerEdit.setText(this.notif.getTrigger(index));
                    triggerEdit.setChangedListener(this::setTrigger);

                    this.options.add(triggerEdit);

                    if (index != 0) {
                        options.add(ButtonWidget.builder(Text.literal("X"),
                                        (button) -> {
                                            notif.removeTrigger(index);
                                            listWidget.refreshScreen();
                                        })
                                .size(25, 20)
                                .position(width / 2 + 120 + 5, 0)
                                .build());
                    }
                }
                else {
                    options.add(ButtonWidget.builder(Text.literal("+"),
                                    (button) -> {
                                        notif.addTrigger("");
                                        listWidget.refreshScreen();
                                    })
                            .size(240, 20)
                            .position(width / 2 - 120, 0)
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

                options.add(ButtonWidget.builder(Text.literal(key[1]),
                        (button) -> {
                            notif.setTrigger(key[0]);
                            listWidget.refreshScreen();
                        })
                        .size(240, 20)
                        .position(width / 2 - 120, 0)
                        .build());
            }
        }

        private static class SoundConfigButton extends Entry
        {
            SoundConfigButton(int width, Notification notif,
                              NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                options.add(ButtonWidget.builder(
                                Text.literal(notif.getSound().toString()),
                                (button) -> listWidget.openSoundConfig())
                        .size(240, 20)
                        .position(width / 2 - 120, 0)
                        .build());
            }
        }

        private static class ControlConfigHeader extends Entry
        {
            ControlConfigHeader(int width, Notification notif,
                                @NotNull MinecraftClient client,
                                NotificationConfigListWidget listWidget,
                                String label, int index)
            {
                super(width, notif, listWidget);
                options.add(new TextWidget(width / 2 - 60, 0, 120, 20,
                        Text.literal(label),
                        client.textRenderer));
                options.add(CyclingButtonWidget.onOffBuilder()
                        .omitKeyText()
                        .initially(notif.getControl(index))
                        .build(this.width / 2 + 60, 0, 25, 20, Text.empty(),
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

                MutableText message;

                if (notif.getColor().getRgb() == 16777215) {
                    message = Text.literal("[No Color]");
                }
                else {
                    message = Text.literal(notif.getColor().getHexCode());
                    message.setStyle(Style.EMPTY.withColor(notif.getColor()));
                }
                options.add(ButtonWidget.builder(message,
                                (button) -> listWidget.openColorConfig())
                        .size(240, 20)
                        .position(width / 2 - 120, 0)
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
                options.add(CyclingButtonWidget.onOffBuilder()
                        .initially(notif.getFormatControl(index))
                        .build(this.width / 2 - 120, 0, 240, 20,
                                Text.literal(label), (button, status) -> {
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

                options.add(ButtonWidget.builder(
                        Text.literal("Here be dragons!"),
                                (button) -> listWidget.openAdvancedConfig())
                        .size(240, 20)
                        .position(width / 2 - 120, 0)
                        .build());
            }
        }
    }
}