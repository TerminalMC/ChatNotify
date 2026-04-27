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

package dev.terminalmc.chatnotify.compat.commandkeys;

import com.mojang.blaze3d.platform.InputConstants;
import dev.terminalmc.chatnotify.ChatNotify;
import dev.terminalmc.chatnotify.mixin.accessor.KeyAccessor;

import java.lang.reflect.Method;

public class CommandKeysCompat {

    public static final String DUAL_KEY_PATTERN_STRING = "^[a-z0-9.]+-[a-z0-9.]++$";

    public static final String MOD_NAME = "CommandKeys";

    public static final String KEYBIND_UTIL_CLASS = "dev.terminalmc.commandkeys.util.KeybindUtil";
    public static final String HANDLE_KEYS_METHOD = "handleKeys";
    public static final Class<?>[] HANDLE_KEYS_PARAMS = {
            InputConstants.Key.class,
            InputConstants.Key.class
    };

    private static boolean hasFailed = false;
    private static Method handleKeysMethod;

    private CommandKeysCompat() {
    }

    //
    // Wrappers
    //

    /**
     * Parses the specified string into two {@link InputConstants.Key} instances, and passes them to
     * CommandKeys' keypress handler.
     */
    public static void send(String str) {
        if (hasFailed)
            return;
        if (!str.matches(DUAL_KEY_PATTERN_STRING))
            return;
        String[] splitStr = str.split("-");
        String limitKeyStr = splitStr[0];
        String KeyStr = splitStr[1];

        InputConstants.Key limitKey = KeyAccessor.chatnotify$getNameMap().get(limitKeyStr);
        InputConstants.Key key = KeyAccessor.chatnotify$getNameMap().get(KeyStr);
        if (key == null || limitKey == null)
            return;

        invokeHandleKeys(key, limitKey);
    }

    //
    // Reflective invokers
    //

    public static void invokeHandleKeys(InputConstants.Key key, InputConstants.Key limitKey) {
        try {
            if (handleKeysMethod == null) {
                // Load class and find method
                Class<?> KeybindUtilClass = Class.forName(
                        KEYBIND_UTIL_CLASS,
                        false,
                        Thread.currentThread().getContextClassLoader()
                );
                handleKeysMethod = KeybindUtilClass.getMethod(
                        HANDLE_KEYS_METHOD,
                        HANDLE_KEYS_PARAMS
                );
            }

            // Invoke static
            handleKeysMethod.invoke(null, key, limitKey);
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
}
