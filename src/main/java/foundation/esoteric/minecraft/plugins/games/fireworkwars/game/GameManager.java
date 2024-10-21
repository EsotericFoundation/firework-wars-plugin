package foundation.esoteric.minecraft.plugins.games.fireworkwars.game;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.structure.Arena;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameManager implements Listener {

    private final FireworkWarsPlugin plugin;

    private final Map<Arena, FireworkWarsGame> games = new HashMap<>();

    public GameManager(FireworkWarsPlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public boolean hasOngoingGame(Arena arena) {
        return games.containsKey(arena);
    }

    public FireworkWarsGame getFireworkWarsGame(Arena arena) {
        FireworkWarsGame game = games.get(arena);

        if (game == null) {
            game = new FireworkWarsGame(plugin, arena);
            games.put(arena, game);
        }

        return games.get(arena);
    }

    public FireworkWarsGame getFireworkWarsGame(Player player) {
        return games.values().stream()
                .filter(game -> game.containsPlayer(player))
                .findFirst()
                .orElse(null);
    }

    public FireworkWarsGame getFireworkWarsGame(String worldName) {
        return games.values().stream()
                .filter(game -> game.usesWorld(worldName))
                .findFirst()
                .orElse(null);
    }

    @EventHandler
    public void onWorldReload(WorldLoadEvent event) {
        String worldName = event.getWorld().getName();

        FireworkWarsGame game = getFireworkWarsGame(worldName);
        if (game == null) return;

        game.getWorldLoadStates().put(worldName, true);

        List<Boolean> values = List.copyOf(game.getWorldLoadStates().values());
        if (values.stream().allMatch(Boolean::booleanValue)) {
            game.setGameState(FireworkWarsGame.GameState.WAITING);
        }
    }
}
