package org.esoteric.minecraft.plugins.fireworkwars.commands;

import dev.jorel.commandapi.CommandAPICommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.ServerOperator;
import org.esoteric.minecraft.plugins.fireworkwars.FireworkWarsPlugin;
import org.esoteric.minecraft.plugins.fireworkwars.items.CustomItemManager;
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
        ItemStack item2 = manager.getItem("firework_rifle_ammo").getItem(player, 5);

        player.getInventory().addItem(item1, item2);
    }
}
