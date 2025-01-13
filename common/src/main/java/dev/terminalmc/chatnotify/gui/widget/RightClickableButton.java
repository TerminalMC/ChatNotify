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

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * A {@link Button} that accepts right as well as left clicks.
 * 
 * <p><b>Note:</b> If contained within a parent element such as a 
 * {@link net.minecraft.client.gui.components.ContainerObjectSelectionList},
 * the parent element must also be modified to accept right clicks.</p>
 */
public class RightClickableButton extends Button {
    protected final OnPress onRightPress;
    
    public RightClickableButton(int x, int y, int width, int height, Component msg, 
                                OnPress onPress, OnPress onRightPress) {
        super(x, y, width, height, msg, onPress, DEFAULT_NARRATION);
        this.onRightPress = onRightPress;
    }

    public void onRightPress() {
        this.onRightPress.onPress(this);
    }
    
    @Override
    public void onClick(double mouseX, double mouseY) {
        if (GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), 
                InputConstants.MOUSE_BUTTON_RIGHT) == 1) {
            onRightPress();
        } else {
            onPress();
        }
    }
    
    @Override
    protected boolean isValidClickButton(int button) {
        return super.isValidClickButton(button) || button == InputConstants.MOUSE_BUTTON_RIGHT;
    }
}
