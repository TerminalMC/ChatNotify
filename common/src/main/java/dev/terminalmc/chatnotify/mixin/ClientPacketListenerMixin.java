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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.util.Pair;
import dev.terminalmc.chatnotify.ChatNotify;
import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.Config.CommonDetectionMode;
import dev.terminalmc.chatnotify.util.text.FormatUtil;
import dev.terminalmc.chatnotify.util.text.MessageUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
 * If an incoming message is the return of a message sent by the user,
 * ChatNotify must either;
 * a) Not process the message, if the config checkOwnMessages is FALSE, or
 * b) Remove the first occurrence of the user's name from the message before
 *    checking it for triggers (else every message sent by the user would
 *    trigger the username notification).
 *
 * Player-sent messages can be stripped of identifying data by mods or plugins,
 * or can be converted to server-sent messages, so it is not possible to
 * reliably determine the sender using message data.
 *
 * It is possible to make use of message sender data where it exists, as an
 * example refer to https://github.com/dzwdz/chat_heads. However, as ChatNotify
 * only needs to determine whether a message is sent by the user (not the more
 * general 'who sent this message?'), an alternate heuristic approach is used:
 *
 * 1. Mixins are used to store outgoing message and command strings in a list.
 * 2. If an incoming message string contains a string in the list, and the
 *    part of the string preceding the match contains a trigger string of the
 *    username notification, the message is identified as sent by the user,
 *    and the matched string removed from the list.
 * 3. When any outgoing message or command is recorded, all list entries older
 *    than 5 seconds are removed, as it can be assumed that those generated no
 *    matching return message.
 *
 * Note that some outgoing messages may have modifier prefixes such as ! or
 * /shout that cause them to behave differently (e.g. go to global rather than
 * party chat on a server), but will not appear in the return message.
 * Thus, before a message is stored, it is checked against the ChatNotify list
 * of prefixes (which can be edited by the user), and the first matching prefix
 * (if any) is cut from the message.
 */

@SuppressWarnings("JavadocReference")
@Mixin(
        value = ClientPacketListener.class,
        priority = 792
)
public class ClientPacketListenerMixin {

    /**
     * Packet-level interceptor for action bar messages.
     *
     * @see GuiMixin#wrapSetOverlayMessage
     */
    @WrapOperation(
            method = "setActionBarText",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;setOverlayMessage(Lnet/minecraft/network/chat/Component;Z)V"
            )
    )
    private void wrapSetOverlayMessage(
            Gui instance,
            Component message,
            boolean animateColor,
            Operation<Void> original
    ) {
        if (Config.get().actionBarDetectionMode.equals(CommonDetectionMode.PACKET)) {
            message = MessageUtil.processMessage(message);
            if (message != null)
                original.call(instance, message, animateColor);
        } else {
            original.call(instance, message, animateColor);
        }
    }

    /**
     * Packet-level interceptor for title messages.
     *
     * @see GuiMixin#wrapSetTitle
     */
    @WrapOperation(
            method = "setTitleText",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;setTitle(Lnet/minecraft/network/chat/Component;)V"
            )
    )
    private void wrapSetTitle(Gui instance, Component message, Operation<Void> original) {
        if (Config.get().titleDetectionMode.equals(CommonDetectionMode.PACKET)) {
            message = MessageUtil.processMessage(message);
            if (message != null)
                original.call(instance, message);
        } else {
            original.call(instance, message);
        }
    }

    /**
     * Packet-level interceptor for subtitle messages.
     *
     * @see GuiMixin#wrapSetSubtitle
     */
    @WrapOperation(
            method = "setSubtitleText",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;setSubtitle(Lnet/minecraft/network/chat/Component;)V"
            )
    )
    private void wrapSetSubtitle(Gui instance, Component message, Operation<Void> original) {
        if (Config.get().subtitleDetectionMode.equals(CommonDetectionMode.PACKET)) {
            message = MessageUtil.processMessage(message);
            if (message != null)
                original.call(instance, message);
        } else {
            original.call(instance, message);
        }
    }

    /**
     * Update profileName.
     */
    @Inject(
            method = "handleLogin",
            at = @At("TAIL")
    )
    private void getProfileName(ClientboundLoginPacket packet, CallbackInfo ci) {
        if (Minecraft.getInstance().player == null)
            return;
        String name = FormatUtil.stripCodes(Minecraft.getInstance().player.getName().getString());
        Config.get().setProfileName(name);
        Config.get().setDisplayName(name);
    }

    /**
     * Update displayName.
     * <p>
     * This is a proactive-update approach. A possible reactive-update approach would be to use the
     * following access on each message check.
     * <p>
     * {@code String displayname = minecraft.getConnection().getPlayerInfo(
     * minecraft.player.getUUID()).getProfile().getName();}
     */
    @Inject(
            method = "applyPlayerInfoUpdate",
            at = @At("TAIL")
    )
    private void getDisplayName(
            ClientboundPlayerInfoUpdatePacket.Action action,
            ClientboundPlayerInfoUpdatePacket.Entry entry,
            PlayerInfo playerInfo,
            CallbackInfo ci
    ) {
        if (Minecraft.getInstance().player == null)
            return;
        if (action.equals(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME)
                && playerInfo.getProfile().getId().equals(Minecraft.getInstance().player.getUUID())
                && entry.displayName() != null) {
            Config.get().setDisplayName(FormatUtil.stripCodes(entry.displayName().getString()));
        }
    }

    // Outgoing chat message and command storage

    @Inject(
            method = "sendChat",
            at = @At("HEAD")
    )
    private void getMessage(String message, CallbackInfo ci) {
        chatnotify$storeMessage(message);
    }

    @Inject(
            method = "sendCommand",
            at = @At("HEAD")
    )
    private void getCommand(String command, CallbackInfo ci) {
        chatnotify$storeCommand(command);
    }

    @Inject(
            method = "sendUnsignedCommand",
            at = @At("HEAD")
    )
    private void getUnsignedCommand(String command, CallbackInfoReturnable<Boolean> cir) {
        chatnotify$storeCommand(command);
    }

    @Unique
    private void chatnotify$storeMessage(String message) {
        long time = System.nanoTime();
        chatnotify$removeOldMessages(time);

        String plainMsg = "";

        // If message starts with a prefix, remove the prefix.
        for (String prefix : Config.get().prefixes) {
            if (message.startsWith(prefix)) {
                plainMsg = message.replaceFirst(prefix, "").strip();
                break;
            }
        }
        ChatNotify.recentMessages.add(Pair.of(
                time + 5000000000L,
                plainMsg.isEmpty() ? message : plainMsg
        ));
    }

    @Unique
    private void chatnotify$storeCommand(String command) {
        long time = System.currentTimeMillis();
        chatnotify$removeOldMessages(time);

        // The command '/' is removed before this point, so add it back before
        // checking against prefixes.
        command = '/' + command;

        // If command starts with a prefix, cut the prefix and store the command
        for (String prefix : Config.get().prefixes) {
            if (command.startsWith(prefix)) {
                command = command.replaceFirst(prefix, "").strip();
                if (!command.isEmpty()) {
                    ChatNotify.recentMessages.add(Pair.of(time + 5000000000L, command));
                }
                break;
            }
        }
    }

    @Unique
    private void chatnotify$removeOldMessages(long time) { // no see
        ChatNotify.recentMessages.removeIf(pair -> pair.getFirst() < time);
    }
}
