package foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.FireworkWarsGame;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.LanguageManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.Message;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.scoreboard.api.FastBoard;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.scoreboard.wrapper.FireworkWarsScoreboard;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Pair;

import java.util.*;

public class TeamPlayer {
    private final static Map<UUID, TeamPlayer> activePlayers = new HashMap<>();

    private final UUID uuid;
    private final FireworkWarsGame game;

    private final FireworkWarsPlugin plugin;
    private final LanguageManager languageManager;

    private FireworkWarsTeam team;
    private FireworkWarsScoreboard scoreboard;

    private int kills;
    private double damage;

    public static TeamPlayer from(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        return activePlayers.get(uuid);
    }

    public static TeamPlayer from(Player player) {
        if (player == null) {
            return null;
        }

        return from(player.getUniqueId());
    }

    public FireworkWarsGame getGame() {
        return game;
    }

    public FireworkWarsScoreboard getScoreboard() {
        return scoreboard;
    }

    public int getKills() {
        return kills;
    }

    public double getDamage() {
        return damage;
    }

    public TeamPlayer(UUID uuid, FireworkWarsGame game) {
        this.uuid = uuid;
        this.game = game;

        this.plugin = game.getPlugin();
        this.languageManager = plugin.getLanguageManager();

        register();
    }

    private void register() {
        activePlayers.forEach((uuid, player) -> {
            if (uuid.equals(this.uuid)) {
                player.unregister(true);
            }
        });

        activePlayers.put(uuid, this);
    }

    public void unregister(boolean removeFromGame) {
        activePlayers.remove(uuid);

        if (scoreboard != null) {
            scoreboard.delete();
        }

        if (team != null) {
            team.getPlayers().remove(this);
        }

        if (removeFromGame) {
            game.getPlayers().remove(this);
        }
    }

    public void joinTeam(FireworkWarsTeam team) {
        team.addPlayer(this);
        this.team = team;

        Player player = getPlayer();
        player.teleport(team.getTeamData().getSpawnLocation().getBukkitLocation());

        player.sendTitlePart(TitlePart.TITLE, plugin.getLanguageManager().getMessage(Message.YOU_ARE_ON_TEAM, player));
        player.sendTitlePart(TitlePart.SUBTITLE, team.getColoredTeamName());

        player.setGameMode(GameMode.SURVIVAL);
    }

    public void showScoreboard() {
        Player player = getPlayer();
        FastBoard board = new FastBoard(player);

        List<Component> lines = new ArrayList<>(List.of(
            languageManager.getMessage(Message.SB_SEPARATOR, player),
            languageManager.getMessage(Message.SB_EVENT_SUPPLY_DROP, player, "%"),
            Component.empty(),
            Component.empty(),
            languageManager.getMessage(Message.SB_KILL_COUNT, player, "%"),
            languageManager.getMessage(Message.SB_DAMAGE_DEALT, player, "%"),
            languageManager.getMessage(Message.SB_SEPARATOR, player)
        ));

        Map<FireworkWarsTeam, Component> teamLines = game.getTeams().stream()
            .collect(HashMap::new, (map, team) -> {
                Component component;
                boolean isOwnTeam = team.equals(this.team);

                if (isOwnTeam) {
                    component = languageManager.getMessage(
                        Message.SB_OWN_TEAM, player, team.getColoredTeamName(), "%");
                } else {
                    component = languageManager.getMessage(
                        Message.SB_TEAM, player, team.getColoredTeamName(), "%");
                }
                map.put(team, component);
            }, HashMap::putAll);

        board.updateTitle(languageManager.getMessage(Message.SB_TITLE, player));
        board.updateLines(lines);

        this.scoreboard = new FireworkWarsScoreboard(board, teamLines);

        scoreboard
            .updateLine(4, Pair.of("%", kills + ""))
            .updateLine(5, Pair.of("%", damage + ""));

        game.getTeams().forEach(team ->
            scoreboard.updateTeamLine(team, Pair.of("%", team.getRemainingPlayers().size() + "")));

        scoreboard.update();
    }

    public void showWorldBorder() {
        getPlayer().setWorldBorder(null);
    }

    public FireworkWarsTeam getTeam() {
        return team;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Player getPlayer() {
        return Bukkit.getOfflinePlayer(uuid).getPlayer();
    }

    public Component getColoredName() {
        return getPlayer().displayName().color(team.getTeamColor());
    }

    public void sendMessage(Component message) {
        getPlayer().sendMessage(message);
    }

    public void teleportToWaitingArea() {
        getPlayer().teleport(game.getArena().getWaitingAreaLocation().getBukkitLocation());
    }

    public void teleportToLobby() {
        Location location = plugin.getArenaManager().getFirstLobbySpawnLocation();
        getPlayer().teleport(location);
        getPlayer().setGameMode(GameMode.ADVENTURE);
    }

    public void becomeSpectator() {
        getPlayer().setGameMode(GameMode.SPECTATOR);
    }

    public void playSound(Sound sound, float volume, float pitch) {
        getPlayer().playSound(getPlayer().getLocation(), sound, volume, pitch);
    }

    public void playSound(Sound sound) {
        playSound(sound, 1.0F, 1.0F);
    }

    public void incrementKills() {
        this.kills++;
    }

    public void changeDamageDealt(double damage) {
        this.damage += damage;
    }

    public boolean isAlive() {
        return game.isAlive(getPlayer());
    }

    public boolean isOnSameTeamAs(TeamPlayer other) {
        return team.equals(other.team);
    }

    public boolean isOnSameTeamAs(Player player) {
        return isOnSameTeamAs(from(player));
    }
}
