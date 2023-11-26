package notryken.chatnotify.gui.component.listwidget;

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
import notryken.chatnotify.gui.screen.NotifConfigScreen;
import org.jetbrains.annotations.NotNull;

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
     * Sets the client screen to a new instance of {@code NotifConfigScreen}, 
     * with the specified {@code ConfigListWidget} and the stored parent screen.
     * @param listWidget the {@code ConfigListWidget} to provide to the new
     *                   screen.
     */
    public void reloadScreen(ConfigListWidget listWidget) {
        client.setScreen(new NotifConfigScreen(parentScreen, listWidget.title, listWidget));
    }
    
    // Override implementations

    @Override
    public int getRowWidth() {
        return 300; // Sets the position of the scrollbar
    }

    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 32; // Offset as a buffer
    }

    // Abstract methods

    protected abstract void reloadScreen();

    public abstract ConfigListWidget resize(int width, int height, int top, int bottom);

    /**
     * Base implementation of ChatNotify options list widget entry, with common
     * entries.
     */
    public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        public final List<AbstractWidget> options;

        public Entry() {
            this.options = new ArrayList<>();
        }

        // Override implementations
        
        @Override
        public void render(@NotNull GuiGraphics context, int index, int y, int x,
                           int entryWidth, int entryHeight, int mouseX, int mouseY,
                           boolean hovered, float tickDelta) {
            options.forEach((button) -> {
                button.setY(y);
                button.render(context, mouseX, mouseY, tickDelta);
            });
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return options;
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return options;
        }

        // Common Entry implementations

        /**
         * A {@code Header} is a {@code StringWidget} with position and
         * dimensions set to standard ChatNotify values.
         */
        public static class Header extends Entry {
            public Header(int width, Minecraft client, Component label) {
                super();
                options.add(new StringWidget(width / 2 - 120, 0, 240, 20,
                        label, client.font));
            }

            public Header(int width, Minecraft client, Component label, int labelColor) {
                super();
                options.add(new StringWidget(width / 2 - 120, 0, 240, 20,
                        label, client.font).setColor(labelColor));
            }

            public Header(int width, Minecraft client, Component label, Component tooltip) {
                super();
                StringWidget header = new StringWidget(width / 2 - 120, 0, 240, 20,
                        label, client.font);
                header.setTooltip(Tooltip.create(tooltip));
                options.add(header);
            }

            public Header(int width, Minecraft client, Component label, int labelColor,
                          Component tooltip) {
                super();
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
            public MultiLineHeader(int width, Minecraft client, Component label) {
                super();
                options.add(new MultiLineTextWidget(width / 2 - 120, 0, label, client.font)
                        .setMaxWidth(240));
            }

            public MultiLineHeader(int width, Minecraft client, Component label,
                                   int labelColor) {
                super();
                options.add(new MultiLineTextWidget(width / 2 - 120, 0, label, client.font)
                        .setMaxWidth(240)
                        .setColor(labelColor));
            }

            public MultiLineHeader(int width, Minecraft client, Component label,
                                   Component tooltip) {
                super();
                MultiLineTextWidget header = new MultiLineTextWidget(
                        width / 2 - 120, 0, label, client.font)
                        .setMaxWidth(240);
                header.setTooltip(Tooltip.create(tooltip));
                options.add(header);
            }

            public MultiLineHeader(int width, Minecraft client, Component label,
                                   int labelColor, Component tooltip) {
                super();
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
