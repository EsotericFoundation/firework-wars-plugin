package org.esoteric.minecraft.plugins.games.fireworkwars.arena.manager;

import org.bukkit.Bukkit;
import org.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import org.esoteric.minecraft.plugins.games.fireworkwars.arena.json.components.ChestLocation;
import org.esoteric.minecraft.plugins.games.fireworkwars.arena.json.data.EndgameData;
import org.esoteric.minecraft.plugins.games.fireworkwars.arena.json.data.SupplyDropData;
import org.esoteric.minecraft.plugins.games.fireworkwars.arena.json.data.TeamData;
import org.esoteric.minecraft.plugins.games.fireworkwars.arena.json.components.BlockLocation;
import org.esoteric.minecraft.plugins.games.fireworkwars.arena.json.components.PlayerLocation;
import org.esoteric.minecraft.plugins.games.fireworkwars.arena.json.data.WorldBorderData;
import org.esoteric.minecraft.plugins.games.fireworkwars.arena.json.structure.Arena;
import org.esoteric.minecraft.plugins.games.fireworkwars.arena.json.structure.Lobby;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Arrays.stream;

public class ConfigValidator {
    private final FireworkWarsPlugin plugin;
    private final ArenaManager arenaManager;

    private final List<String> lobbyWorlds = new ArrayList<>();
    private final List<String> arenaWorlds = new ArrayList<>();

    public ConfigValidator(FireworkWarsPlugin plugin, ArenaManager arenaManager) {
        this.plugin = plugin;
        this.arenaManager = arenaManager;
    }

    public void validate() {
        List<Lobby> lobbies = arenaManager.getLobbies();
        List<Arena> arenas = arenaManager.getArenas();

        if (lobbies == null || lobbies.isEmpty()) {
            plugin.getLogger().warning("No lobbies found in arenas.json file; Impossible to start games.");
        } else {
            validateLobbies();
        }

        if (arenas == null || arenas.isEmpty()) {
            plugin.getLogger().warning("No arenas found in arenas.json file.");
        } else {
            validateArenas();
        }

        lobbyWorlds.clear();
        arenaWorlds.clear();
    }

    private void validateLobbies() {
        for (Lobby lobby : arenaManager.getLobbies()) {
            String worldName = lobby.getWorld();

            validateString(worldName, "Invalid lobby configuration: No world set.");

            if (lobbyWorlds.contains(worldName)) {
                throw new InvalidConfigurationException("Invalid lobby configuration: Duplicate world names.");
            } else {
                lobbyWorlds.add(worldName);
            }

            if (plugin.getServer().getWorld(worldName) == null) {
                throw new InvalidConfigurationException("Invalid lobby configuration: World " + worldName + " does not exist.");
            }

            validatePlayerLocation(lobby.getSpawnLocation(), worldName);
        }
    }

    private void validateArenas() {
        for (Arena arena : arenaManager.getArenas()) {
            if (arena.getCountDownSeconds() < 0) {
                throw new InvalidConfigurationException("Invalid arena configuration: Count down seconds cannot be negative.");
            }

            if (arena.getGameDurationMinutes() < 0) {
                throw new InvalidConfigurationException("Invalid arena configuration: Game duration minutes cannot be negative.");
            }

            List<String> worldNames = arena.getWorlds();

            if (worldNames == null || worldNames.isEmpty()) {
                throw new InvalidConfigurationException("Invalid arena configuration: No worlds set.");
            }

            for (String worldName : worldNames) {
                if (Bukkit.getWorld(worldName) == null) {
                    throw new InvalidConfigurationException("Invalid arena configuration: World " + worldName + " does not exist.");
                }
            }

            Predicate<String> isAlreadyUsedInArena = arenaWorlds::contains;
            Predicate<String> isAlreadyUsedInLobby = lobbyWorlds::contains;

            if (worldNames.stream().anyMatch(isAlreadyUsedInLobby.and(isAlreadyUsedInArena))) {
                throw new InvalidConfigurationException("Invalid arena configuration: Duplicate world names.");
            } else {
                arenaWorlds.addAll(worldNames);
            }

            validatePlayerLocation(arena.getWaitingAreaLocation(), worldNames.toArray(String[]::new));

            WorldBorderData worldBorderData = arena.getWorldBorderInformation();
            if (worldBorderData == null) {
                throw new InvalidConfigurationException("Invalid arena configuration: No world border data set.");
            }

            if (worldBorderData.getRadius() < 1) {
                throw new InvalidConfigurationException("Invalid world border configuration: Radius must be greater than 0.");
            }

            if (worldBorderData.getEndgameRadius() < 1) {
                throw new InvalidConfigurationException("Invalid world border configuration: Endgame radius must be greater than 0.");
            }

            for (TeamData teamData : arena.getTeamInformation()) {
                try {
                    teamData.getColor();
                } catch (IllegalArgumentException exception) {
                    throw new InvalidConfigurationException("Invalid team configuration: Invalid color.");
                }

                validatePlayerLocation(teamData.getSpawnLocation(), worldNames.toArray(String[]::new));
                validateString(teamData.getMiniMessageString(), "Invalid team configuration: No team name set.");
            }

            for (ChestLocation chestLocation : arena.getChestLocations()) {
                validateBlockLocation(chestLocation, worldNames.toArray(String[]::new));

                if (chestLocation.getMaxValuePerItem() < 2) {
                    throw new InvalidConfigurationException("Invalid chest location configuration: Max value per item cannot be less than 2.");
                }

                if (chestLocation.getMaxTotalValue() < 2) {
                    throw new InvalidConfigurationException("Invalid chest location configuration: Max total value cannot be less than 2.");
                }
            }

            SupplyDropData supplyDropData = arena.getSupplyDropData();
            if (supplyDropData == null) {
                throw new InvalidConfigurationException("Invalid arena configuration: No supply drop data set.");
            }

            if (supplyDropData.getLongestInterval() <= 0) {
                throw new InvalidConfigurationException("Invalid arena configuration: Supply drop longest interval must be greater than 0.");
            }

            if (supplyDropData.getLongestInterval() > arena.getGameDurationTicks()) {
                throw new InvalidConfigurationException("Invalid arena configuration: Supply drop longest possible interval must be less than game duration.");
            }

            supplyDropData.getSupplyDropLocations().forEach(location ->
                    validatePlayerLocation(location, worldNames.toArray(String[]::new)));

            EndgameData endgameData = arena.getEndgameData();
            if (endgameData == null) {
                throw new InvalidConfigurationException("Invalid arena configuration: No endgame data set.");
            }

            if (endgameData.getEndgameStartMinutes() >= arena.getGameDurationMinutes()) {
                throw new InvalidConfigurationException("Invalid arena configuration: Endgame start minutes must be less than game duration.");
            }

            if (endgameData.getWarnBeforeEndgameSeconds() <= 0) {
                throw new InvalidConfigurationException("Invalid arena configuration: Warn before endgame seconds must be greater than 0.");
            }

            if (endgameData.getWarnBeforeGameEndSeconds() <= 0) {
                throw new InvalidConfigurationException("Invalid arena configuration: Warn before game end seconds must be greater than 0.");
            }

            if (arena.getGameDurationTicks() - arena.getEndgameDurationTicks() < endgameData.getWarnBeforeEndgameTicks()) {
                throw new InvalidConfigurationException("Invalid arena configuration: Warning time before endgame must be less than total duration before the endgame starts.");
            }

            if (arena.getEndgameDurationTicks() < endgameData.getWarnBeforeGameEndTicks()) {
                throw new InvalidConfigurationException("Invalid arena configuration: Warning time before game end must be less than total endgame duration.");
            }

            if (endgameData.getWardenSpawnLocation() != null) {
                validatePlayerLocation(endgameData.getWardenSpawnLocation(), worldNames.toArray(String[]::new));
            }

            if (worldBorderData.getSecondsToReachEndgameRadius() > arena.getEndgameDurationTicks() / 20) {
                throw new InvalidConfigurationException("Invalid world border configuration: Seconds to reach endgame radius must be less than endgame duration.");
            }
        }
    }

    private void validatePlayerLocation(PlayerLocation playerLocation, String... worldNames) {
        if (playerLocation == null) {
            throw new InvalidConfigurationException("Invalid player location: No player location set.");
        }

        validateString(playerLocation.getWorldName(), "Invalid player location: No world set.");

        if (stream(worldNames).noneMatch(name -> name.equals(playerLocation.getWorldName()))) {
            throw new InvalidConfigurationException("Invalid player location: Player location world does not match arena world.");
        }
    }

    private void validateBlockLocation(BlockLocation blockLocation, String... worldNames) {
        if (blockLocation == null) {
            throw new InvalidConfigurationException("Invalid block location: No block location set.");
        }

        validateString(blockLocation.getWorldName(), "Invalid block location: No world set.");

        if (stream(worldNames).noneMatch(name -> name.equals(blockLocation.getWorldName()))) {
            throw new InvalidConfigurationException("Invalid block location: Block location world does not match arena world.");
        }
    }

    private void validateString(String string, String message) {
        if (string == null || string.isEmpty()) {
            throw new InvalidConfigurationException(message);
        }
    }

    private static class InvalidConfigurationException extends RuntimeException {
        public InvalidConfigurationException(String message) {
            super(message);
        }
    }
}
