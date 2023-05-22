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

import java.util.Optional;

public class NotificationConfigListWidget extends ConfigListWidget
{
    private final Notification notif;

    public NotificationConfigListWidget(MinecraftClient client,
                                        int i, int j, int k, int l, int m,
                                        Screen parent, Text title,
                                        Notification notif)
    {
        super(client, i, j, k, l, m, parent, title);
        this.notif = notif;

        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Text.literal("Notification Trigger")));
        this.addEntry(new Entry.TriggerConfigType(width, notif, this));

        if (notif.triggerIsKey) {
            this.addEntry(new Entry.KeyTriggerConfig(width, notif, this));
        }
        else {
            this.addEntry(new Entry.TriggerField(width, notif, client, this));
            this.addEntry(new Entry.TriggerVariationConfig(width, notif, this));
        }

        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Text.literal("Message Color")));
        this.addEntry(new Entry.ColorConfig(width, notif, this));
        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Text.literal("Message Format")));
        this.addEntry(new Entry.FormatConfig(
                width, notif, this, "Bold", 0));
        this.addEntry(new Entry.FormatConfig(
                width, notif, this, "Italic", 1));
        this.addEntry(new Entry.FormatConfig(
                width, notif, this, "Underlined", 2));
        this.addEntry(new Entry.FormatConfig(
                width, notif, this, "Strikethrough", 3));
        this.addEntry(new Entry.FormatConfig(
                width, notif, this, "Obfuscated", 4));
        this.addEntry(new Entry.ControlConfigHeader(
                width, notif, client, this, "Notification Sound", 2));
        this.addEntry(new Entry.SoundConfig(width, notif, this));
    }

    @Override
    public NotificationConfigListWidget resize(int width, int height,
                                               int top, int bottom)
    {
        assert client.currentScreen != null;
        return new NotificationConfigListWidget(client, width, height, top,
                bottom, itemHeight, parent, title, notif);
    }

    @Override
    protected void refreshScreen()
    {
        refreshScreen(new NotificationConfigListWidget(client,
                this.width, this.height, this.top, this.bottom, this.itemHeight,
                this.parent, this.title, this.notif));
    }

    private void openKeyTriggerConfig()
    {
        assert client.currentScreen != null;
        Text title = Text.literal("Notification Trigger Key");
        client.setScreen(new ConfigScreen(client.currentScreen, client.options,
                title, new KeyTriggerConfigListWidget(client,
                client.currentScreen.width, client.currentScreen.height,
                32, client.currentScreen.height - 32, 25,
                client.currentScreen, title, this.notif)));
    }

    private void openTriggerVariationConfig()
    {
        assert client.currentScreen != null;
        Text title = Text.literal("Notification Trigger Variations");
        client.setScreen(new ConfigScreen(client.currentScreen, client.options,
                title, new TriggerVariationConfigListWidget(client,
                client.currentScreen.width, client.currentScreen.height,
                32, client.currentScreen.height - 32, 25,
                client.currentScreen, title, this.notif)));
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

        private static class KeyTriggerConfig extends Entry
        {
            KeyTriggerConfig(int width, Notification notif,
                             NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                String label = notif.getTrigger();
                if (label.equals("")) {
                    label = "> Click to Set <";
                }
                else {
                    label = "[Key] " + label;
                }

                options.add(ButtonWidget.builder(Text.literal(label),
                                (button) -> listWidget.openKeyTriggerConfig())
                        .size(240, 20).position(width / 2 - 120, 0).build());
            }
        }

        private static class TriggerField extends Entry {
            TriggerField(int width, Notification notif,
                         @NotNull MinecraftClient client,
                         NotificationConfigListWidget listWidget) {
                super(width, notif, listWidget);

                TextFieldWidget triggerEdit = new TextFieldWidget(
                        client.textRenderer, this.width / 2 - 120, 0, 240, 20,
                        Text.literal("Notification Trigger"));
                triggerEdit.setMaxLength(120);
                triggerEdit.setText(this.notif.getTrigger());
                triggerEdit.setChangedListener(this::setTrigger);

                options.add(triggerEdit);
            }

            private void setTrigger(String trigger)
            {
                notif.setTrigger(trigger.strip());
            }
        }

        private static class TriggerVariationConfig extends Entry
        {
            TriggerVariationConfig(int width, Notification notif,
                                   NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                options.add(ButtonWidget.builder(
                        Text.literal("Trigger Variations (" +
                                (notif.getNumTriggers() - 1) + ")"),
                                (button) -> {
                                    if (notif.getTrigger() != null &&
                                            !notif.getTrigger().equals("")) {
                                        listWidget.openTriggerVariationConfig();
                                    }})
                        .size(240, 20).position(width / 2 - 120, 0).build());
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

        private static class ColorConfig extends Entry
        {
            ColorConfig(int width, Notification notif,
                        NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                MutableText message;

                if (notif.getColor().getRgb() == 16777215) {
                    message = Text.literal("[No Color]");
                }
                else {
                    message = Text.literal(notif.getColor().getHexCode());

                    message.setStyle(Style.of(
                            Optional.of(notif.getColor()),
                            Optional.of(false),
                            Optional.of(false),
                            Optional.of(false),
                            Optional.of(false),
                            Optional.of(false),
                            Optional.empty(),
                            Optional.empty()));
                }
                options.add(ButtonWidget.builder(message,
                                (button) -> listWidget.openColorConfig())
                        .size(240, 20).position(width / 2 - 120, 0).build());
            }
        }

        private static class FormatConfig extends Entry
        {
            FormatConfig(int width, Notification notif,
                         NotificationConfigListWidget listWidget, String label,
                         int index)
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

        private static class SoundConfig extends Entry
        {
            SoundConfig(int width, Notification notif,
                        NotificationConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                options.add(ButtonWidget.builder(
                        Text.literal(notif.getSound().toString()),
                        (button) -> listWidget.openSoundConfig())
                        .size(240, 20).position(width / 2 - 120, 0).build());
            }
        }
    }
}