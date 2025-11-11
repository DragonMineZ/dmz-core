package com.dragonminez.core.common.component.network.model;

import com.dragonminez.core.common.network.model.ISerializable;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;

public record ComponentRemoveSync(UUID holder, String componentId)
    implements ISerializable<ComponentRemoveSync> {

  @Override
  public void encode(FriendlyByteBuf buf) {
    buf.writeUUID(holder);
    buf.writeUtf(componentId);
  }

  @Override
  public ComponentRemoveSync decode(FriendlyByteBuf buf) {
    final UUID holder = buf.readUUID();
    final String componentId = buf.readUtf();
    return new ComponentRemoveSync(holder, componentId);
  }
}
