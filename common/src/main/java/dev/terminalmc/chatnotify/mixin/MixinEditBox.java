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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.terminalmc.chatnotify.gui.widget.field.TextField;
import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Allows {@link TextField} instances to accept the section sign character
 * (§, ASCII 167).
 *
 * <p>The main reason for this approach is to avoid having to fully
 * re-implement the {@link EditBox} methods in {@link TextField}.</p>
 */
@Mixin(EditBox.class)
public class MixinEditBox {
    @WrapOperation(
            method = "charTyped",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/StringUtil;isAllowedChatCharacter(C)Z"
            )
    )
    private boolean allowSectionSign(char c, Operation<Boolean> original) {
        if (((Object)this) instanceof TextField tf && tf.allowSectionSign) {
            return chatNotify$isAllowedChatCharacter(c);
        } else {
            return original.call(c);
        }
    }

    @WrapOperation(
            method = "insertText",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/util/StringUtil;filterText(Ljava/lang/String;)Ljava/lang/String;"
            )
    )
    private String allowSectionSign(String s, Operation<String> original) {
        if (((Object)this) instanceof TextField tf && tf.allowSectionSign) {
            StringBuilder b = new StringBuilder();
            for (char c : s.toCharArray()) {
                if (chatNotify$isAllowedChatCharacter(c)) {
                    b.append(c);
                }
            }
            return b.toString();
        } else {
            return original.call(s);
        }
    }

    @Unique
    private boolean chatNotify$isAllowedChatCharacter(char c) {
        return c >= ' ' && c != 127;
    }
}
