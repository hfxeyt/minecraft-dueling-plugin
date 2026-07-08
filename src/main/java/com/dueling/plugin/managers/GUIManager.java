package com.dueling.plugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import com.dueling.plugin.DuelingPlugin;

import java.util.Collections;

/**
 * Manages all GUI interactions for the dueling system
 */
public class GUIManager implements Listener {

    private static final String ARENA_SELECT_PREFIX = "arena_select_";
    private static final String TIME_LIMIT_PREFIX = "time_limit_";
    private static final String CONFIRM_PREFIX = "confirm_";

    /**
     * Opens arena selection GUI
     */
    public static void openArenaSelectionGUI(DuelingPlugin plugin, Player player1, Player player2) {
        Inventory inv = Bukkit.createInventory(null, 27, "§eSelect an Arena");

        int slot = 0;
        for (ArenaManager.Arena arena : plugin.getArenaManager().getAllArenas().values()) {
            ItemStack item = createItemStack(Material.GRASS_BLOCK, arena.getName(), "");
            inv.setItem(slot++, item);
        }

        plugin.getServer().getPluginManager().registerEvents(
            new ArenaSelectListener(plugin, player1, player2), plugin
        );

        player1.openInventory(inv);
    }

    /**
     * Opens time limit selection GUI
     */
    public static void openTimeLimitGUI(DuelingPlugin plugin, Player player1, Player player2, String arenaName) {
        Inventory inv = Bukkit.createInventory(null, 9, "§eSelect Time Limit");

        ItemStack item3min = createItemStack(Material.CLOCK, "3 Minutes", "§7180 seconds");
        ItemStack item5min = createItemStack(Material.CLOCK, "5 Minutes", "§7300 seconds");
        ItemStack item10min = createItemStack(Material.CLOCK, "10 Minutes", "§7600 seconds");

        inv.setItem(2, item3min);
        inv.setItem(4, item5min);
        inv.setItem(6, item10min);

        plugin.getServer().getPluginManager().registerEvents(
            new TimeLimitListener(plugin, player1, player2, arenaName), plugin
        );

        player1.openInventory(inv);
    }

    /**
     * Opens confirmation GUI
     */
    public static void openConfirmationGUI(DuelingPlugin plugin, Player player1, Player player2, String arenaName, int timeLimit) {
        Inventory inv = Bukkit.createInventory(null, 27, "§eConfirm Duel");

        // Display info
        ItemStack infoItem = createItemStack(Material.PAPER, "§eDuel Information", 
            "§7Player 1: " + player1.getName() + 
            "\n§7Player 2: " + player2.getName() +
            "\n§7Time Limit: " + timeLimit + "s" +
            "\n§7Ping P1: " + player1.getPing() + "ms" +
            "\n§7Ping P2: " + player2.getPing() + "ms" +
            "\n§7Arena: " + arenaName);
        
        inv.setItem(13, infoItem);

        // Confirm button (Green Glass Pane)
        ItemStack confirmItem = createItemStack(Material.GREEN_STAINED_GLASS_PANE, "§aConfirm", "§7Click to start the duel");
        inv.setItem(11, confirmItem);

        // Cancel button (Red Glass Pane)
        ItemStack cancelItem = createItemStack(Material.RED_STAINED_GLASS_PANE, "§cCancel", "§7Click to cancel");
        inv.setItem(15, cancelItem);

        plugin.getServer().getPluginManager().registerEvents(
            new ConfirmListener(plugin, player1, player2, arenaName, timeLimit), plugin
        );

        player1.openInventory(inv);
    }

    /**
     * Creates an ItemStack with name and lore
     */
    private static ItemStack createItemStack(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (!lore.isEmpty()) {
                meta.setLore(Collections.singletonList(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Listener for arena selection
     */
    static class ArenaSelectListener implements Listener {
        private DuelingPlugin plugin;
        private Player player1;
        private Player player2;

        ArenaSelectListener(DuelingPlugin plugin, Player player1, Player player2) {
            this.plugin = plugin;
            this.player1 = player1;
            this.player2 = player2;
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!event.getView().getTitle().contains("Select an Arena")) return;

            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();

            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || !clicked.hasItemMeta()) return;

            String arenaName = clicked.getItemMeta().getDisplayName();
            openTimeLimitGUI(plugin, player1, player2, arenaName);
        }
    }

    /**
     * Listener for time limit selection
     */
    static class TimeLimitListener implements Listener {
        private DuelingPlugin plugin;
        private Player player1;
        private Player player2;
        private String arenaName;

        TimeLimitListener(DuelingPlugin plugin, Player player1, Player player2, String arenaName) {
            this.plugin = plugin;
            this.player1 = player1;
            this.player2 = player2;
            this.arenaName = arenaName;
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!event.getView().getTitle().contains("Select Time Limit")) return;

            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();

            int slot = event.getRawSlot();
            int timeLimit = 0;

            if (slot == 2) timeLimit = 180;      // 3 minutes
            else if (slot == 4) timeLimit = 300; // 5 minutes
            else if (slot == 6) timeLimit = 600; // 10 minutes

            if (timeLimit > 0) {
                openConfirmationGUI(plugin, player1, player2, arenaName, timeLimit);
            }
        }
    }

    /**
     * Listener for confirmation
     */
    static class ConfirmListener implements Listener {
        private DuelingPlugin plugin;
        private Player player1;
        private Player player2;
        private String arenaName;
        private int timeLimit;

        ConfirmListener(DuelingPlugin plugin, Player player1, Player player2, String arenaName, int timeLimit) {
            this.plugin = plugin;
            this.player1 = player1;
            this.player2 = player2;
            this.arenaName = arenaName;
            this.timeLimit = timeLimit;
        }

        @EventHandler
        public void onInventoryClick(InventoryClickEvent event) {
            if (!event.getView().getTitle().contains("Confirm Duel")) return;

            event.setCancelled(true);

            int slot = event.getRawSlot();
            if (slot == 11) { // Green pane - Confirm
                plugin.getMatchHandler().startMatch(player1, player2, arenaName, timeLimit);
                player1.closeInventory();
                player2.closeInventory();
            } else if (slot == 15) { // Red pane - Cancel
                player1.sendMessage("§cDuel cancelled");
                player2.sendMessage("§cDuel cancelled");
                player1.closeInventory();
                player2.closeInventory();
            }
        }
    }
}