/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.gui.widget.list;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.network.chat.Component;
import dev.terminalmc.chatnotify.gui.widget.slider.DoubleSlider;
import dev.terminalmc.chatnotify.gui.widget.SilentButton;
import dev.terminalmc.chatnotify.gui.screen.OptionsScreen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * An {@link OptionsList} is tightly coupled to a generic {@link OptionsScreen},
 * allowing many unique options screens to use a single screen implementation,
 * while displaying different options.
 *
 * <p>An {@link OptionsList} has a list of {@link Entry} objects, which are
 * drawn onto the screen top-down in the order that they are stored, with each
 * entry being allocated a standard amount of space specified by
 * {@link OptionsList#itemHeight}. The actual height of list entries, specified
 * by {@link OptionsList#entryHeight}, can be less but should not be more.</p>
 *
 * <p><b>Note:</b> If you want multiple widgets to appear side-by-side, you must
 * add them all to a single {@link Entry}'s list of widgets, which are all
 * rendered at the same list level.</p>
 */
public abstract class OptionsList extends ContainerObjectSelectionList<OptionsList.Entry> {
    protected OptionsScreen screen;

    // Standard positional and dimensional values used by entries
    protected final int rowWidth;
    protected final int entryWidth;
    protected final int entryHeight;
    protected final int entryRelX;
    protected final int entryX;

    public OptionsList(Minecraft mc, int width, int height, int y, int rowWidth,
                       int itemHeight, int entryWidth, int entryHeight) {
        super(mc, width, height, y, itemHeight);
        this.rowWidth = rowWidth;
        this.entryWidth = entryWidth;
        this.entryHeight = entryHeight;
        this.entryRelX = -entryWidth / 2;
        this.entryX = width / 2 + entryRelX;
    }

    @Override
    public int getRowWidth() {
        // Clickable width
        return rowWidth;
    }

    @Override
    protected int getScrollbarPosition() {
        return width / 2 + rowWidth / 2;
    }

    protected void reload() {
        screen.reload();
    }

    public OptionsList reload(OptionsScreen screen, int width, int height, double scrollAmount) {
        OptionsList newList = reload(width, height, scrollAmount);
        newList.screen = screen;
        return newList;
    }

    protected abstract OptionsList reload(int width, int height, double scrollAmount);

    public void onClose() {}

    /**
     * Base implementation of options list widget entry, with common entries.
     */
    public abstract static class Entry extends ContainerObjectSelectionList.Entry<Entry> {
        public static final int SPACING = 4;

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
                           boolean hovered, float delta) {
            elements.forEach((widget) -> {
                widget.setY(y);
                widget.render(context, mouseX, mouseY, delta);
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
            public ActionButtonEntry(int x, int width, int height, Component message,
                                     @Nullable Tooltip tooltip, int tooltipDelay,
                                     Button.OnPress onPress) {
                super();

                Button button = Button.builder(message, onPress)
                        .pos(x, 0)
                        .size(width, height)
                        .build();
                if (tooltip != null) button.setTooltip(tooltip);
                if (tooltipDelay >= 0) button.setTooltipDelay(Duration.ofMillis(tooltipDelay));

                elements.add(button);
            }
        }

        public static class SilentActionButtonEntry extends Entry {
            public SilentActionButtonEntry(int x, int width, int height, Component message,
                                           @Nullable Tooltip tooltip, int tooltipDelay,
                                           Button.OnPress onPress) {
                super();

                Button silentButton = new SilentButton(x, 0, width, height, message, onPress);
                if (tooltip != null) silentButton.setTooltip(tooltip);
                if (tooltipDelay >= 0) silentButton.setTooltipDelay(Duration.ofMillis(tooltipDelay));

                elements.add(silentButton);
            }
        }

        public static class DoubleSliderEntry extends Entry {
            public DoubleSliderEntry(int x, int width, int height, double min, double max, int precision,
                                     @Nullable String messagePrefix, @Nullable String messageSuffix,
                                     @Nullable String valueNameMin, @Nullable String valueNameMax,
                                     Supplier<Double> source, Consumer<Double> dest) {
                super();
                elements.add(new DoubleSlider(x, 0, width, height, min, max, precision,
                        messagePrefix, messageSuffix, valueNameMin, valueNameMax, source, dest));
            }
        }
    }

    /**
     * The {@link AbstractSelectionList} class (second-degree superclass of
     * {@link OptionsList}) is hard-coded to only support fixed spacing of
     * entries. This is an invisible entry which defers all actions to the
     * given {@link Entry}, thereby allowing that entry to span multiple slots
     * of the {@link OptionsList}.
     */
    public static class SpaceEntry extends Entry {
        private final Entry entry;

        public SpaceEntry(Entry entry) {
            super();
            this.entry = entry;
        }

        @Override
        public boolean isDragging() {
            return entry.isDragging();
        }

        @Override
        public void setDragging(boolean dragging) {
            entry.setDragging(dragging);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return entry.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button,
                                    double deltaX, double deltaY) {
            return entry.mouseDragged(mouseX, mouseY, button, deltaY, deltaX);
        }

        public void setFocused(GuiEventListener listener) {
            entry.setFocused(listener);
        }

        public GuiEventListener getFocused() {
            return entry.getFocused();
        }

        public ComponentPath focusPathAtIndex(@NotNull FocusNavigationEvent event, int i) {
            if (entry.children().isEmpty()) {
                return null;
            } else {
                ComponentPath $$2 = entry.children().get(
                        Math.min(i, entry.children().size() - 1)).nextFocusPath(event);
                return ComponentPath.path(entry, $$2);
            }
        }
    }
}
