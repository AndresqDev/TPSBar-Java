package com.ping.tpsbar.managers;

import java.util.HashMap;
import java.util.Map;

import com.ping.tpsbar.config.TpsBarSettings;
import com.ping.tpsbar.models.BarMetric;
import com.ping.tpsbar.models.Severity;
import com.ping.tpsbar.permissions.TpsBarPermissions;
import com.ping.tpsbar.utils.TpsBarDisplay;
import pumpkin.BossBar;
import pumpkin.Player;
import pumpkin.Server;
import pumpkin.TextComponent;

/** Owns per-player TPS bar state and refreshes it from server metrics. */
public final class TpsBarManager {
    private final Map<String, Boolean> enabled = new HashMap<>();
    private final Map<String, BarMetric> metrics = new HashMap<>();
    private final Map<String, BarState> bars = new HashMap<>();

    public boolean toggle(Player player, Server server) {
        String name = player.getName();
        boolean next = !isEnabled(name);
        enabled.put(name, next);
        if (next) {
            refreshPlayer(player, Math.min(server.getTps(), TpsBarSettings.TARGET_TPS), server.getMspt());
        } else {
            removeBar(name);
        }
        return next;
    }

    public void setMetric(Player player, BarMetric metric, Server server) {
        metrics.put(player.getName(), metric);
        refreshPlayer(player, Math.min(server.getTps(), TpsBarSettings.TARGET_TPS), server.getMspt());
    }

    public void onPlayerLeave(Player player) {
        removeBar(player.getName());
    }

    public void refreshAll(Server server) {
        double tps = Math.min(server.getTps(), TpsBarSettings.TARGET_TPS);
        double mspt = server.getMspt();
        for (Player player : server.getAllPlayers()) {
            refreshPlayer(player, tps, mspt);
        }
    }

    public void shutdown() {
        for (BarState state : bars.values()) {
            state.bar.removeAll();
        }
        bars.clear();
        enabled.clear();
        metrics.clear();
    }

    public boolean isEnabled(String name) {
        return Boolean.TRUE.equals(enabled.get(name));
    }

    public BarMetric metricFor(String name) {
        BarMetric metric = metrics.get(name);
        return metric == null ? BarMetric.MSPT : metric;
    }

    private void refreshPlayer(Player player, double tps, double mspt) {
        String name = player.getName();
        boolean shouldShow = isEnabled(name) && player.hasPermission(TpsBarPermissions.COMMAND_TOGGLE);
        if (!shouldShow) {
            removeBar(name);
            return;
        }

        BarMetric metric = metricFor(name);
        int ping = player.getPing();
        Severity severity = TpsBarDisplay.msptSeverity(mspt);
        TextComponent title = TpsBarDisplay.title(tps, mspt, ping);
        BarState state = bars.get(name);
        if (state == null) {
            BossBar bar = new BossBar(
                    title,
                    TpsBarDisplay.barColor(severity),
                    BossBar.BossBarDivision.NO_DIVISION);
            bar.setHealth(TpsBarDisplay.progress(metric, tps, mspt, ping));
            bar.addPlayer(player);
            bars.put(name, new BarState(bar, severity));
        } else {
            state.bar.setTitle(title);
            state.bar.setHealth(TpsBarDisplay.progress(metric, tps, mspt, ping));
            if (state.severity != severity) {
                state.bar.setColor(TpsBarDisplay.barColor(severity));
                state.severity = severity;
            }
        }
    }

    private void removeBar(String name) {
        BarState state = bars.remove(name);
        if (state != null) {
            // BossBar bindings consume the Player resource passed to addPlayer.
            // Hiding the bar through removeAll avoids reusing a consumed handle.
            state.bar.removeAll();
        }
    }
}
