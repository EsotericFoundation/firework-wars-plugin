package foundation.esoteric.minecraft.plugins.games.fireworkwars.items.nms;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Keys;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.PersistentDataManager;

import java.util.UUID;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
public class CustomCrossbow extends CrossbowItem {
    public static final Item.Properties PROPERTIES = new Item.Properties()
        .stacksTo(1)
        .durability(465)
        .component(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);

    public CustomCrossbow(Item.Properties settings) {
        super(settings);
    }

    public CustomCrossbow() {
        super(PROPERTIES);
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return stack -> this.isValidAmmoItem(stack, false);
    }

    @Override
    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return stack -> this.isValidAmmoItem(stack, true);
    }

    @SuppressWarnings("UnstableApiUsage")
    private boolean isValidAmmoItem(ItemStack stack, boolean offHand) {
        FireworkWarsPlugin plugin = FireworkWarsPlugin.getInstance();

        boolean superResult = offHand
                ? super.getSupportedHeldProjectiles().test(stack)
                : super.getAllSupportedProjectiles().test(stack);

        if (plugin == null) {
            Bukkit.getLogger().severe("Failed to get FireworkWars plugin instance!");
            return superResult;
        }

        PersistentDataManager pdcManager = plugin.getPdcManager();

        org.bukkit.inventory.ItemStack bukkitStack = stack.asBukkitCopy();
        ItemMeta meta = bukkitStack.getItemMeta();

        if (meta == null) {
            return superResult;
        }

        if (!pdcManager.hasKey(meta, Keys.AMMO_OWNER_UUID)) {
            return superResult;
        }

        if (!pdcManager.hasKey(meta, Keys.CUSTOM_ITEM_ID)) {
            return superResult;
        }

        UUID ownerUuid = pdcManager.getUUIDValue(meta, Keys.AMMO_OWNER_UUID);
        
        if (ownerUuid == null) {
            return superResult;
        }
        
        Player owner = plugin.getServer().getPlayer(ownerUuid);

        if (owner == null) {
            return superResult;
        }

        org.bukkit.inventory.ItemStack mainHandItem = owner.getInventory().getItemInMainHand();
        org.bukkit.inventory.ItemStack offHandItem = owner.getInventory().getItemInOffHand();

        String ammoId = pdcManager.getStringValue(meta, Keys.CUSTOM_ITEM_ID);

        if (!mainHandItem.isEmpty() && toNMS(mainHandItem).is(this)) {
            String acceptedAmmoId = pdcManager.getStringValue(mainHandItem.getItemMeta(), Keys.GUN_ACCEPTED_AMMO_ID);
            return ammoId.equals(acceptedAmmoId);
        } else if (!offHandItem.isEmpty() && toNMS(offHandItem).is(this)) {
            String acceptedAmmoId = pdcManager.getStringValue(offHandItem.getItemMeta(), Keys.GUN_ACCEPTED_AMMO_ID);
            return ammoId.equals(acceptedAmmoId);
        }

        return superResult;
    }

    private ItemStack toNMS(org.bukkit.inventory.ItemStack stack) {
        return CraftItemStack.asNMSCopy(stack);
    }
}
