package foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.components;

import org.bukkit.Bukkit;
import org.bukkit.Location;

@SuppressWarnings("unused")
public class PlayerLocation {
    private String worldName;

    private float x;
    private float y;
    private float z;

    private float pitch;
    private float yaw;

    public String getWorldName() {
        return worldName;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getPitch() {
        return pitch;
    }

    public float getYaw() {
        return yaw;
    }

    public Location getBukkitLocation() {
        return new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
    }
}
