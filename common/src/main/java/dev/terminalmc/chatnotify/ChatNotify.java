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
import dev.terminalmc.chatnotify.config.Config;
import dev.terminalmc.chatnotify.config.Notification;
import dev.terminalmc.chatnotify.config.StyleTarget;
import dev.terminalmc.chatnotify.config.Trigger;
import dev.terminalmc.chatnotify.util.ModLogger;
import dev.terminalmc.chatnotify.util.ResponseUtil;
import dev.terminalmc.chatnotify.util.TimingUtil;
import dev.terminalmc.chatnotify.util.text.FormatUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static dev.terminalmc.chatnotify.util.Localization.localized;

public class ChatNotify {

    public static final String MOD_ID = "chatnotify";
    public static final String MOD_NAME = "ChatNotify";
    public static final ModLogger LOG = new ModLogger(MOD_NAME);
    public static boolean hasResetConfig = false;

    /**
     * Stores messages recently sent by the client, for comparison with incoming messages to
     * determine sender identity.
     */
    public static final List<Pair<Long, String>> recentMessages = new ArrayList<>();

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
                if (trig.type == Trigger.Type.REGEX)
                    trig.tryCompilePattern();
                if (trig.styleTarget.type == StyleTarget.Type.REGEX) {
                    trig.styleTarget.tryCompilePattern();
                } else if (trig.styleTarget.type == StyleTarget.Type.CAPTURING) {
                    trig.styleTarget.tryParseIndexes();
                }
            }
            for (Trigger trig : notif.exclusionTriggers) {
                if (trig.type == Trigger.Type.REGEX)
                    trig.tryCompilePattern();
            }
        }
    }

    public static void afterClientTick(Minecraft mc) {
        ResponseUtil.tickResponses(mc);
        TimingUtil.tickActions();

        // Config reset warning toast
        if (hasResetConfig && mc.screen instanceof TitleScreen) {
            hasResetConfig = false;
            mc.getToastManager().addToast(new SystemToast(
                    new SystemToast.SystemToastId(15000L),
                    localized("toast", "reset.title"),
                    localized(
                            "toast",
                            "reset.message",
                            Component.literal(Config.UNREADABLE_FILE_NAME)
                                    .withStyle(ChatFormatting.GOLD)
                    )
            ));
        }
    }

    public static void updateUsernameNotif(Config config) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            config.setProfileName(FormatUtil.stripCodes(mc.player.getName().getString()));
            config.setDisplayName(FormatUtil.stripCodes(mc.player.getDisplayName().getString()));
        }
    }
}
