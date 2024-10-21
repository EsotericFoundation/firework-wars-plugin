package foundation.esoteric.minecraft.plugins.games.fireworkwars.items.armor;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.AbstractItem;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.ItemType;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.Message;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.ItemBuilder;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Keys;

public class MilitaryHelmetItem extends AbstractItem<ArmorMeta> {
    public MilitaryHelmetItem(FireworkWarsPlugin plugin) {
        super(plugin, "military_helmet", Material.TURTLE_HELMET, 35, 18, ItemType.ARMOR);
    }

    @Override
    public ItemStack getItem(Player player) {
        return new ItemBuilder<ArmorMeta>(plugin, itemMaterial)
            .setName(Message.MILITARY_HELMET, player)
            .setLore(Message.MILITARY_HELMET_LORE, player)
            .setUnbreakable(true)
            .modifyMeta(this::modifyMeta)
            .build();
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    protected void modifyMeta(ArmorMeta meta) {
        super.modifyMeta(meta);
        meta.addEnchant(Enchantment.BLAST_PROTECTION, 1, true);

        AttributeModifier modifier = new AttributeModifier(
            Keys.MILITARY_HELMET_ATTRIBUTE_MOD,
            5,
            AttributeModifier.Operation.ADD_NUMBER,
            EquipmentSlotGroup.HEAD);

        meta.addAttributeModifier(Attribute.GENERIC_ARMOR, modifier);
    }

    @Override
    public int getStackAmount() {
        return 1;
    }
}
