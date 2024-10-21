package foundation.esoteric.minecraft.plugins.games.fireworkwars.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerVelocityManager implements Listener {

    private final Map<UUID, Vector> playerVelocityMap = new HashMap<>();
    private final Map<UUID, Vector> playerPreviousPositionMap = new HashMap<>();

    public PlayerVelocityManager(@NotNull FireworkWarsPlugin plugin) {
        plugin.runTaskTimer(() -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                Vector previousPosition = playerPreviousPositionMap.get(player.getUniqueId());

                if (previousPosition == null) {
                    playerPreviousPositionMap.put(player.getUniqueId(), player.getLocation().toVector());
                    continue;
                }

                Vector velocity = player.getLocation().toVector().subtract(previousPosition);
                playerVelocityMap.put(player.getUniqueId(), velocity);
                playerPreviousPositionMap.put(player.getUniqueId(), player.getLocation().toVector());
            }
        }, 0L, 1L);

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public Vector getPlayerVelocity(@NotNull Player player) {
        return playerVelocityMap.get(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        playerVelocityMap.remove(event.getPlayer().getUniqueId());
        playerPreviousPositionMap.remove(event.getPlayer().getUniqueId());
    }
}
