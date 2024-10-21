package foundation.esoteric.minecraft.plugins.games.fireworkwars.events.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.manager.ArenaManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.FireworkWarsGame;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.GameManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team.TeamPlayer;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.LanguageManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.Message;

import java.util.*;

import static net.kyori.adventure.title.Title.title;

public class PlayerConnectionListener implements Listener {
    private final FireworkWarsPlugin plugin;
    private final FireworkWarsGame game;

    private final GameManager gameManager;
    private final ArenaManager arenaManager;
    private final LanguageManager languageManager;

    private final Map<UUID, TeamPlayer> disconnectedPlayers;

    public PlayerConnectionListener(FireworkWarsPlugin plugin, FireworkWarsGame game) {
        this.plugin = plugin;
        this.game = game;

        this.gameManager = plugin.getGameManager();
        this.arenaManager = plugin.getArenaManager();
        this.languageManager = plugin.getLanguageManager();

        this.disconnectedPlayers = new HashMap<>();
    }

    public Map<UUID, TeamPlayer> getDisconnectedPlayers() {
        return disconnectedPlayers;
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        TeamPlayer teamPlayer = TeamPlayer.from(player.getUniqueId());

        if (teamPlayer == null) {
            return;
        }

        if (game.isWaiting() || game.isStarting()) {
            game.removePlayer(teamPlayer);
        } else if (game.isAlive(player)) {
            Component name = teamPlayer.getColoredName();
            game.getEventListener().performDisconnectionDeath(player, name);

            disconnectedPlayers.put(player.getUniqueId(), teamPlayer);
        }

        event.quitMessage(null);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String worldName = player.getWorld().getName();

        if (!game.equals(gameManager.getFireworkWarsGame(worldName))) {
            return;
        }

        switch (game.getGameState()) {
            case WAITING, STARTING -> {
                game.addPlayer(player);
                event.joinMessage(null);
            }
            case PLAYING -> {
                UUID uuid = player.getUniqueId();

                if (!disconnectedPlayers.containsKey(uuid)) {
                    Location location = arenaManager.getFirstLobbySpawnLocation();
                    player.teleport(location);
                    player.setGameMode(GameMode.ADVENTURE);
                    return;
                }

                TeamPlayer teamPlayer = disconnectedPlayers.remove(uuid);
                teamPlayer.getTeam().getPlayers().add(teamPlayer);
                game.getPlayers().add(teamPlayer);

                teamPlayer.showScoreboard();
                teamPlayer.showWorldBorder();

                Title title = title(
                    languageManager.getMessage(Message.YOU_DIED, player),
                    languageManager.getMessage(Message.YOU_ARE_NOW_SPECTATOR, player));

                player.sendTitlePart(TitlePart.TITLE, title.title());
                player.sendTitlePart(TitlePart.SUBTITLE, title.subtitle());

                player.playSound(player.getLocation(), Sound.ENTITY_SKELETON_DEATH, 1.0F, 1.0F);

                game.sendMessage(Message.PLAYER_RECONNECTED_AS_SPECTATOR, teamPlayer.getColoredName());
                event.joinMessage(null);
            }
            case RESETTING -> {
                Location location = arenaManager.getFirstLobbySpawnLocation();
                player.teleport(location);
                player.setGameMode(GameMode.ADVENTURE);
            }
        }
    }
}
