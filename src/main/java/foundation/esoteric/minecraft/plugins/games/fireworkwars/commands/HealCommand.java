package foundation.esoteric.minecraft.plugins.games.fireworkwars.commands;

import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.permissions.ServerOperator;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import org.jetbrains.annotations.NotNull;

public class HealCommand extends CommandAPICommand {
    public HealCommand(FireworkWarsPlugin plugin) {
        super("heal");

        setRequirements(ServerOperator::isOp);

        executesPlayer((player, args) -> {
            healPlayer(player);
            player.sendMessage(Component.text("You have been healed!").color(NamedTextColor.GREEN));
        });

        register(plugin);
    }

    public void healPlayer(@NotNull Player player) {
        if (!player.isOnline()) {
            return;
        }

        player.setHealth(20.0D);
        player.setFoodLevel(20);
        player.setSaturation(20.0F);

        player.clearActivePotionEffects();
    }
}
