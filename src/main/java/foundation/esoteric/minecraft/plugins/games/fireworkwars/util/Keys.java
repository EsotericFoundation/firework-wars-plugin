package foundation.esoteric.minecraft.plugins.games.fireworkwars.util;

import org.bukkit.NamespacedKey;

public final class Keys {
    public static final NamespacedKey CUSTOM_ITEM_DATA = fromString("custom_item_data");

    public static final NamespacedKey CUSTOM_ITEM_ID = fromString("custom_item_id");
    public static final NamespacedKey AMMO_OWNER_UUID = fromString("ammo_owner_uuid");
    public static final NamespacedKey GUN_ACCEPTED_AMMO_ID = fromString("gun_accepted_ammo_id");
    public static final NamespacedKey PLAYER_COMPASS_ID = fromString("player_compass_id");

    public static final NamespacedKey MILITARY_HELMET_ATTRIBUTE_MOD = fromString("military_helmet_attribute_mod");
    public static final NamespacedKey HEAVY_ARMOR_ATTRIBUTE_MOD = fromString("heavy_armor_attribute_mod");

    private Keys() {}

    public static NamespacedKey fromString(String key) {
        return new NamespacedKey("firework-wars-plugin", key);
    }
}
