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

package dev.terminalmc.chatnotify.gui.toast;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class NotificationToast implements Toast {

    private static final ResourceLocation BACKGROUND_SPRITE =
            ResourceLocation.withDefaultNamespace("toast/advancement");
    private static final int DISPLAY_TIME = 5000;
    private static final int WIDTH = 160;
    private static final int HEIGHT = 32;
    private static final int X_MARGIN = 10;
    private static final int Y_MARGIN = 6;
    private static final int LINE_SPACE = 3;

    private final int lineHeight;
    private final List<FormattedCharSequence> messageLines;
    private Toast.Visibility wantedVisibility;

    public NotificationToast(Component message) {
        this.messageLines = Minecraft.getInstance().font.split(message, WIDTH - X_MARGIN * 2);
        this.lineHeight = Minecraft.getInstance().font.lineHeight + LINE_SPACE;
    }

    @Override
    public @NotNull Visibility getWantedVisibility() {
        return wantedVisibility;
    }

    @Override
    public void update(@NotNull ToastManager manager, long elapsedTime) {
        this.wantedVisibility =
                elapsedTime < DISPLAY_TIME * manager.getNotificationDisplayTimeMultiplier()
                        ? Visibility.SHOW : Visibility.HIDE;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, @NotNull Font font, long elapsedTime) {
        if (messageLines.size() <= 1) {
            // Message fits in a single line, render a single sprite
            graphics.blitSprite(RenderType::guiTextured, BACKGROUND_SPRITE, 0, 0, WIDTH, height());
        } else {
            // Message requires multiple lines, stretch vertically by rendering
            // multiple sprites
            int width = WIDTH;
            int height = HEIGHT + lineHeight * Math.max(0, messageLines.size() - 2);
            int partialSpriteHeight = HEIGHT - 4;
            int bottomSpriteHeight = Math.min(4, height - partialSpriteHeight);

            // Top border
            renderBackgroundRow(graphics, width, 0, 0, partialSpriteHeight);

            // Middle background
            int offset = 10;
            for (int y = partialSpriteHeight; y < height - bottomSpriteHeight; y += offset) {
                int vOffset = HEIGHT / 2;
                int vHeight = Math.min(HEIGHT / 2, height - y - bottomSpriteHeight);
                this.renderBackgroundRow(graphics, width, vOffset, y, vHeight);
            }

            // Bottom border
            renderBackgroundRow(
                    graphics,
                    width,
                    HEIGHT - bottomSpriteHeight,
                    height - bottomSpriteHeight,
                    bottomSpriteHeight
            );
        }

        if (messageLines.size() == 1) {
            // Single line, center vertically
            graphics.drawString(
                    font,
                    messageLines.getFirst(),
                    X_MARGIN,
                    Y_MARGIN + lineHeight / 2,
                    -1,
                    false
            );
        } else {
            // Multiple lines, justify to top margin
            for (int j = 0; j < messageLines.size(); j++) {
                graphics.drawString(
                        font,
                        messageLines.get(j),
                        X_MARGIN,
                        Y_MARGIN + lineHeight * j,
                        -1,
                        false
                );
            }
        }
    }

    private void renderBackgroundRow(
            GuiGraphics graphics,
            int width,
            int vOffset,
            int y,
            int vHeight
    ) {
        int uWidth = vOffset == 0 ? 20 : 5;
        int uRemainder = Math.min(60, width - uWidth);

        // Left border
        graphics.blitSprite(
                RenderType::guiTextured,
                BACKGROUND_SPRITE,
                WIDTH,
                HEIGHT,
                0,
                vOffset,
                0,
                y,
                uWidth,
                vHeight
        );

        // Middle background
        int offset = 64;
        for (int x = uWidth; x < width - uRemainder; x += offset) {
            graphics.blitSprite(
                    RenderType::guiTextured,
                    BACKGROUND_SPRITE,
                    WIDTH,
                    HEIGHT,
                    HEIGHT,
                    vOffset,
                    x,
                    y,
                    Math.min(offset, width - x - uRemainder),
                    vHeight
            );
        }

        // Right border
        graphics.blitSprite(
                RenderType::guiTextured,
                BACKGROUND_SPRITE,
                WIDTH,
                HEIGHT,
                WIDTH - uRemainder,
                vOffset,
                width - uRemainder,
                y,
                uRemainder,
                vHeight
        );
    }
}
