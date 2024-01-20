package notryken.chatnotify.gui.component.listwidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundSource;
import notryken.chatnotify.ChatNotify;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.gui.screen.ConfigScreen;
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
                                  int top, int bottom, int itemHeight, ConfigScreen parent) {
        super(minecraft, width, height, top, bottom, itemHeight, parent);

        int eX = width / 2 - 120;
        int eY = 0;
        int eW = 240;
        int eH = 20;

        Entry globalHeaderEntry = new Entry();
        globalHeaderEntry.elements.add(new StringWidget(eX, eY, eW, eH,
                Component.literal("Global Options"), minecraft.font));
        addEntry(globalHeaderEntry);

        Entry ignoreButtonEntry = new Entry();
        ignoreButtonEntry.elements.add(CycleButton.booleanBuilder(
                Component.literal("No"), Component.literal("Yes"))
                .withInitialValue(ChatNotify.config().ignoreOwnMessages)
                .withTooltip((status) -> Tooltip.create(Component.nullToEmpty(
                        "Allows or prevents your own messages triggering notifications.")))
                .create(eX, eY, eW, eH, Component.literal("Check Own Messages"),
                        (button, status) -> ChatNotify.config().ignoreOwnMessages = status));
        addEntry(ignoreButtonEntry);

        Entry soundSourceButtonEntry = new Entry();
        soundSourceButtonEntry.elements.add(CycleButton.<SoundSource>builder(
                source -> Component.translatable("soundCategory." + source.getName()))
                .withValues(SoundSource.values())
                .withInitialValue(ChatNotify.config().notifSoundSource)
                .withTooltip((status) -> Tooltip.create(Component.literal(
                        "Notification sound volume control category.")))
                .create(eX, eY, eW, eH, Component.literal("Sound Volume Type"),
                        (button, status) -> ChatNotify.config().notifSoundSource = status));
        addEntry(soundSourceButtonEntry);

        Entry prefixButtonEntry = new Entry();
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
        prefixButtonEntry.elements.add(Button.builder(Component.literal(labelBuilder.toString()),
                        (button) -> openPrefixConfig())
                .pos(eX, eY).size(eW, eH).build());
        addEntry(prefixButtonEntry);

        Entry notifListHeaderEntry = new Entry();
        StringWidget notifListHeader = new StringWidget(eX, eY, eW, eH,
                Component.literal("Notifications \u2139"), minecraft.font);
        notifListHeader.setTooltip(Tooltip.create(Component.literal(
                "Notifications are checked in sequential order as displayed below. " +
                        "A message can activate at most 1 notification.")));
        notifListHeaderEntry.elements.add(notifListHeader);
        addEntry(notifListHeaderEntry);

        int max = ChatNotify.config().getNumNotifs();
        for (int i = 0; i < max; i++) {
            addEntry(new Entry.NotifButton(eX, eY, eW, eH, this, i));
        }

        Entry addNotifButtonEntry = new Entry();
        addNotifButtonEntry.elements.add(Button.builder(Component.literal("+"),
                        (button) -> addNotification())
                .pos(eX, eY).size(eW, eH).build());
        addEntry(addNotifButtonEntry);
    }

    @Override
    public GlobalConfigListWidget resize(int width, int height, int top, int bottom) {
        GlobalConfigListWidget listWidget = new GlobalConfigListWidget(
                minecraft, width, height, top, bottom, itemHeight, parent);
        listWidget.setScrollAmount(getScrollAmount());
        return listWidget;
    }

    @Override
    protected void reloadScreen() {
        minecraft.setScreen(new GlobalConfigScreen(parent));
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
        openNotificationConfig(ChatNotify.config().getNumNotifs() - 1);
    }

    private void removeNotification(int index) {
        if (ChatNotify.config().removeNotif(index)) {
            reloadScreen();
        }
    }

    private void openPrefixConfig() {
        Component title = Component.translatable("screen.chatnotify.title.prefix");
                minecraft.setScreen(new NotifConfigScreen(parent, title,
                new PrefixConfigListWidget(minecraft, parent.width, parent.height,
                        32, parent.height - 32, 25, parent, title)));
    }

    private void openNotificationConfig(int index) {
        Component title = Component.translatable("screen.chatnotify.title.notif");
        minecraft.setScreen(new NotifConfigScreen(parent, title,
                new NotifConfigListWidget(minecraft, parent.width, parent.height,
                        32, parent.height - 32, 25,
                        parent, ChatNotify.config().getNotif(index))));
    }

    public static class Entry extends ConfigListWidget.Entry {

        private static class NotifButton extends Entry {
            /**
             * @param index the index of the {@code Notification} or -1 for the
             *              'add notification' button.
             */
            NotifButton(int x, int y, int width, int height,
                        GlobalConfigListWidget listWidget, int index) {
                super();

                int mainButtonWidth = width - 30;

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

                elements.add(Button.builder(labelText,
                                (button) -> listWidget.openNotificationConfig(index))
                        .pos(x, y).size(mainButtonWidth, height).build());

                elements.add(CycleButton.onOffBuilder()
                        .displayOnlyValue()
                        .withInitialValue(notif.getEnabled())
                        .create(x + mainButtonWidth + 5, y, 25, height,
                                Component.empty(),
                                (button, status) -> notif.setEnabled(status)));

                if (index > 0) {
                    elements.add(Button.builder(Component.literal("⬆"),
                                    (button) -> listWidget.moveNotifUp(index))
                            .pos(x - 29, y).size(12, height).build());

                    elements.add(Button.builder(Component.literal("⬇"),
                                    (button) -> listWidget.moveNotifDown(index))
                            .pos(x - 17, y).size(12, height).build());

                    elements.add(Button.builder(Component.literal("X"),
                                    (button) -> listWidget.removeNotification(index))
                            .pos(x + width + 5, y).size(25, height).build());
                }
            }
        }
    }
}