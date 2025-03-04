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

import com.mojang.blaze3d.platform.InputConstants;
import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.Notification;
import dev.terminalmc.chatnotify.config.Trigger;
import dev.terminalmc.chatnotify.util.ColorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static dev.terminalmc.chatnotify.util.Localization.localized;

/**
 * A custom {@link EditBox} which supports click-dragging to select text,
 * double-clicking to select words, triple-clicking to select all, and content
 * validation with warning text color and tooltip.
 */
@SuppressWarnings("UnusedReturnValue")
public class TextField extends EditBox {
    public static final long CLICK_CHAIN_TIME = 250L;
    public static final int TEXT_COLOR_DEFAULT = 0xE0E0E0;
    public static final int TEXT_COLOR_ERROR = 0xFF5555;
    public static final int TEXT_COLOR_HINT = 0x555555;
    public static final int TEXT_COLOR_PREVIEW = 0xAAAAAA;

    private final Font font;

    // Validation
    public final List<@NotNull Validator> validators = new ArrayList<>();
    public boolean lenient = true;
    private int normalTextColor = TEXT_COLOR_DEFAULT;
    private @Nullable Tooltip normalTooltip;
    private @Nullable Tooltip errorTooltip;

    // Undo-redo history
    private final List<String> history = new ArrayList<>();
    private int historyIndex = -1;

    // Click-drag selection
    private double dragOriginX;
    private int dragOriginPos;

    // Double and triple-click selection
    private long lastClickTime;
    private int chainedClicks;

    public TextField(int x, int y, int width, int height) {
        this(Minecraft.getInstance().font, x, y, width, height, Component.empty(), null);
    }

    public TextField(int x, int y, int width, int height, @Nullable Validator validator) {
        this(Minecraft.getInstance().font, x, y, width, height, Component.empty(), validator);
    }

    public TextField(Font font, int x, int y, int width, int height, Component msg,
                     @Nullable Validator validator) {
        super(font, x, y, width, height, msg);
        this.font = font;
        if (validator != null) {
            this.validators.add(validator);
        }
    }

    public TextField withValidator(@NotNull Validator validator) {
        this.validators.add(validator);
        return this;
    }

    public TextField regexValidator() {
        this.validators.add(new Validator.Regex());
        return this;
    }

    public TextField hexColorValidator() {
        this.validators.add(new Validator.HexColor());
        return this;
    }

    public TextField soundValidator() {
        this.validators.add(new Validator.Sound());
        return this;
    }

    public TextField posIntValidator() {
        this.validators.add(new Validator.PosInt());
        return this;
    }

    public TextField strict() {
        this.lenient = false;
        return this;
    }

    @SuppressWarnings("unused")
    public TextField lenient() {
        this.lenient = true;
        return this;
    }

    @Override
    public void setResponder(@NotNull Consumer<String> responder) {
        super.setResponder((str) -> {
            if (validate(str) || lenient) {
                updateHistory(str);
                responder.accept(str);
            }
        });
    }

    private boolean validate(String str) {
        for (Validator v : validators) {
            Optional<Component> error = v.validate(str);
            if (error.isPresent()) {
                errorTooltip = Tooltip.create(error.get());
                super.setTooltip(errorTooltip);
                super.setTextColor(TEXT_COLOR_ERROR);
                return false;
            }
        }
        errorTooltip = null;
        super.setTextColor(normalTextColor);
        super.setTooltip(normalTooltip);
        return true;
    }

    @Override
    public void setHint(@NotNull Component hint) {
        super.setHint(hint.copy().withColor(TEXT_COLOR_HINT));
    }

    @Override
    public void setTooltip(@Nullable Tooltip tooltip) {
        normalTooltip = tooltip;
        if (errorTooltip == null) {
            super.setTooltip(tooltip);
        }
    }

    @Override
    public void setTextColor(int color) {
        normalTextColor = color;
        if (errorTooltip == null) {
            super.setTextColor(color);
        }
    }

    // Chained clicks and click-drag

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
                        if (pos < 0) {
                            start = 0;
                        } else if (pos >= getValue().length()
                                || getValue().charAt(pos) == ' '
                                || (pos > 0 && getValue().charAt(pos - 1) != ' ')) {
                            start = getWordPosition(-1);
                        }
                        int end = getWordPosition(1);
                        moveCursorTo(start, false);
                        moveCursorTo(end, true);
                    }
                    case 2, 3 -> {
                        // triple-click: select all
                        // duplicated for quadruple to inhibit overshoot
                        moveCursorToEnd(false);
                        setHighlightPos(0);
                    }
                    case 4 -> {
                        // quintuple-click: reset chain and deselect all
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

    // Undo-redo history

    private void updateHistory(String str) {
        if (historyIndex == -1 || !history.get(historyIndex).equals(str)) {
            if (historyIndex < history.size() - 1) {
                // Remove old history before writing new
                for (int i = history.size() - 1; i > historyIndex; i--) {
                    history.removeLast();
                }
            }
            history.add(str);
            historyIndex++;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!super.keyPressed(keyCode, scanCode, modifiers)) {
            if (isUndo(keyCode)) {
                undo();
                return true;
            }
            else if (isRedo(keyCode)) {
                redo();
                return true;
            }
            return false;
        }
        return true;
    }

    private void undo() {
        if (historyIndex > 0) {
            setValue(history.get(--historyIndex));
        }
    }

    private void redo() {
        if (historyIndex < history.size() - 1) {
            setValue(history.get(++historyIndex));
        }
    }

    // Validator

    @FunctionalInterface
    public interface Validator {
        Optional<Component> validate(String str);

        // Implementations

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
                    return Optional.of(localized("ui", "field.error.color")
                            .withStyle(ChatFormatting.RED));
                }
            }
        }

        class Sound implements Validator {
            private final Set<String> sounds = new HashSet<>(Minecraft.getInstance()
                    .getSoundManager().getAvailableSounds().stream()
                    .map(ResourceLocation::toString).toList());

            @Override
            public Optional<Component> validate(String str) {
                if (sounds.contains(str)
                        || (!str.contains(":") && sounds.contains(("minecraft:" + str)))) {
                    return Optional.empty();
                } else {
                    return Optional.of(localized("ui", "field.error.sound")
                            .withStyle(ChatFormatting.RED));
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
                    return Optional.of(localized("ui", "field.error.pos_int")
                            .withStyle(ChatFormatting.RED));
                }
            }
        }

        class InputKey implements Validator {
            List<String> keys;

            public InputKey(List<String> keys) {
                this.keys = keys;
            }

            @Override
            public Optional<Component> validate(String str) {
                if (keys.contains(str)) {
                    return Optional.empty();
                } else {
                    return Optional.of(localized("ui", "field.error.input_key")
                            .withStyle(ChatFormatting.RED));
                }
            }
        }

        class UniqueTrigger implements Validator {
            final Notification notif;
            final Trigger trigger;
            final Type type;

            private enum Type {
                MAIN,
                INCLUSION,
                EXCLUSION
            }

            public UniqueTrigger(Notification notif, Trigger trigger) {
                this(notif, trigger, Type.MAIN);
            }

            private UniqueTrigger(Notification notif, Trigger trigger, Type type) {
                this.notif = notif;
                this.trigger = trigger;
                this.type = type;
            }

            public static UniqueTrigger inclusion(Notification notif, Trigger trigger) {
                return new UniqueTrigger(notif, trigger, Type.INCLUSION);
            }

            public static UniqueTrigger exclusion(Notification notif, Trigger trigger) {
                return new UniqueTrigger(notif, trigger, Type.EXCLUSION);
            }

            @Override
            public Optional<Component> validate(String str) {
                if (str.isBlank()) return Optional.empty();
                MutableComponent err = Component.empty().withStyle(ChatFormatting.RED);

                boolean hasErr = checkTriggers(err, false, notif.triggers, str, "");
                if (notif.inclusionEnabled) {
                    hasErr = checkTriggers(err, hasErr, notif.inclusionTriggers, str, ".inclusion") || hasErr;
                }
                if (notif.exclusionEnabled) {
                    hasErr = checkTriggers(err, hasErr, notif.exclusionTriggers, str, ".exclusion") || hasErr;
                }

                // Only check other notifications if inclusion/exclusion are
                // not in use, too complex to check those.
                if (
                        type == Type.MAIN
                                && (!notif.inclusionEnabled || notif.inclusionTriggers.isEmpty())
                                && (!notif.exclusionEnabled || notif.exclusionTriggers.isEmpty())
                ) {
                    hasErr = checkOtherNotifs(err, hasErr, str);
                }

                return hasErr ? Optional.of(err) : Optional.empty();
            }

            private boolean checkOtherNotifs(MutableComponent err, boolean hasErr, String str) {
                int i = 0;
                for (Notification n : Config.get().getNotifs()) {
                    i++; // 1-indexed for users
                    // Only check other notifications if inclusion/exclusion are
                    // not in use, too complex to check those.
                    if (
                            n != notif
                                    && n.enabled
                                    && (!n.inclusionEnabled || n.inclusionTriggers.isEmpty())
                                    && (!n.exclusionEnabled || n.exclusionTriggers.isEmpty())
                    ) {
                        int j = 0;
                        for (Trigger t : n.triggers) {
                            j++;
                            if (t.type == trigger.type && t.string.equals(str)) {
                                if (hasErr) err.append("\n");
                                err.append(localized(
                                        "ui","field.error.trigger.duplicate",
                                        Component.literal(String.valueOf(j))
                                                .withStyle(ChatFormatting.GOLD),
                                        Component.literal(String.valueOf(i))
                                                .withStyle(ChatFormatting.GOLD)));
                                return true;
                            }
                        }
                    }
                }
                return hasErr;
            }

            private boolean checkTriggers(MutableComponent err, boolean hasErr,
                                          List<Trigger> triggers, String str, String errKey) {
                int i = 0;
                for (Trigger t : triggers) {
                    i++; // 1-indexed for users
                    if (!t.equals(trigger) && t.type == trigger.type && t.string.equals(str)) {
                        if (hasErr) err.append("\n");
                        err.append(localized("ui", "field.error.trigger.duplicate.here" + errKey,
                                Component.literal(String.valueOf(i))
                                        .withStyle(ChatFormatting.GOLD)));
                        return true;
                    }
                }
                return hasErr;
            }
        }
    }

    // Utility methods

    public static boolean isUndo(int keyCode) {
        return keyCode == InputConstants.KEY_Z
                && Screen.hasControlDown()
                && !Screen.hasShiftDown()
                && !Screen.hasAltDown();
    }

    public static boolean isRedo(int keyCode) {
        return keyCode == InputConstants.KEY_Y
                && Screen.hasControlDown()
                && !Screen.hasShiftDown()
                && !Screen.hasAltDown();
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
