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

import dzwdz.chat_heads.ChatHeads;
import dzwdz.chat_heads.HeadData;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ChatHeadsCompat {

    private ChatHeadsCompat() {
    }

    /**
     * @return the most recently saved message ownership data from ChatHeads, if any exists.
     */
    static @Nullable PlayerInfo getPlayerInfo() {
        if (ChatHeads.lastSenderData == HeadData.EMPTY) {
            return null;
        } else {
            return ChatHeads.lastSenderData.playerInfo();
        }
    }

    /**
     * Instructs ChatHeads to update its saved message ownership data.
     */
    static void handleAddedMessage(
            Component message,
            @Nullable PlayerInfo playerInfo
    ) {
        ChatHeads.handleAddedMessage(message, playerInfo);
    }
}
