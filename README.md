# Dueling Plugin

A hardcore 1v1 dueling system for Minecraft Paper/Spigot servers.

## Features

- Hardcore 1v1 dueling with player inventory management
- Iron Hoe wand for arena creation
- GUI-based arena selection and time limit configuration
- Instant arena regeneration system
- Block breaking restrictions (only player-placed blocks)
- Natural item dropping on death
- EssentialsX integration for spawn teleport
- Draw request system
- Configurable time limits and settings

## Commands

- `/duel` - Open duel menu
- `/duel <player>` - Challenge a player
- `/duel wand` - Get the selection wand
- `/duel setarena <name>` - Create an arena
- `/duel accept` - Accept a duel request
- `/duel deny` - Deny a duel request
- `/duel draw` - Request a draw
- `/duel acceptdraw` - Accept a draw request
- `/duel denydraw` - Deny a draw request
- `/duel leave` - Leave after duel ends
- `/duel reload` - Reload configuration

## Installation

1. Download the latest JAR file
2. Place it in your `plugins` directory
3. Install EssentialsX if not already installed
4. Restart the server
5. Configure `config.yml` as needed

## Requirements

- Minecraft 1.21.1 or later
- Paper/Spigot server
- Java 21 or later
- EssentialsX plugin