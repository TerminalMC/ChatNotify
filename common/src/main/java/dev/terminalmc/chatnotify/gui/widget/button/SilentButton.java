/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify.gui.widget.button;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class SilentButton extends Button {

    public SilentButton(int x, int y, int width, int height,
                           Component message, OnPress onPress,
                           CreateNarration narrationProvider) {
        super(x, y, width, height, message, onPress, narrationProvider);
    }

    public SilentButton(int x, int y, int width, int height,
                           Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    public void playDownSound(@NotNull SoundManager soundManager) {
        // Shut up
    }
}

