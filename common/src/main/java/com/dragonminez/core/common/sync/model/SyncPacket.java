package com.dragonminez.core.common.sync.model;

import com.dragonminez.core.common.Env;
import com.dragonminez.core.common.network.model.ISerializable;
import net.minecraft.network.FriendlyByteBuf;

public class SyncPacket<T extends ISerializable<T>> implements ISerializable<SyncPacket<T>> {

  private Env receiverSide;
  private T data;

  public SyncPacket() {
  }

  public SyncPacket(Env receiverSide, T data) {
    this.receiverSide = receiverSide;
    this.data = data;
  }

  @Override
  public void encode(FriendlyByteBuf buf) {
    buf.writeEnum(receiverSide);
    data.encode(buf);
  }

  @Override
  public SyncPacket<T> decode(FriendlyByteBuf buf) {
    final Env receiver = buf.readEnum(Env.class);
    final T decodedData = data.decode(buf);
    return new SyncPacket<>(receiver, decodedData);
  }

  public T data() {
    return data;
  }

  public Env receiverSide() {
    return receiverSide;
  }
}
