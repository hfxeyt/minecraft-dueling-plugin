package com.dueling.plugin.listeners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import com.dueling.plugin.DuelingPlugin;
import com.dueling.plugin.managers.MatchHandler;

import java.util.*;

/**
 * Listens for block break and place events during matches
 */
public class BlockBreakListener implements Listener {

    private final DuelingPlugin plugin;
    private final Map<String, Set<Location>> placedBlocks = new HashMap<>();

    public BlockBreakListener(DuelingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        MatchHandler.Match match = plugin.getMatchHandler().getPlayerMatch(player);

        if (match == null) {
            return;
        }

        Location blockLoc = event.getBlock().getLocation();
        placedBlocks.computeIfAbsent(match.matchId, k -> new HashSet<>()).add(blockLoc);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfigManager().isBlockBreakingAllowed()) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("§cBlock breaking is disabled in this arena");
            return;
        }

        Player player = event.getPlayer();
        MatchHandler.Match match = plugin.getMatchHandler().getPlayerMatch(player);

        if (match == null) {
            return;
        }

        Block block = event.getBlock();
        Set<Location> placed = placedBlocks.getOrDefault(match.matchId, new HashSet<>());

        // Allow breaking only blocks that were placed by players
        if (!placed.contains(block.getLocation())) {
            event.setCancelled(true);
            player.sendMessage("§cYou can only break blocks placed by players");
        }
    }
}