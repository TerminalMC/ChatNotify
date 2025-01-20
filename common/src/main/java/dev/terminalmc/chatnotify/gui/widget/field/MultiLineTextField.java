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

package dev.terminalmc.chatnotify.gui.widget.field;

import dev.terminalmc.chatnotify.mixin.accessor.MultiLineEditBoxAccessor;
import dev.terminalmc.chatnotify.mixin.accessor.MultilineTextFieldAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A custom {@link MultiLineEditBox} which supports resizing, double-clicking
 * to select all text, and when combined with 
 * {@link dev.terminalmc.chatnotify.mixin.MixinMultiLineEditBox}, supports
 * content validation with text color and tooltip change.
 */
public class MultiLineTextField extends MultiLineEditBox {
    public static final int NORMAL_COLOR = 0xE0E0E0;
    public static final int ERROR_COLOR = 0xFF5555;
    public static final long DOUBLE_CLICK_TIME = 250L;
    private TextField.Validator validator;
    public boolean lenient = false;
    private int defaultTextColor;
    private Tooltip defaultTooltip;
    private int textColor;
    private long lastClickTime;
    
    public MultiLineTextField(Font font, int x, int y, int width, int height, 
                              Component placeholder, Component message) {
        super(font, x, y, width, height, placeholder, message);
        this.validator = new Validator.Default();
        this.defaultTextColor = NORMAL_COLOR;
        this.textColor = defaultTextColor;
    }

    public MultiLineTextField regexValidator() {
        validator = new Validator.Regex();
        return this;
    }

    @Override
    public void setValueListener(@NotNull Consumer<String> responder) {
        super.setValueListener((str) -> {
            if (valid(str) || lenient) responder.accept(str);
        });
    }

    private boolean valid(String str) {
        Optional<Component> error = validator.validate(str);
        if (error.isPresent()) {
            super.setTooltip(Tooltip.create(error.get()));
            this.textColor = ERROR_COLOR;
            return false;
        } else {
            this.textColor = defaultTextColor;
            super.setTooltip(defaultTooltip);
            return true;
        }
    }

    @Override
    public void setTooltip(@Nullable Tooltip tooltip) {
        defaultTooltip = tooltip;
        super.setTooltip(tooltip);
    }

    @Override
    public void setWidth(int width) {
        super.setWidth(width);
        ((MultilineTextFieldAccessor)((MultiLineEditBoxAccessor)this)
                .getTextField()).setWidth(width);
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int color) {
        if (textColor == defaultTextColor) textColor = color;
        defaultTextColor = color;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            // Double-click to select all
            long time = System.currentTimeMillis();
            if (lastClickTime + DOUBLE_CLICK_TIME > time) {
                ((MultilineTextFieldAccessor)((MultiLineEditBoxAccessor)this)
                        .getTextField()).setCursor(this.getValue().length());
                ((MultilineTextFieldAccessor)((MultiLineEditBoxAccessor)this)
                        .getTextField()).setSelectCursor(0);
            }
            lastClickTime = time;
            return true;
        }
        return false;
    }

    public interface Validator {
        Optional<Component> validate(String str);

        // Implementations

        class Default implements TextField.Validator {
            @Override
            public Optional<Component> validate(String str) {
                return Optional.empty();
            }
        }

        class Regex implements TextField.Validator {
            @Override
            public Optional<Component> validate(String str) {
                try {
                    Pattern.compile(str);
                    return Optional.empty();
                } catch (PatternSyntaxException e) {
                    return Optional.of(Component.literal(TextField.fixRegexMessage(e.getMessage()))
                            .withStyle(ChatFormatting.RED));
                }
            }
        }
    }
}
