package dev.terminalmc.chatnotify.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = {"net.minecraft.client.gui.components.MultilineTextField.StringView"})
public interface StringViewAccessor {
    @Accessor
    int getBeginIndex();

    @Accessor
    int getEndIndex();
}
