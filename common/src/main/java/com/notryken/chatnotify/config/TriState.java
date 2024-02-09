package com.notryken.chatnotify.config;

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
