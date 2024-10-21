package foundation.esoteric.minecraft.plugins.games.fireworkwars.util;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Player;

import java.util.function.Function;

public class NMSUtil {
    @SuppressWarnings("unchecked")
    public static <T extends Entity> T toNMSEntity(org.bukkit.entity.Entity bukkit) {
        return (T) ((CraftEntity) bukkit).getHandle();
    }

    public static void sendPacket(Player player, Function<ServerPlayer, Packet<ClientGamePacketListener>> packetCreator) {
        ServerPlayer serverPlayer = toNMSEntity(player);
        serverPlayer.connection.send(packetCreator.apply(serverPlayer));
    }
}
