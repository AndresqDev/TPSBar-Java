package com.ping.tpsbar.managers;

import com.ping.tpsbar.models.Severity;
import pumpkin.BossBar;

/** Package-private mutable state for one player's boss bar. */
public final class BarState {
    final BossBar bar;
    Severity severity;

    public BarState(BossBar bar, Severity severity) {
        this.bar = bar;
        this.severity = severity;
    }
}
