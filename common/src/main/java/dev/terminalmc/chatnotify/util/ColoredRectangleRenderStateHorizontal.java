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

package dev.terminalmc.chatnotify.util;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

public record ColoredRectangleRenderStateHorizontal(
        RenderPipeline pipeline,
        TextureSetup textureSetup,
        Matrix3x2f pose,
        int x0,
        int y0,
        int x1,
        int y1,
        int colorFrom,
        int colorTo,
        @Nullable ScreenRectangle scissorArea,
        @Nullable ScreenRectangle bounds
) implements GuiElementRenderState {

    public ColoredRectangleRenderStateHorizontal(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2f pose,
            int x0,
            int y0,
            int x1,
            int y1,
            int colorFrom,
            int colorTo,
            @Nullable ScreenRectangle bounds
    ) {
        this(
                pipeline,
                textureSetup,
                pose,
                x0,
                y0,
                x1,
                y1,
                colorFrom,
                colorTo,
                bounds,
                getBounds(x0, y0, x1, y1, pose, bounds)
        );
    }

    public void buildVertices(VertexConsumer consumer) {
        consumer.addVertexWith2DPose(pose(), (float) x0(), (float) y0()).setColor(colorFrom());
        consumer.addVertexWith2DPose(pose(), (float) x0(), (float) y1()).setColor(colorFrom());
        consumer.addVertexWith2DPose(pose(), (float) x1(), (float) y1()).setColor(colorTo());
        consumer.addVertexWith2DPose(pose(), (float) x1(), (float) y0()).setColor(colorTo());
    }

    @Nullable
    private static ScreenRectangle getBounds(
            int x0,
            int y0,
            int x1,
            int y1,
            Matrix3x2f pose,
            @Nullable ScreenRectangle bounds
    ) {
        ScreenRectangle screenrectangle =
                (new ScreenRectangle(x0, y0, x1 - x0, y1 - y0)).transformMaxBounds(pose);
        return bounds != null ? bounds.intersection(screenrectangle) : screenrectangle;
    }
}
