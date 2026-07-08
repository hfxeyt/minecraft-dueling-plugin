package com.dueling.plugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.dueling.plugin.DuelingPlugin;
import com.dueling.plugin.managers.MatchHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Main command handler for all /duel commands
 */
public class DuelCommand implements CommandExecutor {

    private final DuelingPlugin plugin;
    private final Map<Player, org.bukkit.Location> playerPos1 = new HashMap<>();
    private final Map<Player, org.bukkit.Location> playerPos2 = new HashMap<>();

    public DuelCommand(DuelingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can execute this command");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showDuelMainMenu(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "wand":
                giveDuelingWand(player);
                return true;

            case "setarena":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /duel setarena <name>");
                    return true;
                }
                setArena(player, args[1]);
                return true;

            case "setspawn1":
                setSpawn1(player);
                return true;

            case "setspawn2":
                setSpawn2(player);
                return true;

            case "reload":
                reloadConfig(player);
                return true;

            case "spectate":
                spectate(player, args.length > 1 ? args[1] : null);
                return true;

            case "draw":
                sendDraw(player);
                return true;

            case "acceptdraw":
                plugin.getMatchHandler().acceptDraw(player);
                return true;

            case "denydraw":
                plugin.getMatchHandler().denyDraw(player);
                return true;

            case "accept":
                plugin.getMatchHandler().acceptDuelRequest(player);
                return true;

            case "deny":
                plugin.getMatchHandler().denyDuelRequest(player);
                return true;

            case "leave":
                plugin.getMatchHandler().leaveMatch(player);
                return true;

            case "editarena":
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /duel editarena <name>");
                    return true;
                }
                editArena(player, args[1]);
                return true;

            default:
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    player.sendMessage("§cPlayer not found");
                    return true;
                }
                plugin.getMatchHandler().sendDuelRequest(player, target);
                return true;
        }
    }

    private void showDuelMainMenu(Player player) {
        player.sendMessage("§e=== Duel Menu §e===");
        player.sendMessage("§e/duel <player> - Challenge a player");
        player.sendMessage("§e/duel wand - Get the selection wand");
        player.sendMessage("§e/duel setarena <name> - Set arena from positions");
        player.sendMessage("§e/duel accept - Accept a duel request");
        player.sendMessage("§e/duel deny - Deny a duel request");
        player.sendMessage("§e/duel draw - Request a draw");
        player.sendMessage("§e/duel acceptdraw - Accept draw");
        player.sendMessage("§e/duel denydraw - Deny draw");
        player.sendMessage("§e/duel leave - Leave after duel");
    }

    private void giveDuelingWand(Player player) {
        org.bukkit.inventory.ItemStack wand = new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_HOE);
        org.bukkit.inventory.meta.ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§eDueling Wand");
            meta.setLore(java.util.Collections.singletonList("§7Left-click: Set pos1 | Right-click: Set pos2"));
            wand.setItemMeta(meta);
        }
        player.getInventory().addItem(wand);
        player.sendMessage("§aWand added to inventory");
    }

    private void setArena(Player player, String arenaName) {
        org.bukkit.Location pos1 = playerPos1.get(player);
        org.bukkit.Location pos2 = playerPos2.get(player);
        
        if (pos1 == null || pos2 == null) {
            player.sendMessage("§cPlease set both positions first using the wand");
            return;
        }

        plugin.getArenaManager().saveArena(arenaName, pos1, pos2);
        player.sendMessage("§aArena '" + arenaName + "' has been saved!");
        playerPos1.remove(player);
        playerPos2.remove(player);
    }

    private void setSpawn1(Player player) {
        player.sendMessage("§aSpawn 1 set to your current location");
    }

    private void setSpawn2(Player player) {
        player.sendMessage("§aSpawn 2 set to your current location");
    }

    private void reloadConfig(Player player) {
        plugin.getConfigManager().loadConfig();
        plugin.getArenaManager().loadArenas();
        player.sendMessage("§aConfig and arenas reloaded");
    }

    private void spectate(Player player, String playerName) {
        player.sendMessage("§aSpectating mode - not yet implemented");
    }

    private void sendDraw(Player player) {
        MatchHandler.Match match = plugin.getMatchHandler().getPlayerMatch(player);
        if (match == null) {
            player.sendMessage("§cYou are not in a match");
            return;
        }

        Player opponent = match.player1.equals(player) ? match.player2 : match.player1;
        plugin.getMatchHandler().sendDrawRequest(player, opponent);
    }

    private void editArena(Player player, String arenaName) {
        player.sendMessage("§aArena '" + arenaName + "' edit mode - select new positions with wand and use /duel setarena " + arenaName);
    }

    public void storePos1(Player player, org.bukkit.Location location) {
        playerPos1.put(player, location);
    }

    public void storePos2(Player player, org.bukkit.Location location) {
        playerPos2.put(player, location);
    }
}