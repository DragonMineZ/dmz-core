package com.dragonminez.core.common.keybind.network;

import com.dragonminez.core.common.network.model.ISerializable;
import net.minecraft.network.FriendlyByteBuf;

public record KeybindHandlerSync(String keybindId, boolean heldDown)
    implements ISerializable<KeybindHandlerSync> {

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeUtf(this.keybindId);
    buffer.writeBoolean(this.heldDown);
  }

  @Override
  public KeybindHandlerSync decode(FriendlyByteBuf buffer) {
    final String keybindId = buffer.readUtf();
    final boolean heldDown = buffer.readBoolean();
    return new KeybindHandlerSync(keybindId, heldDown);
  }
}
