/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package com.notryken.chatnotify.gui.screen;

import com.notryken.chatnotify.gui.widget.list.OptionsList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * An OptionsScreen contains one tightly-coupled {@link OptionsList},
 * which is used to display all option controls required for the screen.
 */
public class OptionsScreen extends OptionsSubScreen {

    protected OptionsList listWidget;

    public final int listTop = 32;
    public final int bottomMargin = 32;
    public final int listItemHeight = 25;

    public OptionsScreen(Screen lastScreen, Component title, OptionsList listWidget) {
        super(lastScreen, Minecraft.getInstance().options, title);
        this.listWidget = listWidget;
    }

    @Override
    protected void init() {
        reloadListWidget();
    }

    @Override
    public void onClose() {
        listWidget.onClose();
        if (super.lastScreen instanceof OptionsScreen screen) screen.reloadListWidget();
        super.onClose();
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(font, title, width / 2, 5, 0xffffff);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        this.renderDirtBackground(context);
    }

    public void reloadListWidget() {
        clearWidgets();
        listWidget = listWidget.resize(width, height - listTop - bottomMargin,
                listTop, listItemHeight, listWidget.getScrollAmount());
        listWidget.setScreen(this);
        addRenderableWidget(listWidget);
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE,
                        (button) -> onClose())
                .pos(width / 2 - 120, height - 27)
                .size(240, 20)
                .build());
    }
}
