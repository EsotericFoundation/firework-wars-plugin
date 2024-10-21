package foundation.esoteric.minecraft.plugins.games.fireworkwars.util;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class Util {
    public static void playSound(Player player, Sound sound) {
        player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
    }

    public static void playSound(Player player, Sound sound, float volume, float pitch) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public static boolean testInventory(Inventory inventory, Predicate<ItemStack> predicate) {
        return Stream
                .of(inventory.getContents())
                .filter(Objects::nonNull)
                .anyMatch(predicate);
    }

    public static int randomInt(int min, int max) {
        return (int) (Math.random() * (max - min + 1) + min);
    }

    public static double randomDouble(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    public static boolean randomChance(double chance) {
        return Math.random() < chance;
    }

    public static boolean randomChance() {
        return randomChance(0.5D);
    }

    public static List<Integer> orderedNumberList(int start, int end) {
        List<Integer> list = new ArrayList<>();

        for (int i = start; i <= end; i++) {
            list.add(i);
        }

        return list;
    }

    public static <E> E randomElement(List<E> list) {
        return list.get(randomInt(0, list.size() - 1));
    }

    public static Color randomRainbowColor() {
        return List
            .of(Color.RED, Color.ORANGE, Color.YELLOW, Color.LIME, Color.AQUA, Color.BLUE, Color.PURPLE)
            .get(randomInt(0, 6));
    }

    public static boolean usedInteractableItem(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.OFF_HAND) {
            return false;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Block block = event.getClickedBlock();

        if (item.isEmpty()) {
            return false;
        }

        Material material = item.getType();

        if (material.isEdible()) {
            return true;
        }

        List<Material> usableItems = List.of(Material.BOW, Material.CROSSBOW, Material.SNOWBALL, Material.EGG, Material.ENDER_PEARL, Material.ENDER_EYE, Material.TRIDENT, Material.SPLASH_POTION, Material.LINGERING_POTION, Material.POTION, Material.ELYTRA, Material.FISHING_ROD, Material.CARROT_ON_A_STICK, Material.WARPED_FUNGUS_ON_A_STICK, Material.SHEARS, Material.MILK_BUCKET, Material.SHIELD);

        if (usableItems.contains(material)) {
            return true;
        }

        if ("throwable_tnt".equals(getItemCustomId(item))) {
            return true;
        }

        if ("player_compass".equals(getItemCustomId(item))) {
            return true;
        }

        if (block == null) {
            return false;
        }

        if (material.isBlock()) {
            return true;
        }

        List<Material> axes = List.of(Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE);
        List<Material> woods = List.of(Material.OAK_LOG, Material.SPRUCE_LOG, Material.BIRCH_LOG, Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG, Material.CRIMSON_STEM, Material.WARPED_STEM, Material.OAK_WOOD, Material.SPRUCE_WOOD, Material.BIRCH_WOOD, Material.JUNGLE_WOOD, Material.ACACIA_WOOD, Material.DARK_OAK_WOOD, Material.CRIMSON_HYPHAE, Material.WARPED_HYPHAE);

        if (axes.contains(material) && woods.contains(block.getType())) {
            return true;
        }

        List<Material> shovels = List.of(Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL);
        List<Material> dirt = List.of(Material.DIRT, Material.COARSE_DIRT, Material.PODZOL, Material.GRASS_BLOCK, Material.MYCELIUM);

        if (shovels.contains(material) && dirt.contains(block.getType())) {
            return true;
        }

        List<Material> boots = List.of(Material.LEATHER_BOOTS, Material.CHAINMAIL_BOOTS, Material.IRON_BOOTS, Material.GOLDEN_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS);
        List<Material> leggings = List.of(Material.LEATHER_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.IRON_LEGGINGS, Material.GOLDEN_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS);
        List<Material> chestplates = List.of(Material.LEATHER_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.IRON_CHESTPLATE, Material.GOLDEN_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE);
        List<Material> helmets = List.of(Material.LEATHER_HELMET, Material.CHAINMAIL_HELMET, Material.IRON_HELMET, Material.GOLDEN_HELMET, Material.DIAMOND_HELMET, Material.NETHERITE_HELMET, Material.TURTLE_HELMET);

        if (boots.contains(material) && !item.equals(player.getInventory().getItem(EquipmentSlot.FEET))) {
            return true;
        }

        if (leggings.contains(material) && !item.equals(player.getInventory().getItem(EquipmentSlot.LEGS))) {
            return true;
        }

        if (chestplates.contains(material) && !item.equals(player.getInventory().getItem(EquipmentSlot.CHEST))) {
            return true;
        }

        if (helmets.contains(material) && !item.equals(player.getInventory().getItem(EquipmentSlot.HEAD))) {
            return true;
        }

        List<Material> usableOnBlocks = List.of(Material.FLINT_AND_STEEL, Material.FIRE_CHARGE, Material.FIREWORK_ROCKET, Material.PUFFERFISH_BUCKET, Material.SALMON_BUCKET, Material.COD_BUCKET, Material.TROPICAL_FISH_BUCKET, Material.BUCKET, Material.WATER_BUCKET, Material.LAVA_BUCKET);

        return usableOnBlocks.contains(material);
    }

    public static String getItemCustomId(ItemStack item) {
        return FireworkWarsPlugin.getInstance().getPdcManager().getStringValue(item.getItemMeta(), Keys.CUSTOM_ITEM_ID);
    }
}
