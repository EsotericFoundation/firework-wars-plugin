package foundation.esoteric.minecraft.plugins.games.fireworkwars.items.guns.rifle;

import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.FireworkWarsGame;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team.FireworkWarsTeam;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team.TeamPlayer;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.guns.BaseGunItem;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.Message;
import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;

import java.util.List;

public class FireworkRifleItem extends BaseGunItem {

    public FireworkRifleItem(FireworkWarsPlugin plugin) {
        super(plugin, "firework_rifle", "firework_rifle_ammo", 30, 21);
    }

    @Override
    public ItemStack getItem(Player player) {
        return getBaseCrossbowBuilder()
            .setName(Message.FIREWORK_RIFLE, player)
            .setLore(Message.FIREWORK_RIFLE_LORE, player)
            .build();
    }

    @Override
    protected void modifyMeta(CrossbowMeta meta) {
        super.modifyMeta(meta);
        meta.addEnchant(Enchantment.QUICK_CHARGE, 3, true);
    }

    @Override
    protected void onCrossbowLoad(Player player, FireworkWarsGame game, EntityLoadCrossbowEvent event) {
        TeamPlayer teamPlayer = TeamPlayer.from(player.getUniqueId());
        FireworkWarsTeam team = teamPlayer.getTeam();

        ItemStack firework = createFirework(team.getTeamData().getColor(), 4, 2);

        editCrossbowMeta(event.getCrossbow(), meta -> meta.setChargedProjectiles(List.of(firework)));
    }

    @Override
    protected void onCrossbowShoot(Player player, FireworkWarsGame game, EntityShootBowEvent event) {

    }

    @Override
    public int getStackAmount() {
        return 1;
    }
}
