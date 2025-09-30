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
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import dev.terminalmc.chatnotify.mixin.accessor.TextColorAccessor;
import dev.terminalmc.chatnotify.util.inject.IGuiGraphics;
import dev.terminalmc.chatnotify.util.text.ColorUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static dev.terminalmc.chatnotify.util.Localization.localized;

public class HsvColorPicker extends OverlayWidget {

    public static final int GUI_SHADOW = 0xA0000000;
    public static final int GUI_LIGHT = 0xFFA0A0A0;
    public static final int GUI_DARK = 0xFF2C2C2C;
    public static final int ARGB_WHITE = 0xFFFFFFFF;
    public static final int ARGB_RED = 0xFFFF0000;
    public static final int ARGB_YELLOW = 0xFFFFFF00;
    public static final int ARGB_GREEN = 0xFF00FF00;
    public static final int ARGB_CYAN = 0xFF00FFFF;
    public static final int ARGB_BLUE = 0xFF0000FF;
    public static final int ARGB_MAGENTA = 0xFFFF00FF;
    public static final int[] ARGB_GRADIENT = new int[]{
            ARGB_RED,
            ARGB_YELLOW,
            ARGB_GREEN,
            ARGB_CYAN,
            ARGB_BLUE,
            ARGB_MAGENTA,
            ARGB_RED
    };

    public static final int MIN_WIDTH = 200;
    public static final int MIN_HEIGHT = 80;
    public static final int MAX_WIDTH = 400;
    public static final int MAX_HEIGHT = 300;

    public static final int BORDER = 2;
    public static final int OUTLINE = 1;
    public static final int CURSOR = 1;

    private final String newColorLabel = " " + localized("common", "new").getString() + " ";
    private final String oldColorLabel = " " + localized("common", "old").getString() + " ";

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

    private TextField hexField;
    private Button cancelButton;
    private Button confirmButton;

    private final float[] hsv = new float[3];
    private int oldColor;

    private boolean updateFromCursor;

    public HsvColorPicker(
            int x,
            int y,
            int width,
            int height,
            Supplier<Integer> source,
            Consumer<Integer> dest,
            Consumer<OverlayWidget> close
    ) {
        super(x, y, width, height, true, Component.empty(), close);
        this.source = source;
        this.dest = dest;
        this.updateColorFromSource();
        this.init();
    }

    /**
     * Builds or rebuilds the widget based on its positional and dimensional fields.
     */
    protected void init() {
        Minecraft mc = Minecraft.getInstance();

        // Fixed values
        int hexFieldHeight = 20;
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
        Font font = mc.font;
        int hexFieldX = BORDER + hsvPickerBoxWidth;
        int hexFieldY = BORDER;
        int hexFieldWidth = interiorWidth - hsvPickerBoxWidth;

        hexField = new TextField(
                getX() + hexFieldX,
                getY() + hexFieldY,
                hexFieldWidth,
                hexFieldHeight
        );
        hexField.strict().hexColorValidator();
        hexField.setMaxLength(7);
        hexField.setResponder(this::updateColorFromHexField);
        hexField.setValue(((TextColorAccessor) (Object) TextColor.fromRgb(Color.HSBtoRGB(
                hsv[0],
                hsv[1],
                hsv[2]
        ))).chatnotify$formatValue());

        // Cancel and confirm buttons
        int cancelButtonWidth = interiorWidth - hsvPickerBoxWidth - (hsvPickerBoxWidth / 2);
        int buttonHeight = 20;
        int cancelButtonX = BORDER + hsvPickerBoxWidth;
        int buttonY = BORDER + interiorHeight - buttonHeight;

        cancelButton = Button.builder(CommonComponents.GUI_CANCEL, (button) -> onClose())
                .pos(getX() + cancelButtonX, getY() + buttonY)
                .size(cancelButtonWidth, buttonHeight)
                .build();

        int confirmButtonWidth = interiorWidth - hsvPickerBoxWidth - cancelButtonWidth;
        int confirmButtonX = BORDER + hsvPickerBoxWidth + cancelButtonWidth;

        confirmButton = Button.builder(
                        CommonComponents.GUI_DONE,
                        (button) -> {
                            dest.accept(Mth.hsvToRgb(hsv[0], hsv[1], hsv[2]));
                            onClose();
                        }
                )
                .pos(getX() + confirmButtonX, getY() + buttonY)
                .size(confirmButtonWidth, buttonHeight)
                .build();

        // New and old color display boxes
        int cFieldTextWidth =
                Math.min(
                        Math.max(font.width(newColorLabel), font.width(oldColorLabel)),
                        interiorWidth - hsvPickerBoxWidth - minColorBoxWidth
                );
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
        Color.RGBtoHSB(
                ARGB.red(color),
                ARGB.green(color),
                ARGB.blue(color),
                hsv
        );
        oldColor = color;
        if (hexField != null) {
            hexField.setValue(((TextColorAccessor) (Object) TextColor.fromRgb(color)).chatnotify$formatValue());
        }
    }

    private void updateHexField() {
        updateFromCursor = true;
        int color = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
        hexField.setValue(((TextColorAccessor) (Object) TextColor.fromRgb(color)).chatnotify$formatValue());
        updateFromCursor = false;
    }

    private void updateColorFromHexField(String s) {
        TextColor textColor = ColorUtil.parseColor(s);
        if (textColor != null) {
            int color = textColor.getValue();
            if (!updateFromCursor) {
                Color.RGBtoHSB(
                        ARGB.red(color),
                        ARGB.green(color),
                        ARGB.blue(color),
                        hsv
                );
                updateHCursor();
                updateSvCursor();
            }
            if (hsv[2] < 0.1)
                hexField.setTextColor(0xFFFFFFFF); // Keep text visible
            else
                hexField.setTextColor((0xFF << 24) | color);
        }
    }

    private void updateHCursor() {
        hCursorY = hFieldY + (int) (hsv[0] * hFieldHeight);
    }

    private void updateSvCursor() {
        svCursorX = svFieldX + (int) (hsv[1] * svFieldWidth);
        svCursorY = svFieldY + (int) ((1.0F - hsv[2]) * svFieldHeight);
    }

    private void updateHFromCursor(double cursorY) {
        hsv[0] = (float) cursorY / (float) hFieldHeight;
        updateHexField();
    }

    private void updateSvFromCursor(double cursorX, double cursorY) {
        hsv[1] = (float) cursorX / (float) svFieldWidth;
        hsv[2] = 1.0F - (float) cursorY / (float) svFieldHeight;
        updateHexField();
    }

    private float getHFromCursor() {
        return ((float) hCursorY - hFieldY) / (float) hFieldHeight;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (hexField.isFocused()) {
            return hexField.keyPressed(event);
        } else {
            return false;
        }
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (hexField.isFocused()) {
            return hexField.charTyped(event);
        } else {
            return false;
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        hasClickedOnH = false;
        hasClickedOnSv = false;
        if (mouseOnElement(event.x(), event.y(), getX(), getY(), width, height).isPresent()) {
            // Hex code field
            if (mouseOnWidget(hexField, event.x(), event.y())) {
                if (!hexField.isFocused()) {
                    hexField.setFocused(true);
                } else {
                    hexField.mouseClicked(event, doubleClick);
                }
                return true;
            } else {
                hexField.setFocused(false);
            }
            // Other elements can only use left clicks
            if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                // Hue field
                Optional<double[]> hFieldCursor = mouseOnElement(
                        event.x(),
                        event.y(),
                        getX() + hFieldX,
                        getY() + hFieldY,
                        hFieldWidth,
                        hFieldHeight
                );
                if (hFieldCursor.isPresent()) {
                    hasClickedOnH = true;
                    hCursorY = (int) hFieldCursor.get()[1] + hFieldY;
                    updateHFromCursor(hFieldCursor.get()[1]);
                    return true;
                }
                // Saturation/value field
                Optional<double[]> svFieldCursor = mouseOnElement(
                        event.x(),
                        event.y(),
                        getX() + svFieldX,
                        getY() + svFieldY,
                        svFieldWidth,
                        svFieldHeight
                );
                if (svFieldCursor.isPresent()) {
                    hasClickedOnSv = true;
                    svCursorX = (int) svFieldCursor.get()[0] + svFieldX;
                    svCursorY = (int) svFieldCursor.get()[1] + svFieldY;
                    updateSvFromCursor(svFieldCursor.get()[0], svFieldCursor.get()[1]);
                    return true;
                }
                if (mouseOnWidget(cancelButton, event.x(), event.y())) {
                    cancelButton.mouseClicked(event, doubleClick);
                    return true;
                }
                if (mouseOnWidget(confirmButton, event.x(), event.y())) {
                    confirmButton.mouseClicked(event, doubleClick);
                    return true;
                }
            }
        } else {
            onClose();
        }
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (hasClickedOnH) {
            double cursorY = event.y() - getY();
            if (cursorY < hFieldY)
                cursorY = hFieldY;
            else if (cursorY > hFieldY + hFieldHeight)
                cursorY = hFieldY + hFieldHeight;
            updateHFromCursor(cursorY - hFieldY);
            hCursorY = (int) cursorY;
            return true;
        } else if (hasClickedOnSv) {
            double cursorX = event.x() - getX();
            double cursorY = event.y() - getY();
            if (cursorX < svFieldX)
                cursorX = svFieldX;
            else if (cursorX > svFieldX + svFieldWidth)
                cursorX = svFieldX + svFieldWidth;
            if (cursorY < svFieldY)
                cursorY = svFieldY;
            else if (cursorY > svFieldY + svFieldHeight)
                cursorY = svFieldY + svFieldHeight;
            updateSvFromCursor(cursorX - svFieldX, cursorY - svFieldY);
            svCursorX = (int) cursorX;
            svCursorY = (int) cursorY;
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    private boolean mouseOnWidget(AbstractWidget widget, double mouseX, double mouseY) {
        Optional<double[]> mouseOnWidget = mouseOnElement(
                mouseX,
                mouseY,
                widget.getX(),
                widget.getY(),
                widget.getWidth(),
                widget.getHeight()
        );
        return mouseOnWidget.isPresent();
    }

    private Optional<double[]> mouseOnElement(
            double mouseX,
            double mouseY,
            int elementX,
            int elementY,
            int elementWidth,
            int elementHeight
    ) {
        if ((elementX <= mouseX && mouseX < elementX + elementWidth) && (elementY <= mouseY
                && mouseY < elementY + elementHeight)) {
            return Optional.of(new double[]{mouseX - elementX, mouseY - elementY});
        } else {
            return Optional.empty();
        }
    }

    @Override
    protected void renderWidget(
            @NotNull GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        drawColorPicker(graphics);
        graphics.drawString(
                Minecraft.getInstance().font,
                newColorLabel,
                newCFieldTextX,
                newCFieldTextY,
                0xFFFFFFFF
        );
        graphics.drawString(
                Minecraft.getInstance().font,
                oldColorLabel,
                oldCFieldTextX,
                oldCFieldTextY,
                0xFFFFFFFF
        );
        hexField.renderWidget(graphics, mouseX, mouseY, delta);
        cancelButton.render(graphics, mouseX, mouseY, delta);
        confirmButton.render(graphics, mouseX, mouseY, delta);
    }

    private void drawColorPicker(@NotNull GuiGraphics graphics) {
        int left = getX();
        int top = getY();
        int right = left + width;
        int bottom = top + height;

        int insideL = left + BORDER;
        int insideT = top + BORDER;
        int insideR = right - BORDER;
        int insideB = bottom - BORDER;

        int svL = left + svFieldX;
        int svT = top + svFieldY;
        int svR = svL + svFieldWidth;
        int svB = svT + svFieldHeight;

        int hL = left + hFieldX;
        int hT = top + hFieldY;
        int hR = hL + hFieldWidth;
        int hB = hT + hFieldHeight;

        int ncL = left + newCFieldX;
        int ncT = top + newCFieldY;
        int ncR = ncL + newCFieldWidth;
        int ncB = ncT + newCFieldHeight;

        int ocL = left + oldCFieldX;
        int ocT = top + oldCFieldY;
        int ocR = ocL + oldCFieldWidth;
        int ocB = ocT + oldCFieldHeight;

        // Screen shadow
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), GUI_SHADOW);

        // Main box border
        graphics.fill(left, top, right, bottom, GUI_LIGHT);

        // Main box background
        graphics.fill(insideL, insideT, insideR, insideB, GUI_DARK);

        // SV and H fields box
        graphics.fill(insideL, insideT, insideL + hsvPickerBoxWidth, insideB, GUI_LIGHT);

        // SV field outline
        graphics.fill(svL - OUTLINE, svT - OUTLINE, svR + OUTLINE, svB + OUTLINE, GUI_DARK);

        // SV field white fill
        graphics.fill(svL, svT, svR, svB, ARGB_WHITE);

        // SV field hue fill; transparent left to solid right
        int rgb = Color.HSBtoRGB(getHFromCursor(), 1, 1);
        ((IGuiGraphics) graphics).chatnotify$fillGradientHorizontal(
                svL, svT, svR, svB, rgb & 0xFFFFFF, (0xFF << 24) | rgb);

        // SV field black fill; transparent top to solid bottom
        graphics.fillGradient(svL, svT, svR, svB, 0x0, 0xFF000000);

        // SV field crosshair horizontal
        // Keep within outline
        int limitSvCursorY = top + Math.min(svCursorY, svFieldY + svFieldHeight - CURSOR);
        graphics.fill(svL, limitSvCursorY, svR, limitSvCursorY + CURSOR, GUI_LIGHT);

        // SV field crosshair vertical
        // Keep within outline
        int limitSvCursorX = left + Math.min(svCursorX, svFieldX + svFieldWidth - CURSOR);
        graphics.fill(limitSvCursorX, svT, limitSvCursorX + CURSOR, svB, GUI_LIGHT);

        // H field outline
        graphics.fill(hL - OUTLINE, hT - OUTLINE, hR + OUTLINE, hB + OUTLINE, GUI_DARK);

        // H field gradient
        int start = hT;
        int end = start + hSecSize;
        for (int i = 0; i < ARGB_GRADIENT.length - 1; i++) {
            graphics.fillGradient(hL, start, hR, end, ARGB_GRADIENT[i], ARGB_GRADIENT[i + 1]);
            start = end;
            end += hSecSize;
        }

        // H field bar horizontal
        // Keep within outline
        int limitHCursorY = Math.min(hCursorY, hFieldY + hFieldHeight - CURSOR);
        graphics.fill(hL, top + limitHCursorY, hR, top + limitHCursorY + CURSOR, GUI_LIGHT);

        // New color indicator outline
        graphics.fill(ncL - OUTLINE, ncT - OUTLINE, ncR + OUTLINE, ncB + OUTLINE, GUI_LIGHT);

        // New color indicator
        rgb = Mth.hsvToRgb(hsv[0], hsv[1], hsv[2]);
        graphics.fill(ncL, ncT, ncR, ncB, (0xFF << 24) | rgb);

        // Old color indicator outline
        graphics.fill(ocL - OUTLINE, ocT - OUTLINE, ocR + OUTLINE, ocB + OUTLINE, GUI_LIGHT);

        // Old color indicator
        graphics.fill(ocL, ocT, ocR, ocB, (0xFF << 24) | oldColor);
    }
}
