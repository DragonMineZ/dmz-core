package com.dragonminez.core.common.network.event;

import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketRegistrationEvent extends Event {

    private final SimpleChannel instance;

    public PacketRegistrationEvent(SimpleChannel instance) {
        this.instance = instance;
    }

    public SimpleChannel instance() {
        return instance;
    }
}
