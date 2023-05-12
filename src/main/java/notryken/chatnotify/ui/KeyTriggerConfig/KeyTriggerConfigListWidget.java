package notryken.chatnotify.ui.KeyTriggerConfig;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import notryken.chatnotify.config.Notification;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class KeyTriggerConfigListWidget extends
        ElementListWidget<KeyTriggerConfigListWidget.ConfigEntry>
{
    public KeyTriggerConfigListWidget(MinecraftClient client, int i, int j, int k, int l,
                                      int m, Notification notif, Screen parent)
    {
        super(client, i, j, k, l, m);
        this.setRenderSelection(true);

        this.addEntry(new ConfigEntry.Header(
                width, notif, client, this, "Notification Trigger Key"));
        this.addEntry(new ConfigEntry.TriggerField(
                width, notif, client, this));
        this.addEntry(new ConfigEntry.Header(
                width, notif, client, this, "Quick Keys"));

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
            this.addEntry(new ConfigEntry.TriggerOption(
                    width, notif, client, this, parent, key));
        }
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
        public KeyTriggerConfigListWidget listWidget;
        public int width;

        ConfigEntry(int width, Notification notif, KeyTriggerConfigListWidget
                listWidget)
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
                   KeyTriggerConfigListWidget listWidget,
                   String label)
            {
                super(width, notif, listWidget);
                this.options.add(new TextWidget(width / 2 - 120, 0, 240, 20,
                        Text.literal(label),
                        client.textRenderer));
            }
        }

        public static class TriggerField extends ConfigEntry
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

                this.options.add(triggerEdit);
            }

            public void setTrigger(String trigger)
            {
                this.notif.setTrigger(trigger.strip());
            }
        }

        public static class TriggerOption extends ConfigEntry
        {
            TriggerOption(int width, Notification notif, MinecraftClient client,
                          KeyTriggerConfigListWidget listWidget, Screen parent,
                          String[] key)
            {
                super(width, notif, listWidget);

                this.options.add(ButtonWidget.builder(Text.literal(key[1]),
                        (button) -> {
                    notif.setTrigger(key[0]);
                    client.setScreen(new KeyTriggerConfigScreen(parent, notif));
                }).size(240, 20).position(width / 2 - 120, 0).build());
            }
        }
    }
}