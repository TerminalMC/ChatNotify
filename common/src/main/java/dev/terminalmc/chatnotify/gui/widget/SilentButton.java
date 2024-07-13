/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.gui.widget;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class SilentButton extends Button {
    public SilentButton(int x, int y, int width, int height, Component msg, OnPress onPress) {
        super(x, y, width, height, msg, onPress, DEFAULT_NARRATION);
    }

    @Override
    public void playDownSound(@NotNull SoundManager soundManager) {
        // Shut up
    }
}
