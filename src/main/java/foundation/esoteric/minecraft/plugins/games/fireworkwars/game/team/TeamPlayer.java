package foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team;

import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.FireworkWarsGame;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.LanguageManager;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.language.Message;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.scoreboard.api.FastBoard;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.scoreboard.wrapper.FireworkWarsScoreboard;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Pair;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.util.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

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

    private boolean isSpectator;
    private boolean isSpectating;

    private final List<TeamPlayer> spectators;

    public TeamPlayer(UUID uuid, FireworkWarsGame game) {
        this.uuid = uuid;
        this.game = game;

        this.plugin = game.getPlugin();
        this.languageManager = plugin.getLanguageManager();

        this.spectators = new ArrayList<>();

        register();
    }

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

    public UUID getUuid() {
        return uuid;
    }

    public FireworkWarsGame getGame() {
        return game;
    }

    public FireworkWarsTeam getTeam() {
        return team;
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

    public boolean isSpectator() {
        return isSpectator;
    }

    public boolean isSpectating() {
        return isSpectating;
    }

    public List<TeamPlayer> getSpectators() {
        return spectators;
    }

    public boolean isAlive() {
        return !isSpectator;
    }

    public Player getPlayer() {
        return Bukkit.getOfflinePlayer(uuid).getPlayer();
    }

    public boolean isOnline() {
        return getPlayer() != null;
    }

    public boolean isOffline() {
        return getPlayer() == null;
    }

    private void register() {
        activePlayers.keySet().stream()
            .filter(uuid -> uuid.equals(getUuid()))
            .forEach(uuid -> activePlayers.get(uuid).unregister(true));

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

    public Component getColoredName() {
        return getPlayer().displayName().color(team.getTeamColor());
    }

    public void sendMessage(Component message) {
        getPlayer().sendMessage(message);
    }

    public void sendMessage(Message message, Object... arguments) {
        sendMessage(languageManager.getMessage(message, getPlayer(), arguments));
    }

    public void teleportToWaitingArea() {
        getPlayer().teleport(game.getArena().getWaitingAreaLocation().getBukkitLocation());
    }

    public void teleportToLobby() {
        removeSpectatorAbilities();

        getPlayer().teleport(plugin.getArenaManager().getFirstLobbySpawnLocation());
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

    public void becomeSpectator() {
        this.isSpectator = true;

        Player player = getPlayer();
        player.setGameMode(GameMode.ADVENTURE);
        player.setCollidable(false); //todo: verify functionality
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setInvulnerable(true);
        player.setSilent(true);
        player.addPotionEffect(Util.getSpectatorInvisibility());
    }

    public void removeSpectatorAbilities() {
        Player player = getPlayer();
        player.setCollidable(true);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.setInvulnerable(false);
        player.setSilent(false);
        player.removePotionEffect(Util.getSpectatorInvisibility().getType());
    }

    public void removeSpectators() {
        List.copyOf(spectators).forEach(spectator -> spectator.stopSpectating(this));
    }

    public void startSpectating(TeamPlayer other) {
        this.isSpectating = true;

        Player player = getPlayer();
        player.setGameMode(GameMode.SPECTATOR);
        player.setSpectatorTarget(other.getPlayer());

        other.getSpectators().add(this);

        sendMessage(Message.SPECTATING_PLAYER, other.getColoredName());
    }

    public void stopSpectating(TeamPlayer other) {
        this.isSpectating = false;

        Player player = getPlayer();
        player.setGameMode(GameMode.ADVENTURE);
        player.setCollidable(false);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.addPotionEffect(Util.getSpectatorInvisibility());

        other.getSpectators().remove(this);
    }

    public void stopSpectating() {
        if (isSpectating) {
            Entity spectatorTarget = getPlayer().getSpectatorTarget();
            if (spectatorTarget == null) {
                return;
            }

            TeamPlayer other = TeamPlayer.from(getPlayer().getSpectatorTarget().getUniqueId());
            if (other == null) {
                return;
            }

            stopSpectating(other);
        }
    }

    public boolean isOnSameTeamAs(TeamPlayer other) {
        return team.equals(other.team);
    }
}
