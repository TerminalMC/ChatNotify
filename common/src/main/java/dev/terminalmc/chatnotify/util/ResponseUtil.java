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

package dev.terminalmc.chatnotify.util;

import dev.terminalmc.chatnotify.compat.commandkeys.CommandKeysWrapper;
import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.Response;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;

import java.util.ArrayList;
import java.util.List;

public class ResponseUtil {

    private ResponseUtil() {
    }

    /**
     * Stores activated (but not sent) response messages.
     */
    private static final List<ScheduledResponse> RESPONSES = new ArrayList<>();

    public static void send(Response response, int totalDelay) {
        RESPONSES.add(new ScheduledResponse(response, totalDelay));
    }

    public static void clear() {
        RESPONSES.clear();
    }

    public static void tickResponses(Minecraft mc) {
        if (mc.getConnection() == null || !mc.getConnection().isAcceptingMessages()) {
            RESPONSES.clear();
            return;
        }

        List<String> sending = new ArrayList<>();
        RESPONSES.removeIf((sr) -> {
            if (sr.tick()) {
                Response res = sr.response;
                if (res.cooldown <= 0) {
                    res.cooldown = res.cooldownTicks;
                    if (res.sendingString != null && !res.sendingString.isBlank()) {
                        if (res.type.equals(Response.Type.COMMANDKEYS)) {
                            CommandKeysWrapper.trySend(res.sendingString);
                        } else if (res.type.equals(Response.Type.DISCORD)) {
                            DiscordWebhookHandler.sendAsync(res.webhookUrl, res.sendingString);
                        } else {
                            sending.add(res.sendingString);
                        }
                    }
                }
                return false;
            } else {
                return sr.canRemove();
            }
        });

        sendMessages(sending, mc.getConnection());
    }

    private static void sendMessages(List<String> messages, ClientPacketListener connection) {
        if (messages.isEmpty())
            return;
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
                mc.setScreen(oldScreen);
            }
            case PACKET -> {
                for (String msg : messages) {
                    if (msg.startsWith("/")) {
                        connection.sendCommand(msg.substring(1));
                    } else {
                        connection.sendChat(msg);
                    }
                }
            }
        }
    }

    private static class ScheduledResponse {

        Response response;
        int ticks;
        boolean completed;

        ScheduledResponse(Response response, int delay) {
            this.response = response;
            this.ticks = delay;
            this.completed = false;
        }

        /**
         * @return {@code true} if {@link ScheduledResponse#ticks} is at or below zero for the first
         * time.
         */
        boolean tick() {
            response.cooldown--;
            if (--ticks <= 0 && !completed) {
                completed = true;
                return true;
            }
            return false;
        }

        /**
         * @return {@code true} if both {@link ScheduledResponse#ticks} and
         * {@link Response#cooldown} are below zero.
         */
        boolean canRemove() {
            return completed && response.cooldown <= 0;
        }
    }
}
