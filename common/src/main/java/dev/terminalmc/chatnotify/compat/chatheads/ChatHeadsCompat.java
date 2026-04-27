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

package dev.terminalmc.chatnotify.compat.chatheads;

import dev.terminalmc.chatnotify.ChatNotify;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ChatHeadsCompat {

    public static final String MOD_NAME = "ChatHeads";

    public static final String CHAT_HEADS_CLASS = "dzwdz.chat_heads.ChatHeads";
    public static final String LAST_SENDER_DATA_FIELD = "lastSenderData";
    public static final String HANDLE_ADDED_MESSAGE_METHOD = "handleAddedMessage";
    public static final Class<?>[] HANDLE_ADDED_MESSAGE_PARAMS = {
            Component.class,
            PlayerInfo.class
    };

    public static final String HEAD_DATA_CLASS = "dzwdz.chat_heads.HeadData";
    public static final String PLAYER_INFO_METHOD = "playerInfo";
    public static final Class<?>[] PLAYER_INFO_PARAMS = {};

    private static boolean hasFailed = false;
    private static Field lastSenderDataField;
    private static Method handleAddedMessageMethod;
    private static Method playerInfoMethod;
    private ChatHeadsCompat() {
    }

    //
    // Wrappers
    //

    /**
     * @return the most recently saved message ownership data from ChatHeads, if any exists.
     */
    public static @Nullable PlayerInfo getPlayerInfo() {
        if (hasFailed)
            return null;
        return invokePlayerInfo();
    }

    /**
     * Instructs ChatHeads to update its saved message ownership data.
     */
    public static void handleAddedMessage(
            Component message,
            @Nullable PlayerInfo playerInfo
    ) {
        if (hasFailed)
            return;
        invokeHandleAddedMessage(message, playerInfo);
    }

    //
    // Reflective invokers
    //

    public static void invokeHandleAddedMessage(
            Component message,
            @Nullable PlayerInfo playerInfo
    ) {
        try {
            if (handleAddedMessageMethod == null) {
                // Load class and find method
                Class<?> chatHeadsClass = Class.forName(
                        CHAT_HEADS_CLASS,
                        false,
                        Thread.currentThread().getContextClassLoader()
                );
                handleAddedMessageMethod = chatHeadsClass.getMethod(
                        HANDLE_ADDED_MESSAGE_METHOD,
                        HANDLE_ADDED_MESSAGE_PARAMS
                );
            }

            // Invoke static
            handleAddedMessageMethod.invoke(null, message, playerInfo);
            return;

        } catch (Exception e) {
            ChatNotify.LOG.info(
                    "Error accessing {} - compat is now disabled: {}",
                    MOD_NAME,
                    e.getMessage()
            );
        }
        hasFailed = true;
    }

    public static @Nullable PlayerInfo invokePlayerInfo() {
        try {
            if (playerInfoMethod == null) {
                // Load class and find method
                Class<?> headDataClass = Class.forName(
                        HEAD_DATA_CLASS,
                        false,
                        Thread.currentThread().getContextClassLoader()
                );
                playerInfoMethod = headDataClass.getMethod(
                        PLAYER_INFO_METHOD,
                        PLAYER_INFO_PARAMS
                );
            }
            if (lastSenderDataField == null) {
                // Load class and find field
                Class<?> chatHeadsClass = Class.forName(
                        CHAT_HEADS_CLASS,
                        false,
                        Thread.currentThread().getContextClassLoader()
                );
                lastSenderDataField = chatHeadsClass.getField(
                        LAST_SENDER_DATA_FIELD
                );
            }

            // Invoke static
            Object result = playerInfoMethod.invoke(lastSenderDataField.get(null));
            if (result == null) {
                return null;
            } else if (result instanceof PlayerInfo playerInfo) {
                return playerInfo;
            } else {
                throw new ClassCastException();
            }

        } catch (Exception e) {
            ChatNotify.LOG.info(
                    "Error accessing {} - compat is now disabled: {}",
                    MOD_NAME,
                    e.getMessage()
            );
        }
        hasFailed = true;
        return null;
    }
}
