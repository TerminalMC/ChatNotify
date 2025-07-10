/*
 * Copyright 2025 TerminalMC
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

import com.mojang.blaze3d.systems.RenderSystem;
import dev.terminalmc.chatnotify.ChatNotify;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
public class HorizontalList<E extends AbstractWidget> extends AbstractContainerWidget {

    private static final ResourceLocation LEFT_SEPARATOR =
            ResourceLocation.fromNamespaceAndPath(
                    ChatNotify.MOD_ID,
                    "textures/gui/left_separator.png"
            );
    private static final ResourceLocation RIGHT_SEPARATOR =
            ResourceLocation.fromNamespaceAndPath(
                    ChatNotify.MOD_ID,
                    "textures/gui/right_separator.png"
            );
    private static final ResourceLocation MENU_LIST_BACKGROUND =
            ResourceLocation.withDefaultNamespace(
                    "textures/gui/menu_list_background.png"
            );
    private static final ResourceLocation SCROLLER_SPRITE =
            ResourceLocation.fromNamespaceAndPath(
                    ChatNotify.MOD_ID,
                    "widget/scroller_horizontal"
            );
    private static final ResourceLocation SCROLLER_BACKGROUND_SPRITE =
            ResourceLocation.fromNamespaceAndPath(
                    ChatNotify.MOD_ID,
                    "widget/scroller_background_horizontal"
            );

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

    public HorizontalList(int x, int y, int width, int height, int spacing, boolean topScrollbar) {
        super(x, y, width, height, Component.empty());
        this.space = spacing;
        this.mc = Minecraft.getInstance();
        this.topScrollbar = topScrollbar;
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

    @Override
    public void setWidth(int width) {
        setSize(width, getHeight());
    }

    @Override
    public void setHeight(int height) {
        setSize(getWidth(), height);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(Math.max(width, MIN_WIDTH), Math.max(height, MIN_HEIGHT));
        refreshScrollAmount();
    }

    public void setBounds(int width, int height, int x, int y) {
        setSize(width, height);
        setPosition(x, y);
    }

    // Rendering

    @Override
    protected void renderWidget(
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
        RenderSystem.enableBlend();
        graphics.blit(
                RenderType::guiTextured,
                MENU_LIST_BACKGROUND,
                getX(),
                getY(),
                0,
                0,
                getWidth(),
                getHeight(),
                32,
                32
        );
        RenderSystem.disableBlend();
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
            if (child.getRight() > getX() && child.getX() < getRight()) {
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
    protected void renderScrollbar(@NotNull GuiGraphics graphics) {
        if (scrollbarVisible()) {
            int y = scrollBarX();

            int scrollerWidth =
                    (int) ((float) (getWidth() * getWidth()) / (float) getMaxPosition());
            scrollerWidth = Mth.clamp(scrollerWidth, SCROLLBAR_WIDTH, getWidth());

            int scrollerPos = Math.max(
                    getX(),
                    (int) scrollAmount * (getWidth() - scrollerWidth) / getMaxScroll() + getX()
            );

            RenderSystem.enableBlend();
            graphics.blitSprite(
                    RenderType::guiTextured,
                    SCROLLER_BACKGROUND_SPRITE,
                    getX(),
                    y,
                    getWidth(),
                    SCROLLBAR_HEIGHT
            );
            graphics.blitSprite(
                    RenderType::guiTextured,
                    SCROLLER_SPRITE,
                    scrollerPos,
                    y,
                    scrollerWidth,
                    SCROLLBAR_HEIGHT
            );
            RenderSystem.disableBlend();
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
        RenderSystem.enableBlend();
        guiGraphics.blit(
                RenderType::guiTextured,
                LEFT_SEPARATOR,
                getX() - 2,
                getY() - 1,
                0.0F,
                0.0F,
                2,
                getHeight() + 2,
                2,
                32
        );
        guiGraphics.blit(
                RenderType::guiTextured,
                RIGHT_SEPARATOR,
                getRight(),
                getY() - 1,
                0.0F,
                0.0F,
                2,
                getHeight() + 2,
                2,
                32
        );
        guiGraphics.blit(
                RenderType::guiTextured,
                Screen.HEADER_SEPARATOR,
                getX() - 1,
                getY() - 2,
                0.0F,
                0.0F,
                getWidth() + 2,
                2,
                32,
                2
        );
        guiGraphics.blit(
                RenderType::guiTextured,
                Screen.FOOTER_SEPARATOR,
                getX() - 1,
                getBottom(),
                0.0F,
                0.0F,
                getWidth() + 2,
                2,
                32,
                2
        );
        RenderSystem.disableBlend();
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
        int rightHang = entry.getRight() - getRight();
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int scrollMultiplier = 10;
        setScrollAmount(scrollAmount - scrollY * scrollMultiplier);
        return true;
    }

    protected void updateScrollingState(double mouseX, double mouseY, int button) {
        scrolling =
                button == 0 && mouseY >= scrollBarX() && mouseY < (scrollBarX()
                        + SCROLLBAR_HEIGHT) && mouseX >= getX() && mouseX < getRight();
    }

    protected int scrollBarX() {
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

    public void refreshScrollAmount() {
        setClampedScrollAmount(scrollAmount);
    }

    public void setClampedScrollAmount(double scroll) {
        scrollAmount = Mth.clamp(scroll, 0.0F, getMaxScroll());
    }

    public int getMaxScroll() {
        return Math.max(0, getMaxPosition() - getWidth());
    }

    @Override
    protected int contentHeight() {
        // Not currently used
        return 0;
    }

    @Override
    protected double scrollRate() {
        // Not currently used
        return 0;
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
    public void updateWidgetNarration(@NotNull NarrationElementOutput output) {
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
