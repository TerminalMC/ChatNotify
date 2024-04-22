/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package com.notryken.chatnotify.gui.screen;

import com.notryken.chatnotify.gui.component.listwidget.OptionsListWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * An OptionsScreen contains one tightly-coupled {@link OptionsListWidget},
 * which is used to display all option controls required for the screen.
 */
public class OptionsScreen extends OptionsSubScreen {

    protected OptionsListWidget listWidget;

    public final int listTop = 32;
    public final Supplier<Integer> listBottom = () -> height - 32;
    public final int listItemHeight = 25;

    public OptionsScreen(Screen lastScreen, Component title, OptionsListWidget listWidget) {
        super(lastScreen, Minecraft.getInstance().options, title);
        this.listWidget = listWidget;
    }

    @Override
    protected void init() {
        reloadListWidget();
    }

    @Override
    public void render(@NotNull GuiGraphics context, int mouseX, int mouseY, float delta) {
        renderDirtBackground(context);
        context.drawCenteredString(font, title, width / 2, 5, 0xffffff);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        listWidget.onClose();
        super.onClose();
    }

    public void reloadListWidget() {
        clearWidgets();
        listWidget = listWidget.resize(width, height, listTop, listBottom.get(), listItemHeight, listWidget.getScrollAmount());
        listWidget.setScreen(this);
        addRenderableWidget(listWidget);
        addRenderableWidget(Button.builder(CommonComponents.GUI_DONE,
                        (button) -> onClose())
                .pos(width / 2 - 120, height - 27)
                .size(240, 20)
                .build());
    }
}