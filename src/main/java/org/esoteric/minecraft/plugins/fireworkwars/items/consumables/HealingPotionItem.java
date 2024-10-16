package org.esoteric.minecraft.plugins.fireworkwars.items.consumables;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.esoteric.minecraft.plugins.fireworkwars.FireworkWarsPlugin;
import org.esoteric.minecraft.plugins.fireworkwars.items.AbstractItem;
import org.esoteric.minecraft.plugins.fireworkwars.items.ItemType;
import org.esoteric.minecraft.plugins.fireworkwars.util.ItemBuilder;

public class HealingPotionItem extends AbstractItem<PotionMeta> {
    public HealingPotionItem(FireworkWarsPlugin plugin) {
        super(plugin, "healing_potion", Material.SPLASH_POTION, 75, 5, ItemType.CONSUMABLE);
    }

    @Override
    public ItemStack getItem(Player player) {
        return new ItemBuilder<PotionMeta>(plugin, itemMaterial)
            .modifyMeta(this::modifyMeta)
            .build();
    }

    @Override
    protected void modifyMeta(PotionMeta meta) {
        super.modifyMeta(meta);
        meta.setBasePotionType(PotionType.STRONG_HEALING);
        meta.setMaxStackSize(3);
    }

    @Override
    public int getStackAmount() {
        return 1;
    }
}
