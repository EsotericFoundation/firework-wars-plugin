package foundation.esoteric.minecraft.plugins.games.fireworkwars.events.global;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team.TeamPlayer;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.CustomItemManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Keys;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.PersistentDataManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Util;

public class ItemOwnerChangeListener implements Listener {
    private final FireworkWarsPlugin plugin;
    private final PersistentDataManager pdcManager;
    private final CustomItemManager itemManager;

    public ItemOwnerChangeListener(FireworkWarsPlugin plugin) {
        this.plugin = plugin;
        this.pdcManager = plugin.getPdcManager();
        this.itemManager = plugin.getCustomItemManager();
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        updateItem(item, player);
    }

    @EventHandler
    public void onItemMoveInventory(InventoryClickEvent event) {
        ItemStack item = event.getCurrentItem();
        ItemStack cursorItem = event.getCursor();

        Player player = (Player) event.getWhoClicked();
        InventoryAction action = event.getAction();

        if (event.getInventory().getType() == InventoryType.CRAFTING) {
            return;
        }

        if (!event.getInventory().equals(event.getClickedInventory()) && action != InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            return;
        }

        if (item == null) {
            return;
        }

        if (item.isEmpty() && cursorItem.isEmpty()) {
            return;
        }

        if (TeamPlayer.from(player) == null) {
            return;
        }

        switch (action) {
            case NOTHING, UNKNOWN, DROP_ALL_SLOT, DROP_ONE_SLOT, DROP_ALL_CURSOR, DROP_ONE_CURSOR -> {}
            case PLACE_ALL ->
                updateItem(cursorItem, null);
            case PICKUP_ALL, COLLECT_TO_CURSOR ->
                updateItem(item, player);
            case PICKUP_SOME, PICKUP_HALF, PICKUP_ONE -> plugin.runTaskLater(() ->
                updateItem(player.getOpenInventory().getCursor(), player), 1L);
            case PLACE_SOME, PLACE_ONE -> plugin.runTaskLater(() ->
                updateItem(player.getOpenInventory().getTopInventory().getItem(event.getSlot()), null), 1L);
            case HOTBAR_SWAP -> {
                updateItem(item, player);
                updateItem(player.getInventory().getItem(event.getHotbarButton()), null);
            }
            case SWAP_WITH_CURSOR -> {
                updateItem(cursorItem, null);
                updateItem(item, player);
            }
            case MOVE_TO_OTHER_INVENTORY -> {
                if (event.getInventory().equals(event.getClickedInventory())) {
                    updateItem(item, player);
                } else {
                    updateItem(item, null);
                }
            }
        }
    }

    private void updateItem(ItemStack item, Player player) {
        if (item == null) {
            return;
        }

        updateWoolColor(item, player);
        updateLeatherArmorColor(item, player);
        updateAmmoOwner(item, player);
        updateTNTText(item, player);
        updateItemLocale(item, player);
    }

    @SuppressWarnings("deprecation")
    private void updateWoolColor(ItemStack item, Player player) {
        TeamPlayer teamPlayer = TeamPlayer.from(player);

        if (item.getType().name().endsWith("_WOOL")) {
            if (teamPlayer != null) {
                item.setType(teamPlayer.getTeam().getWoolMaterial());
                itemManager.getItem("wool").updateItemTexts(item, player);
            } else {
                item.setType(Material.WHITE_WOOL);
            }
        }
    }

    private void updateLeatherArmorColor(ItemStack item, Player player) {
        TeamPlayer teamPlayer = TeamPlayer.from(player);

        if ("heavy_armor".equals(Util.getItemCustomId(item))) {
            if (teamPlayer != null) {
                Color color = teamPlayer.getTeam().getTeamData().getColor();
                item.editMeta(meta -> ((LeatherArmorMeta) meta).setColor(color));
            } else {
                item.editMeta(meta -> ((LeatherArmorMeta) meta).setColor(Color.WHITE));
            }
        }
    }

    private void updateAmmoOwner(ItemStack item, Player player) {
        if (pdcManager.hasKey(item.getItemMeta(), Keys.AMMO_OWNER_UUID)) {
            if (player != null) {
                item.editMeta(meta -> pdcManager.setUUIDValue(
                    meta, Keys.AMMO_OWNER_UUID, player.getUniqueId()));
            } else {
                item.editMeta(meta -> pdcManager.setStringValue(
                    meta, Keys.AMMO_OWNER_UUID, ""));
            }
        }
    }

    private void updateTNTText(ItemStack item, Player player) {
        TeamPlayer teamPlayer = TeamPlayer.from(player);

        if (teamPlayer == null) {
            return;
        }

        if ("throwable_tnt".equals(Util.getItemCustomId(item))) {
            return;
        }

        if (item.getType() == Material.TNT) {
            itemManager.getItem("tnt").updateItemTexts(item, player);
        }
    }

    private void updateItemLocale(ItemStack item, Player player) {
        if (pdcManager.hasKey(item.getItemMeta(), Keys.CUSTOM_ITEM_ID)) {
            if (item.getType() == Material.CROSSBOW) {
                return; // Easter egg
            }

            String itemId = Util.getItemCustomId(item);
            itemManager.getItem(itemId).updateItemTexts(item, player);
        }
    }
}
