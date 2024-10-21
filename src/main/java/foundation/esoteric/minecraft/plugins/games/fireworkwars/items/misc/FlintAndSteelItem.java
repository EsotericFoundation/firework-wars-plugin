package foundation.esoteric.minecraft.plugins.games.fireworkwars.items.misc;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.AbstractItem;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.ItemType;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.Message;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.ItemBuilder;

public class FlintAndSteelItem extends AbstractItem<ItemMeta> {
    public FlintAndSteelItem(FireworkWarsPlugin plugin) {
        super(plugin, "flint_and_steel", Material.FLINT_AND_STEEL, 30, 5, ItemType.MISC);
    }

    @Override
    public ItemStack getItem(Player player) {
        return new ItemBuilder<>(plugin, itemMaterial)
            .setName(Message.FLINT_AND_STEEL, player)
            .setLore(Message.FLINT_AND_STEEL_LORE, player)
            .setUnbreakable(true)
            .modifyMeta(this::modifyMeta)
            .build();
    }

    @Override
    public int getStackAmount() {
        return 1;
    }
}
