package foundation.esoteric.minecraft.plugins.games.fireworkwars.items.misc;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.AbstractItem;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.ItemType;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.Message;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.ItemBuilder;

public class ShearsItem extends AbstractItem<ItemMeta> {
    public ShearsItem(FireworkWarsPlugin plugin) {
        super(plugin, "shears", Material.SHEARS, 35, 8, ItemType.MISC);
    }

    @Override
    public ItemStack getItem(Player player) {
        return new ItemBuilder<>(plugin, itemMaterial)
            .setName(Message.SHEARS, player)
            .setLore(Message.SHEARS_LORE, player)
            .setUnbreakable(true)
            .modifyMeta(this::modifyMeta)
            .build();
    }

    @Override
    protected void modifyMeta(ItemMeta meta) {
        super.modifyMeta(meta);
        meta.addEnchant(Enchantment.EFFICIENCY, 3, true);
    }

    @Override
    public int getStackAmount() {
        return 1;
    }
}
