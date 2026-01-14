/*
 * Copyright 2026 TerminalMC
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

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.Config.CommonDetectionMode;
import dev.terminalmc.chatnotify.util.text.MessageUtil;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;

@SuppressWarnings("JavadocReference")
@Mixin(
        value = Gui.class,
        priority = 792
)
public class GuiMixin {

    /**
     * HUD-level interceptor for action bar messages.
     *
     * @see ClientPacketListenerMixin#wrapSetOverlayMessage
     */
    @WrapMethod(method = "setOverlayMessage")
    private void wrapSetOverlayMessage(
            Component message,
            boolean animateColor,
            Operation<Void> original
    ) {
        if (Config.get().actionBarDetectionMode.equals(CommonDetectionMode.HUD)) {
            message = MessageUtil.processMessage(message);
            if (message != null)
                original.call(message, animateColor);
        } else {
            original.call(message, animateColor);
        }
    }

    /**
     * HUD-level interceptor for title messages.
     *
     * @see ClientPacketListenerMixin#wrapSetTitle
     */
    @WrapMethod(method = "setTitle")
    private void wrapSetTitle(Component message, Operation<Void> original) {
        if (Config.get().titleDetectionMode.equals(CommonDetectionMode.HUD)) {
            message = MessageUtil.processMessage(message);
            if (message != null)
                original.call(message);
        } else {
            original.call(message);
        }
    }

    /**
     * HUD-level interceptor for subtitle messages.
     *
     * @see ClientPacketListenerMixin#wrapSetSubtitle
     */
    @WrapMethod(method = "setSubtitle")
    private void wrapSetSubtitle(Component message, Operation<Void> original) {
        if (Config.get().subtitleDetectionMode.equals(CommonDetectionMode.HUD)) {
            message = MessageUtil.processMessage(message);
            if (message != null)
                original.call(message);
        } else {
            original.call(message);
        }
    }
}
