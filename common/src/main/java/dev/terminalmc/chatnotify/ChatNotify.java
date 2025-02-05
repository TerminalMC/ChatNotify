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

package dev.terminalmc.chatnotify;

import com.mojang.datafixers.util.Pair;
import dev.terminalmc.chatnotify.compat.commandkeys.CommandKeysWrapper;
import dev.terminalmc.chatnotify.config.*;
import dev.terminalmc.chatnotify.util.ModLogger;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.util.*;

import static dev.terminalmc.chatnotify.util.Localization.localized;

public class ChatNotify {
    public static final String MOD_ID = "chatnotify";
    public static final String MOD_NAME = "ChatNotify";
    public static final ModLogger LOG = new ModLogger(MOD_NAME);
    public static final Component PREFIX = Component.empty()
            .append(Component.literal("[").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.literal(MOD_NAME).withStyle(ChatFormatting.GOLD))
            .append(Component.literal("] ").withStyle(ChatFormatting.DARK_GRAY))
            .withStyle(ChatFormatting.GRAY);
    public static boolean hasResetConfig = false;

    /**
     * Stores messages recently sent by the client, for comparison with incoming 
     * messages to determine sender identity.
     */
    public static final List<Pair<Long, String>> recentMessages = new ArrayList<>();

    /**
     * Stores activated (but not sent) response messages.
     */
    public static final List<ResponseMessage> responseMessages = new ArrayList<>();

    /**
     * Stores an unmodified copy of recent incoming chat messages.
     */
    public static final Queue<Component> unmodifiedChat = new LinkedList<>();

    public static void init() {
        Config.getAndSave();
    }

    public static void onConfigSaved(Config config) {
        // Compile regex triggers
        for (Notification notif : config.getNotifs()) {
            for (Trigger trig : notif.triggers) {
                if (trig.type == Trigger.Type.REGEX) trig.tryCompilePattern();
                if (trig.styleTarget.type == StyleTarget.Type.REGEX) {
                    trig.styleTarget.tryCompilePattern();
                } else if (trig.styleTarget.type == StyleTarget.Type.CAPTURING) {
                    trig.styleTarget.tryParseIndexes();
                }
            }
            for (Trigger trig : notif.exclusionTriggers) {
                if (trig.type == Trigger.Type.REGEX) trig.tryCompilePattern();
            }
        }
    }

    public static void onEndTick(Minecraft mc) {
        tickResponseMessages(mc);
        
        // Config reset warning toast
        if (hasResetConfig && mc.screen instanceof TitleScreen) {
            hasResetConfig = false;
            mc.getToastManager().addToast(new SystemToast(new SystemToast.SystemToastId(15000L), 
                    localized("toast", "reset.title"), localized("toast", "reset.message",
                    Component.literal(Config.UNREADABLE_FILE_NAME).withStyle(ChatFormatting.GOLD))));
        }
    }

    private static void tickResponseMessages(Minecraft mc) {
        if (mc.getConnection() == null || !mc.getConnection().isAcceptingMessages()) {
            responseMessages.clear();
            return;
        }
        
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
        
        sendMessages(sending);
    }
    
    private static void sendMessages(List<String> messages) {
        if (messages.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        switch (Config.get().sendMode) {
            case SCREEN -> {
                // Compat mode for mods mixing into handleChatInput
                Screen oldScreen = mc.screen;
                if (!(mc.screen instanceof ChatScreen)) {
                    mc.setScreen(new ChatScreen(""));
                }
                if (mc.screen instanceof ChatScreen cs) {
                    for (String msg : messages) {
                        cs.handleChatInput(msg, false);
                    }
                }
                mc.screen = oldScreen;
            }
            case PACKET -> {
                for (String msg : messages) {
                    if (msg.startsWith("/")) {
                        mc.getConnection().sendCommand(msg.substring(1));
                    } else {
                        mc.getConnection().sendChat(msg);
                    }
                }
            }
        }
    }
}
