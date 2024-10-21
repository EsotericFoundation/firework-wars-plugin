package foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.data;

import org.bukkit.Color;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.components.PlayerLocation;
import foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.components.TeamColor;

@SuppressWarnings("unused")
public class TeamData {
    private String miniMessageString;
    private TeamColor color;
    private PlayerLocation spawnLocation;

    public String getMiniMessageString() {
        return miniMessageString;
    }

    public Color getColor() {
        return color.toBukkit();
    }

    public TeamColor getColorData() {
        return color;
    }

    public PlayerLocation getSpawnLocation() {
        return spawnLocation;
    }
}
