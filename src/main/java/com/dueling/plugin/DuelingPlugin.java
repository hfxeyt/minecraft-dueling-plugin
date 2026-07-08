package com.dueling.plugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import com.dueling.plugin.managers.ArenaManager;
import com.dueling.plugin.managers.MatchHandler;
import com.dueling.plugin.managers.ConfigManager;
import com.dueling.plugin.commands.DuelCommand;
import com.dueling.plugin.listeners.PlayerInteractionListener;
import com.dueling.plugin.listeners.PlayerDeathListener;
import com.dueling.plugin.listeners.BlockBreakListener;

/**
 * Main plugin class for the Dueling system
 * Handles initialization and registration of all managers and listeners
 */
public class DuelingPlugin extends JavaPlugin {

    private static DuelingPlugin instance;
    private ArenaManager arenaManager;
    private MatchHandler matchHandler;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize config
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        getLogger().info("Config loaded successfully");

        // Initialize managers
        arenaManager = new ArenaManager(this);
        matchHandler = new MatchHandler(this);
        
        getLogger().info("Managers initialized successfully");

        // Register commands
        getCommand("duel").setExecutor(new DuelCommand(this));
        
        // Register listeners
        Bukkit.getPluginManager().registerEvents(new PlayerInteractionListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDeathListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(this), this);

        getLogger().info("§a[Dueling Plugin] Plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (matchHandler != null) {
            matchHandler.cancelAllMatches();
        }
        getLogger().info("§c[Dueling Plugin] Plugin disabled!");
    }

    public static DuelingPlugin getInstance() {
        return instance;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public MatchHandler getMatchHandler() {
        return matchHandler;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}