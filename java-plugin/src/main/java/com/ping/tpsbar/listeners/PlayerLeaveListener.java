package com.ping.tpsbar.listeners;

import com.ping.tpsbar.managers.TpsBarManager;
import pumpkin.Event;

/** Removes a player's bar when they leave the server. */
public final class PlayerLeaveListener {
    private final TpsBarManager manager;

    public PlayerLeaveListener(TpsBarManager manager) {
        this.manager = manager;
    }

    public void handle(Event.PlayerLeaveEvent event) {
        manager.onPlayerLeave(event.getPlayer());
    }
}
