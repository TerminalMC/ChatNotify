package notryken.chatnotify.gui.listwidget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.gui.screen.ConfigScreen;
import notryken.chatnotify.gui.screen.ModMenuIntegration;

import java.util.List;
import java.util.Optional;

import static notryken.chatnotify.ChatNotifyFabric.config;

public class ModConfigListWidget extends ConfigListWidget
{
    public ModConfigListWidget(MinecraftClient client, int width, int height,
                               int top, int bottom, int itemHeight,
                               Screen parent, Text title)
    {
        super(client, width, height, top, bottom, itemHeight, parent, title);

        this.addEntry(new ConfigListWidget.Entry.Header(this.width, this,
                client, Text.literal("Options")));

        this.addEntry(new Entry.IgnoreToggle(this.width, this));
        this.addEntry(new Entry.PrefixConfigButton(this.width, this));

        this.addEntry(new ConfigListWidget.Entry.Header(this.width, this,
                client, Text.literal("Notifications ℹ"),
                Text.literal("Notifications are processed in sequential " +
                        "order as displayed below. No more than one " +
                        "notification can activate at a time.")));

        int max = config.getNumNotifs();
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
        client.setScreen(new ModMenuIntegration.ModMenuOptionsScreen(parent));
    }

    private void openPrefixConfig()
    {
        assert client.currentScreen != null;
        Text title = Text.literal("Message Modifier Prefixes");
        client.setScreen(new ConfigScreen(client.currentScreen, client.options,
                title, new PrefixConfigListWidget(client,
                client.currentScreen.width, client.currentScreen.height,
                32, client.currentScreen.height - 32, 25,
                client.currentScreen, title)));
    }

    private void moveNotifUp(int index)
    {
        config.moveNotifUp(index);
        refreshScreen();
    }

    private void moveNotifDown(int index)
    {
        config.moveNotifDown(index);
        refreshScreen();
    }

    private void addNotification()
    {
        config.addNotif();
        refreshScreen();
    }

    private void removeNotification(int index)
    {
        if (config.removeNotif(index) == 0) {
            refreshScreen();
        }
    }

    private void openNotificationConfig(int index)
    {
        assert client.currentScreen != null;
        Text title = Text.literal("Notification Settings");
        client.setScreen(new ConfigScreen(client.currentScreen, client.options,
                title, new NotificationConfigListWidget(client,
                client.currentScreen.width, client.currentScreen.height,
                32, client.currentScreen.height - 32, 25,
                client.currentScreen, title, config.getNotif(index))));
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

                CyclingButtonWidget<Boolean> ignoreButton =
                        CyclingButtonWidget.onOffBuilder(
                                Text.of("No"), Text.of("Yes"))
                                .initially(config.ignoreOwnMessages)
                                .tooltip((status) -> Tooltip.of(Text.of(
                                        "Allows/Prevents your own messages " +
                                                "triggering notifications.")))
                                .build(this.width / 2 - 120, 32, 240, 20,
                                Text.literal("Check Own Messages"),
                                (button, status) ->
                                        config.ignoreOwnMessages = status);
                ignoreButton.setTooltip(Tooltip.of(Text.literal(
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

                List<String> prefixes = config.getPrefixes();
                String label = "Prefixes: ";

                if (prefixes.isEmpty()) {
                    label = label + "[None]";
                }
                else {
                    StringBuilder builder = new StringBuilder(label);
                    builder.append(config.getPrefix(0));
                    for (int i = 1; i < prefixes.size(); i++) {
                        builder.append(", ");
                        builder.append(prefixes.get(i));
                    }
                    label = builder.toString();
                }

                options.add(ButtonWidget.builder(
                                Text.literal(label),
                                (button) -> listWidget.openPrefixConfig())
                        .size(240, 20)
                        .position(width / 2 - 120, 0)
                        .build());
            }
        }

        private static class NotifButton extends Entry
        {
            NotifButton(int width, ModConfigListWidget listWidget, int index)
            {
                super(width, listWidget, index);

                if (index >= 0) {
                    Notification notif = config.getNotif(index);
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

                    MutableText labelText = Text.literal(label)
                            .setStyle(Style.of(
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

                    options.add(ButtonWidget.builder(labelText, (button) ->
                                    listWidget.openNotificationConfig(index))
                            .size(210, 20)
                            .position(width / 2 - 120, 0)
                            .build());

                    options.add(CyclingButtonWidget.onOffBuilder()
                            .omitKeyText()
                            .initially(notif.enabled)
                            .build(width / 2 + 95, 0, 25, 20,
                                    Text.empty(), (button, status)
                                            -> notif.enabled = status));

                    if (index > 0) {
                        options.add(ButtonWidget.builder(Text.literal("⬆"),
                                        (button) ->
                                                listWidget.moveNotifUp(index))
                                .size(12, 20)
                                .position(width / 2 - 120 - 29, 0)
                                .build());

                        options.add(ButtonWidget.builder(Text.literal("⬇"),
                                        (button) ->
                                                listWidget.moveNotifDown(index))
                                .size(12, 20)
                                .position(width / 2 - 120 - 17, 0)
                                .build());

                        options.add(ButtonWidget.builder(Text.literal("X"),
                                        (button) -> listWidget
                                                .removeNotification(index))
                                .size(25, 20)
                                .position(width / 2 + 120 + 5, 0)
                                .build());
                    }
                }
                else {
                    options.add(ButtonWidget.builder(Text.literal("+"),
                                    (button) -> listWidget.addNotification())
                            .size(240, 20)
                            .position(width / 2 - 120, 0)
                            .build());
                }
            }
        }

        @Override
        public void render(DrawContext context, int index, int y, int x,
                           int entryWidth, int entryHeight, int mouseX,
                           int mouseY, boolean hovered, float tickDelta) {
            options.forEach((button) -> {
                button.setY(y);
                button.render(context, mouseX, mouseY, tickDelta);
            });

            if (this.index >= 1) {
                context.drawCenteredTextWithShadow(
                        listWidget.client.textRenderer,
                        String.valueOf(this.index),
                        -36, y + entryHeight / 3, 16777215);
            }
        }
    }

}