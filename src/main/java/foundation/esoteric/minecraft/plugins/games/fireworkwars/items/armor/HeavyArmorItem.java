package foundation.esoteric.minecraft.plugins.games.fireworkwars.items.armor;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team.TeamPlayer;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.AbstractItem;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.ItemType;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.Message;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.ItemBuilder;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Keys;

public class HeavyArmorItem extends AbstractItem<LeatherArmorMeta> {
    public HeavyArmorItem(FireworkWarsPlugin plugin) {
        super(plugin, "heavy_armor", Material.LEATHER_CHESTPLATE, 30, 20, ItemType.ARMOR);
    }

    @Override
    public ItemStack getItem(Player player) {
        TeamPlayer teamPlayer = TeamPlayer.from(player);
        Color color;

        if (teamPlayer == null) {
            color = Color.WHITE;
        } else {
            color = teamPlayer.getTeam().getTeamData().getColor();
        }

        return new ItemBuilder<LeatherArmorMeta>(plugin, itemMaterial)
            .setName(Message.HEAVY_ARMOR, player)
            .setLore(Message.HEAVY_ARMOR_LORE, player)
            .setUnbreakable(true)
            .modifyMeta(meta -> modifyArmorMeta(meta, color))
            .build();
    }

    @SuppressWarnings("UnstableApiUsage")
    private void modifyArmorMeta(LeatherArmorMeta meta, Color color) {
        super.modifyMeta(meta);
        meta.setColor(color);
        meta.addEnchant(Enchantment.BLAST_PROTECTION, 1, true);

        AttributeModifier modifier = new AttributeModifier(
            Keys.HEAVY_ARMOR_ATTRIBUTE_MOD,
            7,
            AttributeModifier.Operation.ADD_NUMBER,
            EquipmentSlotGroup.CHEST);

        meta.addAttributeModifier(Attribute.GENERIC_ARMOR, modifier);
        meta.addItemFlags(ItemFlag.HIDE_DYE);
    }

    @Override
    public int getStackAmount() {
        return 1;
    }
}
