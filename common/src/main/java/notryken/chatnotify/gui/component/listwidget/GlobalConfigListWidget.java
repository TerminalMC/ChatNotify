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
    public GlobalConfigListWidget(Minecraft minecraft, int width, int height,
                                  int top, int bottom, int itemHeight, Screen parentScreen) {
        super(minecraft, width, height, top, bottom, itemHeight, parentScreen);

        int eX = width / 2 - 120;
        int eW = 240;
        int eH = 20;

        addEntry(new ConfigListWidget.Entry.TextEntry(eX, eW, eH,
                Component.literal("Global Options"), null, -1));

        addEntry(new Entry.IgnoreToggleEntry(eX, eW, eH));
        addEntry(new Entry.SoundSourceEntry(eX, eW, eH));
        addEntry(new Entry.PrefixConfigEntry(eX, eW, eH, this));

        addEntry(new ConfigListWidget.Entry.TextEntry(eX, eW, eH,
                Component.literal("Notifications \u2139"),
                Tooltip.create(Component.literal("Notifications are checked in sequential order as " +
                        "displayed below. A message can activate at most 1 notification.")), -1));

        int max = ChatNotify.config().getNumNotifs();
        for (int i = 0; i < max; i++) {
            addEntry(new Entry.NotifConfigEntry(eX, eW, eH, this, i));
        }
        addEntry(new ConfigListWidget.Entry.ActionButtonEntry(eX, 0, eW, eH,
                Component.literal("+"), null, -1,
                (button) -> addNotification()));
    }

    @Override
    public GlobalConfigListWidget resize(int width, int height, int top, int bottom) {
        GlobalConfigListWidget listWidget = new GlobalConfigListWidget(
                minecraft, width, height, top, bottom, itemHeight, parentScreen);
        listWidget.setScrollAmount(getScrollAmount());
        return listWidget;
    }

    @Override
    protected void reloadScreen() {
        minecraft.setScreen(new GlobalConfigScreen(parentScreen));
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
        minecraft.setScreen(new NotifConfigScreen(minecraft.screen,
                Component.translatable("screen.chatnotify.title.prefix"),
                new PrefixConfigListWidget(minecraft, parentScreen.width, parentScreen.height,
                        32, parentScreen.height - 32, 25, minecraft.screen)));
    }

    private void openNotificationConfig(int index) {
        minecraft.setScreen(new NotifConfigScreen(minecraft.screen,
                Component.translatable("screen.chatnotify.title.notif"),
                new NotifConfigListWidget(minecraft, parentScreen.width, parentScreen.height,
                        32, parentScreen.height - 32, 25, minecraft.screen,
                        ChatNotify.config().getNotif(index))));
    }

    public static class Entry extends ConfigListWidget.Entry {

        private static class IgnoreToggleEntry extends Entry {
            IgnoreToggleEntry(int x, int width, int height) {
                super();
                elements.add(CycleButton.booleanBuilder(Component.nullToEmpty("No"),
                                Component.nullToEmpty("Yes"))
                        .withInitialValue(ChatNotify.config().ignoreOwnMessages)
                        .withTooltip((status) -> Tooltip.create(Component.nullToEmpty(
                                "Allows or prevents your own messages triggering notifications.")))
                        .create(x, 0, width, height, Component.literal("Check Own Messages"),
                                (button, status) -> ChatNotify.config().ignoreOwnMessages = status));
            }
        }

        private static class SoundSourceEntry extends Entry {
            SoundSourceEntry(int x, int width, int height) {
                super();
                elements.add(CycleButton.<SoundSource>builder(source -> Component.translatable(
                        "soundCategory." + source.getName()))
                        .withValues(SoundSource.values())
                        .withInitialValue(ChatNotify.config().notifSoundSource)
                        .withTooltip((status) -> Tooltip.create(Component.nullToEmpty(
                                "Notification sound volume control category.")))
                        .create(x, 0, width, height, Component.literal("Sound Volume Type"),
                                (button, status) -> ChatNotify.config().notifSoundSource = status));
            }
        }

        private static class PrefixConfigEntry extends Entry {
            PrefixConfigEntry(int x, int width, int height, GlobalConfigListWidget listWidget) {
                super();
                elements.add(Button.builder(getMessage(),
                                (button) -> listWidget.openPrefixConfig())
                        .pos(x, 0)
                        .size(width, height)
                        .build());
            }

            private MutableComponent getMessage() {
                StringBuilder messageBuilder = new StringBuilder("Prefixes: ");
                List<String> prefixes = ChatNotify.config().getPrefixes();
                if (prefixes.isEmpty()) {
                    messageBuilder.append("[None]");
                }
                else {
                    messageBuilder.append(ChatNotify.config().getPrefix(0));
                    for (int i = 1; i < prefixes.size(); i++) {
                        messageBuilder.append(", ");
                        messageBuilder.append(prefixes.get(i));
                    }
                }
                return Component.literal(messageBuilder.toString());
            }
        }

        private static class NotifConfigEntry extends Entry {
            NotifConfigEntry(int x, int width, int height, GlobalConfigListWidget listWidget, int index) {
                super();

                int spacing = 5;
                int statusButtonWidth = 25;
                int mainButtonWidth = width - statusButtonWidth - spacing;
                int moveButtonWidth = 12;
                int removeButtonWidth = 24;
                Notification notif = ChatNotify.config().getNotif(index);

                elements.add(Button.builder(getMessage(notif),
                                (button) -> listWidget.openNotificationConfig(index))
                        .pos(x, 0)
                        .size(mainButtonWidth, height)
                        .build());

                elements.add(CycleButton.onOffBuilder()
                        .displayOnlyValue()
                        .withInitialValue(notif.getEnabled())
                        .create(x + mainButtonWidth + spacing, 0, statusButtonWidth, height,
                                Component.empty(),
                                (button, status) -> notif.setEnabled(status)));

                if (index > 0) {
                    elements.add(Button.builder(Component.literal("⬆"),
                                    (button) -> listWidget.moveNotifUp(index))
                            .pos(x - 2 * moveButtonWidth - spacing, 0)
                            .size(moveButtonWidth, height)
                            .build());

                    elements.add(Button.builder(Component.literal("⬇"),
                                    (button) -> listWidget.moveNotifDown(index))
                            .pos(x - moveButtonWidth - spacing, 0)
                            .size(moveButtonWidth, height)
                            .build());

                    elements.add(Button.builder(Component.literal("X"),
                                    (button) -> listWidget.removeNotification(index))
                            .pos(x + width + spacing, 0)
                            .size(removeButtonWidth, height)
                            .build());
                }
            }

            private MutableComponent getMessage(Notification notif) {
                String mainTrigger = notif.getTrigger();
                MutableComponent message;
                if (mainTrigger.isEmpty()) {
                    message = Component.literal("> Click to Configure <");
                }
                else {
                    String messageStr;
                    if (notif.triggerIsKey) {
                        messageStr = "[Key] " + (mainTrigger.equals(".") ? "Any Message" : mainTrigger);
                    }
                    else {
                        StringBuilder builder = new StringBuilder(mainTrigger);
                        for (int i = 1; i < notif.getTriggers().size(); i++)
                        {
                            builder.append(", ");
                            builder.append(notif.getTrigger(i));
                        }
                        messageStr = builder.toString();
                    }
                    message = Component.literal(messageStr)
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
                return message;
            }
        }
    }
}