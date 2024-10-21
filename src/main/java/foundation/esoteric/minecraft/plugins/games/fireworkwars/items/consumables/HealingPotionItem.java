package foundation.esoteric.minecraft.plugins.games.fireworkwars.items.consumables;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.AbstractItem;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.ItemType;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.Message;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.ItemBuilder;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Util;

public class HealingPotionItem extends AbstractItem<PotionMeta> {
    public HealingPotionItem(FireworkWarsPlugin plugin) {
        super(plugin, "healing_potion", Material.SPLASH_POTION, 75, 5, ItemType.CONSUMABLE);
    }

    @Override
    public ItemStack getItem(Player player) {
        return new ItemBuilder<PotionMeta>(plugin, itemMaterial)
            .setName(Message.HEALING_POTION, player)
            .setLore(Message.HEALING_POTION_LORE, player)
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
        return Util.randomInt(1, 2);
    }
}
