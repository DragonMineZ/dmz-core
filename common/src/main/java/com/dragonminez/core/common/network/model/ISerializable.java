package com.dragonminez.core.common.network.model;

import net.minecraft.network.FriendlyByteBuf;

/**
 * Represents a generic object that can be serialized and sent over the network.
 * <p>
 * Implementations define how the object is written to and read from a {@link FriendlyByteBuf}.
 * While this interface is generic and can be used for other serialization purposes, its
 * primary use case is for sending and receiving data between client and server.
 * </p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * public class PlayerData implements ISerializable<PlayerData> {
 *     private final String name;
 *     private final int score;
 *
 *     public PlayerData(String name, int score) {
 *         this.name = name;
 *         this.score = score;
 *     }
 *
 *     @Override
 *     public void encode(FriendlyByteBuf buffer) {
 *         buffer.writeUtf(name);
 *         buffer.writeInt(score);
 *     }
 *
 *     @Override
 *     public PlayerData decode(FriendlyByteBuf buffer) {
 *         String name = buffer.readUtf(32767);
 *         int score = buffer.readInt();
 *         return new PlayerData(name, score);
 *     }
 * }
 *
 * // Sending over network:
 * NetworkManager.sendToServer(new PlayerData("Yusex", 42));
 * NetworkManager.sendTo(player, new PlayerData("Choco", 42));
 * }</pre>
 *
 * @param <T> the concrete type of the serializable object
 */
public interface ISerializable<T extends ISerializable<T>> {

    /**
     * Serializes this object's data into the given buffer.
     *
     * @param buffer the {@link FriendlyByteBuf} to write data into
     */
    void encode(FriendlyByteBuf buffer);

    /**
     * Deserializes an object of type {@code T} from the given buffer.
     *
     * @param buffer the {@link FriendlyByteBuf} to read data from
     * @return a new instance of the object with data read from the buffer
     */
    T decode(FriendlyByteBuf buffer);
}
