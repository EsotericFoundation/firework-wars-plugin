package foundation.esoteric.minecraft.plugins.games.fireworkwars.events.game;

import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.FireworkWarsGame;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team.FireworkWarsTeam;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team.TeamPlayer;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.items.AbstractItem;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.LanguageManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.Message;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.scoreboard.wrapper.FireworkWarsScoreboard;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static net.kyori.adventure.title.Title.title;

public class GameEventListener implements Listener {
    private final FireworkWarsPlugin plugin;
    private final LanguageManager languageManager;
    private final FireworkWarsGame game;

    private final Map<UUID, Pair<Double, Integer>> lastDamagePerPlayer;
    private final Map<UUID, Integer> lastNoFriendlyFireWarning;

    public GameEventListener(FireworkWarsPlugin plugin, FireworkWarsGame game) {
        this.plugin = plugin;
        this.languageManager = plugin.getLanguageManager();
        this.game = game;

        this.lastDamagePerPlayer = new HashMap<>();
        this.lastNoFriendlyFireWarning = new HashMap<>();
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler
    @SuppressWarnings("UnstableApiUsage")
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (!(event.getDamageSource().getCausingEntity() instanceof Player damager)) {
            return;
        }

        if (!game.isAlive(player) || !game.isAlive(damager)) {
            return;
        }

        if (game.isSpectator(damager)) {
            event.setCancelled(true);
            return;
        }

        if (damager.getUniqueId().equals(player.getUniqueId())) {
            return;
        }

        TeamPlayer teamPlayer = TeamPlayer.from(player);
        TeamPlayer damagerTeamPlayer = TeamPlayer.from(damager);

        int currentTick = plugin.getServer().getCurrentTick();
        int lastWarningTick = lastNoFriendlyFireWarning.getOrDefault(damager.getUniqueId(), 0);

        boolean shouldWarn = currentTick - lastWarningTick > 20 * 5;

        if (damagerTeamPlayer.isOnSameTeamAs(teamPlayer)) {
            if (shouldWarn) {
                damagerTeamPlayer.sendMessage(Message.NO_FRIENDLY_FIRE);
                lastNoFriendlyFireWarning.put(damager.getUniqueId(), currentTick);
            }

            event.setCancelled(true);
            return;
        }

        double finalDamage = event.getFinalDamage();

        Pair<Double, Integer> lastDamageInfo = lastDamagePerPlayer.getOrDefault(
            player.getUniqueId(), Pair.of(0.0D, currentTick));

        double lastDamage = lastDamageInfo.getLeft();
        int lastDamageTime = lastDamageInfo.getRight();

        if (finalDamage < lastDamage && currentTick - lastDamageTime < 10) {
            return;
        }

        if (currentTick - lastDamageTime < 10) {
            damagerTeamPlayer.changeDamageDealt(-lastDamage);
        } else {
            damagerTeamPlayer.playSound(Sound.ENTITY_PLAYER_HURT);
        }

        damagerTeamPlayer.changeDamageDealt(Math.round(finalDamage));
        lastDamagePerPlayer.put(player.getUniqueId(), Pair.of(finalDamage, currentTick));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();

        if (!game.isAlive(player)) {
            return;
        }

        if (player.getKiller() != null) {
            TeamPlayer.from(player.getKiller()).incrementKills();
        }

        performDeath(player, event.deathMessage(), false);
        event.setCancelled(true);
    }

    @SuppressWarnings("UnstableApiUsage")
    public void performDeath(Player player, Component deathMessage, boolean disconnected) {
        TeamPlayer teamPlayer = TeamPlayer.from(player);
        FireworkWarsTeam team = teamPlayer.getTeam();

        if (disconnected) {
            teamPlayer.unregister(true);
        }

        EntityDamageEvent lastDamageCause = player.getLastDamageCause();
        if (lastDamageCause != null && disconnected && lastDamageCause.getDamageSource().getCausingEntity() instanceof Player killer) {
            TeamPlayer killerTeamPlayer = TeamPlayer.from(killer);

            if (killerTeamPlayer != null && !team.equals(killerTeamPlayer.getTeam())) {
                killerTeamPlayer.incrementKills();
            }
        }

        teamPlayer.removeSpectators();
        teamPlayer.becomeSpectator();

        plugin.getHealCommand().healPlayer(player);

        player.getInventory().forEach(drop -> {
            if (drop != null) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        });
        player.getInventory().clear();

        Sound sound = disconnected
            ? Sound.ENTITY_LIGHTNING_BOLT_THUNDER
            : Sound.ENTITY_SKELETON_DEATH;

        game.getPlayers().forEach(tp -> tp.sendMessage(deathMessage));
        game.getPlayers().forEach(tp -> tp.playSound(sound));

        for (TeamPlayer tp : game.getPlayers()) {
            Player p = tp.getPlayer();
            FireworkWarsScoreboard scoreboard = tp.getScoreboard();

            Component teamName = team.getColoredTeamName();

            if (team.isEliminated()) {
                if (tp.getTeam().equals(team)) {
                    scoreboard.setTeamLine(
                        team, languageManager.getMessage(Message.SB_ELIMINATED_OWN_TEAM, p, teamName));
                } else {
                    scoreboard.setTeamLine(
                        team, languageManager.getMessage(Message.SB_ELIMINATED_TEAM, p, teamName));
                }
            } else {
                scoreboard.updateTeamLine(
                    team, Pair.of("%", team.getRemainingPlayers().size() + ""));
            }
        }

        boolean gameEnded = false;

        if (team.isEliminated()) {
            game.eliminateTeam(team);
            List<FireworkWarsTeam> remainingTeams = game.getRemainingTeams();

            if (remainingTeams.size() == 1) {
                game.preEndGame(remainingTeams.get(0));
                gameEnded = true;
            }
        }

        if (!gameEnded && !disconnected) {
            Title title = title(
                languageManager.getMessage(Message.YOU_DIED, player),
                languageManager.getMessage(Message.YOU_ARE_NOW_SPECTATOR, player));

            player.sendTitlePart(TitlePart.TITLE, title.title());
            player.sendTitlePart(TitlePart.SUBTITLE, title.subtitle());
        }
    }

    public void performDisconnectionDeath(Player player, Component displayName) {
        Component deathMessage = languageManager.getMessage(
            Message.PLAYER_KILLED_BY_DISCONNECTION, player, displayName);

        performDeath(player, deathMessage, true);
    }

    @EventHandler
    public void onAppleSpawn(ItemSpawnEvent event) {
        Item item = event.getEntity();
        ItemStack itemStack = item.getItemStack();

        if (event.getEntity().getThrower() != null) {
            return;
        }

        if (itemStack.getType() == Material.APPLE) {
            AbstractItem<? extends ItemMeta> goldenApple = plugin.getCustomItemManager().getItem("golden_apple");

            item.getWorld().dropItemNaturally(
                item.getLocation(),
                goldenApple.getItem(null, itemStack.getAmount()));

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCandleLight(BlockIgniteEvent event) {
        Player player = null;

        if (event.getIgnitingEntity() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player shooter) {
                player = shooter;
            } else {
                return;
            }
        }

        if (event.getPlayer() != null) {
            player = event.getPlayer();
        }

        if (!(player instanceof Player finalPlayer)) {
            return;
        }

        Block block = event.getBlock();

        if (block.getType() != Material.RED_CANDLE) {
            return;
        }

        block.getWorld().playSound(block.getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0F, 1.0F);

        plugin.runTaskLater(() -> {
            Block below = block.getRelative(BlockFace.DOWN);

            if (below.getType() == Material.TNT) {
                below.setType(Material.AIR);
                below.getWorld().createExplosion(finalPlayer, below.getLocation(), 4.0F, false, true, false);
            }
        }, 40L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpectatorInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!game.isSpectator(player)) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onSpectatorInteractPlayer(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if (!game.isSpectator(player)) {
            return;
        }

        if (!(event.getRightClicked() instanceof Player target)) {
            return;
        }

        TeamPlayer targetTeamPlayer = TeamPlayer.from(target);
        if (targetTeamPlayer == null) {
            return;
        }

        TeamPlayer teamPlayer = TeamPlayer.from(player);
        teamPlayer.startSpectating(targetTeamPlayer);

        event.setCancelled(true);
    }

    @EventHandler
    public void onSpectatorStopSpectating(PlayerStopSpectatingEntityEvent event) {
        Player player = event.getPlayer();

        if (!game.isSpectator(player)) {
            return;
        }

        TeamPlayer teamPlayer = TeamPlayer.from(player);
        TeamPlayer targetTeamPlayer = TeamPlayer.from(event.getSpectatorTarget().getUniqueId());

        if (targetTeamPlayer != null) {
            teamPlayer.stopSpectating(targetTeamPlayer);
        }
    }
}
