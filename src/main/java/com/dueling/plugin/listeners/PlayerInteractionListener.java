package com.dueling.plugin.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import com.dueling.plugin.DuelingPlugin;
import com.dueling.plugin.commands.DuelCommand;

import java.util.HashMap;
import java.util.Map;

/**
 * Listens for player interactions with the dueling wand
 */
public class PlayerInteractionListener implements Listener {

    private final DuelingPlugin plugin;
    private final Map<Player, Location> pos1 = new HashMap<>();
    private final Map<Player, Location> pos2 = new HashMap<>();

    public PlayerInteractionListener(DuelingPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.IRON_HOE) {
            return;
        }

        if (item.getItemMeta() == null || !item.getItemMeta().getDisplayName().contains("Dueling Wand")) {
            return;
        }

        if (event.getClickedBlock() == null) {
            return;
        }

        event.setCancelled(true);

        if (event.getAction().toString().contains("LEFT")) {
            pos1.put(player, event.getClickedBlock().getLocation());
            player.sendMessage("§a§lPos1 set at: " + event.getClickedBlock().getLocation().getBlockX() + ", " 
                + event.getClickedBlock().getLocation().getBlockY() + ", " 
                + event.getClickedBlock().getLocation().getBlockZ());
        } else if (event.getAction().toString().contains("RIGHT")) {
            pos2.put(player, event.getClickedBlock().getLocation());
            player.sendMessage("§a§lPos2 set at: " + event.getClickedBlock().getLocation().getBlockX() + ", " 
                + event.getClickedBlock().getLocation().getBlockY() + ", " 
                + event.getClickedBlock().getLocation().getBlockZ());
        }
    }
}