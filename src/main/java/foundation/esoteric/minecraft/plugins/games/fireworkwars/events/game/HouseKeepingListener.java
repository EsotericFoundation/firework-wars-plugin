package foundation.esoteric.minecraft.plugins.games.fireworkwars.events.game;

import com.destroystokyo.paper.event.block.BlockDestroyEvent;
import com.destroystokyo.paper.event.player.PlayerSetSpawnEvent;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.FireworkWarsGame;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team.TeamPlayer;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
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
    public void onBlockDestroy(@NotNull BlockDestroyEvent event) {
        World world = event.getBlock().getWorld();

        if (!game.isPlaying() && game.usesWorld(world.getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        World world = event.getPlayer().getWorld();

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

        if (!game.usesWorld(player.getWorld().getName())) {
            teamPlayer.teleportToLobby();
            teamPlayer.unregister(true);
        }
    }

    @EventHandler
    public void onPlayerSpawnChange(PlayerSetSpawnEvent event) {
        World world = event.getLocation().getWorld();

        if (game.usesWorld(world.getName())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCauldronLevelChange(CauldronLevelChangeEvent event) {
        World world = event.getBlock().getWorld();

        if (game.usesWorld(world.getName())) {
            if (event.getReason() == CauldronLevelChangeEvent.ChangeReason.EXTINGUISH) {
                if (event.getEntity() instanceof Entity entity) {
                    entity.setFireTicks(0);
                }
            }

            event.setCancelled(true);
        }
    }
}
