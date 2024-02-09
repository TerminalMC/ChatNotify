package com.notryken.chatnotify.config;

import java.util.Locale;

public class Trigger {
    public String string;
    public boolean enabled;
    private boolean isKey;
    public boolean isRegex;

    public Trigger() {
        this.string = "";
        this.enabled = true;
        this.isKey = false;
        this.isRegex = false;
    }

    public Trigger(String string) {
        this.string = string;
        this.enabled = true;
        this.isKey = false;
        this.isRegex = false;
    }

    public Trigger(String string, boolean enabled, boolean isKey, boolean isRegex) {
        this.string = string;
        this.enabled = enabled;
        this.isKey = isKey;
        this.isRegex = isRegex;
    }


    public boolean isKey() {
        return isKey;
    }

    /**
     * If {@code isKey} is {@code true}, converts the trigger string to
     * lowercase.
     */
    public void setIsKey(boolean isKey) {
        this.isKey = isKey;
        if (isKey) string = string.toLowerCase(Locale.ROOT);
    }
}
