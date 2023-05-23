package notryken.chatnotify.gui.listwidget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import notryken.chatnotify.config.Notification;
import org.jetbrains.annotations.NotNull;

public class KeyTriggerConfigListWidget extends ConfigListWidget
{
    private final Notification notif;

    public KeyTriggerConfigListWidget(MinecraftClient client,
                                      int i, int j, int k, int l, int m,
                                      Screen parent, Text title,
                                      Notification notif)
    {
        super(client, i, j, k, l, m, parent, title);
        this.notif = notif;

        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Text.literal("May not work on some servers.")));
        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Text.literal("Trigger Key")));
        this.addEntry(new Entry.TriggerField(
                width, notif, client, this));
        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Text.literal("Quick Keys")));

        String[][] keys = new String[][]
                {
                        {"chat.type", "Any Message"},
                        {"commands.message.display", "Private Message"},
                        {"multiplayer.player.joined", "Player Joined"},
                        {"multiplayer.player.left", "Player Left"},
                        {"chat.type.advancement", "Advancement"},
                        {"death.", "Player/Pet Died"}
                };
        for (String[] key : keys) {
            this.addEntry(new Entry.TriggerOption(width, notif, this, key));
        }
    }

    @Override
    public KeyTriggerConfigListWidget resize(int width, int height,
                                             int top, int bottom)
    {
        KeyTriggerConfigListWidget listWidget =
                new KeyTriggerConfigListWidget(client, width, height, top,
                        bottom, itemHeight, parent, title, notif);
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

        Entry(int width, Notification notif,
              KeyTriggerConfigListWidget listWidget)
        {
            super(width, listWidget);
            this.notif = notif;
        }

        private static class TriggerField extends Entry
        {
            TriggerField(int width, Notification notif,
                         @NotNull MinecraftClient client,
                         KeyTriggerConfigListWidget listWidget)
            {
                super(width, notif, listWidget);

                TextFieldWidget triggerEdit = new TextFieldWidget(
                        client.textRenderer, this.width / 2 - 120, 0, 240, 20,
                        Text.literal("Notification Trigger"));
                triggerEdit.setMaxLength(120);
                triggerEdit.setText(this.notif.getTrigger());
                triggerEdit.setChangedListener(this::setTrigger);

                options.add(triggerEdit);
            }

            private void setTrigger(String trigger)
            {
                notif.setTrigger(trigger.strip());
            }
        }

        private static class TriggerOption extends Entry
        {
            TriggerOption(int width, Notification notif,
                          KeyTriggerConfigListWidget listWidget, String[] key)
            {
                super(width, notif, listWidget);

                options.add(ButtonWidget.builder(Text.literal(key[1]),
                        (button) -> {
                    notif.setTrigger(key[0]);
                    listWidget.refreshScreen();
                }).size(240, 20).position(width / 2 - 120, 0).build());
            }
        }
    }
}