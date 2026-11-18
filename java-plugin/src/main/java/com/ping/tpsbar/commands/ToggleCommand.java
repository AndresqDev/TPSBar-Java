package com.ping.tpsbar.commands;

import java.util.List;

import com.ping.tpsbar.managers.TpsBarManager;
import com.ping.tpsbar.utils.MessageUtil;
import pumpkin.Command;
import pumpkin.Player;
import pumpkin.Server;

/** Handles {@code /tpsbar}. */
public final class ToggleCommand extends TpsBarCommand {
    public ToggleCommand(TpsBarManager manager) {
        super(manager);
    }

    @Override
    public Command command() {
        return new Command(List.of("tpsbar"), "Toggle your personal TPSBar");
    }

    @Override
    protected int execute(Player player, Command.CommandSender sender, Server server) {
        boolean enabled = manager().toggle(player, server);
        sender.sendMessage(enabled
                ? MessageUtil.success("TPSBar enabled.")
                : MessageUtil.muted("TPSBar disabled."));
        return 1;
    }
}
