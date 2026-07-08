package com.dueling.plugin.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import com.dueling.plugin.DuelingPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages all arena-related operations including creation, saving, and regeneration
 */
public class ArenaManager {

    private final DuelingPlugin plugin;
    private final File arenasFile;
    private YamlConfiguration arenasConfig;
    private Map<String, Arena> loadedArenas = new HashMap<>();
    private Map<String, BlockSnapshot> arenaSnapshots = new HashMap<>();

    public ArenaManager(DuelingPlugin plugin) {
        this.plugin = plugin;
        this.arenasFile = new File(plugin.getDataFolder(), "arenas.yml");
        loadArenas();
    }

    /**
     * Loads all arenas from configuration file
     */
    public void loadArenas() {
        if (!arenasFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                arenasFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        arenasConfig = YamlConfiguration.loadConfiguration(arenasFile);
        loadedArenas.clear();

        for (String key : arenasConfig.getKeys(false)) {
            if (arenasConfig.contains(key + ".pos1") && arenasConfig.contains(key + ".pos2")) {
                Location pos1 = arenasConfig.getLocation(key + ".pos1");
                Location pos2 = arenasConfig.getLocation(key + ".pos2");
                loadedArenas.put(key, new Arena(key, pos1, pos2));
            }
        }

        plugin.getLogger().info("Loaded " + loadedArenas.size() + " arenas");
    }

    /**
     * Saves an arena with given name and two positions
     */
    public void saveArena(String arenaName, Location pos1, Location pos2) {
        arenasConfig.set(arenaName + ".pos1", pos1);
        arenasConfig.set(arenaName + ".pos2", pos2);

        try {
            arenasConfig.save(arenasFile);
            loadedArenas.put(arenaName, new Arena(arenaName, pos1, pos2));
            plugin.getLogger().info("Arena '" + arenaName + "' saved successfully");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets an arena by name
     */
    public Arena getArena(String arenaName) {
        return loadedArenas.get(arenaName);
    }

    /**
     * Gets all available arenas
     */
    public Map<String, Arena> getAllArenas() {
        return new HashMap<>(loadedArenas);
    }

    /**
     * Takes a snapshot of the arena for later restoration
     */
    public void snapshotArena(String arenaName) {
        Arena arena = getArena(arenaName);
        if (arena == null) return;

        BlockSnapshot snapshot = new BlockSnapshot(arena);
        arenaSnapshots.put(arenaName, snapshot);
    }

    /**
     * Restores arena to previously saved snapshot
     */
    public void restoreArena(String arenaName) {
        BlockSnapshot snapshot = arenaSnapshots.get(arenaName);
        if (snapshot == null) return;

        snapshot.restore();
        arenaSnapshots.remove(arenaName);
    }

    /**
     * Inner class representing an Arena
     */
    public static class Arena {
        private String name;
        private Location pos1;
        private Location pos2;

        public Arena(String name, Location pos1, Location pos2) {
            this.name = name;
            this.pos1 = pos1;
            this.pos2 = pos2;
        }

        public String getName() {
            return name;
        }

        public Location getPos1() {
            return pos1;
        }

        public Location getPos2() {
            return pos2;
        }

        public Location getCenter() {
            double x = (pos1.getX() + pos2.getX()) / 2;
            double y = (pos1.getY() + pos2.getY()) / 2;
            double z = (pos1.getZ() + pos2.getZ()) / 2;
            return new Location(pos1.getWorld(), x, y, z);
        }

        public int getMinX() {
            return Math.min(pos1.getBlockX(), pos2.getBlockX());
        }

        public int getMaxX() {
            return Math.max(pos1.getBlockX(), pos2.getBlockX());
        }

        public int getMinY() {
            return Math.min(pos1.getBlockY(), pos2.getBlockY());
        }

        public int getMaxY() {
            return Math.max(pos1.getBlockY(), pos2.getBlockY());
        }

        public int getMinZ() {
            return Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        }

        public int getMaxZ() {
            return Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        }
    }

    /**
     * Inner class for storing block snapshots
     */
    public static class BlockSnapshot {
        private Map<String, org.bukkit.block.data.BlockData> blockStates = new HashMap<>();
        private Arena arena;

        public BlockSnapshot(Arena arena) {
            this.arena = arena;
            captureBlocks();
        }

        /**
         * Captures all block states in the arena
         */
        private void captureBlocks() {
            for (int x = arena.getMinX(); x <= arena.getMaxX(); x++) {
                for (int y = arena.getMinY(); y <= arena.getMaxY(); y++) {
                    for (int z = arena.getMinZ(); z <= arena.getMaxZ(); z++) {
                        Block block = arena.getPos1().getWorld().getBlockAt(x, y, z);
                        String key = x + "," + y + "," + z;
                        blockStates.put(key, block.getBlockData().clone());
                    }
                }
            }
        }

        /**
         * Restores all captured block states
         */
        public void restore() {
            for (Map.Entry<String, org.bukkit.block.data.BlockData> entry : blockStates.entrySet()) {
                String[] coords = entry.getKey().split(",");
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);
                int z = Integer.parseInt(coords[2]);

                Block block = arena.getPos1().getWorld().getBlockAt(x, y, z);
                block.setBlockData(entry.getValue());
            }
        }
    }
}
