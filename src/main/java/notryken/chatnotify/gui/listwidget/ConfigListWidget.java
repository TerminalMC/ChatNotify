package notryken.chatnotify.gui.listwidget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import notryken.chatnotify.gui.screen.ConfigScreen;

import java.util.ArrayList;
import java.util.List;

public abstract class ConfigListWidget
        extends ElementListWidget<ConfigListWidget.Entry>
{
    public final MinecraftClient client;
    public final Screen parent;
    public final Text title;

    public ConfigListWidget(MinecraftClient client,
                            int i, int j, int k, int l, int m,
                            Screen parent, Text title)
    {
        super(client, i, j, k, l, m);
        this.setRenderSelection(true);
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

    protected int getScrollbarPositionX()
    {
        return super.getScrollbarPositionX() + 32;
    }

    public abstract ConfigListWidget resize(int width, int height,
                                            int top, int bottom);

    protected abstract void refreshScreen();

    public abstract static class Entry extends ElementListWidget.Entry<Entry>
    {
        public List<ClickableWidget> options;
        public ConfigListWidget listWidget;
        public int width;

        public Entry(int width, ConfigListWidget listWidget)
        {
            this.options = new ArrayList<>();
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

        public static class Header extends Entry {
            public Header(int width, ConfigListWidget listWidget,
                          MinecraftClient client, Text label) {
                super(width, listWidget);
                this.options.add(new TextWidget(width / 2 - 120, 0, 240, 20,
                        label, client.textRenderer));
            }
        }
    }
}
