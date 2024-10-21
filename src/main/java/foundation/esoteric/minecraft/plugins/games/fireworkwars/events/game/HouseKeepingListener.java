package foundation.esoteric.minecraft.plugins.games.fireworkwars.events.game;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.FireworkWarsGame;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team.TeamPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public class HouseKeepingListener implements Listener {
    private final FireworkWarsPlugin plugin;
    private final FireworkWarsGame game;

    public HouseKeepingListener(FireworkWarsPlugin plugin, FireworkWarsGame game) {
        this.plugin = plugin;
        this.game = game;
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    public void onPlayerWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (!game.isAlive(player)) {
            return;
        }

        TeamPlayer teamPlayer = TeamPlayer.from(player);
        teamPlayer.showWorldBorder();
    }

    @EventHandler
    public void onPlayerSpawnChange(PlayerSetSpawnEvent event) {
        Player player = event.getPlayer();

        if (!game.isAlive(player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onCauldronLevelChange(CauldronLevelChangeEvent event) {
        if (event.getEntity() instanceof Player player && !game.isAlive(player)) {
            return;
        }

        event.setCancelled(true);
    }
}
