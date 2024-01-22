package notryken.chatnotify.gui.component.listwidget;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import notryken.chatnotify.ChatNotify;
import notryken.chatnotify.config.Notification;

/**
 * {@code ConfigListWidget} containing controls for advanced settings of the
 * specified {@code Notification}, including regex toggle, exclusion triggers
 * and automatic response messages.
 */
public class AdvancedConfigListWidget extends ConfigListWidget {
    private final Notification notif;

    public AdvancedConfigListWidget(Minecraft minecraft, int width, int height,
                                    int top, int bottom, int itemHeight,
                                    int entryRelX, int entryWidth, int entryHeight,
                                    Notification notif) {
        super(minecraft, width, height, top, bottom, itemHeight,
                width / 2 + entryRelX, entryWidth, entryHeight);
        this.notif = notif;

        addEntry(new ConfigListWidget.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("WARNING").withStyle(Style.EMPTY.withColor(ChatFormatting.RED)),
                null, -1));

        addEntry(new ConfigListWidget.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("These settings allow you to break ChatNotify and crash " +
                        "Minecraft. Use with caution."), null, -1));

        addEntry(new ConfigListWidget.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Notification Trigger Regex"), null, -1));
        addEntry(new Entry.RegexToggleButton(entryX, entryWidth, entryHeight, notif, this));

        addEntry(new ConfigListWidget.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Notification Exclusion Triggers"), null, -1));
        addEntry(new Entry.ExclusionToggleButton(entryX, entryWidth, entryHeight, notif, this));

        if (this.notif.exclusionEnabled) {
            for (int i = 0; i < this.notif.getExclusionTriggers().size(); i ++) {
                addEntry(new Entry.ExclusionTriggerField(entryX, entryWidth, entryHeight, notif, this, i));
            }
            addEntry(new ConfigListWidget.Entry.ActionButtonEntry(entryX, 0, entryWidth, entryHeight,
                    Component.literal("+"), null, -1,
                    (button) -> {
                        notif.addExclusionTrigger("");
                        reload();
                    }));
        }

        addEntry(new ConfigListWidget.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Auto Response Messages"), null, -1));
        addEntry(new Entry.ResponseToggleButton(entryX, entryWidth, entryHeight, notif, this));

        if (this.notif.responseEnabled) {
            for (int i = 0; i < this.notif.getResponseMessages().size(); i ++) {
                addEntry(new Entry.ResponseMessageField(entryX, entryWidth, entryHeight, notif, this, i));
            }
            addEntry(new ConfigListWidget.Entry.ActionButtonEntry(entryX, 0, entryWidth, entryHeight,
                    Component.literal("+"), null, -1,
                    (button) -> {
                        notif.addResponseMessage("");
                        reload();
                    }));
        }

        addEntry(new ConfigListWidget.Entry.TextEntry(entryX, entryWidth, entryHeight,
                Component.literal("Broken Everything?"), null, -1));

        addEntry(new ConfigListWidget.Entry.ActionButtonEntry(entryX, 0, entryWidth, entryHeight,
                Component.literal("Reset Advanced Options"), Tooltip.create(
                        Component.literal("Resets all advanced settings for THIS notification.")),
                -1,
                (button) -> {
                    notif.resetAdvanced();
                    reload();
                }));

        addEntry(new ConfigListWidget.Entry.ActionButtonEntry(entryX, 0, entryWidth, entryHeight,
                Component.literal("Reset All Advanced Options"), Tooltip.create(
                Component.literal("Resets all advanced settings for ALL notifications.")),
                -1,
                (button) -> {
                    for (Notification notif2 : ChatNotify.config().getNotifs()) {
                        notif2.resetAdvanced();
                    }
                    reload();
                }));

        addEntry(new ConfigListWidget.Entry.ActionButtonEntry(entryX, 0, entryWidth, entryHeight,
                Component.literal("Nuclear Reset"), Tooltip.create(
                Component.literal("Deletes all ChatNotify notifications and resets all settings.")),
                -1,
                (button) -> minecraft.setScreen(new ConfirmScreen(
                        (value) -> {
                            if (value) {
                                ChatNotify.restoreDefaultConfig();
                                minecraft.setScreen(null);
                            }
                            else {
                                reload();
                            }},
                        Component.literal("Nuclear Reset"),
                        Component.literal("Are you sure you want to delete all ChatNotify " +
                                "notifications and reset all settings?")))));

    }

    @Override
    public AdvancedConfigListWidget resize(int width, int height, int top, int bottom, int itemHeight) {
        return new AdvancedConfigListWidget(minecraft, width, height, top, bottom, itemHeight,
                entryX, entryWidth, entryHeight, notif);
    }

    private abstract static class Entry extends ConfigListWidget.Entry {

        private static class RegexToggleButton extends Entry {
            RegexToggleButton(int x, int width, int height, Notification notif,
                              AdvancedConfigListWidget listWidget) {
                super();
                elements.add(CycleButton.booleanBuilder(
                        Component.literal("Enabled"), Component.literal("Disabled"))
                        .withInitialValue(notif.regexEnabled)
                        .withTooltip((status) -> Tooltip.create(Component.nullToEmpty(
                                "If enabled, all triggers for this notification will be " +
                                "processed as complete regular expressions.")))
                        .create(x, 0, width, height, Component.literal("Regex"),
                                (button, status) -> {
                                    notif.regexEnabled = status;
                                    listWidget.reload();
                                }));
            }
        }

        private static class ExclusionToggleButton extends Entry {
            ExclusionToggleButton(int x, int width, int height, Notification notif,
                              AdvancedConfigListWidget listWidget) {
                super();
                elements.add(CycleButton.booleanBuilder(
                        Component.literal("Enabled"), Component.literal("Disabled"))
                        .withInitialValue(notif.exclusionEnabled)
                        .withTooltip((status) -> Tooltip.create(Component.nullToEmpty(
                                "If an exclusion trigger is detected in a message, " +
                                "it will prevent this notification from activating.")))
                        .create(x, 0, width, height, Component.literal("Exclusion Triggers"),
                                (button, status) -> {
                                    notif.exclusionEnabled = status;
                                    listWidget.reload();
                                }));
            }
        }

        private static class ExclusionTriggerField extends Entry {

            ExclusionTriggerField(int x, int width, int height, Notification notif,
                                  AdvancedConfigListWidget listWidget, int index) {
                super();

                int spacing = 5;
                int removeButtonWidth = 20;

                EditBox triggerEditBox = new EditBox(Minecraft.getInstance().font,
                        x, 0, width, height, Component.literal("Exclusion Trigger"));
                triggerEditBox.setMaxLength(120);
                triggerEditBox.setValue(notif.getExclusionTrigger(index));
                triggerEditBox.setResponder((trigger) ->
                        notif.setExclusionTrigger(index, trigger.strip()));
                elements.add(triggerEditBox);

                elements.add(Button.builder(Component.literal("X"),
                                (button) -> {
                                    notif.removeExclusionTrigger(index);
                                    listWidget.reload();
                                })
                        .pos(x + width + spacing, 0)
                        .size(removeButtonWidth, height)
                        .build());
            }
        }

        private static class ResponseToggleButton extends Entry {
            ResponseToggleButton(int x, int width, int height, Notification notif,
                                 AdvancedConfigListWidget listWidget)
            {
                super();
                elements.add(CycleButton.booleanBuilder(
                        Component.literal("Enabled"), Component.literal("Disabled"))
                        .withInitialValue(notif.responseEnabled)
                        .withTooltip((status) -> Tooltip.create(Component.nullToEmpty(
                                "Chat messages or commands to be sent by the client " +
                                        "immediately when this notification is activated. " +
                                        "Use with caution.")))
                        .create(x, 0, width, height, Component.literal("Response Messages"),
                                (button, status) -> {
                                    notif.responseEnabled = status;
                                    listWidget.reload();
                                }));
            }
        }

        private static class ResponseMessageField extends Entry {

            ResponseMessageField(int x, int width, int height, Notification notif,
                                 AdvancedConfigListWidget listWidget, int index) {
                super();

                int spacing = 5;
                int removeButtonWidth = 20;

                EditBox messageEditBox = new EditBox(Minecraft.getInstance().font,
                        x, 0, width, height, Component.literal("Response Message"));
                messageEditBox.setMaxLength(120);
                messageEditBox.setValue(notif.getResponseMessage(index));
                messageEditBox.setResponder((message) ->
                        notif.setResponseMessage(index, message.strip()));
                elements.add(messageEditBox);

                elements.add(Button.builder(Component.literal("X"),
                                (button) -> {
                                    notif.removeResponseMessage(index);
                                    listWidget.reload();
                                })
                        .pos(x + width + spacing, 0)
                        .size(removeButtonWidth, height)
                        .build());
            }
        }
    }
}