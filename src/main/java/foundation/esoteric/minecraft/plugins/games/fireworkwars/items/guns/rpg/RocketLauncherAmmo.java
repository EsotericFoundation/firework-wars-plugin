package foundation.esoteric.minecraft.plugins.games.fireworkwars.items.guns.rpg;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.guns.BaseAmmoItem;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.Message;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Util;

public class RocketLauncherAmmo extends BaseAmmoItem<FireworkMeta> {

    public RocketLauncherAmmo(FireworkWarsPlugin plugin) {
        super(plugin, "rocket_launcher_ammo", Material.FIREWORK_ROCKET, 35, 4);
    }

    @Override
    public ItemStack getItem(Player player) {
        return getBaseAmmoBuilder(player)
            .setName(Message.ROCKET_LAUNCHER_AMMO, player)
            .setLore(Message.ROCKET_LAUNCHER_AMMO_LORE, player)
            .build();
    }

    @Override
    public int getStackAmount() {
        return Util.randomInt(1, 2);
    }
}
