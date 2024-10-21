package foundation.esoteric.minecraft.plugins.games.fireworkwars.items;

public enum ItemType {
    ARMOR(0.8D),
    BLOCK(0.2D),
    CONSUMABLE(0.6D),
    EXPLOSIVE(0.4D),
    GUN(1.0D),
    AMMO(0.2D),
    MISC(0.35D);

    private final double maxChestValue;

    ItemType(double maxChestValue) {
        this.maxChestValue = maxChestValue;
    }

    public double getMaxChestPercentage() {
        return maxChestValue;
    }
}
