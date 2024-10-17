package org.esoteric.minecraft.plugins.games.firework.wars.arena.json.components;

import org.bukkit.block.Block;

@SuppressWarnings("unused")
public class ChestLocation extends BlockLocation {
    private int maxTotalValue;
    private int maxValuePerItem;

    public int getMaxTotalValue() {
        return maxTotalValue;
    }

    public int getMaxValuePerItem() {
        return maxValuePerItem;
    }

    public Block getChestBlock() {
        return getBukkitLocation().getBlock();
    }
}
