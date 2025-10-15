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

package dev.terminalmc.chatnotify.util;

import java.util.ArrayList;
import java.util.List;

public class TimingUtil {

    private TimingUtil() {
    }

    /**
     * Stores triggered (but not executed) actions.
     */
    private static final List<ScheduledActions> ACTIONS = new ArrayList<>();

    public static void send(Runnable action, int totalDelay) {
        ACTIONS.add(new ScheduledActions(action, totalDelay));
    }

    public static void clear() {
        ACTIONS.clear();
    }

    public static void tickActions() {
        ACTIONS.removeIf((sa) -> {
            if (sa.tick()) {
                sa.exec();
                return false;
            } else {
                return sa.canRemove();
            }
        });
    }

    private static class ScheduledActions {

        Runnable action;
        int ticks;
        boolean completed;

        ScheduledActions(Runnable action, int delay) {
            this.action = action;
            this.ticks = delay;
            this.completed = false;
        }

        /**
         * Run the related action.
         */
        void exec() {
            this.action.run();
        }

        /**
         * @return {@code true} if {@link ScheduledActions#ticks} is at or below zero for the first
         * time.
         */
        boolean tick() {
            if (--ticks <= 0 && !completed) {
                completed = true;
                return true;
            }
            return false;
        }

        /**
         * @return {@code true} if {@link ScheduledActions#ticks} is below zero.
         */
        boolean canRemove() {
            return completed;
        }
    }
}
