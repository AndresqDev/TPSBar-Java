package com.ping.tpsbar.commands;

import java.util.List;

import com.ping.tpsbar.managers.TpsBarManager;
import com.ping.tpsbar.models.BarMetric;
import com.ping.tpsbar.utils.MessageUtil;
import pumpkin.Command;
import pumpkin.Player;
import pumpkin.Server;

/** Base class for the metric-selection commands. */
public abstract class MetricCommand extends TpsBarCommand {
    private final String name;
    private final String description;
    private final BarMetric metric;

    protected MetricCommand(TpsBarManager manager, String name, String description, BarMetric metric) {
        super(manager);
        this.name = name;
        this.description = description;
        this.metric = metric;
    }

    @Override
    public final Command command() {
        return new Command(List.of(name), description);
    }

    @Override
    protected final int execute(Player player, Command.CommandSender sender, Server server) {
        manager().setMetric(player, metric, server);
        sender.sendMessage(MessageUtil.success(
                "Your TPSBar progress metric is now " + metric.getDisplayName() + "."));
        return 1;
    }
}
