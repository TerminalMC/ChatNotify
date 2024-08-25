/*
 * Copyright 2023, 2024 NotRyken
 * SPDX-License-Identifier: Apache-2.0
 */

package dev.terminalmc.chatnotify;

import com.mojang.datafixers.util.Pair;
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

import java.util.ArrayList;
import java.util.List;

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
        for (ResponseMessage resMsg : responseMessages) {
            resMsg.countdown--;
            if (resMsg.countdown <= 0 && resMsg.sendingString != null) {
                sending.add(resMsg.sendingString);
            }
        }
        responseMessages.removeIf((resMsg) -> resMsg.countdown <= 0);
        if (mc.getConnection() != null && mc.getConnection().isAcceptingMessages()) {
            if (!sending.isEmpty()) {
                Screen oldScreen = mc.screen;
                mc.setScreen(new ChatScreen(""));
                for (String msg : sending) {
                    if (mc.screen instanceof ChatScreen cs) {
                        cs.handleChatInput(msg, false);
                    }
                }
                mc.setScreen(oldScreen);
            }
        }
        else {
            responseMessages.clear();
        }
    }

    public static boolean mixinEarly() {
        return switch(Config.get().mixinEarly.state) {
            case ON -> true;
            case OFF -> false;
            case DISABLED -> hasChatHistoryMod;
        };
    }
}
