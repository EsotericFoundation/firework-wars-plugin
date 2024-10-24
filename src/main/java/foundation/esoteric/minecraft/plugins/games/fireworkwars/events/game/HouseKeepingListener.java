package foundation.esoteric.minecraft.plugins.games.fireworkwars.events.game;

import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.FireworkWarsGame;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team.TeamPlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.jetbrains.annotations.NotNull;

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
    public void onDamage(@NotNull EntityDamageEvent event) {
        World world = event.getEntity().getWorld();

        if (event.getEntity() instanceof Villager) {
            return;
        }

        if (!game.isPlaying() && game.usesWorld(world.getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        World world = event.getBlock().getWorld();

        if (!game.isPlaying() && game.usesWorld(world.getName())) {
            event.setCancelled(true);
        }
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
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!game.isAlive(player)) {
            return;
        }

        event.setCancelled(true);
        player.setFireTicks(0);
    }
}
