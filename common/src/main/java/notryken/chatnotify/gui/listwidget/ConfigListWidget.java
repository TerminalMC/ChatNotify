package notryken.chatnotify.gui.listwidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import notryken.chatnotify.gui.screen.ConfigScreen;

import java.util.ArrayList;
import java.util.List;

public abstract class ConfigListWidget
        extends ContainerObjectSelectionList<ConfigListWidget.Entry>
{
    public final Minecraft client;
    public final Screen parent;
    public final Component title;

    public ConfigListWidget(Minecraft client, int width, int height,
                            int top, int bottom, int itemHeight,
                            Screen parent, Component title)
    {
        super(client, width, height, top, bottom, itemHeight);
        this.client = client;
        this.parent = parent;
        this.title = title;
    }

    public void refreshScreen(ConfigListWidget listWidget)
    {
        client.setScreen(new ConfigScreen(this.parent, client.options,
                this.title, listWidget));
    }

    public int getRowWidth()
    {
        return 300;
    }

    protected int getScrollbarPosition()
    {
        return super.getScrollbarPosition() + 32;
    }

    public abstract ConfigListWidget resize(int width, int height,
                                            int top, int bottom);

    protected abstract void refreshScreen();

    public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry>
    {
        public final List<AbstractWidget> options;
        public final ConfigListWidget listWidget;
        public final int width;

        public Entry(int width, ConfigListWidget listWidget)
        {
            this.options = new ArrayList<>();
            this.listWidget = listWidget;
            this.width = width;
        }

        public void render(GuiGraphics context, int index, int y, int x,
                           int entryWidth, int entryHeight,
                           int mouseX, int mouseY,
                           boolean hovered, float tickDelta)
        {
            this.options.forEach((button) -> {
                button.setY(y);
                button.render(context, mouseX, mouseY, tickDelta);
            });
        }

        public List<? extends GuiEventListener> children()
        {
            return this.options;
        }

        public List<? extends NarratableEntry> narratables()
        {
            return this.options;
        }

        public static class Header extends Entry
        {
            public Header(int width, ConfigListWidget listWidget,
                          Minecraft client, Component label)
            {
                super(width, listWidget);
                this.options.add(new StringWidget(width / 2 - 120, 0, 240, 20,
                        label, client.font));
            }

            public Header(int width, ConfigListWidget listWidget,
                          Minecraft client, Component label, int labelColor)
            {
                super(width, listWidget);
                this.options.add(new StringWidget(width / 2 - 120, 0, 240, 20,
                        label, client.font).setColor(labelColor));
            }

            public Header(int width, ConfigListWidget listWidget,
                          Minecraft client, Component label, Component tooltip)
            {
                super(width, listWidget);
                StringWidget header = new StringWidget(width / 2 - 120, 0, 240, 20,
                        label, client.font);
                header.setTooltip(Tooltip.create(tooltip));
                this.options.add(header);
            }

            public Header(int width, ConfigListWidget listWidget,
                          Minecraft client, Component label, int labelColor,
                          Component tooltip)
            {
                super(width, listWidget);
                StringWidget header = new StringWidget(width / 2 - 120, 0, 240, 20,
                        label, client.font);
                header.setColor(labelColor);
                header.setTooltip(Tooltip.create(tooltip));
                this.options.add(header);
            }
        }

        public static class MultiLineHeader extends Entry
        {
            public MultiLineHeader(int width, ConfigListWidget listWidget,
                                   Minecraft client, Component label)
            {
                super(width, listWidget);
                this.options.add(new MultiLineTextWidget(width / 2 - 120, 0,
                        label, client.font)
                        .setMaxWidth(240));
            }

            public MultiLineHeader(int width, ConfigListWidget listWidget,
                                   Minecraft client, Component label,
                                   int labelColor)
            {
                super(width, listWidget);
                this.options.add(new MultiLineTextWidget(width / 2 - 120, 0,
                                label, client.font)
                        .setMaxWidth(240)
                        .setColor(labelColor));
            }

            public MultiLineHeader(int width, ConfigListWidget listWidget,
                                   Minecraft client,
                                   Component label, Component tooltip)
            {
                super(width, listWidget);
                MultiLineTextWidget header = new MultiLineTextWidget(
                        width / 2 - 120, 0, label, client.font)
                        .setMaxWidth(240);
                header.setTooltip(Tooltip.create(tooltip));
                this.options.add(header);
            }

            public MultiLineHeader(int width, ConfigListWidget listWidget,
                                   Minecraft client,
                                   Component label, int labelColor, Component tooltip)
            {
                super(width, listWidget);
                MultiLineTextWidget header = new MultiLineTextWidget(
                        width / 2 - 120, 0, label, client.font)
                        .setMaxWidth(240);
                header.setColor(labelColor);
                header.setTooltip(Tooltip.create(tooltip));
                this.options.add(header);
            }
        }
    }
}
