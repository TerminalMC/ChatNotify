package notryken.chatnotify.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.ui.NotificationConfigScreen.NotificationConfigScreen;

import java.util.ArrayList;
import java.util.List;

import static notryken.chatnotify.client.ChatNotifyClient.config;

public class NotificationListWidget extends
        ElementListWidget<NotificationListWidget.NotificationEntry>
{
    public NotificationListWidget(MinecraftClient client, int i, int j, int k,
                                  int l, int m)
    {
        super(client, i, j, k, l, m);
        this.setRenderSelection(true);

        int max = config.getNumNotifications();
        for (int idx = 0; idx < max; idx++) {
            this.addEntry(NotificationListWidget.NotificationEntry.create(
                    idx, this.width, this));
        }

        this.addEntry(NotificationListWidget.NotificationEntry.create(
                -1, this.width, this));
    }

    public void addNotification()
    {
        List<NotificationEntry> entries = this.children();

        int size = config.getNumNotifications();
        config.addNotification();

        entries.add(size, NotificationEntry.create(size, this.width, this));
    }

    public void removeNotification(int index)
    {
        List<NotificationEntry> entries = this.children();

        config.removeNotification(index);

        entries.remove(index);
    }

    public void openNotificationConfig(int index)
    {
        if (client != null) {
            client.setScreen(new NotificationConfigScreen(
                    client.currentScreen, config.getNotification(index)));
        }
    }

    public int getRowWidth()
    {
        return 300;
    }

    protected int getScrollbarPositionX()
    {
        return super.getScrollbarPositionX() + 32;
    }

    public static class NotificationEntry extends
            ElementListWidget.Entry<NotificationListWidget.NotificationEntry>
    {
        private final List<ClickableWidget> buttons;
        private final NotificationListWidget listWidget;
        private final int index;

        private NotificationEntry(List<ClickableWidget> buttons, int index,
                                  NotificationListWidget listWidget)
        {
            this.buttons = buttons;
            this.listWidget = listWidget;
            this.index = index;
        }

        public static NotificationListWidget.NotificationEntry
        create(int index, int width, NotificationListWidget listWidget)
        {
            ArrayList<ClickableWidget> widgets = new ArrayList<>();

            if (index >= 0) {
                Notification notif = config.getNotification(index);
                String label = notif.getTrigger();
                if (notif.isKeyTrigger() && !label.equals("")) {
                    label = "[Key] " + label;
                }

                widgets.add(ButtonWidget.builder(Text.literal(label),
                        (button) -> listWidget.openNotificationConfig(index))
                        .size(240, 20).position(width / 2 - 120, 0).build());

                if (index >= 1) {
                    widgets.add(ButtonWidget.builder(Text.literal("X"),
                            (button) -> listWidget.removeNotification(index))
                            .size(20, 20).position(width / 2 + 120 + 5, 0)
                            .build());
                }
            }
            else {
                widgets.add(ButtonWidget.builder(Text.literal("+"),
                        (button) -> listWidget.addNotification()).size(240, 20)
                        .position(width / 2 - 120, 0)
                        .build());
            }
            return new NotificationEntry(widgets, index, listWidget);
        }

        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            this.buttons.forEach((button) -> {
                button.setY(y);
                button.render(matrices, mouseX, mouseY, tickDelta);
            });

            if (this.index >= 1) {
                listWidget.client.textRenderer.draw(matrices, String.valueOf(this.index), (float) listWidget.width / 2 - 150 + 13, y + (float) entryHeight / 3, 16777215);
            }
        }

        public List<? extends Element> children() {
            return this.buttons;
        }

        public List<? extends Selectable> selectableChildren() {
            return this.buttons;
        }
    }

}