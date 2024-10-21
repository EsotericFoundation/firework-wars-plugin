package foundation.esoteric.minecraft.plugins.games.fireworkwars.commands;

import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.ServerOperator;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.CustomItemManager;
import org.jetbrains.annotations.NotNull;

public class ResetInventoryCommand extends CommandAPICommand {

    private final CustomItemManager manager;

    public ResetInventoryCommand(@NotNull FireworkWarsPlugin plugin) {
        super("reset");

        withRequirement(ServerOperator::isOp);

        this.manager = plugin.getCustomItemManager();

        executesPlayer((player, args) -> {
            giveItems(player);
            player.sendMessage(Component.text("Inventory reset!").color(NamedTextColor.GREEN));
        });

        register(plugin);
    }

    public void giveItems(@NotNull Player player) {
        player.getInventory().clear();

        ItemStack item1 = manager.getItem("firework_rifle").getItem(player);
        ItemStack item2 = manager.getItem("firework_rifle_ammo").getItem(player, 20);

        player.getInventory().setItem(0, item1);
        player.getInventory().setItem(9, item2);
    }
}
