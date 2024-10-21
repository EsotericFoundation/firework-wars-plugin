package foundation.esoteric.minecraft.plugins.games.fireworkwars.items.guns.shotgun;

import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.FireworkWarsGame;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team.FireworkWarsTeam;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team.TeamPlayer;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.guns.BaseGunItem;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.Message;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Util;

import java.util.List;

public class FireworkShotgunItem extends BaseGunItem {
    public FireworkShotgunItem(FireworkWarsPlugin plugin) {
        super(plugin, "firework_shotgun", "firework_shotgun_ammo", 20, 28);
    }

    @Override
    public ItemStack getItem(Player player) {
        return getBaseCrossbowBuilder()
            .setName(Message.FIREWORK_SHOTGUN, player)
            .setLore(Message.FIREWORK_SHOTGUN_LORE, player)
            .build();
    }

    @Override
    protected void onCrossbowLoad(Player player, FireworkWarsGame game, EntityLoadCrossbowEvent event) {
        TeamPlayer teamPlayer = TeamPlayer.from(player.getUniqueId());
        FireworkWarsTeam team = teamPlayer.getTeam();

        ItemStack firework = createFirework(team.getTeamData().getColor(), 6, 1);
        editCrossbowMeta(event.getCrossbow(), meta -> meta.setChargedProjectiles(List.of(
            firework.clone(),
            firework.clone(),
            firework.clone(),
            firework.clone(),
            firework.clone(),
            firework.clone(),
            firework.clone())));
    }

    @Override
    protected void onCrossbowShoot(Player player, FireworkWarsGame game, EntityShootBowEvent event) {
        if (!(event.getProjectile() instanceof Firework firework)) {
            return;
        }

        if (!getAmmoItem().isValidCustomItem(firework.getItem())) {
            return;
        }

        Vector newVelocity = firework.getVelocity().clone()
            .rotateAroundY(Util.randomDouble(-Math.PI / 12.0D, Math.PI / 12.0D))
            .multiply(new Vector(1.0D, Util.randomDouble(0.9D, 1.1D), 1.0D));

        firework.setVelocity(newVelocity);
        firework.setTicksToDetonate((int) Math.round(9 + Util.randomDouble(-1.5, 1.5)));
    }

    @Override
    public int getStackAmount() {
        return 1;
    }
}
