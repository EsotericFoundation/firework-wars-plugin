package org.esoteric.minecraft.plugins.fireworkwars.events.global;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.esoteric.minecraft.plugins.fireworkwars.FireworkWarsPlugin;

public class PlayerLoseHungerListener implements Listener {
    private final FireworkWarsPlugin plugin;

    public PlayerLoseHungerListener(FireworkWarsPlugin plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerLoseHunger(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
}
