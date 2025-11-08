package com.dragonminez.core.common.network.model;

import net.minecraftforge.network.NetworkEvent;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class EmptyNetworkHandler<T extends IPacket> {

    private void handler(T packet, Supplier<NetworkEvent.Context> ctx) {
        final NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> {});
        context.setPacketHandled(true);
    }

    public static <T extends IPacket> BiConsumer<T, Supplier<NetworkEvent.Context>> handle() {
        return (t, ctxSupplier) -> new EmptyNetworkHandler<T>().handler(t, ctxSupplier);
    }
}
