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
import dev.terminalmc.chatnotify.gui.widget.ExpandingList;
import dev.terminalmc.chatnotify.gui.widget.OverlayWidget;
import dev.terminalmc.chatnotify.util.Unicode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An overlay-capable single-line text field with confirmation and cancellation buttons, and a
 * responsive tab-navigable dropdown list of suggestion strings.
 */
public class DropdownTextField extends OverlayWidget {

    public static final int MIN_WIDTH = 80;
    public static final int MIN_HEIGHT = 40;
    public static final int MAX_WIDTH = 500;
    public static final int MAX_HEIGHT = 800;

    private final Supplier<String> supplier;
    private final Consumer<String> consumer;

    private final Collection<String> dropdownValues;
    private Function<String, DropdownWidget> dropWidgetProvider;

    private Button cancelButton;
    private Button confirmButton;
    private TextField textField;
    private ExpandingList dropdown;
    private boolean suppressUpdate;
    private @Nullable String oldVal = null;

    public DropdownTextField(
            int x,
            int y,
            int width,
            int height,
            Component msg,
            Supplier<String> supplier,
            Consumer<String> consumer,
            Consumer<OverlayWidget> close,
            Collection<String> dropdownValues
    ) {
        super(x, y, width, height, false, msg, close);
        this.supplier = supplier;
        this.consumer = consumer;
        this.dropdownValues = dropdownValues;
        this.dropWidgetProvider = this::createDefaultDropWidget;
        init();
    }

    protected void init() {
        Minecraft mc = Minecraft.getInstance();

        int x = getX();
        int y = getY();

        int widgetHeight = 20;
        int verticalSpace = 1;
        int buttonWidth = 20;
        int textFieldWidth = width - (2 * widgetHeight);

        cancelButton = Button.builder(
                        Component.literal(Unicode.CROSS.str).withStyle(ChatFormatting.RED),
                        (button) -> onClose()
                )
                .pos(x + width - (buttonWidth * 2), y)
                .size(buttonWidth, widgetHeight)
                .build();
        confirmButton = Button.builder(
                        Component.literal(Unicode.CHECK.str).withStyle(ChatFormatting.GREEN),
                        (button) -> {
                            consumer.accept(textField.getValue());
                            onClose();
                        }
                )
                .pos(x + width - buttonWidth, y)
                .size(buttonWidth, widgetHeight)
                .build();
        textField = new TextField(x, y, textFieldWidth, widgetHeight);
        dropdown = new ExpandingList(
                x,
                y + widgetHeight + verticalSpace,
                width,
                height - widgetHeight - verticalSpace,
                mc.font.lineHeight,
                mc.font.lineHeight,
                2
        );

        textField.setMaxLength(240);
        textField.setResponder(this::valueResponder);
        textField.setValue(oldVal == null ? supplier.get() : oldVal);
        textField.setFocused(true);
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

    private DropdownWidget createDefaultDropWidget(String str) {
        return new DropdownWidget(
                textField.getX(),
                textField.getY() + textField.getHeight(),
                textField.getWidth(),
                Minecraft.getInstance().font.lineHeight + 2,
                Component.literal(str),
                Minecraft.getInstance().font,
                this::tabComplete
        );
    }

    public DropdownTextField withSoundDropType() {
        dropWidgetProvider = this::createSoundDropWidget;
        init();
        return this;
    }

    private SoundDropdownWidget createSoundDropWidget(String str) {
        return new SoundDropdownWidget(
                textField.getX(),
                textField.getY() + textField.getHeight(),
                textField.getWidth(),
                Minecraft.getInstance().font.lineHeight + 2,
                Component.literal(str),
                Minecraft.getInstance().font,
                this::tabComplete
        );
    }

    private void tabComplete(String str) {
        suppressUpdate = true;
        textField.setValue(str);
        suppressUpdate = false;
    }

    private void valueResponder(String str) {
        oldVal = str;
        if (!suppressUpdate) {
            dropdown.highlightIndex = -1;
            dropdown.clearWidgets();
            dropdown.setScrollAmount(0);
            for (String suggestion : dropdownValues) {
                if (suggestion.contains(str) && !suggestion.equals(str)) {
                    StringWidget widget = dropWidgetProvider.apply(suggestion);
                    dropdown.addWidget(widget);
                }
            }
        }
    }

    @Override
    public boolean keyPressed(@NotNull KeyEvent event) {
        // Only textField can handle key presses
        if (textField.isFocused()) {
            if (!dropdown.isEmpty()) {
                if (event.key() == InputConstants.KEY_TAB) {
                    if (event.hasShiftDown()) {
                        tabUp();
                    } else {
                        tabDown();
                    }
                    return true;
                } else if (event.key() == InputConstants.KEY_UP) {
                    tabUp();
                    return true;
                } else if (event.key() == InputConstants.KEY_DOWN) {
                    tabDown();
                    return true;
                }
            }
            return textField.keyPressed(event);
        }
        return false;
    }

    private void tabUp() {
        if (--dropdown.highlightIndex < 0)
            dropdown.highlightIndex = dropdown.size() - 1;
        dropdown.ensureVisible(dropdown.highlightIndex);
        this.tabComplete(dropdown.get(dropdown.highlightIndex).getMessage().getString());
    }

    private void tabDown() {
        if (++dropdown.highlightIndex >= dropdown.size())
            dropdown.highlightIndex = 0;
        dropdown.ensureVisible(dropdown.highlightIndex);
        this.tabComplete(dropdown.get(dropdown.highlightIndex).getMessage().getString());
    }

    @Override
    public boolean charTyped(@NotNull CharacterEvent event) {
        if (textField.isFocused()) {
            return textField.charTyped(event);
        }
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (mouseOnWidget(this, event.x(), event.y())) {
            if (mouseOnWidget(textField, event.x(), event.y())) {
                if (!textField.isFocused()) {
                    textField.setFocused(true);
                } else {
                    textField.mouseClicked(event, doubleClick);
                }
            } else if (textField.isFocused() && mouseOnWidget(dropdown, event.x(), event.y())) {
                dropdown.mouseClicked(event, doubleClick);
            } else {
                textField.setFocused(false);
                if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) {
                    if (mouseOnWidget(cancelButton, event.x(), event.y())) {
                        cancelButton.mouseClicked(event, doubleClick);
                    } else if (mouseOnWidget(confirmButton, event.x(), event.y())) {
                        confirmButton.mouseClicked(event, doubleClick);
                    }
                }
            }
        } else {
            cancelButton.onPress(event);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(@NotNull MouseButtonEvent event, double deltaX, double deltaY) {
        if (textField.isFocused() && mouseOnWidget(textField, event.x(), event.y())) {
            return textField.mouseDragged(event, deltaX, deltaY);
        } else {
            dropdown.setFocused(null);
            return dropdown.mouseDragged(event, deltaX, deltaY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (textField.isFocused() && mouseOnWidget(dropdown, mouseX, mouseY)) {
            return dropdown.mouseScrolled(mouseX, mouseY, deltaX, deltaY);
        }
        return false;
    }

    private boolean mouseOnWidget(AbstractWidget widget, double mouseX, double mouseY) {
        return ((widget.getX() <= mouseX && mouseX < widget.getX() + widget.getWidth())
                && (widget.getY() <= mouseY && mouseY < widget.getY() + widget.getHeight()));
    }

    @Override
    protected void renderWidget(
            @NotNull GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        textField.renderWidget(graphics, mouseX, mouseY, delta);
        cancelButton.render(graphics, mouseX, mouseY, delta);
        confirmButton.render(graphics, mouseX, mouseY, delta);

        if (textField.isFocused() && !dropdown.isEmpty()) {
            dropdown.renderWidget(graphics, mouseX, mouseY, delta);
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        if (SoundDropdownWidget.lastSound != null) {
            Minecraft.getInstance().getSoundManager().stop(SoundDropdownWidget.lastSound);
        }
    }

    // Suggestion dropdown list element

    public static class DropdownWidget extends StringWidget {

        private final Consumer<String> consumer;

        private DropdownWidget(
                int x,
                int y,
                int width,
                int height,
                Component msg,
                Font font,
                Consumer<String> consumer
        ) {
            super(x, y, width, height, msg, font);
            this.active = true;
            this.consumer = consumer;
        }

        public static DropdownWidget create(
                int x,
                int y,
                int width,
                int height,
                Component msg,
                Font font,
                Consumer<String> dest
        ) {
            return new DropdownWidget(x, y, width, height, msg, font, dest);
        }

        @Override
        public void onClick(@NotNull MouseButtonEvent event, boolean doubleClick) {
            consumer.accept(getMessage().getString());
        }
    }

    public static class SoundDropdownWidget extends DropdownWidget {

        private static @Nullable SoundInstance lastSound;

        private SoundDropdownWidget(
                int x,
                int y,
                int width,
                int height,
                Component msg,
                Font font,
                Consumer<String> consumer
        ) {
            super(x, y, width, height, msg, font, consumer);
        }

        public static SoundDropdownWidget create(
                int x,
                int y,
                int width,
                int height,
                Component msg,
                Font font,
                Consumer<String> consumer
        ) {
            return new SoundDropdownWidget(x, y, width, height, msg, font, consumer);
        }

        @Override
        public void playDownSound(@NotNull SoundManager soundManager) {
            if (lastSound != null)
                soundManager.stop(lastSound);
            lastSound = new SimpleSoundInstance(
                    Identifier.parse(getMessage().getString()),
                    SoundSource.MASTER,
                    1.0F,
                    1.0F,
                    SoundInstance.createUnseededRandom(),
                    false,
                    0,
                    SoundInstance.Attenuation.NONE,
                    0,
                    0,
                    0,
                    true
            );
            soundManager.play(lastSound);
        }
    }
}
