package foundation.esoteric.minecraft.plugins.games.fireworkwars.items;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.LanguageManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Keys;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.PersistentDataManager;
import org.jetbrains.annotations.Contract;

public abstract class AbstractItem<M extends ItemMeta> implements Listener {
    protected final FireworkWarsPlugin plugin;
    protected final MiniMessage MM;

    protected final PersistentDataManager pdcManager;
    protected final LanguageManager languageManager;

    protected final String itemId;
    protected final Material itemMaterial;

    protected final int weight;
    protected final int value;

    protected final ItemType type;

    protected final NamespacedKey customItemIdKey;

    public AbstractItem(FireworkWarsPlugin plugin, String itemId, Material itemMaterial, int weight, int value, ItemType type) {
        this.plugin = plugin;
        this.MM = MiniMessage.miniMessage();

        this.pdcManager = plugin.getPdcManager();
        this.languageManager = plugin.getLanguageManager();

        this.itemId = itemId;
        this.itemMaterial = itemMaterial;

        this.weight = weight;
        this.value = value;

        this.type = type;

        this.customItemIdKey = Keys.CUSTOM_ITEM_ID;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public String getItemId() {
        return itemId;
    }

    public Material getItemMaterial() {
        return itemMaterial;
    }

    public int getWeight() {
        return weight;
    }

    public int getValue() {
        return value;
    }

    public ItemType getType() {
        return type;
    }

    public boolean isConsumable() {
        return type == ItemType.CONSUMABLE;
    }

    public boolean isAmmo() {
        return type == ItemType.AMMO;
    }

    public boolean isBlock() {
        return type == ItemType.BLOCK;
    }

    @Contract("null -> false")
    public boolean isValidCustomItem(ItemStack itemStack) {
        if (itemStack == null) {
            return false;
        }

        if (!itemStack.hasItemMeta()) {
            return false;
        }

        String itemStackItemId = plugin.getPdcManager().getStringValue(itemStack.getItemMeta(), customItemIdKey);
        return itemId.equals(itemStackItemId);
    }

    protected ItemStack getBaseItemStack() {
        return new ItemStack(itemMaterial);
    }

    protected void modifyMeta(M meta) {
        pdcManager.setStringValue(meta, customItemIdKey, itemId);
    }

    public abstract ItemStack getItem(Player player);

    public abstract int getStackAmount();

    public ItemStack getItem(Player player, int amount) {
        ItemStack item = getItem(player);
        item.setAmount(amount);
        return item;
    }

    public void updateItemTexts(ItemStack item) {
        updateItemTexts(item, null);
    }

    public void updateItemTexts(ItemStack item, Player player) {
        ItemStack defaultItem = getItem(player);

        item.editMeta(meta -> {
            meta.displayName(defaultItem.getItemMeta().displayName());
            meta.lore(defaultItem.lore());
        });
    }
}
