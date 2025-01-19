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

package dev.terminalmc.chatnotify.compat.chatheads;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Wraps {@link ChatHeadsUtil} to catch errors caused by the ChatHeads mod not 
 * being available.
 */
public class ChatHeadsWrapper {
    private static boolean hasFailed = false;

    /**
     * Gets the most recently saved message ownership data.
     * 
     * <p>Returns {@code null} if the operation failed.</p>
     */
    public static @Nullable Pair<PlayerInfo,Integer> getPlayerInfo() {
        if (hasFailed) return null;
        try {
            return ChatHeadsUtil.getPlayerInfo();
        } catch (NoClassDefFoundError | NoSuchMethodError ignored) {
            hasFailed = true;
            return null;
        }
    }

    /**
     * Tells ChatHeads to update its saved message ownership data.
     */
    public static void handleAddedMessage(Component message,
                                   @Nullable ChatType.Bound bound,
                                   @Nullable PlayerInfo playerInfo) {
        if (hasFailed) return;
        try {
            ChatHeadsUtil.handleAddedMessage(message, bound, playerInfo);
        } catch (NoClassDefFoundError | NoSuchMethodError ignored) {
            hasFailed = true;
        }
    }
}
