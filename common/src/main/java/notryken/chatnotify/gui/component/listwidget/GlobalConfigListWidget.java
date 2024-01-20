package notryken.chatnotify.gui.component.listwidget;

import net.minecraft.client.Minecraft;
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
import notryken.chatnotify.gui.screen.GlobalConfigScreen;
import notryken.chatnotify.gui.screen.NotifConfigScreen;

import java.util.List;
import java.util.Optional;

/**
 * {@code ConfigListWidget} containing global ChatNotify controls and a list
 * of buttons referencing {@code Notification} instances.
 */
public class GlobalConfigListWidget extends ConfigListWidget {
    public GlobalConfigListWidget(Minecraft client, int width, int height,
                                  int top, int bottom, int itemHeight,
                                  Screen parent, Component title) {
        super(client, width, height, top, bottom, itemHeight, parent, title);

        addEntry(new ConfigListWidget.Entry.Header(this.width, this.client, 
                Component.literal("Global Options")));

        addEntry(new Entry.IgnoreToggle(this.width));
        addEntry(new Entry.SoundSourceButton(this.width));
        addEntry(new Entry.PrefixConfigButton(this.width, this));

        addEntry(new ConfigListWidget.Entry.Header(this.width, this.client, 
                Component.literal("Notifications \u2139"),
                Component.literal("Notifications are checked in sequential order as " +
                        "displayed below. A message can activate at most 1 notification.")));

        int max = ChatNotify.config().getNumNotifs();
        for (int i = 0; i < max; i++) {
            addEntry(new Entry.NotifButton(this.width, this, i));
        }
        addEntry(new Entry.NotifButton(this.width, this, -1));
    }

    @Override
    public GlobalConfigListWidget resize(int width, int height, int top, int bottom) {
        GlobalConfigListWidget listWidget = new GlobalConfigListWidget(
                client, width, height, top, bottom, itemHeight, parentScreen, title);
        listWidget.setScrollAmount(getScrollAmount());
        return listWidget;
    }

    @Override
    protected void reloadScreen() {
        client.setScreen(new GlobalConfigScreen(parentScreen));
    }

    private void moveNotifUp(int index) {
        ChatNotify.config().increasePriority(index);
        reloadScreen();
    }

    private void moveNotifDown(int index) {
        ChatNotify.config().decreasePriority(index);
        reloadScreen();
    }

    private void addNotification() {
        ChatNotify.config().addNotif();
        openNotificationConfig(ChatNotify.config().getNumNotifs()-1);
    }

    private void removeNotification(int index) {
        if (ChatNotify.config().removeNotif(index)) {
            reloadScreen();
        }
    }

    private void openPrefixConfig() {
        assert client.screen != null;
        Component title = Component.translatable("screen.chatnotify.title.prefix");
                client.setScreen(new NotifConfigScreen(client.screen, title,
                new PrefixConfigListWidget(client,
                        client.screen.width, client.screen.height,
                        32, client.screen.height - 32, 25,
                        client.screen, title)));
    }

    private void openNotificationConfig(int index) {
        assert client.screen != null;
        Component title = Component.translatable("screen.chatnotify.title.notif");
        client.setScreen(new NotifConfigScreen(client.screen, title,
                new NotificationConfigListWidget(client,
                        client.screen.width, client.screen.height,
                        32, client.screen.height - 32, 25,
                        client.screen, title, ChatNotify.config().getNotif(index))));
    }

    public static class Entry extends ConfigListWidget.Entry {

        private static class IgnoreToggle extends Entry {
            IgnoreToggle(int width) {
                super();

                CycleButton<Boolean> ignoreButton =
                        CycleButton.booleanBuilder(Component.nullToEmpty("No"),
                                        Component.nullToEmpty("Yes"))
                                .withInitialValue(ChatNotify.config().ignoreOwnMessages)
                                .withTooltip((status) -> Tooltip.create(Component.nullToEmpty(
                                        "Allows or prevents your own messages " +
                                                "triggering notifications.")))
                                .create(width / 2 - 120, 32, 240, 20,
                                Component.literal("Check Own Messages"),
                                (button, status) ->
                                        ChatNotify.config().ignoreOwnMessages = status);
                options.add(ignoreButton);
            }
        }

        private static class SoundSourceButton extends Entry {
            SoundSourceButton(int width) {
                super();

                CycleButton<SoundSource> soundSourceButton =
                        CycleButton.<SoundSource>builder(source -> Component.translatable(
                                "soundCategory." + source.getName()))
                                .withValues(SoundSource.values())
                                .withInitialValue(ChatNotify.config().notifSoundSource)
                                .withTooltip((status) -> Tooltip.create(Component.nullToEmpty(
                                        "Notification sound volume control category.")))
                                .create(width / 2 - 120, 32, 240, 20,
                                        Component.literal("Sound Volume Type"),
                                        (button, status) ->
                                                ChatNotify.config().notifSoundSource = status);
                options.add(soundSourceButton);
            }
        }

        private static class PrefixConfigButton extends Entry {
            PrefixConfigButton(int width, GlobalConfigListWidget listWidget) {
                super();

                StringBuilder labelBuilder = new StringBuilder("Prefixes: ");

                List<String> prefixes = ChatNotify.config().getPrefixes();
                if (prefixes.isEmpty()) {
                    labelBuilder.append("[None]");
                }
                else {
                    labelBuilder.append(ChatNotify.config().getPrefix(0));
                    for (int i = 1; i < prefixes.size(); i++) {
                        labelBuilder.append(", ");
                        labelBuilder.append(prefixes.get(i));
                    }
                }

                options.add(Button.builder(Component.literal(labelBuilder.toString()),
                                (button) -> listWidget.openPrefixConfig())
                        .size(240, 20)
                        .pos(width / 2 - 120, 0)
                        .build());
            }
        }

        private static class NotifButton extends Entry {
            /**
             * @param index the index of the {@code Notification} or -1 for the
             *              'add notification' button.
             */
            NotifButton(int width, GlobalConfigListWidget listWidget, int index) {
                super();

                if (index == -1) {
                    options.add(Button.builder(Component.literal("+"),
                                    (button) -> listWidget.addNotification())
                            .size(240, 20)
                            .pos(width / 2 - 120, 0)
                            .build());
                }
                else if (index >= 0) {
                    Notification notif = ChatNotify.config().getNotif(index);
                    String label = notif.getTrigger();
                    MutableComponent labelText;
                    if (label.isEmpty()) {
                        labelText = Component.literal("> Click to Configure <");
                    }
                    else {
                        if (notif.triggerIsKey) {
                            label = "[Key] " + (label.equals(".") ? "Any Message" : label);
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
                        labelText = Component.literal(label)
                                .setStyle(Style.create((notif.getControl(0) ?
                                                Optional.of(notif.getColor()) : Optional.empty()),
                                Optional.of(notif.getFormatControl(0)),
                                Optional.of(notif.getFormatControl(1)),
                                Optional.of(notif.getFormatControl(2)),
                                Optional.of(notif.getFormatControl(3)),
                                Optional.of(notif.getFormatControl(4)),
                                Optional.empty(),
                                Optional.empty()));
                    }

                    options.add(Button.builder(labelText,
                                    (button) -> listWidget.openNotificationConfig(index))
                            .size(210, 20)
                            .pos(width / 2 - 120, 0)
                            .build());

                    options.add(CycleButton.onOffBuilder()
                            .displayOnlyValue()
                            .withInitialValue(notif.getEnabled())
                            .create(width / 2 + 95, 0, 25, 20,
                                    Component.empty(),
                                    (button, status) -> notif.setEnabled(status)));

                    if (index > 0) {
                        options.add(Button.builder(Component.literal("⬆"),
                                        (button) -> listWidget.moveNotifUp(index))
                                .size(12, 20)
                                .pos(width / 2 - 120 - 29, 0)
                                .build());

                        options.add(Button.builder(Component.literal("⬇"),
                                        (button) -> listWidget.moveNotifDown(index))
                                .size(12, 20)
                                .pos(width / 2 - 120 - 17, 0)
                                .build());

                        options.add(Button.builder(Component.literal("X"),
                                        (button) -> listWidget.removeNotification(index))
                                .size(25, 20)
                                .pos(width / 2 + 120 + 5, 0)
                                .build());
                    }
                }
            }
        }
    }
}