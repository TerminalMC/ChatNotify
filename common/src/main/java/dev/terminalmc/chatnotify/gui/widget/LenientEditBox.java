package dev.terminalmc.chatnotify.gui.widget;

import dev.terminalmc.chatnotify.mixin.MixinEditBox;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/**
 * Wrapper for {@link EditBox}, used to flag the widget for {@link MixinEditBox}
 */
public class LenientEditBox extends EditBox {
    public LenientEditBox(Font font, int x, int y, int width, int height, Component msg) {
        super(font, x, y, width, height, msg);
    }
}
