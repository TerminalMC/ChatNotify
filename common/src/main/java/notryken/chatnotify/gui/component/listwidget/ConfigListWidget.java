package notryken.chatnotify.gui.component.listwidget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import notryken.chatnotify.gui.screen.NotifConfigScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public final Screen parentScreen;

    public ConfigListWidget(Minecraft minecraft, int width, int height, int top, int bottom,
                            int itemHeight, Screen parentScreen) {
        super(minecraft, width, height, top, bottom, itemHeight);
        this.parentScreen = parentScreen;
    }

    // TODO make dynamic
    @Override
    public int getRowWidth() {
        return 300; // Sets the position of the scrollbar
    }

    // TODO make dynamic
    @Override
    protected int getScrollbarPosition() {
        return super.getScrollbarPosition() + 32; // Offset as a buffer
    }

    // Default methods

    /**
     * Sets the client screen to a new instance of {@code NotifConfigScreen}, 
     * with the specified {@code ConfigListWidget} and the stored parent screen.
     * @param listWidget the {@code ConfigListWidget} to provide to the new
     *                   screen.
     */
    public void reloadScreen(ConfigListWidget listWidget) {
        minecraft.setScreen(new NotifConfigScreen(parentScreen, listWidget.parentScreen.getTitle(), listWidget));
    }

    // Abstract methods

    protected abstract void reloadScreen();

    public abstract ConfigListWidget resize(int width, int height, int top, int bottom);

    /**
     * Base implementation of ChatNotify options list widget entry, with common
     * entries.
     */
    public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        public final List<AbstractWidget> elements;

        public Entry() {
            this.elements = new ArrayList<>();
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return elements;
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return elements;
        }

        @Override
        public void render(@NotNull GuiGraphics context, int index, int y, int x,
                           int entryWidth, int entryHeight, int mouseX, int mouseY,
                           boolean hovered, float tickDelta) {
            elements.forEach((button) -> {
                button.setY(y);
                button.render(context, mouseX, mouseY, tickDelta);
            });
        }

        // Common Entry implementations

        public static class TextEntry extends Entry {
            public TextEntry(int x, int width, int height, Component message,
                             @Nullable Tooltip tooltip, int tooltipDelay) {
                super();

                AbstractStringWidget widget;
                if (Minecraft.getInstance().font.width(message.getString()) <= width) {
                    widget = new StringWidget(x, 0, width, height, message, Minecraft.getInstance().font);
                }
                else {
                    widget = new MultiLineTextWidget(x, 0, message, Minecraft.getInstance().font)
                            .setMaxWidth(width)
                            .setCentered(true);
                }
                if (tooltip != null) widget.setTooltip(tooltip);
                if (tooltipDelay >= 0) widget.setTooltipDelay(tooltipDelay);

                elements.add(widget);
            }
        }

        public static class ActionButtonEntry extends Entry {
            public ActionButtonEntry(int x, int y, int width, int height, Component message,
                                     @Nullable Tooltip tooltip, int tooltipDelay, Button.OnPress onPress) {
                super();

                Button button = Button.builder(message, onPress)
                        .pos(x, y)
                        .size(width, height)
                        .build();
                if (tooltip != null) button.setTooltip(tooltip);
                if (tooltipDelay >= 0) button.setTooltipDelay(tooltipDelay);

                elements.add(button);
            }
        }
    }
}
