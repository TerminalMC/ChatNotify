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

package dev.terminalmc.chatnotify.mixin;

import com.mojang.serialization.Codec;
import dev.terminalmc.chatnotify.gui.widget.slider.ChatHeightSlider;
import net.minecraft.client.OptionInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Boosts the chat height slider.
 */
@Mixin(OptionInstance.class)
public class MixinOptionInstance {
    @Shadow
    @Final
    public Component caption;

    @Shadow
    @Final
    @Mutable
    private OptionInstance.ValueSet<Double> values;

    @Shadow
    @Final
    @Mutable
    private Codec<Double> codec;

    /**
     * Increases the maximum value of the chat height focused slider.
     */
    @Inject(at = @At("RETURN"), method = "<init>*")
    private void init(CallbackInfo ci) {
        ComponentContents content = this.caption.getContents();
        if (!(content instanceof TranslatableContents)) return;

        String key = ((TranslatableContents) content).getKey();
        if (!key.equals("options.chat.height.focused")) return;

        values = ChatHeightSlider.INSTANCE;
        codec = values.codec();
    }
}
