package com.dueling.plugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import com.dueling.plugin.DuelingPlugin;
import net.essentialsx.api.v2.Essentials;

import java.util.*;

/**
 * Handles all match-related operations including starting, ending, and managing active matches
 */
public class MatchHandler {

    private final DuelingPlugin plugin;
    private Map<String, Match> activeMatches = new HashMap<>();
    private Map<Player, String> pendingDuelRequests = new HashMap<>();
    private Map<Player, String> pendingDrawRequests = new HashMap<>();
    private Map<Player, List<ItemStack>> playerInventoryBackup = new HashMap<>();

    public MatchHandler(DuelingPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Sends a duel request from one player to another
     */
    public void sendDuelRequest(Player requester, Player opponent) {
        if (pendingDuelRequests.containsKey(opponent)) {
            requester.sendMessage("§cThis player already has a pending duel request");
            return;
        }

        pendingDuelRequests.put(opponent, requester.getName());
        opponent.sendMessage("§e" + requester.getName() + " §ehas sent you a duel request!");
        opponent.sendMessage("§eType §a/duel accept §eto accept or §c/duel deny §eto deny");
        requester.sendMessage("§aDuel request sent to " + opponent.getName());
    }

    /**
     * Accepts a pending duel request and opens arena selection GUI
     */
    public void acceptDuelRequest(Player accepter) {
        String requesterName = pendingDuelRequests.remove(accepter);
        if (requesterName == null) {
            accepter.sendMessage("§cYou don't have any pending duel requests");
            return;
        }

        Player requester = Bukkit.getPlayer(requesterName);
        if (requester == null) {
            accepter.sendMessage("§cThe player who requested the duel is no longer online");
            return;
        }

        openArenaSelectionGUI(requester, accepter);
    }

    /**
     * Denies a pending duel request
     */
    public void denyDuelRequest(Player denier) {
        String requesterName = pendingDuelRequests.remove(denier);
        if (requesterName == null) {
            denier.sendMessage("§cYou don't have any pending duel requests");
            return;
        }

        Player requester = Bukkit.getPlayer(requesterName);
        if (requester != null) {
            requester.sendMessage("§c" + denier.getName() + " has denied your duel request");
        }
        denier.sendMessage("§aYou denied the duel request");
    }

    /**
     * Opens arena selection GUI (skips if only one arena)
     */
    private void openArenaSelectionGUI(Player player1, Player player2) {
        Map<String, ArenaManager.Arena> arenas = plugin.getArenaManager().getAllArenas();
        
        if (arenas.isEmpty()) {
            player1.sendMessage("§cNo arenas available for dueling");
            return;
        }

        if (arenas.size() == 1) {
            String arenaName = arenas.keySet().iterator().next();
            openTimeLimitGUI(player1, player2, arenaName);
        } else {
            // Open arena selection GUI
            GUIManager.openArenaSelectionGUI(plugin, player1, player2);
        }
    }

    /**
     * Opens time limit selection GUI
     */
    public void openTimeLimitGUI(Player player1, Player player2, String arenaName) {
        GUIManager.openTimeLimitGUI(plugin, player1, player2, arenaName);
    }

    /**
     * Opens confirmation GUI before starting match
     */
    public void openConfirmationGUI(Player player1, Player player2, String arenaName, int timeLimit) {
        GUIManager.openConfirmationGUI(plugin, player1, player2, arenaName, timeLimit);
    }

    /**
     * Starts a duel match between two players
     */
    public void startMatch(Player player1, Player player2, String arenaName, int timeLimitSeconds) {
        ArenaManager.Arena arena = plugin.getArenaManager().getArena(arenaName);
        if (arena == null) {
            player1.sendMessage("§cArena not found");
            player2.sendMessage("§cArena not found");
            return;
        }

        String matchId = UUID.randomUUID().toString();
        Match match = new Match(matchId, player1, player2, arena, timeLimitSeconds, plugin);
        activeMatches.put(matchId, match);

        // Snapshot the arena
        plugin.getArenaManager().snapshotArena(arenaName);

        // Start countdown
        match.startCountdown();
    }

    /**
     * Ends a match and handles winner/loser logic
     */
    public void endMatch(String matchId, Player winner, Player loser) {
        Match match = activeMatches.get(matchId);
        if (match == null) return;

        winner.sendMessage("§a§lYou won the duel! You have 60 seconds to pick up loot.");
        winner.sendMessage("§eType §a/duel leave §eto return to spawn");

        // Schedule auto-return to spawn after 60 seconds
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            if (winner.isOnline()) {
                returnToSpawn(winner);
            }
            plugin.getArenaManager().restoreArena(match.arena.getName());
            activeMatches.remove(matchId);
        }, 1200L); // 60 seconds

        // Remove loser from arena
        if (loser.isOnline()) {
            loser.teleport(Bukkit.getWorld("world").getSpawnLocation());
        }
    }

    /**
     * Returns a player to spawn (integrates with EssentialsX)
     */
    private void returnToSpawn(Player player) {
        try {
            Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
            if (ess != null) {
                player.teleport(ess.getUsers().getUser(player.getUniqueId()).getHome("home"));
                player.sendMessage("§aReturned to spawn!");
            }
        } catch (Exception e) {
            // Fallback to world spawn
            player.teleport(player.getWorld().getSpawnLocation());
        }
    }

    /**
     * Allows player to manually leave after duel
     */
    public void leaveMatch(Player player) {
        Match match = getPlayerMatch(player);
        if (match == null) {
            player.sendMessage("§cYou are not in a match");
            return;
        }

        if (!match.isFinished) {
            player.sendMessage("§cYou cannot leave while the match is ongoing. Forfeit will kill you!");
            return;
        }

        returnToSpawn(player);
        activeMatches.remove(match.matchId);
    }

    /**
     * Gets the match a player is currently in
     */
    public Match getPlayerMatch(Player player) {
        for (Match match : activeMatches.values()) {
            if (match.player1.equals(player) || match.player2.equals(player)) {
                return match;
            }
        }
        return null;
    }

    /**
     * Sends a draw request
     */
    public void sendDrawRequest(Player requester, Player opponent) {
        Match match = getPlayerMatch(requester);
        if (match == null) {
            requester.sendMessage("§cYou are not in a match");
            return;
        }

        pendingDrawRequests.put(opponent, requester.getName());
        opponent.sendMessage("§e" + requester.getName() + " §ehas requested a draw!");
        opponent.sendMessage("§eType §a/duel acceptdraw §eto accept or §c/duel denydraw §eto deny");
        requester.sendMessage("§aDraw request sent");
    }

    /**
     * Accepts a draw request
     */
    public void acceptDraw(Player accepter) {
        String requesterName = pendingDrawRequests.remove(accepter);
        if (requesterName == null) {
            accepter.sendMessage("§cYou don't have any pending draw requests");
            return;
        }

        Player requester = Bukkit.getPlayer(requesterName);
        if (requester == null) {
            accepter.sendMessage("§cThe player who requested the draw is no longer online");
            return;
        }

        Match match = getPlayerMatch(accepter);
        if (match != null) {
            match.isFinished = true;
            requester.sendMessage("§aThe match ended in a draw!");
            accepter.sendMessage("§aThe match ended in a draw!");
            returnToSpawn(requester);
            returnToSpawn(accepter);
            plugin.getArenaManager().restoreArena(match.arena.getName());
            activeMatches.remove(match.matchId);
        }
    }

    /**
     * Denies a draw request
     */
    public void denyDraw(Player denier) {
        String requesterName = pendingDrawRequests.remove(denier);
        if (requesterName == null) {
            denier.sendMessage("§cYou don't have any pending draw requests");
            return;
        }

        Player requester = Bukkit.getPlayer(requesterName);
        if (requester != null) {
            requester.sendMessage("§c" + denier.getName() + " has denied your draw request");
        }
        denier.sendMessage("§aYou denied the draw request");
    }

    /**
     * Cancels all active matches
     */
    public void cancelAllMatches() {
        for (Match match : activeMatches.values()) {
            plugin.getArenaManager().restoreArena(match.arena.getName());
        }
        activeMatches.clear();
    }

    /**
     * Inner class representing an active match
     */
    public static class Match {
        public String matchId;
        public Player player1;
        public Player player2;
        public ArenaManager.Arena arena;
        public int timeLimitSeconds;
        public boolean isFinished = false;
        public int countdownSeconds = 0;
        private DuelingPlugin plugin;
        private BukkitTask matchTask;
        private BukkitTask countdownTask;

        public Match(String matchId, Player player1, Player player2, ArenaManager.Arena arena, 
                     int timeLimitSeconds, DuelingPlugin plugin) {
            this.matchId = matchId;
            this.player1 = player1;
            this.player2 = player2;
            this.arena = arena;
            this.timeLimitSeconds = timeLimitSeconds;
            this.plugin = plugin;
        }

        /**
         * Starts the countdown before match begins
         */
        public void startCountdown() {
            countdownSeconds = 3;
            countdownTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (countdownSeconds > 0) {
                    player1.sendMessage("§e" + countdownSeconds + "...");
                    player2.sendMessage("§e" + countdownSeconds + "...");
                    countdownSeconds--;
                } else {
                    countdownTask.cancel();
                    startMatch();
                }
            }, 0L, 20L);
        }

        /**
         * Starts the actual match
         */
        private void startMatch() {
            // Teleport players to arena center
            Location spawn1 = arena.getPos1().clone().add(2, 0.5, 2);
            Location spawn2 = arena.getPos2().clone().add(-2, 0.5, -2);

            player1.teleport(spawn1);
            player2.teleport(spawn2);

            player1.sendMessage("§a§lMatch started! Time limit: " + timeLimitSeconds + " seconds");
            player2.sendMessage("§a§lMatch started! Time limit: " + timeLimitSeconds + " seconds");

            // Start match timer
            matchTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                timeLimitSeconds--;

                if (timeLimitSeconds <= 0) {
                    matchTask.cancel();
                    isFinished = true;
                    player1.sendMessage("§c§lMatch time limit reached! Match ended in a draw.");
                    player2.sendMessage("§c§lMatch time limit reached! Match ended in a draw.");
                    plugin.getMatchHandler().endMatch(matchId, player1, player2);
                }
            }, 20L, 20L);
        }
    }
}