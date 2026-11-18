package com.ping.tpsbar.commands;

import com.ping.tpsbar.managers.TpsBarManager;
import com.ping.tpsbar.models.BarMetric;

/** Handles {@code /tpsbartps}. */
public final class TpsCommand extends MetricCommand {
    public TpsCommand(TpsBarManager manager) {
        super(manager, "tpsbartps", "Use TPS for your TPSBar progress", BarMetric.TPS);
    }
}
