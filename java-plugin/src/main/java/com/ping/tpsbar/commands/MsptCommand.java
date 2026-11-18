package com.ping.tpsbar.commands;

import com.ping.tpsbar.managers.TpsBarManager;
import com.ping.tpsbar.models.BarMetric;

/** Handles {@code /tpsbarmspt}. */
public final class MsptCommand extends MetricCommand {
    public MsptCommand(TpsBarManager manager) {
        super(manager, "tpsbarmspt", "Use MSPT for your TPSBar progress", BarMetric.MSPT);
    }
}
