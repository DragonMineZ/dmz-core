package com.dragonminez.core.common.network;

import com.dragonminez.core.common.Reference;
import com.dragonminez.core.common.network.model.ISerializable;
import com.dragonminez.core.common.sync.model.SyncPacket;
import com.dragonminez.core.common.sync.network.HandlerSyncPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * This class manages the registration and handling of network messages for client-server
 * communication. It provides a SimpleChannel for sending and receiving serializables between
 * clients and the server.
 *
 * <p>To send serializables from the server to a specific client, use:</p>
 * <pre>
 * NetworkRegistry.sendTo(message, player);
 * </pre>
 *
 * <p>To send serializables to all clients tracking a specific chunk, use:</p>
 * <pre>
 * NetworkRegistry.sendToClient(message, levelChunk);
 * </pre>
 *
 * <p>To send serializables to all clients tracking a specific position, use:</p>
 * <pre>
 * NetworkRegistry.sendToClient(message, level, position);
 * </pre>
 *
 * <p>To send serializables to all clients tracking a specific entity, use:</p>
 * <pre>
 * NetworkRegistry.sendToAllTracking(message, entityToTrack);
 * </pre>
 *
 * <p>To broadcast serializables to all connected clients, use:</p>
 * <pre>
 * NetworkRegistry.sendToAll(message);
 * </pre>
 *
 * <p>To send serializables from the client to the server, use:</p>
 * <pre>
 * NetworkRegistry.sendToServer(message);
 * </pre>
 *
 * <p>In the examples above, <code>message</code> is the data being sent, and parameters like
 * <code>player</code>,
 * <code>levelChunk</code>, <code>position</code>, and <code>entityToTrack</code> represent the
 * target clients or entities for the message.</p>
 */
public class NetworkManager {

  // Version identifier for the network protocol, used to verify compatibility
  private static final String PROTOCOL_VERSION = "2";

  // Singleton instance of the SimpleChannel, which is the main channel for network messages
  public static SimpleChannel INSTANCE;

  /**
   * Registers the main network channel and registers client and server messages. This method
   * initializes the SimpleChannel with a unique resource location and protocol version.
   */
  @SuppressWarnings("unchecked")
  public static void init() {
    // Initialize the main SimpleChannel instance for network messaging
    NetworkManager.INSTANCE = net.minecraftforge.network.NetworkRegistry.newSimpleChannel(
        ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "main"),
        // Unique identifier for the channel
        () -> PROTOCOL_VERSION, // Protocol version supplier for compatibility check
        PROTOCOL_VERSION::equals, // Predicate to validate the client version
        PROTOCOL_VERSION::equals  // Predicate to validate the server version
    );
    // Register synchronization serializable handler
    NetworkManager.INSTANCE.registerMessage(0,
        SyncPacket.class, SyncPacket::encode,
        buf -> new SyncPacket<>().decode(buf), HandlerSyncPacket::handle);
  }

  /**
   * Provides access to the network channel for message distribution.
   *
   * @return The SimpleChannel instance for sending network messages.
   */
  public static SimpleChannel playChannel() {
    return INSTANCE;
  }

  /**
   * Sends a message to a specific client.
   *
   * @param serializable The message to send.
   * @param player       The player who will receive the message.
   */
  public static <T extends ISerializable<T>> void sendTo(T serializable, Player player) {
    INSTANCE.sendTo(serializable, ((ServerPlayer) player).connection.connection,
        NetworkDirection.PLAY_TO_CLIENT);
  }

  /**
   * Sends a message to all clients tracking a specific position within the world.
   *
   * @param message The message to send.
   * @param level   The level where the position is located.
   * @param pos     The position within the level.
   */
  public static <T extends ISerializable<T>> void sendToClient(T message, Level level,
      BlockPos pos) {
    sendToClient(message, level.getChunkAt(pos));
  }

  /**
   * Sends a message to all clients tracking a specific chunk.
   *
   * @param serializable The message to send.
   * @param chunk        The chunk being tracked by the clients.
   */
  public static <T extends ISerializable<T>> void sendToClient(T serializable, LevelChunk chunk) {
    INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> chunk), serializable);
  }

  /**
   * Sends a message to all clients tracking a specific entity.
   *
   * @param serializable  The message to send.
   * @param entityToTrack The entity being tracked by the clients.
   */
  public static <T extends ISerializable<T>> void sendToAllTracking(T serializable,
      LivingEntity entityToTrack) {
    INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> entityToTrack), serializable);
  }

  /**
   * Broadcasts a message to all connected clients.
   *
   * @param serializable The message to broadcast.
   */
  public static <T extends ISerializable<T>> void sendToAll(T serializable) {
    INSTANCE.send(PacketDistributor.ALL.noArg(), serializable);
  }

  /**
   * Sends a message from the client to the server.
   *
   * @param serializable The message to send to the server.
   */
  public static <T extends ISerializable<T>> void sendToServer(T serializable) {
    INSTANCE.sendToServer(serializable);
  }
}