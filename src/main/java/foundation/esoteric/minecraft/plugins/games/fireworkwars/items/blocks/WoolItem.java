package foundation.esoteric.minecraft.plugins.games.fireworkwars.items.blocks;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team.TeamPlayer;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.AbstractItem;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.ItemType;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.Message;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.ItemBuilder;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Util;

import java.util.List;

public class WoolItem extends AbstractItem<ItemMeta> {
    public WoolItem(FireworkWarsPlugin plugin) {
        super(plugin, "wool", Material.WHITE_WOOL, 105, 2, ItemType.BLOCK);
    }

    @Override
    public ItemStack getItem(Player player) {
        TeamPlayer teamPlayer = TeamPlayer.from(player);
        Material material;

        if (teamPlayer == null) {
            material = itemMaterial;
        } else {
            material = teamPlayer.getTeam().getWoolMaterial();
        }

        return new ItemBuilder<>(plugin, material)
            .setName(Message.WOOL, player)
            .setLore(Message.WOOL_LORE, player)
            .modifyMeta(this::modifyMeta)
            .build();
    }

    @Override
    public int getStackAmount() {
        return Util.randomElement(List.of(32, 32, 48, 48, 64));
    }

    @Override
    public void updateItemTexts(ItemStack item, Player player) {
        item.setItemMeta(getItem(player).getItemMeta());
    }
}
