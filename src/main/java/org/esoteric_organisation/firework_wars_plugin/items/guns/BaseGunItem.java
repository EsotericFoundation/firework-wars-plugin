package org.esoteric_organisation.firework_wars_plugin.items.guns;

import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.esoteric_organisation.firework_wars_plugin.FireworkWarsPlugin;
import org.esoteric_organisation.firework_wars_plugin.game.FireworkWarsGame;
import org.esoteric_organisation.firework_wars_plugin.items.AbstractItem;
import org.esoteric_organisation.firework_wars_plugin.items.nms.CustomCrossbow;
import org.esoteric_organisation.firework_wars_plugin.util.ItemBuilder;
import org.esoteric_organisation.firework_wars_plugin.util.Keys;
import org.esoteric_organisation.firework_wars_plugin.util.Util;

import java.util.function.Consumer;

public abstract class BaseGunItem extends AbstractItem {
    protected final String ammoId;

    public BaseGunItem(FireworkWarsPlugin plugin, String itemId, String ammoId, int weight, int value) {
        super(plugin, itemId, Material.CROSSBOW, weight, value);

        this.ammoId = ammoId;
    }

    protected ItemBuilder<CrossbowMeta> getBaseCrossbowBuilder() {
        return new ItemBuilder<CrossbowMeta>(plugin, itemMaterial)
                .setEnchanted(true)
                .setUnbreakable(true)
                .itemSupplier(this::getCustomCrossbow)
                .modifyMeta(this::modifyMeta);
    }

    protected ItemStack getCustomCrossbow() {
        CustomCrossbow crossbow = (CustomCrossbow) plugin.getCustomItemManager().getNMSItem("crossbow");
        ItemStack itemStack = crossbow.getDefaultInstance().asBukkitCopy();

        itemStack.setItemMeta(new ItemStack(Material.CROSSBOW).getItemMeta());
        return itemStack;
    }

    protected void modifyMeta(CrossbowMeta meta) {
        pdcManager.setStringValue(meta, customItemIdKey, itemId);
        pdcManager.setStringValue(meta, Keys.GUN_ACCEPTED_AMMO_ID, ammoId);
    }

    protected ItemStack createFirework(Color color, int stars, int fd) {
        ItemStack firework = new ItemStack(Material.FIREWORK_ROCKET);
        FireworkMeta fireworkMeta = (FireworkMeta) firework.getItemMeta();

        pdcManager.setStringValue(fireworkMeta, Keys.CUSTOM_ITEM_ID, ammoId);

        addFireworkStars(fireworkMeta, color, stars);
        fireworkMeta.setPower(fd);
        firework.setItemMeta(fireworkMeta);

        return firework;
    }

    protected void addFireworkStars(FireworkMeta meta, Color color, int amount) {
        for (int i = 0; i < amount; i++) {
            meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BURST).withColor(Color.WHITE).withFade(color).withTrail().withFlicker().build());
        }
    }

    protected AbstractItem getAmmoItem() {
        return plugin.getCustomItemManager().getItem(ammoId);
    }

    protected void editCrossbowMeta(ItemStack crossbow, Consumer<CrossbowMeta> consumer) {
        plugin.runTaskLater(() -> crossbow.editMeta(meta -> consumer.accept((CrossbowMeta) meta)), 1L);
    }

    protected abstract void onCrossbowLoad(Player player, FireworkWarsGame game, EntityLoadCrossbowEvent event);

    protected abstract void onCrossbowShoot(Player player, FireworkWarsGame game, EntityShootBowEvent event);

    @EventHandler
    public void onCrossbowLoad(EntityLoadCrossbowEvent event) {
        if (!isValidCustomItem(event.getCrossbow())) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        FireworkWarsGame game = plugin.getGameManager().getFireworkWarsGame(player);

        if (game == null || !game.isPlaying()) {
            return;
        }

        boolean hasAmmo = Util.testInventory(player.getInventory(), item ->
                getAmmoItem().isValidCustomItem(item));

        if (!hasAmmo) {
            return;
        }

        onCrossbowLoad(player, game, event);
    }

    @EventHandler
    public void onCrossbowShoot(EntityShootBowEvent event) {
        if (!isValidCustomItem(event.getBow())) {
            return;
        }

        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        FireworkWarsGame game = plugin.getGameManager().getFireworkWarsGame(player);

        if (game == null || !game.isPlaying()) {
            return;
        }

        onCrossbowShoot(player, game, event);
    }
}
