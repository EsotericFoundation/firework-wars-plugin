package org.esoteric.minecraft.plugins.games.fireworkwars.items.explosives;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import org.esoteric.minecraft.plugins.games.fireworkwars.items.AbstractItem;
import org.esoteric.minecraft.plugins.games.fireworkwars.items.ItemType;
import org.esoteric.minecraft.plugins.games.fireworkwars.language.Message;
import org.esoteric.minecraft.plugins.games.fireworkwars.util.ItemBuilder;
import org.esoteric.minecraft.plugins.games.fireworkwars.util.Util;

public class TNTItem extends AbstractItem<ItemMeta> {
    public TNTItem(FireworkWarsPlugin plugin) {
        super(plugin, "tnt", Material.TNT, 45, 9, ItemType.EXPLOSIVE);
    }

    @Override
    public ItemStack getItem(Player player) {
        return new ItemBuilder<>(plugin, itemMaterial)
            .setName(Message.TNT, player)
            .setLore(Message.TNT_LORE, player)
            .modifyMeta(this::modifyMeta)
            .build();
    }

    @Override
    public int getStackAmount() {
        return Util.randomInt(4, 5);
    }

    @Override
    public void updateItemTexts(ItemStack item, Player player) {
        item.setItemMeta(getItem(player).getItemMeta());
    }
}
