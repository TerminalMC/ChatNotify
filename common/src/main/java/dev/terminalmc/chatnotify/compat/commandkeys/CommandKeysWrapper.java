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

package dev.terminalmc.chatnotify.compat.commandkeys;

public class CommandKeysWrapper {
    private static boolean hasFailed = false;

    /**
     * Wraps {@link CommandKeys#send} to catch errors caused by the CommandKeys
     * mod not being available.
     */
    public static void trySend(String str) {
        if (hasFailed) return;
        try {
            CommandKeys.send(str);
        } catch (NoClassDefFoundError | NoSuchMethodError ignored) {
            hasFailed = true;
        }
    }
}
