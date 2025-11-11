package com.dragonminez.core.common.sync;

import com.dragonminez.core.common.Env;
import com.dragonminez.core.common.network.NetworkManager;
import com.dragonminez.core.common.network.model.ISerializable;
import com.dragonminez.core.common.sync.model.SyncPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/**
 * SyncManager is the central class for sending synchronizable objects (POJOs)
 * between the server and client in a generic way.
 * <p>
 * All objects sent must be serializable via {@link SyncPacket}.
 * This manager provides methods to send packets to a specific player,
 * to all players, or to all players tracking a specific entity.
 * </p>
 */
public class SyncManager {

    /**
     * Sends a synchronizable object to a specific player.
     *
     * @param object The object to send. Must be compatible with {@link SyncPacket}.
     * @param player The player who will receive the packet.
     * @param <T>    The type of the object being synchronized.
     */
    public static <T extends ISerializable<T>> void sendTo(T object, ServerPlayer player) {
        NetworkManager.sendTo(new SyncPacket<>(Env.CLIENT, object), player);
    }

    /**
     * Sends a synchronizable object to all connected players.
     *
     * @param object The object to send. Must be compatible with {@link SyncPacket}.
     * @param <T>    The type of the object being synchronized.
     */
    public static <T extends ISerializable<T>> void broadcast(T object) {
        NetworkManager.sendToAll(new SyncPacket<>(Env.CLIENT, object));
    }

    /**
     * Sends a synchronizable object to all players tracking a specific entity.
     *
     * @param object        The object to send. Must be compatible with {@link SyncPacket}.
     * @param entityToTrack The entity being tracked by the players who will receive the packet.
     * @param <T>           The type of the object being synchronized.
     */
    public static <T extends ISerializable<T>> void sendTracking(T object, LivingEntity entityToTrack) {
        NetworkManager.sendToAllTracking(new SyncPacket<>(Env.CLIENT, object), entityToTrack);
    }

    /**
     * Sends a synchronizable object from the client to the server.
     *
     * @param object The object to send. Must be compatible with {@link SyncPacket}.
     * @param <T>    The type of the object being synchronized.
     */
    public static <T extends ISerializable<T>> void sendToServer(T object) {
        NetworkManager.sendToServer(new SyncPacket<>(Env.SERVER, object));
    }
}
