package foundation.esoteric.minecraft.plugins.games.fireworkwars.game.chests;

import org.bukkit.entity.Entity;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.meta.ItemMeta;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.components.ChestLocation;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.structure.Arena;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.FireworkWarsGame;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.AbstractItem;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.CustomItemManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.ItemType;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Util;

import java.util.*;

import static java.util.Comparator.comparingInt;

public class ChestManager {
    private final Arena arena;
    private final CustomItemManager itemManager;

    private final List<StorageMinecart> supplyDropMinecarts;

    public ChestManager(FireworkWarsPlugin plugin, FireworkWarsGame game) {
        this.arena = game.getArena();
        this.itemManager = plugin.getCustomItemManager();

        this.supplyDropMinecarts = new ArrayList<>();
    }

    public List<StorageMinecart> getSupplyDropMinecarts() {
        return supplyDropMinecarts;
    }

    public void refillChests(double valueFactor) {
        supplyDropMinecarts.removeIf(Entity::isDead);

        for (InventoryHolder minecart : supplyDropMinecarts) {
            int maxTotalValue = (int) (40 * valueFactor);
            int maxValuePerItem = (int) (35 * valueFactor);

            this.refillChest(minecart, maxTotalValue, maxValuePerItem);
        }

        for (ChestLocation chestLocation : arena.getChestLocations()) {
            if (!(chestLocation.getChestBlock().getState() instanceof InventoryHolder chest)) {
                continue;
            }

            int maxTotalValue = (int) (40 * valueFactor);
            int maxValuePerItem = (int) (35 * valueFactor);

            this.refillChest(chest, maxTotalValue, maxValuePerItem);
        }
    }

    private void refillChest(InventoryHolder chest, int maxTotalValue, int maxItemValue) {
        List<AbstractItem<? extends ItemMeta>> itemsToAdd = new ArrayList<>();

        this.addRandomItemsToList(
            itemsToAdd,
            maxItemValue, maxTotalValue,
            chest.getInventory().getSize(),
            new HashMap<>(), new EnumMap<>(ItemType.class));

        if (itemsToAdd.isEmpty()) {
            return;
        }

        this.addItemsToChest(itemsToAdd, chest, maxTotalValue);
    }

    private void addRandomItemsToList(List<AbstractItem<? extends ItemMeta>> itemList, int maxItemValue, int maxTotalValue, int maxChestCapacity, Map<AbstractItem<? extends ItemMeta>, Integer> weightAdjustments, Map<ItemType, Integer> weightPerItemType) {
        int i = 0;
        while (i < maxTotalValue && itemList.size() < maxChestCapacity) {
            AbstractItem<? extends ItemMeta> item = itemManager.getWeightedRandomItem(weightAdjustments);

            if (item.getValue() > maxItemValue) {
                i++;
                continue;
            }

            ItemType type = item.getType();
            int newTotalTypeWeight = weightPerItemType.getOrDefault(type, 0) + item.getValue();

            if (newTotalTypeWeight > type.getMaxChestPercentage() * maxTotalValue) {
                i++;
                continue;
            }

            itemList.add(item);
            i += item.getValue();

            if (item.isConsumable() || item.isAmmo() || item.isBlock()) {
                weightAdjustments.put(
                    item, (int) Math.floor(weightAdjustments.getOrDefault(item, item.getWeight()) * 0.65F));
            } else {
                weightAdjustments.put(
                    item, (int) Math.floor(weightAdjustments.getOrDefault(item, item.getWeight()) / 2.0F));
            }

            weightPerItemType.put(type, newTotalTypeWeight);
        }
    }

    private void addItemsToChest(List<AbstractItem<? extends ItemMeta>> itemList, InventoryHolder chest, int maxTotalValue) {
        List<Integer> slots = Util.orderedNumberList(0, chest.getInventory().getSize() - 1);
        Collections.shuffle(slots);

        chest.getInventory().clear();

        int i = 0;

        if (Util.randomChance(0.4D)) {
            Comparator<AbstractItem<? extends ItemMeta>> comparator = comparingInt(AbstractItem::getValue);
            List<AbstractItem<? extends ItemMeta>> highestValueItems = itemList.stream()
                .filter(item -> item.getValue() >= 15)
                .sorted(comparator.reversed())
                .toList();

            if (!highestValueItems.isEmpty()) {
                AbstractItem<? extends ItemMeta> item = Util.randomElement(highestValueItems);

                this.putItemInChest(item, chest, slots.removeFirst());
                itemList.remove(item);

                i += item.getValue();
            }
        }

        while (i < maxTotalValue) {
            if (itemList.isEmpty()) {
                break;
            }

            AbstractItem<? extends ItemMeta> item = itemList.removeFirst();

            if (i + item.getValue() > maxTotalValue) {
                continue;
            }

            this.putItemInChest(item, chest, slots.removeFirst());
            i += item.getValue();
        }
    }

    private void putItemInChest(AbstractItem<? extends ItemMeta> item, InventoryHolder chest, int slot) {
        chest.getInventory().setItem(slot, item.getItem(null, item.getStackAmount()));
    }
}
