package foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.components;

import org.bukkit.Color;
import org.bukkit.DyeColor;

@SuppressWarnings("unused")
public class TeamColor {
    private String color;

    public String getColor() {
        return color;
    }

    public Color toBukkit() {
        return DyeColor.valueOf(color.toUpperCase()).getColor();
    }
}
