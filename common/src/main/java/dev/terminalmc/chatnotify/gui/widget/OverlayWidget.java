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

import com.mojang.blaze3d.platform.Window;
import dev.terminalmc.chatnotify.gui.screen.OptionScreen;
import dev.terminalmc.chatnotify.mixin.accessor.AbstractWidgetAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * An {@link OverlayWidget} is designed for display on a {@link OptionScreen},
 * on top of any other GUI elements.
 */
public abstract class OverlayWidget extends AbstractWidget {
    private Consumer<OverlayWidget> close;
    public final boolean fixedSize;
    public final double nominalWidthRatio;
    public final double nominalHeightRatio;

    public OverlayWidget(int x, int y, int width, int height, boolean fixedSize,
                         Component msg, Consumer<OverlayWidget> close) {
        super(x, y, width, height, msg);
        checkWidth(width);
        checkHeight(height);
        this.close = close;
        Window window = Minecraft.getInstance().getWindow();
        this.fixedSize = fixedSize;
        this.nominalWidthRatio = width / (double)window.getGuiScaledWidth();
        this.nominalHeightRatio = height / (double)window.getGuiScaledHeight();
    }
    
    public void addOnClose(Consumer<OverlayWidget> close) {
        Consumer<OverlayWidget> close2 = this.close;
        this.close = (widget) -> {
            close.accept(widget);
            close2.accept(widget);
        };
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return true;
    }

    /**
     * Creates (or re-creates) all sub-widgets and adjusts their sizes and
     * positions based on the current positional and dimensional values of the
     * {@link OverlayWidget}.
     *
     * <p>To be called on initial build and whenever the position or dimensions
     * of the {@link OverlayWidget} are changed.</p>
     */
    protected abstract void init();

    public void onClose() {
        close.accept(this);
    }

    public abstract int getMinWidth();

    public abstract int getMaxWidth();

    public abstract int getMinHeight();

    public abstract int getMaxHeight();

    /*
     * Min dimensions are respected by ALL resizing operations, max dimensions
     * are only respected by nominal resizing.
     */

    public int getNominalWidth(int screenWidth) {
        if (fixedSize) return width;
        return Math.min(Math.max(getMinWidth(), (int)(screenWidth * nominalWidthRatio)),
                getMaxWidth());
    }

    public int getNominalHeight(int screenHeight) {
        if (fixedSize) return height;
        return Math.min(Math.max(getMinHeight(), (int)(screenHeight * nominalHeightRatio)),
                getMaxHeight());
    }

    // Re-init on reposition or resize to maintain sub-widget position and size

    public void updateBounds(int screenWidth, int screenHeight) {
        // Proportional resizing
        super.setWidth(getNominalWidth(screenWidth));
        ((AbstractWidgetAccessor)this).setHeight(getNominalHeight(screenHeight));
        // Recenter
        super.setX(screenWidth / 2 - getWidth() / 2);
        super.setY(screenHeight / 2 - getHeight() / 2);
        // Initialize
        init();
    }

    @Override
    public void setPosition(int x, int y) {
        super.setPosition(x, y);
        init();
    }

    public void setX(int x) {
        super.setX(x);
        init();
    }

    public void setY(int y) {
        super.setY(y);
        init();
    }

    /**
     * @throws IllegalArgumentException if {@code width} or {@code height} is
     * out of range.
     * @see OverlayWidget#checkWidth
     * @see OverlayWidget#checkHeight
     */
    public void setSize(int width, int height) {
        setWidth(width);
        ((AbstractWidgetAccessor)this).setHeight(height);
        init();
    }

    /**
     * @throws IllegalArgumentException if {@code width} is out of range.
     * @see OverlayWidget#checkWidth
     */
    @Override
    public void setWidth(int width) {
        super.setWidth(checkWidth(width));
        init();
    }

    /**
     * @return {@code width}, if it is valid.
     * @throws IllegalArgumentException if {@code width} is less than
     * {@link OverlayWidget#getMinWidth}
     */
    protected int checkWidth(int width) {
        if (width < getMinWidth()) throw new IllegalArgumentException(
                "Width cannot be less than " + getMinWidth() + ", got " + width);
        return width;
    }

    /**
     * @return {@code height}, if it is valid.
     * @throws IllegalArgumentException if {@code height} is less than
     * {@link OverlayWidget#getMinHeight}
     */
    protected int checkHeight(int height) {
        if (height < getMinHeight()) throw new IllegalArgumentException(
                "Height cannot be less than " + getMinHeight() + ", got " + height);
        return height;
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narration) {}
}
