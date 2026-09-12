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

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link Button} that accepts right as well as left clicks.
 * <p>
 * Note: If contained within a parent element such as a
 * {@link net.minecraft.client.gui.components.ContainerObjectSelectionList}, the parent element must
 * also be modified to accept right clicks.
 */
public class RightClickableButton extends Button.Plain {

    protected final OnPress onRightPress;

    public RightClickableButton(
            int x,
            int y,
            int width,
            int height,
            Component msg,
            OnPress onPress,
            OnPress onRightPress
    ) {
        super(x, y, width, height, msg, onPress, DEFAULT_NARRATION);
        this.onRightPress = onRightPress;
    }

    public void onRightPress() {
        this.onRightPress.onPress(this);
    }

    @Override
    public void onClick(@NotNull MouseButtonEvent event, boolean doubleClick) {
        if (event.isRight()) {
            onRightPress();
        } else {
            onPress(event);
        }
    }

    @Override
    protected boolean isValidClickButton(@NotNull MouseButtonInfo info) {
        return super.isValidClickButton(info) || info.button() == InputConstants.MOUSE_BUTTON_RIGHT;
    }
}
