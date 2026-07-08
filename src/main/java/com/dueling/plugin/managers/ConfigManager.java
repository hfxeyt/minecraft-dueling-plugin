package com.dueling.plugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import com.dueling.plugin.DuelingPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Manages configuration loading and saving
 */
public class ConfigManager {

    private final DuelingPlugin plugin;
    private FileConfiguration config;

    public ConfigManager(DuelingPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Loads the configuration file
     */
    public void loadConfig() {
        // Create default config if doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try (InputStream in = plugin.getResource("config.yml")) {
                if (in != null) {
                    java.nio.file.Files.copy(in, configFile.toPath());
                } else {
                    createDefaultConfig(configFile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        applyDefaults();
    }

    /**
     * Creates a default configuration if none exists
     */
    private void createDefaultConfig(File configFile) throws IOException {
        configFile.createNewFile();
        FileConfiguration defaultConfig = new YamlConfiguration();

        defaultConfig.set("lobby.world", "world");
        defaultConfig.set("lobby.x", 0);
        defaultConfig.set("lobby.y", 64);
        defaultConfig.set("lobby.z", 0);

        defaultConfig.set("messages.duel-request", "§e%player% §ehas challenged you to a duel!");
        defaultConfig.set("messages.match-started", "§a§lMatch started!");
        defaultConfig.set("messages.match-ended", "§c§lMatch ended!");
        defaultConfig.set("messages.player-won", "§a%winner% §ahas won the match!");

        defaultConfig.set("match.default-time-limit", 300);
        defaultConfig.set("match.allow-block-breaking", true);
        defaultConfig.set("match.items-drop-on-death", true);
        defaultConfig.set("match.loot-pickup-time", 60);

        defaultConfig.save(configFile);
    }

    /**
     * Applies default values if keys are missing
     */
    private void applyDefaults() {
        if (!config.contains("lobby")) {
            Location lobbyLoc = Bukkit.getWorld("world").getSpawnLocation();
            config.set("lobby.world", "world");
            config.set("lobby.x", lobbyLoc.getX());
            config.set("lobby.y", lobbyLoc.getY());
            config.set("lobby.z", lobbyLoc.getZ());
        }

        if (!config.contains("match.default-time-limit")) {
            config.set("match.default-time-limit", 300);
        }

        if (!config.contains("match.allow-block-breaking")) {
            config.set("match.allow-block-breaking", true);
        }

        if (!config.contains("match.items-drop-on-death")) {
            config.set("match.items-drop-on-death", true);
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public Location getLobbyLocation() {
        String world = config.getString("lobby.world", "world");
        double x = config.getDouble("lobby.x", 0);
        double y = config.getDouble("lobby.y", 64);
        double z = config.getDouble("lobby.z", 0);
        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public int getDefaultTimeLimit() {
        return config.getInt("match.default-time-limit", 300);
    }

    public boolean isBlockBreakingAllowed() {
        return config.getBoolean("match.allow-block-breaking", true);
    }

    public boolean shouldItemsDropOnDeath() {
        return config.getBoolean("match.items-drop-on-death", true);
    }

    public int getLootPickupTime() {
        return config.getInt("match.loot-pickup-time", 60);
    }

    public void saveConfig() {
        try {
            config.save(new File(plugin.getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}