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

package dev.terminalmc.chatnotify.gui.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import dev.terminalmc.chatnotify.gui.widget.OverlayWidget;
import dev.terminalmc.chatnotify.gui.widget.list.option.OptionList;
import dev.terminalmc.chatnotify.mixin.accessor.ScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Contains one tightly-coupled {@link OptionList}, which is used to display
 * all option control widgets.
 * 
 * <p>Supports displaying a single {@link OverlayWidget}, which requires hiding
 * all other widgets to avoid rendering and click conflicts but is still simpler
 * than screen switching.</p>
 */
public class OptionsScreen extends OptionsSubScreen {
    public static final int TOP_MARGIN = 32;
    public static final int BOTTOM_MARGIN = 32;
    public static final int LIST_ENTRY_SPACE = 25;
    public static final int LIST_ENTRY_HEIGHT = 20;
    public static final int BASE_ROW_WIDTH = Window.BASE_WIDTH;
    public static final int LIST_ENTRY_SIDE_MARGIN = 24;
    public static final int BASE_LIST_ENTRY_WIDTH = BASE_ROW_WIDTH - (LIST_ENTRY_SIDE_MARGIN * 2);

    protected OptionList listWidget;
    private OverlayWidget overlayWidget = null;

    /**
     * The {@link OptionList} passed here is not required to have the correct
     * dimensions (width and height), as it will be automatically reloaded (and
     * resized) prior to being displayed.
     */
    public OptionsScreen(Screen lastScreen, Component title, OptionList listWidget) {
        super(lastScreen, Minecraft.getInstance().options, title);
        this.listWidget = listWidget;
    }
    
    // Overrides

    @Override
    protected void init() {
        reload();
    }

    @Override
    protected void addOptions() {
        // Not currently used
    }

    @Override
    public void resize(@NotNull Minecraft mc, int width, int height) {
        super.resize(mc, width, height);
        init();
    }

    @Override
    public void onClose() {
        if (lastScreen instanceof OptionsScreen screen) {
            // Resize the parent screen's OptionList
            screen.reload(width, height);
        }
        listWidget.onClose();
        super.onClose();
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
    }
    
    // Extensions

    /**
     * Clears all widgets, reloads the {@link OptionList}, recreates all fixed
     * widgets, and resizes the overlay widget (if any).
     */
    public void reload() {
        reload(width, height);
    }

    private void reload(int width, int height) {
        clearWidgets();
        listWidget = listWidget.reload(this, width, height - TOP_MARGIN - BOTTOM_MARGIN,
                listWidget.scrollAmount());
        addRenderableWidget(listWidget);

        // Title text
        Font font = Minecraft.getInstance().font;
        addRenderableWidget(new StringWidget(width / 2 - (font.width(title) / 2),
                Math.max(0, TOP_MARGIN / 2 - font.lineHeight / 2),
                font.width(title), font.lineHeight, title, font).alignLeft());

        // Done button
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> onClose())
                .pos(width / 2 - BASE_LIST_ENTRY_WIDTH / 2, Math.min(height - LIST_ENTRY_HEIGHT,
                        height - BOTTOM_MARGIN / 2 - LIST_ENTRY_HEIGHT / 2))
                .size(BASE_LIST_ENTRY_WIDTH, LIST_ENTRY_HEIGHT)
                .build());

        if (overlayWidget != null) {
            // Proportional resizing
            overlayWidget.setWidth(overlayWidget.getNominalWidth(width));
            overlayWidget.setHeight(overlayWidget.getNominalHeight(height));
            // Recenter
            overlayWidget.setX(width / 2 - overlayWidget.getWidth() / 2);
            overlayWidget.setY(height / 2 - overlayWidget.getHeight() / 2);
            setOverlayWidget(overlayWidget);
        }
    }

    // Overlay widget handling
    
    public void setOverlayWidget(OverlayWidget widget) {
        removeOverlayWidget();
        overlayWidget = widget;
        setChildrenVisible(false);
        ((ScreenAccessor)this).getChildren().addFirst(widget);
        ((ScreenAccessor)this).getNarratables().addFirst(widget);
        ((ScreenAccessor)this).getRenderables().addLast(widget);
    }

    public void removeOverlayWidget() {
        if (overlayWidget != null) {
            removeWidget(overlayWidget);
            overlayWidget = null;
            setChildrenVisible(true);
        }
    }

    private void setChildrenVisible(boolean visible) {
        for (GuiEventListener listener : children()) {
            if (listener instanceof AbstractWidget widget) {
                widget.visible = visible;
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (overlayWidget != null) {
            if (keyCode == InputConstants.KEY_ESCAPE) {
                overlayWidget.onClose();
                removeOverlayWidget();
            } else {
                overlayWidget.keyPressed(keyCode, scanCode, modifiers);
            }
            return true;
        } else {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (overlayWidget != null) {
            overlayWidget.charTyped(chr, modifiers);
            return true;
        } else {
            return super.charTyped(chr, modifiers);
        }
    }
}
