package com.dragonminez.core.common.sync.model;

import com.dragonminez.core.common.Env;
import com.dragonminez.core.common.network.model.IPacket;
import com.dragonminez.core.common.util.SerializerUtil;
import net.minecraft.network.FriendlyByteBuf;

/**
 * A generic network packet used for synchronizing arbitrary POJOs between
 * the server and client.
 * <p>
 * The packet includes the class type of the object being sent and the serialized
 * object data. Deserialization reconstructs the original object using the class name.
 * </p>
 *
 * @param <T> The type of object being synchronized.
 */
public class SyncPacket<T> implements IPacket {

    /**
     * The environment (client or server) for which this packet is intended.
     **/
    private final Env receiverSide;

    /**
     * The object being synchronized.
     */
    private final T data;

    /**
     * The class type of the object being synchronized.
     */
    private final Class<?> type;


    /**
     * Creates a new SyncPacket for the given object.
     *
     * @param type The class of the object.
     * @param data The object to synchronize.
     */
    public SyncPacket(Env receiverSide, Class<?> type, T data) {
        this.receiverSide = receiverSide;
        this.type = type;
        this.data = data;
    }

    /**
     * Serializes the object into the given buffer.
     * Writes the class name followed by the serialized object data.
     *
     * @param buf The buffer to write into.
     */
    @Override
    public void encode(FriendlyByteBuf buf) {
        try {
            buf.writeEnum(receiverSide);
            buf.writeUtf(type.getName());
            SerializerUtil.writeObject(buf, data);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to encode object", e);
        }
    }

    /**
     * Deserializes a SyncPacket from the given buffer.
     * Reads the class name and reconstructs the object using {@link SerializerUtil}.
     *
     * @param buf The buffer to read from.
     * @param <T> The type of object being synchronized.
     * @return A new SyncPacket containing the deserialized object.
     */
    @SuppressWarnings("unchecked")
    public static <T> SyncPacket<T> decode(FriendlyByteBuf buf) {
        try {
            final Env receiver = buf.readEnum(Env.class);
            final String className = buf.readUtf();
            final Class<T> clazz = (Class<T>) Class.forName(className);
            final T obj = (T) SerializerUtil.readObject(buf, clazz);
            return new SyncPacket<>(receiver, clazz, obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode SyncPacket", e);
        }
    }

    /**
     * Returns the object contained in this packet.
     *
     * @return The synchronized object.
     */
    public T data() {
        return data;
    }

    /**
     * Returns the receiver side for this packet.
     *
     * @return The environment (client or server).
     */
    public Env receiverSide() {
        return receiverSide;
    }
}
