package foundation.esoteric.minecraft.plugins.games.fireworkwars.profile;

import java.util.UUID;

public class PlayerProfile {
    private final UUID uuid;

    private String language;

    public PlayerProfile(UUID uuid, String language) {
        this.uuid = uuid;
        this.language = language;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
