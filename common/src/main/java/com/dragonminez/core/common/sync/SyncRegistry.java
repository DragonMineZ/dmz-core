package com.dragonminez.core.common.sync;

import com.dragonminez.core.common.network.model.ISerializable;
import com.dragonminez.core.common.registry.RegistryManager;
import com.dragonminez.core.common.registry.model.Registry;
import java.util.function.BiConsumer;
import net.minecraft.world.entity.player.Player;

public final class SyncRegistry extends Registry<
    Class<? extends ISerializable<?>>,
    BiConsumer<Player, ? extends ISerializable<?>>
    > {

  private SyncRegistry() {
  }

  public static <M extends ISerializable<M>> void register(
      Class<M> type,
      BiConsumer<Player, M> handler
  ) {
    RegistryManager.registerValue(SyncRegistry.class, type, handler);
  }
}
