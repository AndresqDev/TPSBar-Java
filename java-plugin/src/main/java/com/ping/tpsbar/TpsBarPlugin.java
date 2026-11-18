package com.ping.tpsbar;

import java.util.List;

import com.ping.tpsbar.commands.MsptCommand;
import com.ping.tpsbar.commands.PingCommand;
import com.ping.tpsbar.commands.ToggleCommand;
import com.ping.tpsbar.commands.TpsBarCommand;
import com.ping.tpsbar.commands.TpsCommand;
import com.ping.tpsbar.config.TpsBarSettings;
import com.ping.tpsbar.listeners.PlayerLeaveListener;
import com.ping.tpsbar.managers.TpsBarManager;
import com.ping.tpsbar.permissions.TpsBarPermissions;
import pumpkin.Event;
import pumpkin.EventPriority;
import pumpkin.EventSubscription;
import pumpkin.EventType;
import pumpkin.Logging;
import pumpkin.Permission;
import pumpkin.PluginContext;
import pumpkin.PluginMetadata;
import pumpkin.PumpkinPlugin;
import pumpkin.ScheduledTask;

/** TPSBar Java plugin entry point. */
public final class TpsBarPlugin extends PumpkinPlugin {
    private final TpsBarManager manager = new TpsBarManager();
    private final List<TpsBarCommand> commandSet = List.of(
            new ToggleCommand(manager),
            new MsptCommand(manager),
            new TpsCommand(manager),
            new PingCommand(manager));
    private ScheduledTask refreshTask;
    private EventSubscription leaveSubscription;

    @Override
    public PluginMetadata metadata() {
        return new PluginMetadata(
                "tpsbar-java",
                "0.1.0",
                List.of("PING"),
                "A personal TPS, MSPT, and ping boss bar written in Java",
                List.of(),
                List.of(TpsBarPermissions.COMMAND_TOGGLE));
    }

    @Override
    public void onLoad(PluginContext context) {
        Logging.log(Logging.Level.INFO, "Loading TPSBar Java");
        context.registerPermission(new Permission(
                TpsBarPermissions.COMMAND_TOGGLE,
                "Toggle your own TPSBar and choose its progress metric",
                Permission.PermissionDefault.OP,
                List.of()));

        for (TpsBarCommand command : commandSet) {
            getCommands().register(
                    context,
                    command.command(),
                    TpsBarPermissions.COMMAND_TOGGLE,
                    command::handle);
        }

        PlayerLeaveListener leaveListener = new PlayerLeaveListener(manager);
        leaveSubscription = getEvents().listen(
                context,
                EventType.PLAYER_LEAVE_EVENT,
                EventPriority.NORMAL,
                false,
                (server, event) -> {
                    if (event instanceof Event.PlayerLeaveEvent leave) {
                        leaveListener.handle(leave);
                    }
                    return event;
                });
        refreshTask = getTasks().everyTicks(
                1,
                TpsBarSettings.REFRESH_INTERVAL_TICKS,
                manager::refreshAll);
        Logging.log(Logging.Level.INFO, "TPSBar Java loaded");
    }

    @Override
    public void onUnload(PluginContext context) {
        if (refreshTask != null) {
            refreshTask.cancel();
        }
        if (leaveSubscription != null) {
            leaveSubscription.unregister();
        }
        manager.shutdown();
        Logging.log(Logging.Level.INFO, "TPSBar Java unloaded");
    }
}
