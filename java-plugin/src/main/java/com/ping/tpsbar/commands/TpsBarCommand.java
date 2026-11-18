package com.ping.tpsbar.commands;

import com.ping.tpsbar.managers.TpsBarManager;
import com.ping.tpsbar.permissions.TpsBarPermissions;
import com.ping.tpsbar.utils.MessageUtil;
import pumpkin.Command;
import pumpkin.Player;
import pumpkin.Server;

/** Base class shared by the plugin's player-only commands. */
public abstract class TpsBarCommand {
    private final TpsBarManager manager;

    protected TpsBarCommand(TpsBarManager manager) {
        this.manager = manager;
    }

    public abstract Command command();

    public final int handle(Command.CommandSender sender, Server server, Command.ConsumedArgs args) {
        if (!sender.hasPermission(server, TpsBarPermissions.COMMAND_TOGGLE)) {
            sender.sendMessage(MessageUtil.error("You do not have permission to use TPSBar."));
            return 0;
        }
        Player player = sender.asPlayer();
        if (player == null) {
            sender.sendMessage(MessageUtil.error("Only players can use TPSBar."));
            return 0;
        }
        return execute(player, sender, server);
    }

    protected abstract int execute(Player player, Command.CommandSender sender, Server server);

    protected final TpsBarManager manager() {
        return manager;
    }
}
