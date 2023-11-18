package notryken.chatnotify.gui.components.listwidget;

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
import notryken.chatnotify.gui.screen.ConfigSubScreen;

import java.util.ArrayList;
import java.util.List;

/**
 * Base implementation of ChatNotify options list widget.
 * <p>
 * A {@code ConfigListWidget} has a list of {@code ConfigListWidget.Entry}
 * objects, which are drawn onto the screen top-down in the order that they
 * are stored, with standard spacing.
 * <p>
 * <b>Note:</b> if you want multiple components (e.g. buttons, text fields) to
 * appear side-by-side rather than spaced vertically, you must add them all to a
 * single Entry's list of {@code AbstractWidgets}.
 */
public abstract class ConfigListWidget
        extends ContainerObjectSelectionList<ConfigListWidget.Entry> {

    public final Minecraft client;
    public final Screen parentScreen;
    public final Component title;

    public ConfigListWidget(Minecraft client, int width, int height, int top, int bottom,
                            int itemHeight, Screen parentScreen, Component title) {
        super(client, width, height, top, bottom, itemHeight);
        this.client = client;
        this.parentScreen = parentScreen;
        this.title = title;
    }

    // Default methods

    /**
     * Pseudo-refresh method. Sets the client screen to a new instance of
     * {@code ConfigSubScreen}, with the specified {@code ConfigListWidget} and
     * the stored parent screen.
     * @param listWidget the {@code ConfigListWidget} to provide to the new
     *                   screen.
     */
    public void refreshScreen(ConfigListWidget listWidget) {
        client.setScreen(new ConfigSubScreen(parentScreen, client.options, title, listWidget));
    }

    public int getRowWidth() {
        // Sets the position of the scrollbar
        return 300;
    }

    protected int getScrollbarPosition() {
        // Offset since ChatNotify config screens use a size-32 buffer
        return super.getScrollbarPosition() + 32;
    }

    // Abstract methods

    protected abstract void refreshScreen();

    public abstract ConfigListWidget resize(int width, int height, int top, int bottom);

    /**
     * Base implementation of ChatNotify options list widget entry, with common
     * entries.
     */
    public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        public final List<AbstractWidget> options;
        public final ConfigListWidget listWidget;
        public final int width;

        public Entry(int width, ConfigListWidget listWidget) {
            this.options = new ArrayList<>();
            this.listWidget = listWidget;
            this.width = width;
        }

        public void render(GuiGraphics context, int index, int y, int x,
                           int entryWidth, int entryHeight, int mouseX, int mouseY,
                           boolean hovered, float tickDelta) {
            options.forEach((button) -> {
                button.setY(y);
                button.render(context, mouseX, mouseY, tickDelta);
            });
        }

        public List<? extends GuiEventListener> children() {
            return this.options;
        }

        public List<? extends NarratableEntry> narratables() {
            return this.options;
        }

        // Default Entry implementations

        /**
         * A {@code Header} is a {@code StringWidget} with position and
         * dimensions set to standard ChatNotify values.
         */
        public static class Header extends Entry {
            public Header(int width, ConfigListWidget listWidget, Minecraft client,
                          Component label) {
                super(width, listWidget);
                options.add(new StringWidget(width / 2 - 120, 0, 240, 20,
                        label, client.font));
            }

            public Header(int width, ConfigListWidget listWidget, Minecraft client,
                          Component label, int labelColor) {
                super(width, listWidget);
                options.add(new StringWidget(width / 2 - 120, 0, 240, 20,
                        label, client.font).setColor(labelColor));
            }

            public Header(int width, ConfigListWidget listWidget, Minecraft client,
                          Component label, Component tooltip) {
                super(width, listWidget);
                StringWidget header = new StringWidget(width / 2 - 120, 0, 240, 20,
                        label, client.font);
                header.setTooltip(Tooltip.create(tooltip));
                options.add(header);
            }

            public Header(int width, ConfigListWidget listWidget, Minecraft client,
                          Component label, int labelColor, Component tooltip) {
                super(width, listWidget);
                StringWidget header = new StringWidget(width / 2 - 120, 0, 240, 20,
                        label, client.font);
                header.setColor(labelColor);
                header.setTooltip(Tooltip.create(tooltip));
                options.add(header);
            }
        }

        /**
         * A {@code MultiLineHeader} is a {@code MultiLineTextWidget} with
         * position and dimensions set to standard ChatNotify values.
         */
        public static class MultiLineHeader extends Entry {
            public MultiLineHeader(int width, ConfigListWidget listWidget,
                                   Minecraft client, Component label) {
                super(width, listWidget);
                options.add(new MultiLineTextWidget(width / 2 - 120, 0, label, client.font)
                        .setMaxWidth(240));
            }

            public MultiLineHeader(int width, ConfigListWidget listWidget, Minecraft client,
                                   Component label, int labelColor) {
                super(width, listWidget);
                options.add(new MultiLineTextWidget(width / 2 - 120, 0, label, client.font)
                        .setMaxWidth(240)
                        .setColor(labelColor));
            }

            public MultiLineHeader(int width, ConfigListWidget listWidget, Minecraft client,
                                   Component label, Component tooltip) {
                super(width, listWidget);
                MultiLineTextWidget header = new MultiLineTextWidget(
                        width / 2 - 120, 0, label, client.font)
                        .setMaxWidth(240);
                header.setTooltip(Tooltip.create(tooltip));
                options.add(header);
            }

            public MultiLineHeader(int width, ConfigListWidget listWidget, Minecraft client,
                                   Component label, int labelColor, Component tooltip) {
                super(width, listWidget);
                MultiLineTextWidget header = new MultiLineTextWidget(
                        width / 2 - 120, 0, label, client.font)
                        .setMaxWidth(240);
                header.setColor(labelColor);
                header.setTooltip(Tooltip.create(tooltip));
                options.add(header);
            }
        }
    }
}
