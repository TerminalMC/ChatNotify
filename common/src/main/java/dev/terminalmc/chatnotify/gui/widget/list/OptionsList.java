/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.gui.widget.list;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import dev.terminalmc.chatnotify.gui.widget.slider.DoubleSlider;
import dev.terminalmc.chatnotify.gui.widget.button.SilentButton;
import dev.terminalmc.chatnotify.gui.screen.OptionsScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An OptionsList is tightly coupled to a generic {@link OptionsScreen},
 * allowing many unique options screens to use a single Screen implementation,
 * with different OptionsLists.
 *
 * <p>An OptionsList has a list of {@link Entry} objects, which are drawn
 * onto the screen top-down in the order that they are stored, with standard
 * spacing specified by the parent {@link OptionsScreen}.
 *
 * <p><b>Note:</b> If you want multiple components (e.g. buttons, text fields)
 * to appear side-by-side, you must add them all to a single {@link Entry}'s
 * list of widgets.
 */
public abstract class OptionsList extends ContainerObjectSelectionList<OptionsList.Entry> {
    protected OptionsScreen screen;

    // Standard positional and dimensional values used by entries
    protected final int entryRelX;
    protected final int entryX;
    protected final int entryWidth;
    protected final int entryHeight;
    protected final int scrollWidth;

    public OptionsList(Minecraft mc, int width, int height, int y,
                       int itemHeight, int entryRelX, int entryWidth, int entryHeight,
                       int scrollWidth) {
        super(mc, width, height, y, itemHeight);
        this.entryRelX = entryRelX;
        this.entryX = width / 2 + entryRelX;
        this.entryWidth = entryWidth;
        this.entryHeight = entryHeight;
        this.scrollWidth = scrollWidth;
    }

    @Override
    public int getRowWidth() {
        // Set the clickable width
        return scrollWidth;
    }

    @Override
    protected int getScrollbarPosition() {
        // Set the scrollbar position
        return width / 2 + scrollWidth / 2;
    }

    /**
     * Must be called when an OptionsList is added to an
     * {@link OptionsScreen}, else breaks.
     */
    public void setScreen(OptionsScreen screen) {
        this.screen = screen;
    }

    /**
     *
     */
    public void reload() {
        screen.reloadListWidget();
    }

    // Abstract methods
    public abstract OptionsList resize(int width, int height, int y,
                                       int itemHeight, double scrollAmount);

    public void onClose() {}

    /**
     * Base implementation of options list widget entry, with common entries.
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
                if (tooltipDelay >= 0) widget.setTooltipDelay(Duration.ofMillis(tooltipDelay));

                elements.add(widget);
            }
        }

        public static class ActionButtonEntry extends Entry {
            public ActionButtonEntry(int x, int y, int width, int height,
                                     Component message, @Nullable Tooltip tooltip,
                                     int tooltipDelay, Button.OnPress onPress) {
                super();

                Button button = Button.builder(message, onPress)
                        .pos(x, y)
                        .size(width, height)
                        .build();
                if (tooltip != null) button.setTooltip(tooltip);
                if (tooltipDelay >= 0) button.setTooltipDelay(Duration.ofMillis(tooltipDelay));

                elements.add(button);
            }
        }

        public static class SilentActionButtonEntry extends Entry {
            public SilentActionButtonEntry(int x, int y, int width, int height,
                                           Component message, @Nullable Tooltip tooltip,
                                           int tooltipDelay, Button.OnPress onPress) {
                super();

                Button silentButton = new SilentButton(x, y, width, height, message, onPress);
                if (tooltip != null) silentButton.setTooltip(tooltip);
                if (tooltipDelay >= 0) silentButton.setTooltipDelay(Duration.ofMillis(tooltipDelay));

                elements.add(silentButton);
            }
        }

        public static class DoubleSliderEntry extends Entry {
            public DoubleSliderEntry(int x, int y, int width, int height, double min, double max, int precision,
                                     @Nullable String messagePrefix, @Nullable String messageSuffix,
                                     @Nullable String valueNameMin, @Nullable String valueNameMax,
                                     Supplier<Double> source, Consumer<Double> dest) {
                super();
                elements.add(new DoubleSlider(x, y, width, height, min, max, precision, 
                        messagePrefix, messageSuffix, valueNameMin, valueNameMax, source, dest));
            }
        }
    }
}
