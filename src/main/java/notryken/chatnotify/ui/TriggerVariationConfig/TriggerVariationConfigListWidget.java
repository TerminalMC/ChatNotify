package notryken.chatnotify.ui.TriggerVariationConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import notryken.chatnotify.config.Notification;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TriggerVariationConfigListWidget extends
        ElementListWidget<TriggerVariationConfigListWidget.ConfigEntry>
{
    public TriggerVariationConfigListWidget(MinecraftClient client, int i,
                                            int j, int k, int l, int m,
                                            Notification notif)
    {
        super(client, i, j, k, l, m);
        this.setRenderSelection(true);

        this.addEntry(new ConfigEntry.Header(width, notif, client, this,
                "Use this if you want the notification to also be triggered " +
                        "by variations of the main word."));
        this.addEntry(new ConfigEntry.Header(
                width, notif, client, this, "(Not CaSe-SeNsItIvE)"));

        for (int idx = 1; idx < notif.getNumTriggers(); idx ++) {
            this.addEntry(new ConfigEntry.TriggerVariationField(
                    width, notif, client, this, idx));
        }
        this.addEntry(new ConfigEntry.TriggerVariationField(
                width, notif, client, this, -1));
    }

    public void addTriggerVariation(Notification notif)
    {
        List<TriggerVariationConfigListWidget.ConfigEntry> entries =
                this.children();

        int size = notif.getNumTriggers();
        notif.addTrigger("");

        entries.add(size + 1, new ConfigEntry.TriggerVariationField(
                width, notif, client, this, size));
    }

    public void removeTriggerVariation(Notification notif, int index)
    {
        List<TriggerVariationConfigListWidget.ConfigEntry> entries =
                this.children();

        notif.removeTriggerVariation(index);

        entries.remove(index + 1); // Offset from headers.
    }

    public int getRowWidth()
    {
        return 300;
    }

    protected int getScrollbarPositionX()
    {
        return super.getScrollbarPositionX() + 32;
    }

    public abstract static class ConfigEntry extends Entry<ConfigEntry>
    {
        public List<ClickableWidget> options;
        public Notification notif;
        public TriggerVariationConfigListWidget listWidget;
        public int width;

        ConfigEntry(int width, Notification notif,
                    TriggerVariationConfigListWidget listWidget)
        {
            this.options = new ArrayList<>();
            this.notif = notif;
            this.listWidget = listWidget;
            this.width = width;
        }

        public void render(MatrixStack matrices, int index, int y, int x,
                           int entryWidth, int entryHeight,
                           int mouseX, int mouseY,
                           boolean hovered, float tickDelta)
        {
            this.options.forEach((button) -> {
                button.setY(y);
                button.render(matrices, mouseX, mouseY, tickDelta);
            });
        }

        public List<? extends Element> children()
        {
            return this.options;
        }

        public List<? extends Selectable> selectableChildren()
        {
            return this.options;
        }

        public static class Header extends ConfigEntry
        {
            Header(int width, Notification notif,
                   @NotNull MinecraftClient client,
                   TriggerVariationConfigListWidget listWidget,
                   String label)
            {
                super(width, notif, listWidget);
                this.options.add(new TextWidget(width / 2 - 120, 0, 240, 20,
                        Text.literal(label),
                        client.textRenderer));
            }
        }

        public static class TriggerVariationField extends ConfigEntry
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

            public void setTriggerVariation(String trigger)
            {
                this.notif.setTrigger(this.index, trigger.strip());
            }
        }
    }
}