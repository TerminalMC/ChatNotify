package notryken.chatnotify.gui.component.listwidget;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundSource;
import notryken.chatnotify.ChatNotify;
import notryken.chatnotify.config.Notification;
import notryken.chatnotify.gui.screen.ConfigScreen;

import java.util.List;
import java.util.Optional;

/**
 * {@code ConfigListWidget} containing global ChatNotify controls and a list
 * of buttons referencing {@code Notification} instances.
 */
public class GlobalConfigListWidget extends ConfigListWidget {
    public GlobalConfigListWidget(Minecraft minecraft, int width, int height,
                                  int top, int bottom, int itemHeight,
                                  int entryRelX, int entryWidth, int entryHeight, 
                                  int scrollWidth) {
        super(minecraft, width, height, top, bottom, itemHeight, 
                entryRelX, entryWidth, entryHeight, scrollWidth);

        addEntry(new ConfigListWidget.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Global Options"), null, -1));

        addEntry(new Entry.IgnoreToggleEntry(entryX, entryWidth, entryHeight));
        addEntry(new Entry.SoundSourceEntry(entryX, entryWidth, entryHeight));
        addEntry(new Entry.PrefixConfigEntry(entryX, entryWidth, entryHeight, this));

        addEntry(new ConfigListWidget.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Notifications \u2139"),
                Tooltip.create(Component.literal("Incoming messages will activate the first " +
                        "enabled notification with a matching trigger.")), -1));

        int max = ChatNotify.config().getNumNotifs();
        for (int i = 0; i < max; i++) {
            addEntry(new Entry.NotifConfigEntry(entryX, entryWidth, entryHeight, this, i));
        }
        addEntry(new ConfigListWidget.Entry.ActionButtonEntry(entryX, 0, entryWidth, entryHeight,
                Component.literal("+"), null, -1,
                (button) -> addNotification()));
    }

    @Override
    public GlobalConfigListWidget resize(int width, int height, int top, int bottom, int itemHeight, double scrollAmount) {
        GlobalConfigListWidget newListWidget = new GlobalConfigListWidget(minecraft, width, height, top, bottom, itemHeight,
                entryRelX, entryWidth, entryHeight, scrollWidth);
        newListWidget.setScrollAmount(scrollAmount);
        return newListWidget;
    }

    private void moveNotifUp(int index) {
        ChatNotify.config().increasePriority(index);
        reload();
    }

    private void moveNotifDown(int index) {
        ChatNotify.config().decreasePriority(index);
        reload();
    }

    private void addNotification() {
        ChatNotify.config().addNotif();
        openNotificationConfig(ChatNotify.config().getNumNotifs()-1);
    }

    private void removeNotification(int index) {
        if (ChatNotify.config().removeNotif(index)) {
            reload();
        }
    }

    private void openPrefixConfig() {
        minecraft.setScreen(new ConfigScreen(minecraft.screen,
                Component.translatable("screen.chatnotify.title.prefix"),
                new PrefixConfigListWidget(minecraft, screen.width, screen.height, y0, y1,
                        itemHeight, entryRelX, entryWidth, entryHeight, scrollWidth)));
    }

    private void openNotificationConfig(int index) {
        minecraft.setScreen(new ConfigScreen(minecraft.screen,
                Component.translatable("screen.chatnotify.title.notif"),
                new NotifConfigListWidget(minecraft, screen.width, screen.height, y0, y1, 
                        itemHeight, entryRelX, entryWidth, entryHeight, scrollWidth,
                        ChatNotify.config().getNotif(index), index == 0)));
    }

    public static class Entry extends ConfigListWidget.Entry {

        private static class IgnoreToggleEntry extends Entry {
            IgnoreToggleEntry(int x, int width, int height) {
                super();
                elements.add(CycleButton.booleanBuilder(
                        Component.translatable("options.off").withStyle(ChatFormatting.RED),
                                Component.translatable("options.on").withStyle(ChatFormatting.GREEN))
                        .withInitialValue(ChatNotify.config().ignoreOwnMessages)
                        .withTooltip((status) -> Tooltip.create(Component.nullToEmpty(
                                "Turn OFF to prevent your own messages activating notifications.")))
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

                elements.add(CycleButton.booleanBuilder(
                        Component.translatable("options.on").withStyle(ChatFormatting.GREEN),
                                Component.translatable("options.off").withStyle(ChatFormatting.RED))
                        .displayOnlyValue()
                        .withInitialValue(notif.isEnabled())
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