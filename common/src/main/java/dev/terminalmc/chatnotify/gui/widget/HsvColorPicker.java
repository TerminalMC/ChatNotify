/*
 * Copyright 2024 TerminalMC
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

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import dev.terminalmc.chatnotify.util.MiscUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.terminalmc.chatnotify.util.Localization.localized;

public class HsvColorPicker extends OverlayWidget {
    public static final int GUI_SHADOW_ALPHA = 160;
    public static final int GUI_LIGHT = 160;
    public static final int GUI_DARK = 44;

    public static final int MIN_WIDTH = 100;
    public static final int MIN_HEIGHT = 80;
    public static final int MAX_WIDTH = 400;
    public static final int MAX_HEIGHT = 300;

    public static final int BORDER = 2;
    public static final int OUTLINE = 1;
    public static final int CURSOR = 1;

    private final String newColorLabel = " " + localized("option", "color.new").getString() + " ";
    private final String oldColorLabel = " " + localized("option", "color.old").getString() + " ";

    private final Supplier<Integer> source;
    private final Consumer<Integer> dest;

    private boolean hasClickedOnH = false;
    private boolean hasClickedOnSv = false;

    private int hsvPickerBoxWidth;

    private int svFieldX;
    private int svFieldY;
    private int svFieldWidth;
    private int svFieldHeight;
    private int svCursorX;
    private int svCursorY;

    private int hFieldX;
    private int hFieldY;
    private int hFieldWidth;
    private int hFieldHeight;
    private int hSecSize;
    private int hCursorY;

    private int newCFieldX;
    private int newCFieldY;
    private int newCFieldWidth;
    private int newCFieldHeight;
    private int newCFieldTextX;
    private int newCFieldTextY;

    private int oldCFieldX;
    private int oldCFieldY;
    private int oldCFieldWidth;
    private int oldCFieldHeight;
    private int oldCFieldTextX;
    private int oldCFieldTextY;

    private int cFieldTextWidth;

    private TextField hexField;
    private Button cancelButton;
    private Button confirmButton;

    private final float[] hsv = new float[3];
    private int oldColor;

    private boolean updateFromCursor;

    public HsvColorPicker(int x, int y, int width, int height, Component msg,
                          Supplier<Integer> source, Consumer<Integer> dest,
                          Consumer<OverlayWidget> close) {
        super(x, y, width, height, msg, close);
        this.source = source;
        this.dest = dest;
        this.updateColorFromSource();
        this.init();
    }

    /**
     * Builds or rebuilds the widget based on its positional and dimensional
     * fields.
     */
    protected void init() {
        // Fixed values
        int textFieldHeight = 20;
        int minColorBoxWidth = 12;

        int interiorWidth = width - (BORDER * 2);
        int interiorHeight = height - (BORDER * 2);
        hsvPickerBoxWidth = interiorWidth / 2;

        // HSV color picker (saturation/value field, and hue field)
        hFieldWidth = minColorBoxWidth;
        hSecSize = (interiorHeight - (2 * OUTLINE)) / 6;
        hFieldHeight = hSecSize * 6;

        svFieldWidth = hsvPickerBoxWidth - hFieldWidth - (BORDER * 2) - (OUTLINE * 4);
        svFieldHeight = hFieldHeight;

        svFieldX = BORDER + OUTLINE;
        hFieldX = svFieldX + svFieldWidth + BORDER + (OUTLINE * 2);
        hFieldY = BORDER + OUTLINE + ((interiorHeight - (hFieldHeight + (OUTLINE * 2))) / 2);
        svFieldY = hFieldY;

        // Hex code text field
        Font font = Minecraft.getInstance().font;
        int hexFieldHeight = textFieldHeight;
        int hexFieldX = BORDER + hsvPickerBoxWidth;
        int hexFieldY = BORDER;
        int hexFieldWidth = interiorWidth - hsvPickerBoxWidth;

        hexField = new TextField(getX() + hexFieldX, getY() + hexFieldY,
                hexFieldWidth, hexFieldHeight);
        hexField.hexColorValidator();
        hexField.setMaxLength(7);
        hexField.setResponder(this::updateColorFromHexField);
        hexField.setValue(TextColor.fromRgb(Color.HSBtoRGB(hsv[0], hsv[1], hsv[2])).formatValue());

        // Cancel and confirm buttons
        int cancelButtonWidth = interiorWidth - hsvPickerBoxWidth - (hsvPickerBoxWidth / 2);
        int buttonHeight = 20;
        int cancelButtonX = BORDER + hsvPickerBoxWidth;
        int cancelButtonY = BORDER + interiorHeight - buttonHeight;

        cancelButton = Button.builder(CommonComponents.GUI_CANCEL, (button) -> onClose())
                .pos(getX() + cancelButtonX, getY() + cancelButtonY)
                .size(cancelButtonWidth, buttonHeight)
                .build();

        int confirmButtonWidth = interiorWidth - hsvPickerBoxWidth - cancelButtonWidth;
        int confirmButtonX = BORDER + hsvPickerBoxWidth + cancelButtonWidth;
        int confirmButtonY = cancelButtonY;

        confirmButton = Button.builder(CommonComponents.GUI_OK, (button) -> {
                    dest.accept(Mth.hsvToRgb(hsv[0], hsv[1], hsv[2]));
                    onClose();
                })
                .pos(getX() + confirmButtonX, getY() + confirmButtonY)
                .size(confirmButtonWidth, buttonHeight)
                .build();

        // New and old color display boxes
        cFieldTextWidth = Math.min(Math.max(font.width(newColorLabel), font.width(oldColorLabel)),
                interiorWidth - hsvPickerBoxWidth - minColorBoxWidth);
        int combinedCFieldHeight = interiorHeight - hexFieldHeight - buttonHeight - (OUTLINE * 4);

        newCFieldWidth = hexFieldWidth - cFieldTextWidth - (OUTLINE * 2);
        newCFieldHeight = combinedCFieldHeight / 2;
        newCFieldX = hexFieldX + cFieldTextWidth + OUTLINE;
        newCFieldY = hexFieldY + hexFieldHeight + OUTLINE;
        newCFieldTextX = getX() + newCFieldX - cFieldTextWidth - OUTLINE;
        newCFieldTextY = getY() + newCFieldY + ((newCFieldHeight - font.lineHeight) / 2);

        oldCFieldWidth = newCFieldWidth;
        oldCFieldHeight = combinedCFieldHeight - newCFieldHeight;
        oldCFieldX = newCFieldX;
        oldCFieldY = newCFieldY + newCFieldHeight + (OUTLINE * 2);
        oldCFieldTextX = newCFieldTextX;
        oldCFieldTextY = getY() + oldCFieldY + ((oldCFieldHeight - font.lineHeight) / 2);
    }

    // Overlay stuff

    @Override
    public int getMinWidth() {
        return MIN_WIDTH;
    }

    @Override
    public int getMaxWidth() {
        return MAX_WIDTH;
    }

    @Override
    public int getMinHeight() {
        return MIN_HEIGHT;
    }

    @Override
    public int getMaxHeight() {
        return MAX_HEIGHT;
    }

    // Regular widget stuff

    public void updateColorFromSource() {
        int color = source.get();
        Color.RGBtoHSB(FastColor.ARGB32.red(color), FastColor.ARGB32.green(color),
                FastColor.ARGB32.blue(color), hsv);
        oldColor = color;
        if (hexField != null) {
            hexField.setValue(TextColor.fromRgb(color).formatValue());
        }
    }

    private void updateHexField() {
        updateFromCursor = true;
        int color = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
        hexField.setValue(TextColor.fromRgb(color).formatValue());
        updateFromCursor = false;
    }

    private void updateColorFromHexField(String s) {
        TextColor textColor = MiscUtil.parseColor(s);
        if (textColor != null) {
            int color = textColor.getValue();
            if (!updateFromCursor) {
                Color.RGBtoHSB(FastColor.ARGB32.red(color), FastColor.ARGB32.green(color),
                        FastColor.ARGB32.blue(color), hsv);
                updateHCursor();
                updateSvCursor();
            }
            if (hsv[2] < 0.1) hexField.setTextColor(16777215); // Keep text visible
            else hexField.setTextColor(color);
        }
    }

    private void updateHCursor() {
        hCursorY = hFieldY + (int)(hsv[0] * hFieldHeight);
    }

    private void updateSvCursor() {
        svCursorX = svFieldX + (int)(hsv[1] * svFieldWidth);
        svCursorY = svFieldY + (int)((1.0F - hsv[2]) * svFieldHeight);
    }

    private void updateHFromCursor(double cursorY) {
        hsv[0] = (float)cursorY / (float)hFieldHeight;
        updateHexField();
    }

    private void updateSvFromCursor(double cursorX, double cursorY) {
        hsv[1] = (float)cursorX / (float)svFieldWidth;
        hsv[2] = 1.0F - (float)cursorY / (float)svFieldHeight;
        updateHexField();
    }

    private float getHFromCursor() {
        return ((float)hCursorY - hFieldY) / (float)hFieldHeight;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (hexField.isFocused()) {
            return hexField.keyPressed(keyCode, scanCode, modifiers);
        } else {
            return false;
        }
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (hexField.isFocused()) {
            return hexField.charTyped(chr, modifiers);
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        hasClickedOnH = false;
        hasClickedOnSv = false;
        if (mouseOnElement(mouseX, mouseY, getX(), getY(), width, height).isPresent()) {
            // Hex code field
            if (mouseOnWidget(hexField, mouseX, mouseY)) {
                if (!hexField.isFocused()) {
                    hexField.setFocused(true);
                } else {
                    hexField.mouseClicked(mouseX, mouseY, button);
                }
                return true;
            } else {
                hexField.setFocused(false);
            }
            // Other elements can only use left clicks
            if (button == InputConstants.MOUSE_BUTTON_LEFT) {
                // Hue field
                Optional<double[]> hFieldCursor = mouseOnElement(mouseX, mouseY,
                        getX() + hFieldX, getY() + hFieldY, hFieldWidth, hFieldHeight);
                if (hFieldCursor.isPresent()) {
                    hasClickedOnH = true;
                    hCursorY = (int)hFieldCursor.get()[1] + hFieldY;
                    updateHFromCursor(hFieldCursor.get()[1]);
                    return true;
                }
                // Saturation/value field
                Optional<double[]> svFieldCursor = mouseOnElement(mouseX, mouseY,
                        getX() + svFieldX, getY() + svFieldY, svFieldWidth, svFieldHeight);
                if (svFieldCursor.isPresent()) {
                    hasClickedOnSv = true;
                    svCursorX = (int)svFieldCursor.get()[0] + svFieldX;
                    svCursorY = (int)svFieldCursor.get()[1] + svFieldY;
                    updateSvFromCursor(svFieldCursor.get()[0], svFieldCursor.get()[1]);
                    return true;
                }
                if (mouseOnWidget(cancelButton, mouseX, mouseY)) {
                    cancelButton.onClick(mouseX, mouseY);
                    return true;
                }
                if (mouseOnWidget(confirmButton, mouseX, mouseY)) {
                    confirmButton.onClick(mouseX, mouseY);
                    return true;
                }
            }
        } else {
            onClose();
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (hasClickedOnH) {
            double cursorY = mouseY - getY();
            if (cursorY < hFieldY) cursorY = hFieldY;
            else if (cursorY > hFieldY + hFieldHeight) cursorY = hFieldY + hFieldHeight;
            updateHFromCursor(cursorY - hFieldY);
            hCursorY = (int)cursorY;
            return true;
        } else if (hasClickedOnSv) {
            double cursorX = mouseX - getX();
            double cursorY = mouseY - getY();
            if (cursorX < svFieldX) cursorX = svFieldX;
            else if (cursorX > svFieldX + svFieldWidth) cursorX = svFieldX + svFieldWidth;
            if (cursorY < svFieldY) cursorY = svFieldY;
            else if (cursorY > svFieldY + svFieldHeight) cursorY = svFieldY + svFieldHeight;
            updateSvFromCursor(cursorX - svFieldX, cursorY - svFieldY);
            svCursorX = (int)cursorX;
            svCursorY = (int)cursorY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    private boolean mouseOnWidget(AbstractWidget widget, double mouseX, double mouseY) {
        Optional<double[]> mouseOnWidget = mouseOnElement(mouseX, mouseY,
                widget.getX(), widget.getY(), widget.getWidth(), widget.getHeight());
        return mouseOnWidget.isPresent();
    }

    private Optional<double[]> mouseOnElement(double mouseX, double mouseY, int elementX,
                                              int elementY, int elementWidth, int elementHeight) {
        if ((elementX <= mouseX && mouseX < elementX + elementWidth)
                && (elementY <= mouseY && mouseY < elementY + elementHeight)) {
            return Optional.of(new double[]{mouseX - elementX, mouseY - elementY});
        } else {
            return Optional.empty();
        }
    }

    @Override
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        drawQuads(graphics);
        graphics.drawString(Minecraft.getInstance().font, newColorLabel,
                newCFieldTextX, newCFieldTextY, 16777215);
        graphics.drawString(Minecraft.getInstance().font, oldColorLabel,
                oldCFieldTextX, oldCFieldTextY, 16777215);
        hexField.renderWidget(graphics, mouseX, mouseY, delta);
        cancelButton.render(graphics, mouseX, mouseY, delta);
        confirmButton.render(graphics, mouseX, mouseY, delta);
    }

    private void drawQuads(GuiGraphics graphics) {
        // Setup
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.depthFunc(GlConst.GL_ALWAYS);
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        BufferBuilder builder = Tesselator.getInstance().begin(
                VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        int x = getX();
        int y = getY();

        // Screen shadow
        builder.addVertex(0, 0, 0F).setColor(0, 0, 0, GUI_SHADOW_ALPHA);
        builder.addVertex(0, graphics.guiHeight(), 0F).setColor(0, 0, 0, GUI_SHADOW_ALPHA);
        builder.addVertex(graphics.guiWidth(), graphics.guiHeight(), 0F).setColor(0, 0, 0, GUI_SHADOW_ALPHA);
        builder.addVertex(graphics.guiWidth(), 0, 0F).setColor(0, 0, 0, GUI_SHADOW_ALPHA);

        // Main box border
        builder.addVertex(x, y, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);
        builder.addVertex(x, y+height, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);
        builder.addVertex(x+width, y+height, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);
        builder.addVertex(x+width, y, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);

        // Main box background
        builder.addVertex(x+BORDER, y+BORDER, 0F).setColor(GUI_DARK, GUI_DARK, GUI_DARK, 255);
        builder.addVertex(x+BORDER, y+height-BORDER, 0F).setColor(GUI_DARK, GUI_DARK, GUI_DARK, 255);
        builder.addVertex(x+width-BORDER, y+height-BORDER, 0F).setColor(GUI_DARK, GUI_DARK, GUI_DARK, 255);
        builder.addVertex(x+width-BORDER, y+BORDER, 0F).setColor(GUI_DARK, GUI_DARK, GUI_DARK, 255);

        // HSV picker box
        builder.addVertex(x+BORDER, y+BORDER, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);
        builder.addVertex(x+BORDER, y+height-BORDER, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);
        builder.addVertex(x+BORDER+ hsvPickerBoxWidth, y+height-BORDER, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);
        builder.addVertex(x+BORDER+ hsvPickerBoxWidth, y+BORDER, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);

        // Saturation/value field outline
        builder.addVertex(x+svFieldX-OUTLINE, y+svFieldY-OUTLINE, 0F).setColor(GUI_DARK, GUI_DARK, GUI_DARK, 255);
        builder.addVertex(x+svFieldX-OUTLINE, y+svFieldY+svFieldHeight+OUTLINE, 0F).setColor(GUI_DARK, GUI_DARK, GUI_DARK, 255);
        builder.addVertex(x+svFieldX+svFieldWidth+OUTLINE, y+svFieldY+svFieldHeight+OUTLINE, 0F).setColor(GUI_DARK, GUI_DARK, GUI_DARK, 255);
        builder.addVertex(x+svFieldX+svFieldWidth+OUTLINE, y+svFieldY-OUTLINE, 0F).setColor(GUI_DARK, GUI_DARK, GUI_DARK, 255);

        // White, solid
        builder.addVertex(x+svFieldX, y+svFieldY, 0F).setColor(255, 255, 255, 255);
        builder.addVertex(x+svFieldX, y+svFieldY+svFieldHeight, 0F).setColor(255, 255, 255, 255);
        builder.addVertex(x+svFieldX+svFieldWidth, y+svFieldY+svFieldHeight, 0F).setColor(255, 255, 255, 255);
        builder.addVertex(x+svFieldX+svFieldWidth, y+svFieldY, 0F).setColor(255, 255, 255, 255);

        // Hue, transparent left to solid right
        Color hue = Color.getHSBColor(getHFromCursor(), 1, 1);
        int hueR = hue.getRed();
        int hueG = hue.getGreen();
        int hueB = hue.getBlue();
        builder.addVertex(x+svFieldX, y+svFieldY, 0F).setColor(hueR, hueG, hueB, 0);
        builder.addVertex(x+svFieldX, y+svFieldY+svFieldHeight, 0F).setColor(hueR, hueG, hueB, 0);
        builder.addVertex(x+svFieldX+svFieldWidth, y+svFieldY+svFieldHeight, 0F).setColor(hueR, hueG, hueB, 255);
        builder.addVertex(x+svFieldX+svFieldWidth, y+svFieldY, 0F).setColor(hueR, hueG, hueB, 255);

        // Black, transparent top to solid bottom
        builder.addVertex(x+svFieldX, y+svFieldY, 0F).setColor(0, 0, 0, 0);
        builder.addVertex(x+svFieldX, y+svFieldY+svFieldHeight, 0F).setColor(0, 0, 0, 255);
        builder.addVertex(x+svFieldX+svFieldWidth, y+svFieldY+svFieldHeight, 0F).setColor(0, 0, 0, 255);
        builder.addVertex(x+svFieldX+svFieldWidth, y+svFieldY, 0F).setColor(0, 0, 0, 0);

        // Saturation/value cursor horizontal
        int limitSvCursorY = Math.min(svCursorY, svFieldY+svFieldHeight-CURSOR); // Keep within outline
        builder.addVertex(x+svFieldX, y+limitSvCursorY, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);
        builder.addVertex(x+svFieldX, y+limitSvCursorY+CURSOR, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);
        builder.addVertex(x+svFieldX+svFieldWidth, y+limitSvCursorY+CURSOR, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);
        builder.addVertex(x+svFieldX+svFieldWidth, y+limitSvCursorY, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);

        // Saturation/value cursor vertical
        int limitSvCursorX = Math.min(svCursorX, svFieldX+svFieldWidth-CURSOR); // Keep within outline
        builder.addVertex(x+limitSvCursorX, y+svFieldY, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);
        builder.addVertex(x+limitSvCursorX, y+svFieldY+svFieldHeight, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);
        builder.addVertex(x+limitSvCursorX+CURSOR, y+svFieldY+svFieldHeight, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);
        builder.addVertex(x+limitSvCursorX+CURSOR, y+svFieldY, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);

        // Hue field outline
        builder.addVertex(x+hFieldX-OUTLINE, y+hFieldY-OUTLINE, 0F).setColor(GUI_DARK, GUI_DARK, GUI_DARK, 255);
        builder.addVertex(x+hFieldX-OUTLINE, y+hFieldY+hFieldHeight+OUTLINE, 0F).setColor(GUI_DARK, GUI_DARK, GUI_DARK, 255);
        builder.addVertex(x+hFieldX+hFieldWidth+OUTLINE, y+hFieldY+hFieldHeight+OUTLINE, 0F).setColor(GUI_DARK, GUI_DARK, GUI_DARK, 255);
        builder.addVertex(x+hFieldX+hFieldWidth+OUTLINE, y+hFieldY-OUTLINE, 0F).setColor(GUI_DARK, GUI_DARK, GUI_DARK, 255);

        // Red to yellow
        int secStart = hFieldY;
        int secEnd = secStart + hSecSize;
        builder.addVertex(x+hFieldX, y+secStart, 0F).setColor(255, 0, 0, 255);
        builder.addVertex(x+hFieldX, y+secEnd, 0F).setColor(255, 255, 0, 255);
        builder.addVertex(x+hFieldX+hFieldWidth, y+secEnd, 0F).setColor(255, 255, 0, 255);
        builder.addVertex(x+hFieldX+hFieldWidth, y+secStart, 0F).setColor(255, 0, 0, 255);

        // Yellow to green
        secStart = secEnd;
        secEnd += hSecSize;
        builder.addVertex(x+hFieldX, y+secStart, 0F).setColor(255, 255, 0, 255);
        builder.addVertex(x+hFieldX, y+secEnd, 0F).setColor(0, 255, 0, 255);
        builder.addVertex(x+hFieldX+hFieldWidth, y+secEnd, 0F).setColor(0, 255, 0, 255);
        builder.addVertex(x+hFieldX+hFieldWidth, y+secStart, 0F).setColor(255, 255, 0, 255);

        // Green to cyan
        secStart = secEnd;
        secEnd += hSecSize;
        builder.addVertex(x+hFieldX, y+secStart, 0F).setColor(0, 255, 0, 255);
        builder.addVertex(x+hFieldX, y+secEnd, 0F).setColor(0, 255, 255, 255);
        builder.addVertex(x+hFieldX+hFieldWidth, y+secEnd, 0F).setColor(0, 255, 255, 255);
        builder.addVertex(x+hFieldX+hFieldWidth, y+secStart, 0F).setColor(0, 255, 0, 255);

        // Cyan to blue
        secStart = secEnd;
        secEnd += hSecSize;
        builder.addVertex(x+hFieldX, y+secStart, 0F).setColor(0, 255, 255, 255);
        builder.addVertex(x+hFieldX, y+secEnd, 0F).setColor(0, 0, 255, 255);
        builder.addVertex(x+hFieldX+hFieldWidth, y+secEnd, 0F).setColor(0, 0, 255, 255);
        builder.addVertex(x+hFieldX+hFieldWidth, y+secStart, 0F).setColor(0, 255, 255, 255);

        // Blue to magenta
        secStart = secEnd;
        secEnd += hSecSize;
        builder.addVertex(x+hFieldX, y+secStart, 0F).setColor(0, 0, 255, 255);
        builder.addVertex(x+hFieldX, y+secEnd, 0F).setColor(255, 0, 255, 255);
        builder.addVertex(x+hFieldX+hFieldWidth, y+secEnd, 0F).setColor(255, 0, 255, 255);
        builder.addVertex(x+hFieldX+hFieldWidth, y+secStart, 0F).setColor(0, 0, 255, 255);

        // Magenta to red
        secStart = secEnd;
        secEnd += hSecSize;
        builder.addVertex(x+hFieldX, y+secStart, 0F).setColor(255, 0, 255, 255);
        builder.addVertex(x+hFieldX, y+secEnd, 0F).setColor(255, 0, 0, 255);
        builder.addVertex(x+hFieldX+hFieldWidth, y+secEnd, 0F).setColor(255, 0, 0, 255);
        builder.addVertex(x+hFieldX+hFieldWidth, y+secStart, 0F).setColor(255, 0, 255, 255);

        // Hue Cursor (horizontal only)
        int limitHCursorY = Math.min(hCursorY, hFieldY+hFieldHeight-CURSOR); // Keep within outline
        builder.addVertex(x+hFieldX, y+limitHCursorY, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);
        builder.addVertex(x+hFieldX, y+limitHCursorY+CURSOR, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);
        builder.addVertex(x+hFieldX+hFieldWidth, y+limitHCursorY+CURSOR, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);
        builder.addVertex(x+hFieldX+hFieldWidth, y+limitHCursorY, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);

        // New color field outline
        builder.addVertex(x+newCFieldX-OUTLINE, y+newCFieldY-OUTLINE, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);
        builder.addVertex(x+newCFieldX-OUTLINE, y+newCFieldY+newCFieldHeight+OUTLINE, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);
        builder.addVertex(x+newCFieldX+newCFieldWidth+OUTLINE, y+newCFieldY+newCFieldHeight+OUTLINE, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);
        builder.addVertex(x+newCFieldX+newCFieldWidth+OUTLINE, y+newCFieldY-OUTLINE, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);

        // New color
        int color = Mth.hsvToRgb(hsv[0], hsv[1], hsv[2]);
        int colorR = FastColor.ARGB32.red(color);
        int colorG = FastColor.ARGB32.green(color);
        int colorB = FastColor.ARGB32.blue(color);
        builder.addVertex(x+newCFieldX, y+newCFieldY, 0F).setColor(colorR, colorG, colorB, 255);
        builder.addVertex(x+newCFieldX, y+newCFieldY+newCFieldHeight, 0F).setColor(colorR, colorG, colorB, 255);
        builder.addVertex(x+newCFieldX+newCFieldWidth, y+newCFieldY+newCFieldHeight, 0F).setColor(colorR, colorG, colorB, 255);
        builder.addVertex(x+newCFieldX+newCFieldWidth, y+newCFieldY, 0F).setColor(colorR, colorG, colorB, 255);

        // Old color field outline
        builder.addVertex(x+oldCFieldX-OUTLINE, y+oldCFieldY-OUTLINE, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);
        builder.addVertex(x+oldCFieldX-OUTLINE, y+oldCFieldY+oldCFieldHeight+OUTLINE, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);
        builder.addVertex(x+oldCFieldX+oldCFieldWidth+OUTLINE, y+oldCFieldY+oldCFieldHeight+OUTLINE, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);
        builder.addVertex(x+oldCFieldX+oldCFieldWidth+OUTLINE, y+oldCFieldY-OUTLINE, 0F).setColor(GUI_LIGHT, GUI_LIGHT, GUI_LIGHT, 255);

        // Old color
        colorR = FastColor.ARGB32.red(oldColor);
        colorG = FastColor.ARGB32.green(oldColor);
        colorB = FastColor.ARGB32.blue(oldColor);
        builder.addVertex(x+oldCFieldX, y+oldCFieldY, 0F).setColor(colorR, colorG, colorB, 255);
        builder.addVertex(x+oldCFieldX, y+oldCFieldY+oldCFieldHeight, 0F).setColor(colorR, colorG, colorB, 255);
        builder.addVertex(x+oldCFieldX+oldCFieldWidth, y+oldCFieldY+oldCFieldHeight, 0F).setColor(colorR, colorG, colorB, 255);
        builder.addVertex(x+oldCFieldX+oldCFieldWidth, y+oldCFieldY, 0F).setColor(colorR, colorG, colorB, 255);

        // Draw
        BufferUploader.drawWithShader(builder.buildOrThrow());

        // Cleanup
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.depthFunc(GlConst.GL_LEQUAL);
    }
}
