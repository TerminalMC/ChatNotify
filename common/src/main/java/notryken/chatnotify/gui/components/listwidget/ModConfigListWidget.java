package notryken.chatnotify.gui.components.listwidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundSource;
import notryken.chatnotify.ChatNotify;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.gui.screen.ConfigScreen;
import notryken.chatnotify.gui.screen.ScreenLauncher;

import java.util.List;
import java.util.Optional;

public class ModConfigListWidget extends ConfigListWidget
{
    public ModConfigListWidget(Minecraft client, int width, int height,
                               int top, int bottom, int itemHeight,
                               Screen parent, Component title)
    {
        super(client, width, height, top, bottom, itemHeight, parent, title);

        this.addEntry(new ConfigListWidget.Entry.Header(this.width, this,
                client, Component.literal("Options")));

        this.addEntry(new Entry.IgnoreToggle(this.width, this));
        this.addEntry(new Entry.SoundSourceButton(this.width, this));
        this.addEntry(new Entry.PrefixConfigButton(this.width, this));

        this.addEntry(new ConfigListWidget.Entry.Header(this.width, this,
                client, Component.literal("Notifications \u2139"),
                Component.literal("Notifications are processed in sequential " +
                        "order as displayed below. No more than one " +
                        "notification can activate at a time.")));

        int max = ChatNotify.config().getNumNotifs();
        for (int idx = 0; idx < max; idx++) {
            this.addEntry(new Entry.NotifButton(this.width, this, idx));
        }
        this.addEntry(new Entry.NotifButton(this.width, this, -1));
    }

    @Override
    public ModConfigListWidget resize(int width, int height,
                                      int top, int bottom)
    {
        ModConfigListWidget listWidget = new ModConfigListWidget(client,
                width, height, top, bottom, itemHeight, parent, title);
        listWidget.setScrollAmount(this.getScrollAmount());
        return listWidget;
    }

    @Override
    protected void refreshScreen()
    {
        client.setScreen(new ScreenLauncher.MainOptionsScreen(parent));
    }

    private void openPrefixConfig()
    {
        assert client.screen != null;
        Component title = Component.literal("Message Modifier Prefixes");
        client.setScreen(new ConfigScreen(client.screen, client.options,
                title, new PrefixConfigListWidget(client,
                client.screen.width, client.screen.height,
                32, client.screen.height - 32, 25,
                client.screen, title)));
    }

    private void moveNotifUp(int index)
    {
        ChatNotify.config().moveNotifUp(index);
        refreshScreen();
    }

    private void moveNotifDown(int index)
    {
        ChatNotify.config().moveNotifDown(index);
        refreshScreen();
    }

    private void addNotification()
    {
        ChatNotify.config().addNotif();
        refreshScreen();
    }

    private void removeNotification(int index)
    {
        if (ChatNotify.config().removeNotif(index) == 0) {
            refreshScreen();
        }
    }

    private void openNotificationConfig(int index)
    {
        assert client.screen != null;
        Component title = Component.literal("Notification Settings");
        client.setScreen(new ConfigScreen(client.screen, client.options,
                title, new NotificationConfigListWidget(client,
                client.screen.width, client.screen.height,
                32, client.screen.height - 32, 25,
                client.screen, title, ChatNotify.config().getNotif(index))));
    }

    public static class Entry extends ConfigListWidget.Entry
    {
        private final int index;

        Entry(int width, ModConfigListWidget listWidget, int index)
        {
            super(width, listWidget);
            this.index = index;
        }

        private static class IgnoreToggle extends Entry
        {
            IgnoreToggle(int width, ModConfigListWidget listWidget)
            {
                super(width, listWidget, -1);

                CycleButton<Boolean> ignoreButton =
                        CycleButton.booleanBuilder(
                                Component.nullToEmpty("No"), Component.nullToEmpty("Yes"))
                                .withInitialValue(ChatNotify.config().ignoreOwnMessages)
                                .withTooltip((status) -> Tooltip.create(Component.nullToEmpty(
                                        "Allows/Prevents your own messages " +
                                                "triggering notifications.")))
                                .create(this.width / 2 - 120, 32, 240, 20,
                                Component.literal("Check Own Messages"),
                                (button, status) ->
                                        ChatNotify.config().ignoreOwnMessages = status);
                ignoreButton.setTooltip(Tooltip.create(Component.literal(
                        "Allows/Prevents your own messages triggering " +
                        "notifications.")));
                options.add(ignoreButton);
            }
        }

        private static class SoundSourceButton extends Entry
        {
            SoundSourceButton(int width, ModConfigListWidget listWidget)
            {
                super(width, listWidget, -1);

                CycleButton<SoundSource> ignoreButton =
                        CycleButton.<SoundSource>builder(source ->
                                        Component.translatable("soundCategory." + source.getName()))
                                .withValues(SoundSource.values())
                                .withInitialValue(ChatNotify.config().notifSoundSource)
                                .withTooltip((status) -> Tooltip.create(Component.nullToEmpty(
                                        "Notification sound volume control category.")))
                                .create(this.width / 2 - 120, 32, 240, 20,
                                        Component.literal("Sound Source"),
                                        (button, status) ->
                                                ChatNotify.config().notifSoundSource = status);
                ignoreButton.setTooltip(Tooltip.create(Component.literal(
                        "Allows/Prevents your own messages triggering " +
                                "notifications.")));
                options.add(ignoreButton);
            }
        }


        private static class PrefixConfigButton extends Entry
        {
            PrefixConfigButton(int width, ModConfigListWidget listWidget)
            {
                super(width, listWidget, -1);

                List<String> prefixes = ChatNotify.config().getPrefixes();
                String label = "Prefixes: ";

                if (prefixes.isEmpty()) {
                    label = label + "[None]";
                }
                else {
                    StringBuilder builder = new StringBuilder(label);
                    builder.append(ChatNotify.config().getPrefix(0));
                    for (int i = 1; i < prefixes.size(); i++) {
                        builder.append(", ");
                        builder.append(prefixes.get(i));
                    }
                    label = builder.toString();
                }

                options.add(Button.builder(
                                Component.literal(label),
                                (button) -> listWidget.openPrefixConfig())
                        .size(240, 20)
                        .pos(width / 2 - 120, 0)
                        .build());
            }
        }

        private static class NotifButton extends Entry
        {
            NotifButton(int width, ModConfigListWidget listWidget, int index)
            {
                super(width, listWidget, index);

                if (index >= 0) {
                    Notification notif = ChatNotify.config().getNotif(index);
                    String label = notif.getTrigger();
                    if (label.isEmpty()) {
                        label = "> Click to Configure <";
                    }
                    else {
                        if (notif.triggerIsKey) {
                            label = "[Key] " + label;
                        }
                        else {
                            StringBuilder builder = new StringBuilder(label);
                            for (int i = 1; i < notif.getTriggers().size(); i++)
                            {
                                builder.append(", ");
                                builder.append(notif.getTrigger(i));
                            }
                            label = builder.toString();
                        }
                    }

                    MutableComponent labelText = Component.literal(label)
                            .setStyle(Style.create(
                                    (notif.getColor() == null ?
                                            Optional.empty() :
                                            Optional.of(notif.getColor())),
                                    Optional.of(notif.getFormatControl(0)),
                                    Optional.of(notif.getFormatControl(1)),
                                    Optional.of(notif.getFormatControl(2)),
                                    Optional.of(notif.getFormatControl(3)),
                                    Optional.of(notif.getFormatControl(4)),
                                    Optional.empty(),
                                    Optional.empty()));

                    options.add(Button.builder(labelText, (button) ->
                                    listWidget.openNotificationConfig(index))
                            .size(210, 20)
                            .pos(width / 2 - 120, 0)
                            .build());

                    options.add(CycleButton.onOffBuilder()
                            .displayOnlyValue()
                            .withInitialValue(notif.enabled)
                            .create(width / 2 + 95, 0, 25, 20,
                                    Component.empty(), (button, status)
                                            -> notif.enabled = status));

                    if (index > 0) {
                        options.add(Button.builder(Component.literal("⬆"),
                                        (button) ->
                                                listWidget.moveNotifUp(index))
                                .size(12, 20)
                                .pos(width / 2 - 120 - 29, 0)
                                .build());

                        options.add(Button.builder(Component.literal("⬇"),
                                        (button) ->
                                                listWidget.moveNotifDown(index))
                                .size(12, 20)
                                .pos(width / 2 - 120 - 17, 0)
                                .build());

                        options.add(Button.builder(Component.literal("X"),
                                        (button) -> listWidget
                                                .removeNotification(index))
                                .size(25, 20)
                                .pos(width / 2 + 120 + 5, 0)
                                .build());
                    }
                }
                else {
                    options.add(Button.builder(Component.literal("+"),
                                    (button) -> listWidget.addNotification())
                            .size(240, 20)
                            .pos(width / 2 - 120, 0)
                            .build());
                }
            }
        }

        @Override
        public void render(GuiGraphics context, int index, int y, int x,
                           int entryWidth, int entryHeight, int mouseX,
                           int mouseY, boolean hovered, float tickDelta) {
            options.forEach((button) -> {
                button.setY(y);
                button.render(context, mouseX, mouseY, tickDelta);
            });

            if (this.index >= 1) {
                context.drawCenteredString(
                        listWidget.client.font,
                        String.valueOf(this.index),
                        -36, y + entryHeight / 3, 16777215);
            }
        }
    }

}