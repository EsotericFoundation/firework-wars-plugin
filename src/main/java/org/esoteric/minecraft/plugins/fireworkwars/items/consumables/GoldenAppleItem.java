package org.esoteric.minecraft.plugins.fireworkwars.items.consumables;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.esoteric.minecraft.plugins.fireworkwars.FireworkWarsPlugin;
import org.esoteric.minecraft.plugins.fireworkwars.items.AbstractItem;
import org.esoteric.minecraft.plugins.fireworkwars.language.Message;
import org.esoteric.minecraft.plugins.fireworkwars.util.ItemBuilder;

public class GoldenAppleItem extends AbstractItem {
    public GoldenAppleItem(FireworkWarsPlugin plugin) {
        super(plugin, "golden_apple", Material.GOLDEN_APPLE, 5, 8);
    }

    @Override
    public ItemStack getItem(Player player) {
        return new ItemBuilder<>(plugin, itemMaterial)
                .setLore(Message.GOLDEN_APPLE_LORE, player)
                .modifyMeta(meta -> pdcManager.setStringValue(meta, customItemIdKey, itemId))
                .build();
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        if (!isValidCustomItem(event.getItem())) {
            return;
        }

        event.getPlayer().addPotionEffect(new PotionEffect(
                PotionEffectType.SPEED, 10 * 20, 0));
    }

    @Override
    public int getStackAmount() {
        return 1;
    }
}