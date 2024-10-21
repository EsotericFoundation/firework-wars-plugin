package org.esoteric.minecraft.plugins.games.fireworkwars.items.explosives;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import org.esoteric.minecraft.plugins.games.fireworkwars.items.AbstractItem;
import org.esoteric.minecraft.plugins.games.fireworkwars.items.ItemType;
import org.esoteric.minecraft.plugins.games.fireworkwars.language.Message;
import org.esoteric.minecraft.plugins.games.fireworkwars.util.ItemBuilder;
import org.esoteric.minecraft.plugins.games.fireworkwars.util.Util;

public class ThrowableTNTItem extends AbstractItem<ItemMeta> {
    public ThrowableTNTItem(FireworkWarsPlugin plugin) {
        super(plugin, "throwable_tnt", Material.TNT, 55, 9, ItemType.EXPLOSIVE);
    }

    @Override
    public ItemStack getItem(Player player) {
        return new ItemBuilder<>(plugin, itemMaterial)
            .setName(Message.THROWABLE_TNT, player)
            .setLore(Message.THROWABLE_TNT_LORE, player)
            .setEnchanted(true)
            .modifyMeta(this::modifyMeta)
            .build();
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onRightClick(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        if (event.getClickedBlock() != null && event.getClickedBlock().getType().isInteractable()) {
            return;
        }

        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (!isValidCustomItem(item)) {
            return;
        }

        if (Util.usedInteractableItem(event)) {
            return;
        }

        item.setAmount(item.getAmount() - 1);

        TNTPrimed tnt = player.getWorld().spawn(player.getEyeLocation(), TNTPrimed.class);

        tnt.setSource(player);
        tnt.setFuseTicks(50);

        Vector velocity = plugin.getPlayerVelocityManager().getPlayerVelocity(player);

        Vector playerDirection = player.getLocation().getDirection().clone();
        Vector playerDirectionWithVelocity = playerDirection.clone().add(velocity);

        tnt.setVelocity(playerDirectionWithVelocity.multiply(0.75D));

        event.setCancelled(true);
    }

    @Override
    public int getStackAmount() {
        return Util.randomInt(3, 4);
    }
}
