package foundation.esoteric.minecraft.plugins.games.fireworkwars.game.team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.FireworkWarsPlugin;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.data.TeamData;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.game.FireworkWarsGame;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class FireworkWarsTeam {
    private final TeamData teamData;
    private final FireworkWarsGame game;

    private final FireworkWarsPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    private final List<TeamPlayer> players = new ArrayList<>();

    public FireworkWarsTeam(TeamData teamData, FireworkWarsGame game, FireworkWarsPlugin plugin) {
        this.teamData = teamData;
        this.game = game;
        this.plugin = plugin;
    }

    public TeamData getTeamData() {
        return teamData;
    }

    public List<TeamPlayer> getPlayers() {
        return players;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public void addPlayer(TeamPlayer teamPlayer) {
        players.add(teamPlayer);
    }

    public Component getColoredTeamName() {
        return miniMessage.deserialize(teamData.getMiniMessageString());
    }

    public TextColor getTeamColor() {
        return getColoredTeamName().color();
    }

    public Material getWoolMaterial() {
        return Material.valueOf(teamData.getColorData().getColor() + "_WOOL");
    }

    public List<TeamPlayer> getRemainingPlayers() {
        return players.stream()
                .filter(TeamPlayer::isAlive)
                .toList();
    }

    public int getRemainingPlayerCount() {
        return getRemainingPlayers().size();
    }

    public boolean isEliminated() {
        return getRemainingPlayers().isEmpty();
    }
}
