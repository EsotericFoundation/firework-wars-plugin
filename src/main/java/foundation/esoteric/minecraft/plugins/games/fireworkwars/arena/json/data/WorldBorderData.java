package foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.data;

import org.bukkit.Location;
import org.bukkit.World;

@SuppressWarnings("unused")
public class WorldBorderData {
    private int centerX;
    private int centerZ;
    private int radius;

    private int endgameRadius;
    private int secondsToReachEndgameRadius;

    public int getCenterX() {
        return centerX;
    }

    public int getCenterZ() {
        return centerZ;
    }

    public Location getCenter(World world) {
        return new Location(world, centerX, 0, centerZ);
    }

    public int getRadius() {
        return radius;
    }

    public int getDiameter() {
        return radius * 2;
    }

    public int getEndgameRadius() {
        return endgameRadius;
    }

    public int getEndgameDiameter() {
        return endgameRadius * 2;
    }

    public int getSecondsToReachEndgameRadius() {
        return secondsToReachEndgameRadius;
    }
}
