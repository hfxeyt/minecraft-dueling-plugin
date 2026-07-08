package com.dueling.plugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import com.dueling.plugin.DuelingPlugin;
import com.dueling.plugin.managers.MatchHandler;

/**
 * Listens for player death events and handles match endings
 */
public class PlayerDeathListener implements Listener {

    private final DuelingPlugin plugin;

    public PlayerDeathListener(DuelingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player deadPlayer = event.getEntity();
        Player killer = deadPlayer.getKiller();

        MatchHandler.Match match = plugin.getMatchHandler().getPlayerMatch(deadPlayer);
        if (match == null) {
            return;
        }

        if (killer == null) {
            return;
        }

        // Items naturally drop - keep default behavior
        if (plugin.getConfigManager().shouldItemsDropOnDeath()) {
            event.setDroppedExp(0);
        }

        match.isFinished = true;
        plugin.getMatchHandler().endMatch(match.matchId, killer, deadPlayer);
    }
}