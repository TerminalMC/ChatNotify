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

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.terminalmc.chatnotify.util.inject.IGuiGraphics;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor.ARGB32;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin implements IGuiGraphics {

    @Shadow
    @Final
    private BufferSource bufferSource;

    @Shadow
    @Final
    private PoseStack pose;

    @Shadow
    protected abstract void flushIfUnmanaged();

    @Override
    public void chatnotify$fillGradientHorizontal(
            int x0,
            int y0,
            int x1,
            int y1,
            int colorFrom,
            int colorTo
    ) {
        float alphaFrom = (float) ARGB32.alpha(colorFrom) / 255.0F;
        float redFrom = (float) ARGB32.red(colorFrom) / 255.0F;
        float greenFrom = (float) ARGB32.green(colorFrom) / 255.0F;
        float blueFrom = (float) ARGB32.blue(colorFrom) / 255.0F;
        float alphaTo = (float) ARGB32.alpha(colorTo) / 255.0F;
        float redTo = (float) ARGB32.red(colorTo) / 255.0F;
        float greenTo = (float) ARGB32.green(colorTo) / 255.0F;
        float blueTo = (float) ARGB32.blue(colorTo) / 255.0F;
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.gui());
        Matrix4f matrix4f = pose.last().pose();
        consumer.vertex(matrix4f, (float) x0, (float) y0, 0F)
                .color(redFrom, greenFrom, blueFrom, alphaFrom).endVertex();
        consumer.vertex(matrix4f, (float) x0, (float) y1, 0F)
                .color(redFrom, greenFrom, blueFrom, alphaFrom).endVertex();
        consumer.vertex(matrix4f, (float) x1, (float) y1, 0F)
                .color(redTo, greenTo, blueTo, alphaTo).endVertex();
        consumer.vertex(matrix4f, (float) x1, (float) y0, 0F)
                .color(redTo, greenTo, blueTo, alphaTo).endVertex();
        flushIfUnmanaged();
    }
}
