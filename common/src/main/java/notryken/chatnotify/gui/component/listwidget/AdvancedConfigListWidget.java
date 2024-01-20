package notryken.chatnotify.gui.component.listwidget;

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

        addEntry(new ConfigListWidget.Entry.Header(this.width, this.client,
                Component.literal("WARNING"), 16711680));
        addEntry(new ConfigListWidget.Entry.MultiLineHeader(this.width, this.client,
                Component.literal("These settings allow you to break " +
                        "ChatNotify and crash Minecraft. Use with caution.")));

        addEntry(new ConfigListWidget.Entry.Header(this.width, this.client,
                Component.nullToEmpty("Notification Trigger Regex")));
        addEntry(new Entry.RegexToggleButton(this.width, this.notif, this));

        addEntry(new ConfigListWidget.Entry.Header(this.width, this.client,
                Component.nullToEmpty("Notification Exclusion Triggers")));
        addEntry(new Entry.ExclusionToggleButton(this.width, this.notif, this));

        if (this.notif.exclusionEnabled) {
            for (int i = 0; i < this.notif.getExclusionTriggers().size(); i ++) {
                addEntry(new Entry.ExclusionTriggerField(this.width, this.notif, this, this.client, i));
            }
            addEntry(new Entry.ExclusionTriggerField(this.width, this.notif, this, this.client, -1));
        }

        addEntry(new ConfigListWidget.Entry.Header(this.width, this.client,
                Component.nullToEmpty("Auto Response Messages")));
        addEntry(new Entry.ResponseToggleButton(this.width, this.notif, this));

        if (this.notif.responseEnabled) {
            for (int i = 0; i < this.notif.getResponseMessages().size(); i ++) {
                addEntry(new Entry.ResponseMessageField(this.width, this.notif, this, this.client, i));
            }
            addEntry(new Entry.ResponseMessageField(this.width, this.notif, this, this.client, -1));
        }

        addEntry(new ConfigListWidget.Entry.Header(this.width, this.client,
                Component.literal("Broken Everything?")));
        addEntry(new Entry.ResetButton(this.width, this.notif, this));
        addEntry(new Entry.ResetAllButton(this.width, this));
        addEntry(new Entry.NuclearResetButton(this.width, this, this.client));
    }

    @Override
    public AdvancedConfigListWidget resize(int width, int height, int top, int bottom) {
        AdvancedConfigListWidget listWidget = new AdvancedConfigListWidget(
                client, width, height, top, bottom, itemHeight, parent, title, notif);
        listWidget.setScrollAmount(getScrollAmount());
        return listWidget;
    }

    @Override
    protected void reload() {
        reload(this);
    }

    private abstract static class Entry extends ConfigListWidget.Entry {

        private static class RegexToggleButton extends Entry {
            RegexToggleButton(int width, Notification notif,
                              AdvancedConfigListWidget listWidget) {
                super();
                elements.add(CycleButton.booleanBuilder(
                                Component.literal("Enabled"),
                                Component.literal("Disabled"))
                        .withInitialValue(notif.regexEnabled)
                        .withTooltip((status) -> Tooltip.create(Component.nullToEmpty(
                                "If enabled, all triggers for this notification will be " +
                                "processed as complete regular expressions.")))
                        .create(width / 2 - 120, 0, 240, 20,
                                Component.literal("Regex"),
                                (button, status) -> {
                                    notif.regexEnabled = status;
                                    listWidget.reload();
                                }));
            }
        }

        private static class ExclusionToggleButton extends Entry {
            ExclusionToggleButton(int width, Notification notif,
                              AdvancedConfigListWidget listWidget) {
                super();
                elements.add(CycleButton.booleanBuilder(
                                Component.literal("Enabled"),
                                Component.literal("Disabled"))
                        .withInitialValue(notif.exclusionEnabled)
                        .withTooltip((status) -> Tooltip.create(Component.nullToEmpty(
                                "If an exclusion trigger is detected in a message, " +
                                "it will prevent this notification from activating.")))
                        .create(width / 2 - 120, 0, 240, 20,
                                Component.literal("Exclusion Triggers"),
                                (button, status) -> {
                                    notif.exclusionEnabled = status;
                                    listWidget.reload();
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
                super();
                this.index = index;

                if (index == -1) {
                    elements.add(Button.builder(Component.literal("+"),
                                    (button) -> {
                                        notif.addExclusionTrigger("");
                                        listWidget.reload();
                                    })
                            .size(240, 20)
                            .pos(width / 2 - 120, 0)
                            .build());
                }
                else if (index >= 0) {

                    EditBox triggerEdit = new EditBox(client.font, width / 2 - 120, 0,
                            240, 20, Component.literal("Exclusion Trigger"));
                    triggerEdit.setMaxLength(120);
                    triggerEdit.setValue(notif.getExclusionTrigger(index));
                    triggerEdit.setResponder((trigger) ->
                            notif.setExclusionTrigger(index, trigger.strip()));
                    elements.add(triggerEdit);

                    elements.add(Button.builder(Component.literal("X"),
                                    (button) -> {
                                        notif.removeExclusionTrigger(index);
                                        listWidget.reload();
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
                super();
                elements.add(CycleButton.booleanBuilder(
                                Component.literal("Enabled"),
                                Component.literal("Disabled"))
                        .withInitialValue(notif.responseEnabled)
                        .withTooltip((status) -> Tooltip.create(Component.nullToEmpty(
                                "Chat messages or commands to be sent by the " +
                                "client immediately when this notification " +
                                "is activated. Use with caution.")))
                        .create(width / 2 - 120, 0, 240, 20,
                                Component.literal("Response Messages"),
                                (button, status) -> {
                                    notif.responseEnabled = status;
                                    listWidget.reload();
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
                super();
                this.index = index;

                if (index == -1) {
                    elements.add(Button.builder(Component.literal("+"),
                                    (button) -> {
                                        notif.addResponseMessage("");
                                        listWidget.reload();
                                    })
                            .size(240, 20)
                            .pos(width / 2 - 120, 0)
                            .build());
                }
                else if (index >= 0) {

                    EditBox messageEdit = new EditBox(client.font, width / 2 - 120, 0,
                            240, 20, Component.literal("Response Message"));
                    messageEdit.setMaxLength(120);
                    messageEdit.setValue(notif.getResponseMessage(index));
                    messageEdit.setResponder((message) ->
                            notif.setResponseMessage(index, message.strip()));
                    elements.add(messageEdit);

                    elements.add(Button.builder(Component.literal("X"),
                                    (button) -> {
                                        notif.removeResponseMessage(index);
                                        listWidget.reload();
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
                super();

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
                            listWidget.reload();
                        })
                        .size(240, 20)
                        .pos(width / 2 - 120, 0)
                        .build();
                resetButton.setTooltip(Tooltip.create(Component.literal(
                        "Resets all advanced settings for THIS notification.")));

                elements.add(resetButton);
            }
        }

        private static class ResetAllButton extends Entry {
            /**
             * When pressed, resets all advanced settings for all
             * {@code Notification}s.
             */
            ResetAllButton(int width, AdvancedConfigListWidget listWidget) {
                super();

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
                            listWidget.reload();
                        })
                        .size(240, 20)
                        .pos(width / 2 - 120, 0)
                        .build();
                resetButton.setTooltip(Tooltip.create(Component.literal(
                        "Resets all advanced settings for ALL notifications.")));

                elements.add(resetButton);
            }
        }

        private static class NuclearResetButton extends Entry {
            NuclearResetButton(int width, AdvancedConfigListWidget listWidget, Minecraft client) {
                super();

                Button resetButton = Button.builder(
                        Component.literal("Nuclear Reset"), (button) -> {
                            client.setScreen(new ConfirmScreen((value) -> {
                                if (value) {
                                    ChatNotify.restoreDefaultConfig();
                                    client.setScreen(null);
                                }
                                else {
                                    listWidget.reload();
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

                elements.add(resetButton);
            }
        }
    }
}