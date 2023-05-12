package notryken.chatnotify.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.ui.NotificationConfig.NotificationConfigScreen;

import java.util.ArrayList;
import java.util.List;

import static notryken.chatnotify.client.ChatNotifyClient.config;

public class NotificationListWidget extends
        ElementListWidget<NotificationListWidget.NotificationEntry>
{
    private final Screen parentScreen;

    public NotificationListWidget(MinecraftClient client, Screen parentScreen,
                                  int i, int j, int k, int l, int m)
    {
        super(client, i, j, k, l, m);
        this.setRenderSelection(true);
        this.parentScreen = parentScreen;

        int max = config.getNumNotifs();
        for (int idx = 0; idx < max; idx++) {
            this.addEntry(NotificationListWidget.NotificationEntry.create(
                    idx, this.width, this));
        }

        this.addEntry(NotificationListWidget.NotificationEntry.create(
                -1, this.width, this));
    }

    public void moveNotifUp(int index)
    {
        config.moveNotifUp(index);
        refreshScreen();
    }

    public void moveNotifDown(int index)
    {
        config.moveNotifDown(index);
        refreshScreen();
    }

    public void addNotification()
    {
        List<NotificationEntry> entries = this.children();

        int size = config.getNumNotifs();
        config.addNotif();

        entries.add(size, NotificationEntry.create(size, this.width, this));
    }

    public void removeNotification(int index)
    {
        List<NotificationEntry> entries = this.children();

        config.removeNotif(index);

        entries.remove(index);
    }

    public void openNotificationConfig(int index)
    {
        if (client != null) {
            client.setScreen(new NotificationConfigScreen(
                    client.currentScreen, config.getNotif(index)));
        }
    }

    public void refreshScreen()
    {
        if (client != null) {
            client.setScreen(
                    new ModMenuIntegration.ModMenuOptionsScreen(
                            this.parentScreen));
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

    public static class NotificationEntry extends Entry<NotificationEntry>
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

        public static NotificationEntry create(int index, int width,
                                               NotificationListWidget
                                                       listWidget)
        {
            ArrayList<ClickableWidget> widgets = new ArrayList<>();

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

                widgets.add(ButtonWidget.builder(Text.literal(label),
                        (button) -> listWidget.openNotificationConfig(index))
                        .size(240, 20).position(width / 2 - 120, 0).build());

                if (index == 0) {
                    widgets.add(CyclingButtonWidget.onOffBuilder()
                            .omitKeyText()
                            .initially(notif.enabled)
                            .build(width / 2 + 120 + 5, 0, 25, 20, Text.empty(),
                                    (button, status)
                                            -> notif.enabled = status));
                }
                else {
                    widgets.add(ButtonWidget.builder(Text.literal("⬆"),
                            (button) -> listWidget.moveNotifUp(index))
                            .size(12, 20).position(width / 2 - 120 - 29, 0)
                            .build());

                    widgets.add(ButtonWidget.builder(Text.literal("⬇"),
                                    (button) -> listWidget.moveNotifDown(index))
                            .size(12, 20).position(width / 2 - 120 - 17, 0)
                            .build());

                    widgets.add(ButtonWidget.builder(Text.literal("X"),
                            (button) -> listWidget.removeNotification(index))
                            .size(25, 20).position(width / 2 + 120 + 5, 0)
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

        public void render(MatrixStack matrices, int index, int y, int x,
                           int entryWidth, int entryHeight, int mouseX,
                           int mouseY, boolean hovered, float tickDelta) {
            this.buttons.forEach((button) -> {
                button.setY(y);
                button.render(matrices, mouseX, mouseY, tickDelta);
            });

            if (this.index >= 1) {
                listWidget.client.textRenderer.draw(matrices,
                        String.valueOf(this.index),
                        (float) listWidget.width / 2 - 120 - 36,
                        y + (float) entryHeight / 3, 16777215);
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