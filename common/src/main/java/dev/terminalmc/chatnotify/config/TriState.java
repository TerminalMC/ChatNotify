/*
 * Copyright 2024 TerminalMC
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

package dev.terminalmc.chatnotify.config;

/**
 * Represents a control with three states: OFF, ON and DISABLED.
 */
public class TriState {

    public enum State {
        OFF,
        ON,
        DISABLED
    }

    public State state;

    public TriState() {
        this.state = State.DISABLED;
    }

    public TriState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public boolean isOff() {
        return state == State.OFF;
    }

    public void turnOff() {
        state = State.OFF;
    }

    public boolean isOn() {
        return state == State.ON;
    }

    public void turnOn() {
        state = State.ON;
    }

    public boolean isEnabled() {
        return state != State.DISABLED;
    }

    public void disable() {
        state = State.DISABLED;
    }
}
