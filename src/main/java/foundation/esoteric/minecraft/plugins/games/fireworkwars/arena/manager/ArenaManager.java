package foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.structure.Arena;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.structure.ArenaInformation;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.structure.Lobby;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class ArenaManager {
    private static final String ARENAS_RESOURCE_PATH = "arenas.json";

    private final FireworkWarsPlugin plugin;

    private List<Lobby> lobbies;
    private List<Arena> arenas;

    public List<Lobby> getLobbies() {
        return lobbies;
    }

    public List<Arena> getArenas() {
        return arenas;
    }

    public boolean isLobby(World world) {
        return lobbies.stream()
            .anyMatch(lobby -> lobby.getWorld().equals(world.getName()));
    }

    public boolean isArena(World world) {
        return arenas.stream()
            .anyMatch(arena -> arena.getWorlds().contains(world.getName()));
    }

    public Location getFirstLobbySpawnLocation() {
        return lobbies.get(0).getSpawnLocation().getBukkitLocation();
    }

    public ArenaManager(FireworkWarsPlugin plugin) {
        this.plugin = plugin;

        loadArenasFromConfig();
        loadWorldsWithoutAutoSave();
    }

    private void loadArenasFromConfig() {
        plugin.saveResource(ARENAS_RESOURCE_PATH, true);

        String arenasFilePath = plugin.getDataFolder().getPath() + File.separator + ARENAS_RESOURCE_PATH;
        File file = new File(arenasFilePath);

        Gson gson = new GsonBuilder()
                .create();

        try (Reader reader = new FileReader(file)) {
            ArenaInformation arenaInformation = gson.fromJson(reader, ArenaInformation.class);

            this.arenas = arenaInformation.getArenas();
            this.lobbies = arenaInformation.getLobbies();
        } catch (IOException exception) {
            plugin.getLogger().severe("Failed to load arenas.json file: " + exception.getMessage());
        }

        new ConfigValidator(plugin, this).validate();
    }

    private void loadWorldsWithoutAutoSave() {
        for (Arena arena : arenas) {
            arena.getWorlds().forEach(worldName -> {
                World world = plugin.getServer().createWorld(new WorldCreator(worldName));

                assert world != null;
                world.setAutoSave(false);
            });
        }
    }
}
