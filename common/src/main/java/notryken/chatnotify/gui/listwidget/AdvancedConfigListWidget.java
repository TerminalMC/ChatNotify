package notryken.chatnotify.gui.listwidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import notryken.chatnotify.ChatNotify;
import notryken.chatnotify.config.Notification;
import org.jetbrains.annotations.NotNull;

public class AdvancedConfigListWidget extends ConfigListWidget
{
    private final Notification notif;

    public AdvancedConfigListWidget(Minecraft client,
                                    int width, int height,
                                    int top, int bottom, int itemHeight,
                                    Screen parent, Component title,
                                    Notification notif)
    {
        super(client, width, height, top, bottom, itemHeight, parent, title);
        this.notif = notif;

        this.addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                Component.literal("WARNING"), 16711680));
        this.addEntry(new ConfigListWidget.Entry.MultiLineHeader(width, this,
                client, Component.literal("These settings allow you to break " +
                    "ChatNotify and crash Minecraft. Use with caution.")));

        this.addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                Component.nullToEmpty("Notification Trigger Regex")));
        this.addEntry(new Entry.RegexToggleButton(width, notif, this));

        this.addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                Component.nullToEmpty("Notification Exclusion Triggers")));
        this.addEntry(new Entry.ExclusionToggleButton(width, notif, this));

        if (notif.exclusionEnabled) {
            for (int idx = 0; idx < notif.getExclusionTriggers().size(); idx ++) {
                this.addEntry(new Entry.ExclusionTriggerField(width, notif, client,
                        this, idx));
            }
            this.addEntry(new Entry.ExclusionTriggerField(width, notif, client,
                    this, -1));
        }

        this.addEntry(new ConfigListWidget.Entry.Header(width, this, client,
                Component.nullToEmpty("Auto Response Messages")));
        this.addEntry(new Entry.ResponseToggleButton(width, notif, this));

        if (notif.responseEnabled) {
            for (int idx = 0; idx < notif.getResponseMessages().size(); idx ++) {
                this.addEntry(new Entry.ResponseMessageField(width, notif, client,
                        this, idx));
            }
            this.addEntry(new Entry.ResponseMessageField(width, notif, client,
                    this, -1));
        }

        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Component.literal("Broken Everything?")));
        this.addEntry(new Entry.ResetButton(width, notif, this));
        this.addEntry(new Entry.TotalResetButton(width, notif, this));
        this.addEntry(new Entry.NuclearResetButton(width, notif, this, client));
    }

    @Override
    public AdvancedConfigListWidget resize(int width, int height,
                                           int top, int bottom)
    {
        AdvancedConfigListWidget listWidget =
                new AdvancedConfigListWidget(client, width, height, top, bottom,
                        itemHeight, parent, title, notif);
        listWidget.setScrollAmount(this.getScrollAmount());
        return listWidget;
    }

    @Override
    protected void refreshScreen()
    {
        refreshScreen(this);
    }

    private abstract static class Entry extends ConfigListWidget.Entry
    {
        public final Notification notif;

        Entry(int width, Notification notif, AdvancedConfigListWidget listWidget)
        {
            super(width, listWidget);
            this.notif = notif;
        }

        private static class RegexToggleButton extends Entry
        {
            RegexToggleButton(int width, Notification notif,
                                  AdvancedConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                options.add(CycleButton.booleanBuilder(
                                Component.literal("Enabled"),
                                Component.literal("Disabled"))
                        .withInitialValue(notif.regexEnabled)
                        .withTooltip((status) -> Tooltip.create(Component.nullToEmpty("If enabled, " +
                                "all triggers for this notification will be " +
                                "processed as regex. Note: If using regex, " +
                                "double-escapes must be used ('\\\\' instead " +
                                "of the normal '\\').")))
                        .create(this.width / 2 - 120, 0, 240, 20,
                                Component.literal("Regex"),
                                (button, status) -> {
                                    notif.regexEnabled = status;
                                    listWidget.refreshScreen();
                                }));
            }
        }

        private static class ExclusionToggleButton extends Entry
        {
            ExclusionToggleButton(int width, Notification notif,
                              AdvancedConfigListWidget listWidget)
            {
                super(width, notif, listWidget);
                options.add(CycleButton.booleanBuilder(
                                Component.literal("Enabled"),
                                Component.literal("Disabled"))
                        .withInitialValue(notif.exclusionEnabled)
                        .withTooltip((status) -> Tooltip.create(Component.nullToEmpty("If an " +
                                "exclusion trigger is detected in a message, " +
                                "it will prevent this notification from " +
                                "activating when it otherwise would.")))
                        .create(this.width / 2 - 120, 0, 240, 20,
                                Component.literal("Exclusion Triggers"),
                                (button, status) -> {
                                    notif.exclusionEnabled = status;
                                    listWidget.refreshScreen();
                                }));
            }
        }

        private static class ExclusionTriggerField extends Entry
        {
            final int index;

            ExclusionTriggerField(int width, Notification notif,
                                  @NotNull Minecraft client,
                                  AdvancedConfigListWidget listWidget,
                                  int index)
            {
                super(width, notif, listWidget);
                this.index = index;

                if (index >= 0) {
                    EditBox triggerEdit = new EditBox(
                            client.font, this.width / 2 - 120, 0, 240,
                            20, Component.literal("Exclusion Trigger"));
                    triggerEdit.setMaxLength(120);
                    triggerEdit.setValue(this.notif.getExclusionTrigger(index));
                    triggerEdit.setResponder(this::setExclusionTrigger);

                    this.options.add(triggerEdit);

                    this.options.add(Button.builder(Component.literal("X"),
                                    (button) -> {
                                        notif.removeExclusionTrigger(index);
                                        listWidget.refreshScreen();
                                    })
                            .size(20, 20)
                            .pos(width / 2 + 120 + 5, 0)
                            .build());
                }
                else {
                    this.options.add(Button.builder(Component.literal("+"),
                                    (button) -> {
                                        notif.addExclusionTrigger("");
                                        listWidget.refreshScreen();
                                    })
                            .size(240, 20)
                            .pos(width / 2 - 120, 0)
                            .build());
                }
            }

            private void setExclusionTrigger(String exclusionTrigger)
            {
                this.notif.setExclusionTrigger(this.index,
                        exclusionTrigger.strip());
            }
        }

        private static class ResponseToggleButton extends Entry
        {
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

        private static class ResponseMessageField extends Entry
        {
            final int index;

            ResponseMessageField(int width, Notification notif,
                                 @NotNull Minecraft client,
                                 AdvancedConfigListWidget listWidget,
                                 int index)
            {
                super(width, notif, listWidget);
                this.index = index;

                if (index >= 0) {
                    EditBox messageEdit = new EditBox(
                            client.font, this.width / 2 - 120, 0, 240,
                            20, Component.literal("Response Message"));
                    messageEdit.setMaxLength(120);
                    messageEdit.setValue(this.notif.getResponseMessage(index));
                    messageEdit.setResponder(this::setResponseMessage);

                    this.options.add(messageEdit);

                    this.options.add(Button.builder(Component.literal("X"),
                                    (button) -> {
                                        notif.removeResponseMessage(index);
                                        listWidget.refreshScreen();
                                    })
                            .size(20, 20)
                            .pos(width / 2 + 120 + 5, 0)
                            .build());
                }
                else {
                    this.options.add(Button.builder(Component.literal("+"),
                                    (button) -> {
                                        notif.addResponseMessage("");
                                        listWidget.refreshScreen();
                                    })
                            .size(240, 20)
                            .pos(width / 2 - 120, 0)
                            .build());
                }
            }

            private void setResponseMessage(String exclusionTrigger)
            {
                this.notif.setResponseMessage(this.index,
                        exclusionTrigger.strip());
            }
        }

        private static class ResetButton extends Entry
        {
            ResetButton(int width, Notification notif,
                        AdvancedConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                Button resetButton = Button.builder(
                        Component.literal("Reset"), (button) -> {
                            notif.regexEnabled = false;
                            notif.exclusionEnabled = false;
                            notif.responseEnabled = false;
                            for (int i = 0;
                                 i < notif.getExclusionTriggers().size();
                                 i++)
                            {
                                notif.removeExclusionTrigger(0);
                            }
                            for (int i = 0;
                                 i < notif.getResponseMessages().size();
                                 i++)
                            {
                                notif.removeResponseMessage(0);
                            }
                            listWidget.refreshScreen();
                        })
                        .size(240, 20)
                        .pos(width / 2 - 120, 0)
                        .build();
                resetButton.setTooltip(Tooltip.create(Component.literal("Resets all " +
                        "advanced settings for THIS notification.")));

                this.options.add(resetButton);
            }
        }

        private static class TotalResetButton extends Entry
        {
            TotalResetButton(int width, Notification notif,
                            AdvancedConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                Button totalResetButton = Button.builder(
                        Component.literal("Reset All"), (button) -> {
                            for (Notification notif2 :
                                    ChatNotify.config.getNotifs())
                            {
                                notif2.regexEnabled = false;
                                notif2.exclusionEnabled = false;
                                notif2.responseEnabled = false;
                                for (int i = 0;
                                     i < notif2.getExclusionTriggers().size();
                                     i++)
                                {
                                    notif2.removeExclusionTrigger(0);
                                }
                                for (int i = 0;
                                     i < notif2.getResponseMessages().size();
                                     i++)
                                {
                                    notif2.removeResponseMessage(0);
                                }
                            }
                            listWidget.refreshScreen();

                        })
                        .size(240, 20)
                        .pos(width / 2 - 120, 0)
                        .build();
                totalResetButton.setTooltip(Tooltip.create(Component.literal("Resets " +
                        "all advanced settings for ALL notifications.")));

                this.options.add(totalResetButton);
            }
        }

        private static class NuclearResetButton extends Entry
        {
            NuclearResetButton(int width, Notification notif,
                               AdvancedConfigListWidget listWidget,
                               Minecraft client)
            {
                super(width, notif, listWidget);

                Button nuclearResetButton = Button.builder(
                        Component.literal("Nuclear Reset"), (button) -> {
                            client.setScreen(new ConfirmScreen((value) -> {
                                if (value) {
                                    ChatNotify.deleteConfigFile();
                                    ChatNotify.config = null;
                                    ChatNotify.loadConfig();
                                    client.setScreen(null);
                                }
                                else {
                                    listWidget.refreshScreen();
                                }
                            }, Component.literal("Nuclear Reset"), Component.literal(
                                    "Are you sure you want to delete all " +
                                            "ChatNotify notifications and " +
                                            "reset all settings?")));
                        })
                        .size(240, 20)
                        .pos(width / 2 - 120, 0)
                        .build();

                nuclearResetButton.setTooltip(Tooltip.create(Component.literal(
                        "Deletes all ChatNotify notifications and resets all " +
                                "settings.")));

                this.options.add(nuclearResetButton);
            }
        }
    }
}