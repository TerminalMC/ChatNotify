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

package dev.terminalmc.chatnotify.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.authlib.GameProfile;
import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.util.MessageUtil;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.PlayerChatMessage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.time.Instant;

/**
 * Refer to {@link MixinChatComponent} for an overview of Minecraft's message
 * handling call stacks.
 */
@Mixin(value = ChatListener.class, priority = 792)
public class MixinChatListener {

    @WrapMethod(method = "handleDisguisedChatMessage")
    private void wrapHandleDisguisedChatMessage(Component message, ChatType.Bound boundChatType, 
                                                Operation<Void> original) {
        message = chatNotify$replaceMessage(message);
        if (message != null) original.call(message, boundChatType);
    }

    @WrapMethod(method = "handleSystemMessage")
    private void wrapHandleSystemMessage(Component message, boolean isOverlay, 
                                         Operation<Void> original) {
        message = chatNotify$replaceMessage(message);
        if (message != null) original.call(message, isOverlay);
    }

    // Unable to use handlePlayerChatMessage as that takes a PlayerChatMessage
    @WrapMethod(method = "showMessageToPlayer")
    private boolean wrapShowMessageToPlayer(ChatType.Bound boundChatType, 
                                            PlayerChatMessage chatMessage, Component message, 
                                            GameProfile gameProfile, boolean onlyShowSecureChat, 
                                            Instant timestamp, Operation<Boolean> original) {
        message = chatNotify$replaceMessage(message);
        if (message != null) return original.call(boundChatType, chatMessage, message, gameProfile, 
                onlyShowSecureChat, timestamp);
        return false;
    }

    @Unique
    private static @Nullable Component chatNotify$replaceMessage(Component message) {
        if (Config.get().detectionMode.equals(Config.DetectionMode.PACKET)) {
            return MessageUtil.processMessage(message);
        } else {
            return message;
        }
    }
}
