package notryken.chatnotify.gui.components.listwidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import notryken.chatnotify.ChatNotify;
import notryken.chatnotify.config.Notification;
import org.jetbrains.annotations.NotNull;

/**
 * {@code ConfigListWidget} containing controls for advanced settings of the
 * specified {@code Notification}, including regex toggle, exclusion triggers
 * and automatic response messages.
 */
public class AdvancedConfigListWidget extends ConfigListWidget {
    private final Notification notif;

    public AdvancedConfigListWidget(Minecraft client, int width, int height,
                                    int top, int bottom, int itemHeight, Screen parent,
                                    Component title, Notification notif) {
        super(client, width, height, top, bottom, itemHeight, parent, title);
        this.notif = notif;

        addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                Component.literal("WARNING"), 16711680));
        addEntry(new ConfigListWidget.Entry.MultiLineHeader(width, this,
                client, Component.literal("These settings allow you to break " +
                    "ChatNotify and crash Minecraft. Use with caution.")));

        addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                Component.nullToEmpty("Notification Trigger Regex")));
        addEntry(new Entry.RegexToggleButton(width, notif, this));

        addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                Component.nullToEmpty("Notification Exclusion Triggers")));
        addEntry(new Entry.ExclusionToggleButton(width, notif, this));

        if (notif.exclusionEnabled) {
            for (int i = 0; i < notif.getExclusionTriggers().size(); i ++) {
                addEntry(new Entry.ExclusionTriggerField(width, notif, this, client, i));
            }
            addEntry(new Entry.ExclusionTriggerField(width, notif, this, client, -1));
        }

        addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                Component.nullToEmpty("Auto Response Messages")));
        addEntry(new Entry.ResponseToggleButton(width, notif, this));

        if (notif.responseEnabled) {
            for (int i = 0; i < notif.getResponseMessages().size(); i ++) {
                addEntry(new Entry.ResponseMessageField(width, notif, this, client, i));
            }
            addEntry(new Entry.ResponseMessageField(width, notif, this, client, -1));
        }

        addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                Component.literal("Broken Everything?")));
        addEntry(new Entry.ResetButton(width, notif, this));
        addEntry(new Entry.ResetAllButton(width, notif, this));
        addEntry(new Entry.NuclearResetButton(width, notif, this, client));
    }

    @Override
    public AdvancedConfigListWidget resize(int width, int height, int top, int bottom) {
        AdvancedConfigListWidget listWidget = new AdvancedConfigListWidget(
                client, width, height, top, bottom, itemHeight, parentScreen, title, notif);
        listWidget.setScrollAmount(getScrollAmount());
        return listWidget;
    }

    @Override
    protected void refreshScreen() {
        refreshScreen(this);
    }

    private abstract static class Entry extends ConfigListWidget.Entry {
        public final Notification notif;

        Entry(int width, Notification notif, AdvancedConfigListWidget listWidget) {
            super(width, listWidget);
            this.notif = notif;
        }

        private static class RegexToggleButton extends Entry {
            RegexToggleButton(int width, Notification notif,
                              AdvancedConfigListWidget listWidget) {
                super(width, notif, listWidget);
                options.add(CycleButton.booleanBuilder(
                                Component.literal("Enabled"),
                                Component.literal("Disabled"))
                        .withInitialValue(notif.regexEnabled)
                        .withTooltip((status) -> Tooltip.create(Component.nullToEmpty(
                                "If enabled, all triggers for this notification will be " +
                                "processed as complete regular expressions.")))
                        .create(this.width / 2 - 120, 0, 240, 20,
                                Component.literal("Regex"),
                                (button, status) -> {
                                    notif.regexEnabled = status;
                                    listWidget.refreshScreen();
                                }));
            }
        }

        private static class ExclusionToggleButton extends Entry {
            ExclusionToggleButton(int width, Notification notif,
                              AdvancedConfigListWidget listWidget) {
                super(width, notif, listWidget);
                options.add(CycleButton.booleanBuilder(
                                Component.literal("Enabled"),
                                Component.literal("Disabled"))
                        .withInitialValue(notif.exclusionEnabled)
                        .withTooltip((status) -> Tooltip.create(Component.nullToEmpty(
                                "If an exclusion trigger is detected in a message, " +
                                "it will prevent this notification from activating.")))
                        .create(this.width / 2 - 120, 0, 240, 20,
                                Component.literal("Exclusion Triggers"),
                                (button, status) -> {
                                    notif.exclusionEnabled = status;
                                    listWidget.refreshScreen();
                                }));
            }
        }

        private static class ExclusionTriggerField extends Entry {
            final int index;

            /**
             * @param index the index of the {@code Notification} exclusion
             *              trigger, or -1 for the 'add field' button.
             */
            ExclusionTriggerField(int width, Notification notif,
                                  AdvancedConfigListWidget listWidget,
                                  @NotNull Minecraft client, int index) {
                super(width, notif, listWidget);
                this.index = index;

                if (index == -1) {
                    options.add(Button.builder(Component.literal("+"),
                                    (button) -> {
                                        notif.addExclusionTrigger("");
                                        listWidget.refreshScreen();
                                    })
                            .size(240, 20)
                            .pos(width / 2 - 120, 0)
                            .build());
                }
                else if (index >= 0) {

                    EditBox triggerEdit = new EditBox(client.font, this.width / 2 - 120, 0,
                            240, 20, Component.literal("Exclusion Trigger"));
                    triggerEdit.setMaxLength(120);
                    triggerEdit.setValue(notif.getExclusionTrigger(index));
                    triggerEdit.setResponder((trigger) ->
                            notif.setExclusionTrigger(index, trigger.strip()));
                    options.add(triggerEdit);

                    options.add(Button.builder(Component.literal("X"),
                                    (button) -> {
                                        notif.removeExclusionTrigger(index);
                                        listWidget.refreshScreen();
                                    })
                            .size(20, 20)
                            .pos(width / 2 + 120 + 5, 0)
                            .build());
                }
            }
        }

        private static class ResponseToggleButton extends Entry {
            ResponseToggleButton(int width, Notification notif,
                                 AdvancedConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                options.add(CycleButton.booleanBuilder(
                                Component.literal("Enabled"),
                                Component.literal("Disabled"))
                        .withInitialValue(notif.responseEnabled)
                        .withTooltip((status) -> Tooltip.create(Component.nullToEmpty(
                                "Chat messages or commands to be sent by the " +
                                "client immediately when this notification " +
                                "is activated. Use with caution.")))
                        .create(this.width / 2 - 120, 0, 240, 20,
                                Component.literal("Response Messages"),
                                (button, status) -> {
                                    notif.responseEnabled = status;
                                    listWidget.refreshScreen();
                                }));
            }
        }

        private static class ResponseMessageField extends Entry {
            final int index;

            /**
             * @param index the index of the {@code Notification} response
             *              message, or -1 for the 'add field' button.
             */
            ResponseMessageField(int width, Notification notif,
                                 AdvancedConfigListWidget listWidget,
                                 @NotNull Minecraft client, int index) {
                super(width, notif, listWidget);
                this.index = index;

                if (index == -1) {
                    options.add(Button.builder(Component.literal("+"),
                                    (button) -> {
                                        notif.addResponseMessage("");
                                        listWidget.refreshScreen();
                                    })
                            .size(240, 20)
                            .pos(width / 2 - 120, 0)
                            .build());
                }
                else if (index >= 0) {

                    EditBox messageEdit = new EditBox(client.font, this.width / 2 - 120, 0,
                            240, 20, Component.literal("Response Message"));
                    messageEdit.setMaxLength(120);
                    messageEdit.setValue(notif.getResponseMessage(index));
                    messageEdit.setResponder((message) ->
                            notif.setResponseMessage(index, message.strip()));
                    options.add(messageEdit);

                    options.add(Button.builder(Component.literal("X"),
                                    (button) -> {
                                        notif.removeResponseMessage(index);
                                        listWidget.refreshScreen();
                                    })
                            .size(20, 20)
                            .pos(width / 2 + 120 + 5, 0)
                            .build());
                }
            }
        }

        private static class ResetButton extends Entry {
            /**
             * When pressed, resets all advanced settings for the specified
             * {@code Notification}
             */
            ResetButton(int width, Notification notif,
                        AdvancedConfigListWidget listWidget) {
                super(width, notif, listWidget);

                Button resetButton = Button.builder(
                        Component.literal("Reset Advanced Options"), (button) -> {
                            notif.regexEnabled = false;
                            notif.exclusionEnabled = false;
                            notif.responseEnabled = false;
                            for (int i = 0; i < notif.getExclusionTriggers().size(); i++)
                            {
                                notif.removeExclusionTrigger(0);
                            }
                            for (int i = 0; i < notif.getResponseMessages().size(); i++)
                            {
                                notif.removeResponseMessage(0);
                            }
                            listWidget.refreshScreen();
                        })
                        .size(240, 20)
                        .pos(width / 2 - 120, 0)
                        .build();
                resetButton.setTooltip(Tooltip.create(Component.literal(
                        "Resets all advanced settings for THIS notification.")));

                options.add(resetButton);
            }
        }

        private static class ResetAllButton extends Entry {
            /**
             * When pressed, resets all advanced settings for all
             * {@code Notification}s.
             */
            ResetAllButton(int width, Notification notif,
                           AdvancedConfigListWidget listWidget) {
                super(width, notif, listWidget);

                Button resetButton = Button.builder(
                        Component.literal("Reset All Advanced Options"), (button) -> {
                            for (Notification notif2 : ChatNotify.config().getNotifs()) {
                                notif2.regexEnabled = false;
                                notif2.exclusionEnabled = false;
                                notif2.responseEnabled = false;
                                for (int i = 0; i < notif2.getExclusionTriggers().size(); i++) {
                                    notif2.removeExclusionTrigger(0);
                                }
                                for (int i = 0; i < notif2.getResponseMessages().size(); i++) {
                                    notif2.removeResponseMessage(0);
                                }
                            }
                            listWidget.refreshScreen();
                        })
                        .size(240, 20)
                        .pos(width / 2 - 120, 0)
                        .build();
                resetButton.setTooltip(Tooltip.create(Component.literal(
                        "Resets all advanced settings for ALL notifications.")));

                options.add(resetButton);
            }
        }

        private static class NuclearResetButton extends Entry {
            NuclearResetButton(int width, Notification notif,
                               AdvancedConfigListWidget listWidget, Minecraft client) {
                super(width, notif, listWidget);

                Button resetButton = Button.builder(
                        Component.literal("Nuclear Reset"), (button) -> {
                            client.setScreen(new ConfirmScreen((value) -> {
                                if (value) {
                                    ChatNotify.restoreDefaultConfig();
                                    client.setScreen(null);
                                }
                                else {
                                    listWidget.refreshScreen();
                                }
                            }, Component.literal("Nuclear Reset"), Component.literal(
                                    "Are you sure you want to delete all ChatNotify " +
                                            "notifications and reset all settings?")));
                        })
                        .size(240, 20)
                        .pos(width / 2 - 120, 0)
                        .build();

                resetButton.setTooltip(Tooltip.create(Component.literal(
                        "Deletes all ChatNotify notifications and resets all settings.")));

                options.add(resetButton);
            }
        }
    }
}