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

package dev.terminalmc.chatnotify.mixin;

import dev.terminalmc.chatnotify.util.ColoredRectangleRenderStateHorizontal;
import dev.terminalmc.chatnotify.util.inject.IGuiGraphics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin implements IGuiGraphics {

    @Shadow
    @Final
    private GuiRenderState guiRenderState;

    @Shadow
    @Final
    private Matrix3x2fStack pose;

    @Shadow
    @Final
    private GuiGraphics.ScissorStack scissorStack;

    @Override
    public void chatnotify$fillGradientHorizontal(
            int x1,
            int y1,
            int x2,
            int y2,
            int colorFrom,
            int colorTo
    ) {
        guiRenderState.submitGuiElement(new ColoredRectangleRenderStateHorizontal(
                RenderPipelines.GUI,
                TextureSetup.noTexture(),
                new Matrix3x2f(pose),
                x1,
                y1,
                x2,
                y2,
                colorFrom,
                colorTo,
                scissorStack.peek()
        ));
    }
}
