package foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.data;

import foundation.esoteric.minecraft.plugins.games.fireworkwars.arena.json.components.PlayerLocation;

import java.util.List;

@SuppressWarnings({"unused", "FieldMayBeFinal"})
public class SupplyDropData {
    private List<PlayerLocation> supplyDropLocations = List.of();
    private int supplyDropIntervalTicks;
    private int supplyDropIntervalRandomness;

    public List<PlayerLocation> getSupplyDropLocations() {
        return supplyDropLocations;
    }

    public PlayerLocation getRandomLocation() {
        return supplyDropLocations.get((int) (Math.random() * supplyDropLocations.size()));
    }

    public int getSupplyDropIntervalTicks() {
        return supplyDropIntervalTicks;
    }

    public int getSupplyDropIntervalRandomness() {
        return supplyDropIntervalRandomness;
    }

    public int getLongestInterval() {
        return supplyDropIntervalTicks + supplyDropIntervalRandomness;
    }
}
