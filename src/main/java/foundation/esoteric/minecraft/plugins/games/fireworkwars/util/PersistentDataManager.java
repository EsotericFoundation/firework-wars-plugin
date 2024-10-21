package foundation.esoteric.minecraft.plugins.games.fireworkwars.util;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.UUID;

@SuppressWarnings("unused")
public class PersistentDataManager {
    public boolean hasKey(PersistentDataHolder holder, NamespacedKey key) {
        if (holder == null) {
            return false;
        }

        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        return pdc.has(key);
    }

    public boolean hasKey(PersistentDataHolder holder, String key) {
        if (holder == null) {
            return false;
        }

        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        return pdc.has(Keys.fromString(key));
    }

    public String getStringValue(PersistentDataHolder holder, NamespacedKey key) {
        if (holder == null) {
            return null;
        }

        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        return pdc.get(key, PersistentDataType.STRING);
    }

    public String getStringValue(PersistentDataHolder holder, String key) {
        if (holder == null) {
            return null;
        }

        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        return pdc.get(Keys.fromString(key), PersistentDataType.STRING);
    }

    public Boolean getBooleanValue(PersistentDataHolder holder, NamespacedKey key) {
        if (holder == null) {
            return null;
        }

        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        return pdc.get(key, PersistentDataType.BOOLEAN);
    }

    public Boolean getBooleanValue(PersistentDataHolder holder, String key) {
        if (holder == null) {
            return null;
        }

        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        return pdc.get(Keys.fromString(key), PersistentDataType.BOOLEAN);
    }

    public Integer getIntValue(PersistentDataHolder holder, NamespacedKey key) {
        if (holder == null) {
            return null;
        }

        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        return pdc.get(key, PersistentDataType.INTEGER);
    }

    public Integer getIntValue(PersistentDataHolder holder, String key) {
        if (holder == null) {
            return null;
        }

        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        return pdc.get(Keys.fromString(key), PersistentDataType.INTEGER);
    }

    public int[] getIntListValue(PersistentDataHolder holder, NamespacedKey key) {
        if (holder == null) {
            return null;
        }

        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        return pdc.get(key, PersistentDataType.INTEGER_ARRAY);
    }

    public int[] getIntListValue(PersistentDataHolder holder, String key) {
        if (holder == null) {
            return null;
        }

        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        return pdc.get(Keys.fromString(key), PersistentDataType.INTEGER_ARRAY);
    }

    public UUID getUUIDValue(PersistentDataHolder holder, NamespacedKey key) {
        if (holder == null) {
            return null;
        }

        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        String value = pdc.get(key, PersistentDataType.STRING);

        try {
            return value == null ? null : UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public void setStringValue(PersistentDataHolder holder, NamespacedKey key, String value) {
        if (holder == null) {
            throw new NullPointerException("Provided PDC holder is null for setStringValue(key=" + key.asString() + ", value=" + value + ")");
        }

        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.STRING, value);
    }

    public void setStringValue(PersistentDataHolder holder, String key, String value) {
        if (holder == null) {
            throw new NullPointerException("Provided PDC holder is null for setStringValue(key=" + key + ", value=" + value + ")");
        }

        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        pdc.set(Keys.fromString(key), PersistentDataType.STRING, value);
    }

    public void setBooleanValue(PersistentDataHolder holder, NamespacedKey key, Boolean value) {
        if (holder == null) {
            throw new NullPointerException("Provided PDC holder is null for setBooleanValue(key=" + key.asString() + ", value=" + value + ")");
        }

        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.BOOLEAN, value);
    }

    public void setBooleanValue(PersistentDataHolder holder, String key, Boolean value) {
        if (holder == null) {
            throw new NullPointerException("Provided PDC holder is null for setBooleanValue(key=" + key + ", value=" + value + ")");
        }

        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        pdc.set(Keys.fromString(key), PersistentDataType.BOOLEAN, value);
    }

    public void setIntValue(PersistentDataHolder holder, NamespacedKey key, Integer value) {
        if (holder == null) {
            throw new NullPointerException("Provided PDC holder is null for setIntValue(key=" + key.asString() + ", value=" + value + ")");
        }

        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.INTEGER, value);
    }

    public void setIntValue(PersistentDataHolder holder, String key, Integer value) {
        if (holder == null) {
            throw new NullPointerException("Provided PDC holder is null for setIntValue(key=" + key + ", value=" + value + ")");
        }

        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        pdc.set(Keys.fromString(key), PersistentDataType.INTEGER, value);
    }

    public void setIntListValue(PersistentDataHolder holder, NamespacedKey key, int[] value) {
        if (holder == null) {
            throw new NullPointerException("Provided PDC holder is null for setIntListValue(key=" + key.asString() + ", value=" + Arrays.toString(value) + ")");
        }

        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.INTEGER_ARRAY, value);
    }

    public void setIntListValue(PersistentDataHolder holder, String key, int[] value) {
        if (holder == null) {
            throw new NullPointerException("Provided PDC holder is null for setIntListValue(key=" + key + ", value=" + Arrays.toString(value) + ")");
        }

        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        pdc.set(Keys.fromString(key), PersistentDataType.INTEGER_ARRAY, value);
    }

    public void setUUIDValue(PersistentDataHolder holder, NamespacedKey key, UUID value) {
        if (holder == null) {
            throw new NullPointerException("Provided PDC holder is null for setUUIDValue(key=" + key.asString() + ", value=" + value + ")");
        }

        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        pdc.set(key, PersistentDataType.STRING, value.toString());
    }

    public void setUUIDValue(PersistentDataHolder holder, String key, UUID value) {
        if (holder == null) {
            throw new NullPointerException("Provided PDC holder is null for setUUIDValue(key=" + key + ", value=" + value + ")");
        }

        PersistentDataContainer pdc = holder.getPersistentDataContainer();
        pdc.set(Keys.fromString(key), PersistentDataType.STRING, value.toString());
    }
}
