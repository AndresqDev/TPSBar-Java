package com.ping.tpsbar.models;

/** The metric displayed by a player's TPS bar. */
public enum BarMetric {
    MSPT("MSPT"),
    TPS("TPS"),
    PING("Ping");

    private final String displayName;

    BarMetric(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
