package dev.terminalmc.chatnotify.gui.widget.field;

import com.mojang.blaze3d.platform.InputConstants;
import dev.terminalmc.chatnotify.gui.widget.OverlayWidget;
import dev.terminalmc.chatnotify.gui.widget.list.ExpandingList;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An overlay-capable single-line text field with confirmation and cancellation
 * buttons, and a responsive tab-navigable dropdown list of suggestion strings.
 */
public class DropdownTextField extends OverlayWidget {
    public static final int MIN_WIDTH = 80;
    public static final int MIN_HEIGHT = 40;
    public static final int MAX_WIDTH = 500;
    public static final int MAX_HEIGHT = 800;

    private final Supplier<String> source;
    private final Consumer<String> dest;

    private final Collection<String> dropdownValues;
    private Function<String, DropdownWidget> dropWidgetProvider;

    private Button cancelButton;
    private Button confirmButton;
    private TextField textField;
    private ExpandingList dropdown;
    private boolean suppressUpdate;
    private @Nullable String oldVal = null;

    public DropdownTextField(int x, int y, int width, int height, Component msg,
                             Supplier<String> source, Consumer<String> dest,
                             Consumer<OverlayWidget> close, Collection<String> dropdownValues) {
        super(x, y, width, height, msg, close);
        this.source = source;
        this.dest = dest;
        this.dropdownValues = dropdownValues;
        this.dropWidgetProvider = this::createDefaultDropWidget;
        init();
    }

    protected void init() {
        int x = getX();
        int y = getY();

        int widgetHeight = 20;
        int verticalSpace = 1;
        int buttonWidth = widgetHeight;
        int textFieldWidth = width - (2 * widgetHeight);

        cancelButton = Button.builder(Component.literal("\u274C").withStyle(ChatFormatting.RED),
                        (button) -> onClose())
                .pos(x + width - (buttonWidth * 2), y)
                .size(buttonWidth, widgetHeight)
                .build();
        confirmButton = Button.builder(Component.literal("\u2714").withStyle(ChatFormatting.GREEN),
                (button) -> {
                    dest.accept(textField.getValue());
                    onClose();
                })
                .pos(x + width - buttonWidth, y)
                .size(buttonWidth, widgetHeight)
                .build();
        textField = new TextField(x, y, textFieldWidth, widgetHeight);
        dropdown = new ExpandingList(x, y + widgetHeight + verticalSpace,
                width, height - widgetHeight - verticalSpace,
                Minecraft.getInstance().font.lineHeight,
                Minecraft.getInstance().font.lineHeight, 2);

        textField.setMaxLength(240);
        textField.setResponder(this::valueResponder);
        textField.setValue(oldVal == null ? source.get() : oldVal);
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

    public DropdownTextField withDefaultDropType() {
        dropWidgetProvider = this::createDefaultDropWidget;
        return this;
    }

    private DropdownWidget createDefaultDropWidget(String str) {
        return new DropdownWidget(textField.getX(), textField.getY() + textField.getHeight(),
                textField.getWidth(), Minecraft.getInstance().font.lineHeight + 2,
                Component.literal(str), Minecraft.getInstance().font, this::tabComplete);
    }

    public DropdownTextField withSoundDropType() {
        dropWidgetProvider = this::createSoundDropWidget;
        return this;
    }

    private SoundDropdownWidget createSoundDropWidget(String str) {
        return new SoundDropdownWidget(textField.getX(), textField.getY() + textField.getHeight(),
                textField.getWidth(), Minecraft.getInstance().font.lineHeight + 2,
                Component.literal(str), Minecraft.getInstance().font, this::tabComplete);
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
                    widget.alignLeft();
                    dropdown.addWidget(widget);
                }
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Only textField can handle key presses
        if (textField.isFocused()) {
            if (!dropdown.isEmpty()) {
                if (keyCode == InputConstants.KEY_TAB) {
                    if (Screen.hasShiftDown()) {
                        tabUp();
                    } else {
                        tabDown();
                    }
                    return true;
                } else if (keyCode == InputConstants.KEY_UP) {
                    tabUp();
                    return true;
                } else if (keyCode == InputConstants.KEY_DOWN) {
                    tabDown();
                    return true;
                }
            }
            return textField.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    private void tabUp() {
        if (--dropdown.highlightIndex < 0) dropdown.highlightIndex = dropdown.size() - 1;
        dropdown.ensureVisible(dropdown.highlightIndex);
        this.tabComplete(dropdown.get(dropdown.highlightIndex).getMessage().getString());
    }

    private void tabDown() {
        if (++dropdown.highlightIndex >= dropdown.size()) dropdown.highlightIndex = 0;
        dropdown.ensureVisible(dropdown.highlightIndex);
        this.tabComplete(dropdown.get(dropdown.highlightIndex).getMessage().getString());
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (textField.isFocused()) {
            return textField.charTyped(chr, modifiers);
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseOnWidget(this, mouseX, mouseY)) {
            if (mouseOnWidget(textField, mouseX, mouseY)) {
                if (!textField.isFocused()) {
                    textField.setFocused(true);
                } else {
                    textField.mouseClicked(mouseX, mouseY, button);
                }
            } else if (textField.isFocused() && mouseOnWidget(dropdown, mouseX, mouseY)) {
                dropdown.mouseClicked(mouseX, mouseY, button);
            } else {
                textField.setFocused(false);
                if (button == InputConstants.MOUSE_BUTTON_LEFT) {
                    if (mouseOnWidget(cancelButton, mouseX, mouseY)) {
                        cancelButton.mouseClicked(mouseX, mouseY, button);
                    } else if (mouseOnWidget(confirmButton, mouseX, mouseY)) {
                        confirmButton.mouseClicked(mouseX, mouseY, button);
                    }
                }
            }
        } else {
            cancelButton.onPress();
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (textField.isFocused()) {
            dropdown.setFocused(null);
            return dropdown.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        return false;
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
    protected void renderWidget(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        textField.renderWidget(graphics, mouseX, mouseY, delta);
        cancelButton.render(graphics, mouseX, mouseY, delta);
        confirmButton.render(graphics, mouseX, mouseY, delta);

        if (textField.isFocused() && !dropdown.isEmpty()) {
            dropdown.renderWidget(graphics, mouseX, mouseY, delta);
        }
    }

    // Suggestion dropdown list element

    public static class DropdownWidget extends StringWidget {
        private final Consumer<String> dest;

        private DropdownWidget(int x, int y, int width, int height, Component msg,
                               Font font, Consumer<String> dest) {
            super(x, y, width, height, msg, font);
            this.active = true;
            this.dest = dest;
        }

        public static DropdownWidget create(int x, int y, int width, int height, Component msg,
                                            Font font, Consumer<String> dest) {
            return new DropdownWidget(x, y, width, height, msg, font, dest);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            dest.accept(getMessage().getString());
        }
    }

    public static class SoundDropdownWidget extends DropdownWidget {
        private SoundDropdownWidget(int x, int y, int width, int height, Component msg,
                                    Font font, Consumer<String> dest) {
            super(x, y, width, height, msg, font, dest);
        }

        public static SoundDropdownWidget create(int x, int y, int width, int height, Component msg,
                                                 Font font, Consumer<String> dest) {
            return new SoundDropdownWidget(x, y, width, height, msg, font, dest);
        }

        @Override
        public void playDownSound(SoundManager soundManager) {
            soundManager.stop();
            soundManager.play(new SimpleSoundInstance(
                    ResourceLocation.parse(getMessage().getString()),
                    SoundSource.MASTER, 1.0F, 1.0F,
                    SoundInstance.createUnseededRandom(), false, 0,
                    SoundInstance.Attenuation.NONE, 0, 0, 0, true));
        }
    }
}
