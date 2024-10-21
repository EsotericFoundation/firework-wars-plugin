package foundation.esoteric.minecraft.plugins.games.fireworkwars.items.misc;

import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.FireworkWarsGame;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team.FireworkWarsTeam;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team.TeamPlayer;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.AbstractItem;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.ItemType;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.Message;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.ItemBuilder;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Keys;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Comparator.comparingDouble;
import static net.kyori.adventure.text.Component.text;

public class PlayerCompassItem extends AbstractItem<CompassMeta> {
    private static final Map<String, CompassUpdater> compassManagers = new HashMap<>();

    private final Message loreNotTracking = Message.PLAYER_COMPASS_LORE_NOT_TRACKING;
    private final Message loreTracking = Message.PLAYER_COMPASS_LORE_TRACKING;
    private final Message noEnemy = Message.PLAYER_COMPASS_NO_ENEMY;
    private final Message trackingTarget = Message.PLAYER_COMPASS_TRACKING_TARGET;
    private final Message actionBarInfo = Message.PLAYER_COMPASS_ACTIONBAR_INFO;

    public PlayerCompassItem(FireworkWarsPlugin plugin) {
        super(plugin, "player_compass", Material.COMPASS, 35, 12, ItemType.MISC);
    }

    @Override
    public ItemStack getItem(Player player) {
        return new ItemBuilder<CompassMeta>(plugin, itemMaterial)
            .setName(Message.PLAYER_COMPASS, player)
            .setLore(loreNotTracking, player)
            .modifyMeta(this::modifyMeta)
            .build();
    }

    @Override
    protected void modifyMeta(CompassMeta meta) {
        super.modifyMeta(meta);
        pdcManager.setStringValue(meta, Keys.PLAYER_COMPASS_ID, UUID.randomUUID().toString());
    }

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }

        if (event.getClickedBlock() != null && event.getClickedBlock().getType().isInteractable()) {
            return;
        }

        Player player = event.getPlayer();
        TeamPlayer teamPlayer = TeamPlayer.from(player.getUniqueId());

        ItemStack item = event.getItem();

        if (!isValidCustomItem(item)) {
            return;
        }

        if (Util.usedInteractableItem(event)) {
            return;
        }

        FireworkWarsGame game = plugin.getGameManager().getFireworkWarsGame(player);

        if (game == null || !game.isPlaying()) {
            return;
        }

        FireworkWarsTeam team = teamPlayer.getTeam();
        Location playerLocation = player.getLocation();

        TeamPlayer nearestEnemy = game.getPlayers()
                .stream()
                .filter(gamePlayer -> !gamePlayer.getTeam().equals(team))
                .min(comparingDouble(enemy -> enemy.getPlayer().getLocation().distanceSquared(playerLocation)))
                .orElse(null);

        String compassId = pdcManager.getStringValue(item.getItemMeta(), Keys.PLAYER_COMPASS_ID);
        CompassUpdater compassUpdater = compassManagers.computeIfAbsent(
                compassId, key -> new CompassUpdater(game, player, item));

        if (nearestEnemy == null) {
            player.sendMessage(languageManager.getMessage(noEnemy, player));
            compassUpdater.setTarget(null);
        } else {
            compassUpdater.setTarget(nearestEnemy);
        }

        if (!compassUpdater.isRunning()) {
            compassUpdater.runTaskTimer(plugin, 0L, 1L);
        }
    }

    @Override
    public int getStackAmount() {
        return 1;
    }

    private class CompassUpdater extends BukkitRunnable {
        private boolean isRunning;

        private final FireworkWarsGame game;
        private final String compassId;
        private final Player player;
        private final ItemStack compass;

        private TeamPlayer target;

        public boolean isRunning() {
            return isRunning;
        }

        public void setTarget(TeamPlayer target) {
            this.target = target;

            if (target != null) {
                Util.playSound(player, Sound.BLOCK_BEACON_POWER_SELECT);

                compass.lore(List.of(languageManager.getMessage(loreTracking, player, target.getColoredName())));
                player.sendMessage(languageManager.getMessage(trackingTarget, player, target.getColoredName()));
            } else {
                Util.playSound(player, Sound.ENTITY_ENDER_EYE_DEATH);

                compass.lore(List.of(languageManager.getMessage(loreNotTracking, player).decoration(TextDecoration.ITALIC, false)));
            }
        }

        public CompassUpdater(FireworkWarsGame game, Player player, ItemStack compass) {
            this.game = game;
            this.compassId = pdcManager.getStringValue(compass.getItemMeta(), Keys.PLAYER_COMPASS_ID);
            this.player = player;
            this.compass = compass;
        }

        @Override
        public void run() {
            if (!game.isPlaying() || game.isSpectator(player) || !hasExactCompass()) {
                cancel();
                return;
            }

            if (target == null || !player.isOnline()) {
                return;
            }

            Player targetPlayer = target.getPlayer();

            if (!targetPlayer.isOnline() || game.isSpectator(targetPlayer)) {
                setTarget(null);
                return;
            }

            player.setCompassTarget(targetPlayer.getLocation());

            ItemStack mainHand = player.getInventory().getItemInMainHand();
            ItemStack offHand = player.getInventory().getItemInOffHand();

            if (mainHand.equals(compass) || offHand.equals(compass)) {
                int distance = (int) player.getLocation().distance(targetPlayer.getLocation());

                player.sendActionBar(languageManager.getMessage(
                        actionBarInfo, player, targetPlayer.displayName(), text(distance + "m").color(NamedTextColor.AQUA)));
            }
        }

        @Override
        public void cancel() {
            this.isRunning = false;
            super.cancel();

            compassManagers.remove(pdcManager.getStringValue(compass.getItemMeta(), Keys.PLAYER_COMPASS_ID));
        }

        private boolean hasExactCompass() {
            return Util.testInventory(player.getInventory(),
                    item -> compassId.equals(pdcManager.getStringValue(item.getItemMeta(), Keys.PLAYER_COMPASS_ID)));
        }

        @Override
        public synchronized @NotNull BukkitTask runTaskTimer(@NotNull Plugin plugin, long delay, long period) throws IllegalArgumentException, IllegalStateException {
            this.isRunning = true;
            return super.runTaskTimer(plugin, delay, period);
        }
    }
}
