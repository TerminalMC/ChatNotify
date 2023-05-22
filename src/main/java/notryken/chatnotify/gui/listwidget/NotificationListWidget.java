package notryken.chatnotify.gui.listwidget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.gui.screen.ConfigScreen;
import notryken.chatnotify.gui.screen.ModMenuIntegration;

import static notryken.chatnotify.client.ChatNotifyClient.config;

public class NotificationListWidget extends ConfigListWidget
{
    public NotificationListWidget(MinecraftClient client,
                                  int i, int j, int k, int l, int m,
                                  Screen parent, Text title)
    {
        super(client, i, j, k, l, m, parent, title);

        this.addEntry(new ConfigListWidget.Entry.Header(this.width, this,
                client, Text.literal("Options")));
        this.addEntry(new Entry.IgnoreToggle(this.width, this));

        this.addEntry(new ConfigListWidget.Entry.Header(this.width, this,
                client, Text.literal("Notifications")));

        int max = config.getNumNotifs();
        for (int idx = 0; idx < max; idx++) {
            this.addEntry(new Entry.NotifButton(this.width, this, idx));
        }

        this.addEntry(new Entry.NotifButton(this.width, this, -1));
    }

    @Override
    protected void refreshScreen()
    {
        client.setScreen(new ModMenuIntegration.ModMenuOptionsScreen(parent));
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
        client.setScreen(new ConfigScreen(client.currentScreen, client.options,
                Text.literal("Notification Options"),
                new NotificationConfigListWidget(client,
                        client.currentScreen.width,
                        client.currentScreen.height, 32,
                        client.currentScreen.height - 32, 25,
                        client.currentScreen,
                        Text.literal("Notification Options"),
                        config.getNotif(index))));
    }

    public static class Entry extends ConfigListWidget.Entry
    {
        private final int index;

        Entry(int width, NotificationListWidget listWidget, int index)
        {
            super(width, listWidget);
            this.index = index;
        }

        private static class IgnoreToggle extends Entry
        {
            IgnoreToggle(int width, NotificationListWidget listWidget)
            {
                super(width, listWidget, -1);

                options.add(CyclingButtonWidget.onOffBuilder()
                        .initially(config.ignoreOwnMessages).build(
                                this.width / 2 - 120, 32, 240, 20,
                                Text.literal("Ignore Your Own Messages"),
                                (button, status) ->
                                        config.ignoreOwnMessages = status));
            }
        }

        private static class NotifButton extends Entry
        {
            NotifButton(int width, NotificationListWidget listWidget, int index)
            {
                super(width, listWidget, index);

                if (index >= 0) {
                    Notification notif = config.getNotif(index);
                    String label = notif.getTrigger();
                    if (label.equals("")) {
                        label = "> Click to Configure <";
                    }
                    else {
                        if (notif.triggerIsKey) {
                            label = "[Key] " + label;
                        }
                        else {
                            int numVariations = notif.getNumTriggers() - 1;
                            if (numVariations == 1) {
                                label = label + " (" + numVariations +
                                        " variation)";
                            }
                            else if (numVariations > 1) {
                                label = label + " (" + numVariations +
                                        " variations)";
                            }
                        }
                    }

                    options.add(ButtonWidget.builder(Text.literal(label),
                                    (button) -> listWidget.openNotificationConfig(index))
                            .size(240, 20).position(width / 2 - 120, 0).build());

                    if (index == 0) {
                        options.add(CyclingButtonWidget.onOffBuilder()
                                .omitKeyText()
                                .initially(notif.enabled)
                                .build(width / 2 + 120 + 5, 0, 25, 20, Text.empty(),
                                        (button, status)
                                                -> notif.enabled = status));
                    }
                    else {
                        options.add(ButtonWidget.builder(Text.literal("⬆"),
                                        (button) -> listWidget.moveNotifUp(index))
                                .size(12, 20).position(width / 2 - 120 - 29, 0)
                                .build());

                        options.add(ButtonWidget.builder(Text.literal("⬇"),
                                        (button) -> listWidget.moveNotifDown(index))
                                .size(12, 20).position(width / 2 - 120 - 17, 0)
                                .build());

                        options.add(ButtonWidget.builder(Text.literal("X"),
                                        (button) -> listWidget.removeNotification(index))
                                .size(25, 20).position(width / 2 + 120 + 5, 0)
                                .build());
                    }
                }
                else {
                    options.add(ButtonWidget.builder(Text.literal("+"),
                                    (button) -> listWidget.addNotification()).size(240, 20)
                            .position(width / 2 - 120, 0)
                            .build());
                }
            }
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x,
                           int entryWidth, int entryHeight, int mouseX,
                           int mouseY, boolean hovered, float tickDelta) {
            options.forEach((button) -> {
                button.setY(y);
                button.render(matrices, mouseX, mouseY, tickDelta);
            });

            if (this.index >= 1) {
                listWidget.client.textRenderer.draw(matrices,
                        String.valueOf(this.index), -36f,
                        y + (float) entryHeight / 3, 16777215);
            }
        }
    }

}