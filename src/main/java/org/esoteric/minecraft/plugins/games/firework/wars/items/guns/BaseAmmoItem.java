package org.esoteric.minecraft.plugins.games.firework.wars.items.guns;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.esoteric.minecraft.plugins.games.firework.wars.FireworkWarsPlugin;
import org.esoteric.minecraft.plugins.games.firework.wars.items.AbstractItem;
import org.esoteric.minecraft.plugins.games.firework.wars.items.ItemType;
import org.esoteric.minecraft.plugins.games.firework.wars.util.ItemBuilder;
import org.esoteric.minecraft.plugins.games.firework.wars.util.Keys;

public abstract class BaseAmmoItem<M extends ItemMeta> extends AbstractItem<M> {

    public BaseAmmoItem(FireworkWarsPlugin plugin, String ammoId, Material ammoMaterial, int weight, int value) {
        super(plugin, ammoId, ammoMaterial, weight, value, ItemType.AMMO);
    }

    protected ItemBuilder<ItemMeta> getBaseAmmoBuilder(Player player) {
        return new ItemBuilder<>(plugin, itemMaterial)
            .modifyMeta(meta -> {
                pdcManager.setStringValue(meta, customItemIdKey, itemId);

                if (player != null) {
                    pdcManager.setUUIDValue(meta, Keys.AMMO_OWNER_UUID, player.getUniqueId());
                } else {
                    pdcManager.setStringValue(meta, Keys.AMMO_OWNER_UUID, "");
                }
            });
    }
}
