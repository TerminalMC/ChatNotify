/*
 * Copyright 2026 TerminalMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.terminalmc.chatnotify.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A {@link ContainerObjectSelectionList} which dynamically adjusts its height between zero and a
 * specified maximum based on the number of entries contained.
 * <p>
 * Each {@link Entry} has a single {@link AbstractWidget}, rather than the normal list of widgets.
 * <p>
 * Supports highlighting of a single {@link Entry}, specified by
 * {@link ExpandingList#highlightIndex}.
 */
public class ExpandingList extends ContainerObjectSelectionList<ExpandingList.Entry> {

    public static final int HIGHLIGHT_COLOR = 0x96787878;
    public static final int SCROLLBAR_WIDTH = 6;
    public static final int VERTICAL_BUFFER = 6;

    protected final int maxHeight;
    protected final int entryWidth;
    protected final int entryHeight;
    protected final int entryX;

    public int highlightIndex = -1;

    /**
     * @param x           the x position of the list widget.
     * @param y           the y position of the list widget.
     * @param width       the full width of the list widget.
     * @param maxHeight   the maximum allowable height of the list widget.
     * @param defaultEntryHeight  the space to allocate for each list entry.
     * @param entryHeight the actual height of each list entry.
     * @param xMargin     the space between the side of each entry and the edge of the list widget.
     */
    public ExpandingList(
            int x,
            int y,
            int width,
            int maxHeight,
            int defaultEntryHeight,
            int entryHeight,
            int xMargin
    ) {
        super(Minecraft.getInstance(), width, 0, y, defaultEntryHeight);
        super.setX(x);
        this.maxHeight = maxHeight;
        this.entryWidth = width - SCROLLBAR_WIDTH - (xMargin * 2);
        this.entryHeight = entryHeight;
        this.entryX = x + xMargin;
    }

    protected Entry getEntry(int index) {
        return this.children().get(index);
    }

    /**
     * Scrolls the list as required to make the {@link Entry} at {@code index} visible.
     */
    public void ensureVisible(int index) {
        scrollToEntry(getEntry(index));
    }

    public boolean isEmpty() {
        return children().isEmpty();
    }

    public int size() {
        return children().size();
    }

    /**
     * @return the {@link AbstractWidget} of the {@link Entry} at {@code index}.
     */
    public AbstractWidget get(int index) {
        return getEntry(index).getWidget();
    }

    /**
     * Clears the list and adds a new entry for each element of {@code widgets}, adjusting the list
     * height accordingly.
     */
    @SuppressWarnings("unused")
    public void replaceWidgets(Iterable<AbstractWidget> widgets) {
        clearWidgets();
        widgets.forEach(this::addWidget);
        setHeight(Math.min(defaultEntryHeight * children().size() + VERTICAL_BUFFER, maxHeight));
    }

    /**
     * Clears the list.
     */
    public void clearWidgets() {
        clearEntries();
        setHeight(0);
    }

    /**
     * Adds a new {@link Entry} for {@code widget}, adjusting the list height accordingly.
     */
    public void addWidget(AbstractWidget widget) {
        addEntry(new Entry(entryX, entryWidth, entryHeight, widget));
        setHeight(Math.min(defaultEntryHeight * children().size() + VERTICAL_BUFFER, maxHeight));
    }

    @Override
    protected void extractItem(
            @NotNull GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta,
            ExpandingList.Entry widget
    ) {
        if (highlightIndex != -1 && widget == getEntry(highlightIndex)) {
            graphics.fill(
                    widget.getX(),
                    widget.getY(),
                    widget.getX() + widget.getWidth(),
                    widget.getY() + widget.getHeight(),
                    HIGHLIGHT_COLOR
            );
        }
        super.extractItem(graphics, mouseX, mouseY, delta, widget);
    }

    @Override
    public int getRowWidth() {
        return width; // Clickable width, override hardcoded 220
    }

    @Override
    protected int scrollBarX() {
        return getX() + width - SCROLLBAR_WIDTH;
    }

    public static class Entry extends ContainerObjectSelectionList.Entry<Entry> {

        private final AbstractWidget widget;

        /**
         * Automatically repositions and resizes {@code widget} using the provided values.
         */
        public Entry(int x, int width, int height, AbstractWidget widget) {
            widget.setX(x);
            widget.setY(0);
            widget.setWidth(width);
            widget.setHeight(height);
            this.widget = widget;
        }

        public AbstractWidget getWidget() {
            return widget;
        }

        @Override
        public @NotNull List<? extends GuiEventListener> children() {
            return List.of(widget);
        }

        @Override
        public @NotNull List<? extends NarratableEntry> narratables() {
            return List.of(widget);
        }

        @Override
        public void extractContent(
                @NotNull GuiGraphicsExtractor graphics,
                int mouseX,
                int mouseY,
                boolean hovered,
                float delta
        ) {
            widget.setY(getContentY());
            widget.extractRenderState(graphics, mouseX, mouseY, delta);
        }
    }
}
