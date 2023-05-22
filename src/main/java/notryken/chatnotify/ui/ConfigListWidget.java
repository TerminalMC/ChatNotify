package notryken.chatnotify.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import notryken.chatnotify.config.Notification;

import java.util.ArrayList;
import java.util.List;

public abstract class ConfigListWidget
        extends ElementListWidget<ConfigListWidget.Entry>
{
    private final Screen parent;
    private final Text title;

    public ConfigListWidget(MinecraftClient minecraftClient,
                            int i, int j, int k, int l, int m,
                            Screen parent,
                            Text title,
                            Notification notif)
    {
        super(minecraftClient, i, j, k, l, m);
        this.setRenderSelection(true);
        this.parent = parent;
        this.title = title;
    }

    public void refreshScreen()
    {
        client.setScreen(new ConfigScreen(this.parent, MinecraftClient.getInstance().options, this.title, this));
    }

    public int getRowWidth()
    {
        return 300;
    }

    protected int getScrollbarPositionX()
    {
        return super.getScrollbarPositionX() + 32;
    }

    public abstract static class Entry extends ElementListWidget.Entry<Entry>
    {
        public List<ClickableWidget> options;
        public Notification notif;
        public ConfigListWidget listWidget;
        public int width;

        Entry(int width, Notification notif, ConfigListWidget listWidget)
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
    }
}
