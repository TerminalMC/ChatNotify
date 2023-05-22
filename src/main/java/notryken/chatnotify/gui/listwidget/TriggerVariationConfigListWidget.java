package notryken.chatnotify.gui.listwidget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import notryken.chatnotify.config.Notification;
import org.jetbrains.annotations.NotNull;

public class TriggerVariationConfigListWidget extends ConfigListWidget
{
    private final Notification notif;

    public TriggerVariationConfigListWidget(MinecraftClient client,
                                            int i, int j, int k, int l, int m,
                                            Screen parent, Text title,
                                            Notification notif)
    {
        super(client, i, j, k, l, m, parent, title);
        this.notif = notif;

        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Text.literal("Other words to trigger the same " +
                "notification.")));

        this.addEntry(new ConfigListWidget.Entry.Header(width, this,
                client, Text.literal("(Not CaSe-SeNsItIvE)")));

        for (int idx = 1; idx < notif.getNumTriggers(); idx ++) {
            this.addEntry(new Entry.TriggerVariationField(
                    width, notif, client, this, idx));
        }
        this.addEntry(new Entry.TriggerVariationField(
                width, notif, client, this, -1));
    }

    @Override
    protected void refreshScreen()
    {
        refreshScreen(new TriggerVariationConfigListWidget(client,
                this.width, this.height, this.top, this.bottom, this.itemHeight,
                this.parent, this.title, this.notif));
    }

    private void addTriggerVariation(Notification notif)
    {
        int size = notif.getNumTriggers();
        notif.addTrigger("");

        this.children().add(size + 1, new Entry.TriggerVariationField(
                width, notif, client, this, size));
    }

    private void removeTriggerVariation(Notification notif, int index)
    {
        notif.removeTriggerVariation(index);

        this.children().remove(index + 1); // Offset from headers.
    }

    private abstract static class Entry extends ConfigListWidget.Entry
    {
        public final Notification notif;

        Entry(int width, Notification notif,
              TriggerVariationConfigListWidget listWidget)
        {
            super(width, listWidget);
            this.notif = notif;
        }

        private static class TriggerVariationField extends Entry
        {
            int index;

            TriggerVariationField(int width, Notification notif,
                                  @NotNull MinecraftClient client,
                                  TriggerVariationConfigListWidget listWidget,
                                  int index)
            {
                super(width, notif, listWidget);
                this.index = index;

                if (index >= 0) {
                    TextFieldWidget triggerEdit = new TextFieldWidget(
                            client.textRenderer, this.width / 2 - 120, 0, 240,
                            20, Text.literal("Notification Trigger"));
                    triggerEdit.setMaxLength(120);
                    triggerEdit.setText(this.notif.getTrigger(index));
                    triggerEdit.setChangedListener(this::setTriggerVariation);

                    this.options.add(triggerEdit);

                    this.options.add(ButtonWidget.builder(Text.literal("X"),
                                    (button) ->
                                            listWidget.removeTriggerVariation(
                                                    notif, index))
                            .size(20, 20).position(width / 2 + 120 + 5, 0)
                            .build());
                }
                else {
                    this.options.add(ButtonWidget.builder(Text.literal("+"),
                                    (button) -> listWidget.addTriggerVariation(
                                            notif)).size(240, 20)
                            .position(width / 2 - 120, 0)
                            .build());
                }
            }

            private void setTriggerVariation(String trigger)
            {
                this.notif.setTrigger(this.index, trigger.strip());
            }
        }
    }
}