package foundation.esoteric.minecraft.plugins.games.fireworkwars.items.guns.rifle;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.guns.BaseAmmoItem;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.Message;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Util;

public class RifleAmmo extends BaseAmmoItem<ItemMeta> {

    public RifleAmmo(FireworkWarsPlugin plugin) {
        super(plugin, "firework_rifle_ammo", Material.GHAST_TEAR, 85, 2);
    }

    @Override
    public ItemStack getItem(Player player) {
        return getBaseAmmoBuilder(player)
            .setName(Message.FIREWORK_RIFLE_AMMO, player)
            .setLore(Message.FIREWORK_RIFLE_AMMO_LORE, player)
            .build();
    }

    @Override
    public int getStackAmount() {
        return Util.randomInt(12, 16);
    }
}
