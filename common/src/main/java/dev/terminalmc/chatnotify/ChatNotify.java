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

package dev.terminalmc.chatnotify;

import com.mojang.datafixers.util.Pair;
import dev.terminalmc.chatnotify.compat.commandkeys.CommandKeysWrapper;
import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.Notification;
import dev.terminalmc.chatnotify.config.ResponseMessage;
import dev.terminalmc.chatnotify.config.Trigger;
import dev.terminalmc.chatnotify.util.ModLogger;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.*;

public class ChatNotify {
    public static final String MOD_ID = "chatnotify";
    public static final String MOD_NAME = "ChatNotify";
    public static final ModLogger LOG = new ModLogger(MOD_NAME);
    public static final Component PREFIX = Component.empty()
            .append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(MOD_NAME).withStyle(ChatFormatting.GOLD))
            .append(Component.literal("] ").withStyle(ChatFormatting.DARK_GRAY))
            .withStyle(ChatFormatting.GRAY);

    public static final List<Pair<Long, String>> recentMessages = new ArrayList<>();
    public static final List<ResponseMessage> responseMessages = new ArrayList<>();
    public static final Queue<Component> unmodifiedChat = new LinkedList<>();

    public static boolean hasChatHistoryMod;

    public static void init(boolean hasChatHistoryMod) {
        Config.getAndSave();
        ChatNotify.hasChatHistoryMod = hasChatHistoryMod;
    }

    public static void onConfigSaved(Config config) {
        for (Notification notif : config.getNotifs()) {
            for (Trigger trig : notif.triggers) {
                if (trig.type == Trigger.Type.REGEX) trig.tryCompilePattern();
            }
            for (Trigger trig : notif.exclusionTriggers) {
                if (trig.type == Trigger.Type.REGEX) trig.tryCompilePattern();
            }
        }
    }

    public static void onEndTick(Minecraft mc) {
        tickResponseMessages(mc);
    }

    private static void tickResponseMessages(Minecraft mc) {
        List<String> sending = new ArrayList<>();
        responseMessages.removeIf((resMsg) -> {
            if (--resMsg.countdown <= 0) {
                if (resMsg.sendingString != null && !resMsg.sendingString.isBlank()) {
                    if (resMsg.type.equals(ResponseMessage.Type.COMMANDKEYS)) {
                        CommandKeysWrapper.trySend(resMsg.sendingString);
                    } else {
                        sending.add(resMsg.sendingString);
                    }
                }
                return true;
            }
            return false;
        });
        if (
                mc.player != null 
                && mc.getConnection() != null 
                && mc.getConnection().isAcceptingMessages()) 
        {
            if (sending.isEmpty()) return;
            switch (Config.get().sendMode) {
                case PACKET -> {
                    // Compat mode for mods mixing into handleChatInput
                    Screen oldScreen = null;
                    if (!(mc.screen instanceof ChatScreen)) {
                        oldScreen = mc.screen;
                        mc.setScreen(new ChatScreen(""));
                    }
                    if (mc.screen instanceof ChatScreen cs) {
                        for (String msg : sending) {
                            cs.handleChatInput(msg, false);
                        }
                    }
                    if (oldScreen != null) mc.setScreen(oldScreen);
                }
                case SCREEN -> {
                    for (String msg : sending) {
                        if (msg.startsWith("/")) {
                            mc.player.connection.sendCommand(msg.substring(1));
                        } else {
                            mc.player.connection.sendChat(msg);
                        }
                    }
                }
            }
        }
        else {
            responseMessages.clear();
        }
    }
}
