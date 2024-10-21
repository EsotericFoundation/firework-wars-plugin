package foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.data;

import foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.components.PlayerLocation;

@SuppressWarnings("unused")
public class EndgameData {
    private double endgameStartMinutes;

    private int warnBeforeEndgameSeconds;
    private int warnBeforeGameEndSeconds;

    private PlayerLocation wardenSpawnLocation;


    public double getEndgameStartMinutes() {
        return endgameStartMinutes;
    }

    public int getEndgameStartTicks() {
        return (int) (endgameStartMinutes * 60 * 20);
    }

    public int getWarnBeforeEndgameSeconds() {
        return warnBeforeEndgameSeconds;
    }

    public int getWarnBeforeEndgameTicks() {
        return warnBeforeEndgameSeconds * 20;
    }

    public int getWarnBeforeGameEndSeconds() {
        return warnBeforeGameEndSeconds;
    }

    public int getWarnBeforeGameEndTicks() {
        return warnBeforeGameEndSeconds * 20;
    }

    public PlayerLocation getWardenSpawnLocation() {
        return wardenSpawnLocation;
    }
}
