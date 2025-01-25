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

import dev.terminalmc.chatnotify.util.ColorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static dev.terminalmc.chatnotify.util.Localization.localized;

/**
 * A custom {@link EditBox} which supports click-dragging to select text,
 * double-clicking to select all text, and content validation with text color 
 * and tooltip change.
 */
public class TextField extends EditBox {
    public static final long CLICK_CHAIN_TIME = 250L;
    
    private Validator validator;
    public boolean lenient = false;
    private int defaultTextColor;
    private Tooltip defaultTooltip;
    private final Font font;
    private double dragOriginX;
    private int dragOriginPos;
    
    private long lastClickTime;
    private int chainedClicks;

    public TextField(int x, int y, int width, int height) {
        this(Minecraft.getInstance().font, x, y, width, height, Component.empty(),
                (str) -> Optional.empty());
    }

    public TextField(Font font, int x, int y, int width, int height, Component msg,
                     Function<String, Optional<Component>> validator) {
        super(font, x, y, width, height, msg);
        this.font = font;
        this.validator = new Validator.Custom(validator);
        this.defaultTextColor = 0xE0E0E0;
    }

    public TextField setValidator(Function<String, Optional<Component>> validator) {
        this.validator = new Validator.Custom(validator);
        return this;
    }

    public TextField defaultValidator() {
        validator = new Validator.Default();
        return this;
    }

    public TextField regexValidator() {
        validator = new Validator.Regex();
        return this;
    }

    public TextField hexColorValidator() {
        validator = new Validator.HexColor();
        return this;
    }

    public TextField soundValidator() {
        validator = new Validator.Sound();
        return this;
    }

    public TextField posIntValidator() {
        validator = new Validator.PosInt();
        return this;
    }

    @Override
    public void setResponder(@NotNull Consumer<String> responder) {
        super.setResponder((str) -> {
            if (valid(str) || lenient) responder.accept(str);
        });
    }

    private boolean valid(String str) {
        Optional<Component> error = validator.validate(str);
        if (error.isPresent()) {
            super.setTooltip(Tooltip.create(error.get()));
            super.setTextColor(0xFF5555);
            return false;
        } else {
            super.setTextColor(defaultTextColor);
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
    public void setTextColor(int color) {
        defaultTextColor = color;
        super.setTextColor(color);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            long time = Util.getMillis();
            if (lastClickTime + CLICK_CHAIN_TIME > time) {
                switch (++chainedClicks) {
                    case 1 -> {
                        // double-click: select word
                        int pos = getCursorPosition();
                        int start = pos;
                        // If next char is space or previous char is not space, 
                        // go backwards to the start of the word.
                        if (getValue().charAt(pos) == ' ' || getValue().charAt(pos - 1) != ' ') {
                            start = getWordPosition(-1);
                        }
                        int end = getWordPosition(1);
                        moveCursorTo(start, false);
                        moveCursorTo(end, true);
                    }
                    case 2 -> {
                        // triple-click: select all
                        moveCursorToEnd(false);
                        setHighlightPos(0);
                    }
                    case 3 -> {
                        // quadruple-click: reset chain and deselect all
                        chainedClicks = 0;
                        setHighlightPos(getCursorPosition());
                    }
                }
            } else {
                chainedClicks = 0;
            }
            lastClickTime = time;
            
            // Reset drag origin
            dragOriginX = mouseX;
            dragOriginPos = getCursorPosition();
            
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button != 0) return false;
        String str = getValue();
        
        if (mouseX < dragOriginX) { // Dragging left
            String subLeft = str.substring(0, dragOriginPos);
            int offsetChars = font.plainSubstrByWidth(subLeft,
                    Mth.floor(dragOriginX - mouseX), true).length();
            moveCursorTo(dragOriginPos - offsetChars, true);
        }
        else { // Dragging right
            String subRight = str.substring(dragOriginPos);
            int offsetChars = font.plainSubstrByWidth(subRight,
                    Mth.floor(mouseX - dragOriginX), false).length();
            moveCursorTo(dragOriginPos + offsetChars, true);
        }
        
        return true;
    }

    public interface Validator {
        Optional<Component> validate(String str);

        // Implementations

        class Custom implements Validator {
            private final Function<String, Optional<Component>> validator;

            public Custom(Function<String, Optional<Component>> validator) {
                this.validator = validator;
            }

            @Override
            public Optional<Component> validate(String str) {
                return validator.apply(str);
            }
        }

        class Default implements Validator {
            @Override
            public Optional<Component> validate(String str) {
                return Optional.empty();
            }
        }

        class Regex implements Validator {
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

        class HexColor implements Validator {
            @Override
            public Optional<Component> validate(String str) {
                if (ColorUtil.parseColor(str) != null) {
                    return Optional.empty();
                } else {
                    return Optional.of(localized("option", "field.color.error"));
                }
            }
        }

        class Sound implements Validator {
            Set<String> sounds = new HashSet<>(Minecraft.getInstance().getSoundManager()
                    .getAvailableSounds().stream().map(ResourceLocation::toString).toList());

            @Override
            public Optional<Component> validate(String str) {
                if (sounds.contains(str) || (!str.contains(":") && sounds.contains(("minecraft:" + str)))) {
                    return Optional.empty();
                } else {
                    return Optional.of(localized("option", "field.sound.error"));
                }
            }
        }

        class PosInt implements Validator {
            @Override
            public Optional<Component> validate(String str) {
                try {
                    if (Integer.parseInt(str) < 0) throw new NumberFormatException();
                    return Optional.empty();
                } catch (NumberFormatException ignored) {
                    return Optional.of(localized("option", "field.posint.error"));
                }
            }
        }
    }

    /**
     * Adjusts {@link PatternSyntaxException} description messages for correct
     * display in tooltips.
     *
     * <p>Messages are intended for display using monospaced fonts, so the
     * cursor indicating the error position will usually be in the wrong place
     * when displayed using the Minecraft font. This method simply moves the
     * cursor to a new position as close as possible to the correct one.</p>
     *
     * <p>Also, messages contain carriage-return characters which don't play
     * well with Minecraft so this method removes them.</p>
     */
    public static String fixRegexMessage(String str) {
        // Remove carriage returns
        str = str.replaceAll("\\u000D", "");

        // If there is a cursor, fix its position
        if (str.endsWith("^")) {
            Matcher indexMatcher = Pattern.compile("near index (\\d+)\n").matcher(str);
            if (indexMatcher.find()) {
                Font font = Minecraft.getInstance().font;
                // Get the index that the cursor is pointing to
                int index = Integer.parseInt(indexMatcher.group(1));
                // Determine the cursor offset distance
                int startPos = indexMatcher.end();
                int cursorPos = startPos + index;
                int cursorOffset = font.width(str.substring(startPos, cursorPos));
                // Construct the new offset space
                char[] charArray = new char[cursorOffset / font.width(" ")];
                Arrays.fill(charArray, ' ');
                String newSpace = new String(charArray);

                Matcher cursorMatcher = Pattern.compile("\n( *\\^)$").matcher(str);
                if (cursorMatcher.find(cursorPos)) {
                    // Get the original cursor and its offset space
                    String cursorLine = cursorMatcher.group(1);
                    // Replace the old space with the new
                    str = str.replaceAll(Pattern.quote(cursorLine) + "$", newSpace + "^");
                }
            }
        }
        return str;
    }
}
