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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A horizontal semi-equivalent of
 * {@link net.minecraft.client.gui.components.AbstractSelectionList}.
 * <p>
 * Note: Minimal methods available, more to be added as required.
 */
public class HorizontalList<E extends AbstractWidget> extends AbstractContainerEventHandler
        implements Renderable, NarratableEntry {

    private static final int SCROLLBAR_WIDTH = 32;
    private static final int SCROLLBAR_HEIGHT = 6;

    private static final int MIN_WIDTH = SCROLLBAR_WIDTH * 2;
    private static final int MIN_HEIGHT = 20 + SCROLLBAR_HEIGHT;


    private final List<E> entries = new ArrayList<>();

    private final Minecraft mc;
    private final int space;

    public Snap snap = Snap.BOTTOM;

    public enum Snap {
        TOP,
        MIDDLE,
        BOTTOM,
    }

    public boolean topScrollbar;

    private boolean scrolling;
    private double scrollAmount;

    private @Nullable E hovered;
    private @Nullable E selected;

    protected int x0;
    protected int x1;
    protected int y0;
    protected int y1;
    protected int width;
    protected int height;

    public HorizontalList(int x, int y, int width, int height, int spacing, boolean topScrollbar) {
        super();
        this.space = spacing;
        this.mc = Minecraft.getInstance();
        this.topScrollbar = topScrollbar;
        this.x0 = x;
        this.x1 = x + width;
        this.y0 = y;
        this.y1 = y + height;
        this.width = width;
        this.height = height;
    }

    // Hovered entry

    /**
     * @return the hovered entry, if any.
     */
    public @Nullable E getHovered() {
        return hovered;
    }

    /**
     * Updates the hovered entry.
     */
    private void updateHovered(double mouseX, double mouseY) {
        if (isMouseOver(mouseX, mouseY)) {
            for (E e : entries) {
                if (e.isMouseOver(mouseX, mouseY)) {
                    hovered = e;
                    return;
                }
            }
        }
    }

    // Selected entry

    /**
     * @return the selected entry, if any.
     */
    public @Nullable E getSelected() {
        return selected;
    }

    /**
     * @param selected the entry to select.
     * @throws IllegalArgumentException if the entry is not in the list.
     */
    public void setSelected(@Nullable E selected) {
        if (!entries.contains(selected))
            throw new IllegalArgumentException("Specified entry is not present in the list.");
        this.selected = selected;
    }

    // Entry list management

    /**
     * @return the number of entries in the list.
     */
    public int numEntries() {
        return entries.size();
    }

    /**
     * @return an unmodifiable view of the entry list.
     */
    public @NotNull List<E> entries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * @return the entry at the specified index.
     * @throws IndexOutOfBoundsException if the index is out of range.
     */
    public E getEntry(int index) {
        return entries.get(index);
    }

    /**
     * Adds the specified entry to the end of the list.
     *
     * @param entry the entry to add.
     * @return the list index of the added entry.
     */
    public int addEntry(E entry) {
        entries.add(entry);
        return entries.size() - 1;
    }

    /**
     * @param entry the entry to remove.
     * @return {@code true} if the list contained the entry.
     */
    public boolean removeEntry(E entry) {
        boolean removed = entries.remove(entry);
        if (removed && entry == getSelected()) {
            setSelected(null);
        }
        return removed;
    }

    /**
     * Clears the entry list.
     */
    public void clearEntries() {
        entries.clear();
        setSelected(null);
    }

    // Bounds

    public int getX() {
        return x0;
    }

    public int getY() {
        return y0;
    }

    public int getRight() {
        return x1;
    }

    public int getBottom() {
        return y1;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        setSize(width, getHeight());
    }

    public void setHeight(int height) {
        setSize(getWidth(), height);
    }

    public void setSize(int width, int height) {
        this.width = Math.max(width, MIN_WIDTH);
        this.height = Math.max(height, MIN_HEIGHT);
        this.x1 = x0 + width;
        this.y1 = y0 + height;
        clampScrollAmount();
    }

    public void setBounds(int width, int height, int x, int y) {
        setSize(width, height);
        setPosition(x, y);
    }

    public void setPosition(int x, int y) {
        this.x0 = x;
        this.x1 = x + width;
        this.y0 = y;
        this.y1 = y + height;
    }

    // Rendering

    @Override
    public void render(
            @NotNull GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderListBackground(graphics);
        renderChildren(graphics, mouseX, mouseY, partialTick);
        renderScrollbar(graphics);
        renderSeparators(graphics);
        updateHovered(mouseX, mouseY);
    }

    /**
     * Renders the partially-translucent background texture.
     */
    protected void renderListBackground(GuiGraphics graphics) {
        graphics.setColor(0.125F, 0.125F, 0.125F, 1.0F);
        graphics.blit(
                Screen.BACKGROUND_LOCATION,
                x0,
                y0,
                (float) x1,
                (float) (y1 + (int) scrollAmount),
                x1 - x0,
                y1 - y0,
                32,
                32
        );
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /**
     * Repositions all entries according to {@link HorizontalList#snap},
     * {@link HorizontalList#topScrollbar} and {@link HorizontalList#scrollAmount}, and renders
     * those that are visible.
     */
    protected void renderChildren(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.enableScissor(getX(), getY(), getRight(), getBottom());
        int x = getX() - (int) scrollAmount;
        int topOffset = topScrollbar ? SCROLLBAR_HEIGHT : 0;
        int bottomOffset = topScrollbar ? 0 : SCROLLBAR_HEIGHT;

        for (E child : entries) {
            // Reposition
            int y = switch (snap) {
                case TOP -> getY() + topOffset;
                case MIDDLE -> getY() + (getHeight() - child.getHeight()) / 2;
                case BOTTOM -> getY() + getHeight() - child.getHeight() - bottomOffset;
            };
            child.setPosition(x, y);
            // Render
            if (child.getX() + child.getWidth() > getX() && child.getX() < getRight()) {
                child.render(graphics, mouseX, mouseY, partialTick);
            }
            // Move to next position
            x += child.getWidth() + space;
        }
        graphics.disableScissor();
    }

    /**
     * Renders the scrollbar, if required.
     */
    protected void renderScrollbar(GuiGraphics graphics) {
        if (scrollbarVisible()) {
            int y = getScrollbarPosition();

            int scrollerWidth =
                    (int) ((float) (getWidth() * getWidth()) / (float) getMaxPosition());
            scrollerWidth = Mth.clamp(scrollerWidth, SCROLLBAR_WIDTH, getWidth());

            int scrollerPos = Math.max(
                    getX(),
                    (int) scrollAmount * (getWidth() - scrollerWidth) / getMaxScroll() + getX()
            );

            // Background
            graphics.fill(getX(), y, getRight(), y + 6, -16777216);
            // Scroll base layer
            graphics.fill(scrollerPos, y, scrollerPos + scrollerWidth, y + 6, -8355712);
            // Scroll top layer
            graphics.fill(scrollerPos, y, scrollerPos + scrollerWidth - 1, y + 6 - 1, -4144960);
        }
    }

    /**
     * @return {@code true} if the scroll bar should be rendered.
     */
    protected boolean scrollbarVisible() {
        return getMaxScroll() > 0;
    }

    /**
     * Renders the list separator textures.
     */
    protected void renderSeparators(GuiGraphics guiGraphics) {
        guiGraphics.setColor(0.25F, 0.25F, 0.25F, 1.0F);
        guiGraphics.blit(Screen.BACKGROUND_LOCATION, x0 - 2, 0, 0.0F, 0.0F, width, y0 - 2, 32, 32);
        guiGraphics.blit(
                Screen.BACKGROUND_LOCATION,
                x0 - 2,
                y1 + 2,
                0.0F,
                (float) y1,
                width,
                height - y1,
                32,
                32
        );
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.fillGradient(
                RenderType.guiOverlay(),
                x0 - 2,
                y0 - 2,
                x1 + 2,
                y0 + 2,
                -16777216,
                0,
                0
        );
        guiGraphics.fillGradient(
                RenderType.guiOverlay(),
                x0 - 2,
                y1 - 2,
                x1 + 2,
                y1 + 2,
                0,
                -16777216,
                0
        );
    }

    // Focus and visibility

    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        super.setFocused(focused);
        @SuppressWarnings("unchecked") int i = entries.indexOf((E) focused);
        if (i >= 0) {
            E entry = entries.get(i);
            setSelected(entry);
            if (mc.getLastInputType().isKeyboard()) {
                ensureVisible(entry);
            }
        }

    }

    /**
     * Scrolls if required to fully display the entry.
     */
    protected void ensureVisible(E entry) {
        int leftHang = entry.getX() - getX();
        int rightHang = entry.getX() + entry.getWidth() - getRight();
        if (leftHang < 0) {
            // Entry is at least partially off the left end
            scroll(leftHang);
        } else if (rightHang > 0) {
            // Entry is at least partially off the right end
            scroll(rightHang);
        }
    }

    // Scrolling

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= (double) getX() && mouseY >= (double) getY()
                && mouseX < (double) (getRight()) && mouseY < (double) (getBottom());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        updateScrollingState(mouseX, mouseY, button);
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        } else {
            return scrolling || super.mouseClicked(mouseX, mouseY, button);
        }

    }

    @Override
    public boolean mouseDragged(
            double mouseX,
            double mouseY,
            int button,
            double dragX,
            double dragY
    ) {
        if (button == 0 && scrolling) {
            if (mouseX < getX()) {
                setScrollAmount(0.0F);
            } else if (mouseX > getRight()) {
                setScrollAmount(getMaxScroll());
            } else {
                double maxScroll = Math.max(1, getMaxScroll());
                int innerWidth = getWidth();
                int scrollerWidth = Mth.clamp(
                        (int) ((float) (innerWidth * innerWidth) / (float) getMaxPosition()),
                        SCROLLBAR_WIDTH,
                        innerWidth
                );
                double multiplier =
                        Math.max(1.0F, maxScroll / (double) (innerWidth - scrollerWidth));
                setScrollAmount(scrollAmount + dragX * multiplier);
            }
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int scrollMultiplier = 10;
        setScrollAmount(scrollAmount - delta * scrollMultiplier);
        return true;
    }

    protected void updateScrollingState(double mouseX, double mouseY, int button) {
        scrolling =
                button == 0 && mouseY >= getScrollbarPosition() && mouseY < (getScrollbarPosition()
                        + SCROLLBAR_HEIGHT) && mouseX >= getX() && mouseX < getRight();
    }

    protected int getScrollbarPosition() {
        return topScrollbar ? getY() : getY() + getHeight() - SCROLLBAR_HEIGHT;
    }

    protected int getMaxPosition() {
        int pos = 0;
        for (AbstractWidget e : entries) {
            pos += e.getWidth();
            pos += space;
        }
        return pos - space;
    }

    private void scroll(int scroll) {
        setScrollAmount(scrollAmount + (double) scroll);
    }

    public void setScrollAmount(double scroll) {
        setClampedScrollAmount(scroll);
    }

    public void clampScrollAmount() {
        setClampedScrollAmount(scrollAmount);
    }

    public void setClampedScrollAmount(double scroll) {
        scrollAmount = Mth.clamp(scroll, 0.0F, getMaxScroll());
    }

    public int getMaxScroll() {
        return Math.max(0, getMaxPosition() - getWidth());
    }

    // Narration

    public NarratableEntry.@NotNull NarrationPriority narrationPriority() {
        if (isFocused()) {
            return NarrationPriority.FOCUSED;
        } else {
            return hovered != null ? NarrationPriority.HOVERED : NarrationPriority.NONE;
        }
    }

    @SuppressWarnings("unchecked")
    public void updateNarration(@NotNull NarrationElementOutput output) {
        E hovered = getHovered();
        if (hovered != null) {
            hovered.updateNarration(output.nest());
            narrateListElementPosition(output, hovered);
        } else {
            E focused = (E) (getFocused());
            if (focused != null) {
                focused.updateNarration(output.nest());
                narrateListElementPosition(output, focused);
            }
        }

        output.add(
                NarratedElementType.USAGE,
                Component.translatable("narration.component_list.usage")
        );
    }

    protected void narrateListElementPosition(NarrationElementOutput output, E entry) {
        if (entries.size() > 1) {
            int index = entries.indexOf(entry);
            if (index != -1) {
                output.add(
                        NarratedElementType.POSITION,
                        Component.translatable("narrator.position.list", index + 1, entries.size())
                );
            }
        }
    }

    // Extra

    /**
     * Alias for {@link HorizontalList#entries} required by
     * {@link net.minecraft.client.gui.components.events.ContainerEventHandler}.
     */
    public final @NotNull List<E> children() {
        return Collections.unmodifiableList(entries);
    }
}
