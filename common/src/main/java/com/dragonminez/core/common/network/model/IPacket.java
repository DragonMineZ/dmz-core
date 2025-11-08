package com.dragonminez.core.common.network.model;

import com.dragonminez.core.common.network.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Represents a network packet that can be sent over a {@link NetworkManager SimpleChannel}.
 * <p>
 * Implementations of this interface must define how the packet's data is serialized into a {@link FriendlyByteBuf}.
 * Typically, a corresponding static {@code decode} method is provided to reconstruct the packet from the buffer.
 * <p>
 * Packets implementing this interface can be sent between the client and server using the helper methods
 * in {@link NetworkManager}, such as {@link NetworkManager#sendTo(IPacket, net.minecraft.world.entity.player.Player)},
 * {@link NetworkManager#sendToServer(IPacket)}, {@link NetworkManager#sendToClient(IPacket, net.minecraft.world.level.Level, net.minecraft.core.BlockPos)},
 * and other variants for broadcasting or targeting entities/chunks.
 *
 * <p>Usage example:</p>
 * <pre>{@code
 * public class ExamplePacket implements IPacket {
 *     private final int value;
 *
 *     public ExamplePacket(int value) { this.value = value; }
 *
 *     @Override
 *     public void encode(FriendlyByteBuf buffer) {
 *         buffer.writeInt(value);
 *     }
 *
 *     public static ExamplePacket decode(FriendlyByteBuf buffer) {
 *         return new ExamplePacket(buffer.readInt());
 *     }
 * }
 *
 * // Sending the packet from server to a player:
 * NetworkManager.sendTo(new ExamplePacket(42), player);
 *
 * // Sending the packet from client to server:
 * NetworkManager.sendToServer(new ExamplePacket(42));
 * }</pre>
 */
public interface IPacket {
    /**
     * Serializes this packet's data into the given buffer.
     *
     * @param buffer the {@link FriendlyByteBuf} to write packet data into
     */
    void encode(FriendlyByteBuf buffer);
}
