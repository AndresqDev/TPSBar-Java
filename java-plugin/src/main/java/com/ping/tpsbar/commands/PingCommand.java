package com.ping.tpsbar.commands;

import com.ping.tpsbar.managers.TpsBarManager;
import com.ping.tpsbar.models.BarMetric;

/** Handles {@code /tpsbarping}. */
public final class PingCommand extends MetricCommand {
    public PingCommand(TpsBarManager manager) {
        super(manager, "tpsbarping", "Use ping for your TPSBar progress", BarMetric.PING);
    }
}
