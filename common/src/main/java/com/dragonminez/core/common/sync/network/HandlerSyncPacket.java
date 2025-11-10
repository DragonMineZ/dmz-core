package com.dragonminez.core.common.sync.network;

import com.dragonminez.core.common.registry.RegistryManager;
import com.dragonminez.core.common.sync.SyncRegistry;
import com.dragonminez.core.common.sync.model.SyncPacket;
import com.dragonminez.core.common.util.LogUtil;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Handles incoming {@link SyncPacket}s from the network and dispatches them
 * to the appropriate registered handler in {@link SyncRegistry}.
 * <p>
 * This class ensures that packet handling occurs on the correct thread
 * by using {@link NetworkEvent.Context#enqueueWork(Runnable)}.
 * If no handler is registered for the packet type, a log message is printed.
 * </p>
 */
public class HandlerSyncPacket {

    /**
     * Processes a received SyncPacket.
     *
     * @param syncPacket The received packet containing the object to synchronize.
     * @param ctx        The network context supplier.
     * @param <T>        The type of the object contained in the packet.
     */
    @SuppressWarnings("unchecked")
    public static <T> void handle(SyncPacket<T> syncPacket, Supplier<NetworkEvent.Context> ctx) {
        // Enqueue work to the main thread to safely handle the packet
        ctx.get().enqueueWork(() -> {
            final Class<T> type = (Class<T>) syncPacket.data().getClass();

            // Retrieve the central SyncRegistry from the global RegistryManager
            final SyncRegistry<T> syncRegistry = RegistryManager.registry(SyncRegistry.class);

            // Attempt to execute the handler for this type
            syncRegistry.get(type).ifPresentOrElse(
                    consumer -> consumer.accept(syncPacket.data()), // Call handler if present
                    () -> LogUtil.info(syncPacket.receiverSide(),
                            "No sync handler registered for type: " + type.getName()) // Log if missing
            );
        });

        ctx.get().setPacketHandled(true);
    }
}
