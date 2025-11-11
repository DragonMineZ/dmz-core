package com.dragonminez.core.client.component.sync;

import com.dragonminez.core.common.component.ComponentManager;
import com.dragonminez.core.common.component.network.model.ComponentAddSync;
import com.dragonminez.core.common.component.network.model.ComponentRemoveSync;
import com.dragonminez.core.common.registry.RegistryManager;
import com.dragonminez.core.common.sync.SyncRegistry;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * Handles client-side processing of component synchronization packets.
 *
 * <p>This class registers handlers into {@link SyncRegistry} for two packet types:
 * <ul>
 *     <li>{@link ComponentAddSync} – synchronizes full component data to the client.</li>
 *     <li>{@link ComponentRemoveSync} – removes a component client-side.</li>
 * </ul>
 *
 * <p>Once registered, incoming packets are automatically dispatched to these handlers
 * by the sync system. The user does not need to call anything except {@link #init()} during
 * client initialization.
 */
public class ComponentSyncRegister {

  /**
   * Registers all client-side synchronization handlers for component packets.
   *
   * <p>This method inserts two handlers into {@link SyncRegistry}:
   * <ul>
   *     <li>A handler for {@link ComponentAddSync} that:
   *         <ul>
   *             <li>Looks up the player corresponding to the packet’s holder UUID.</li>
   *             <li>Merges the received NBT data into the existing client component.</li>
   *         </ul>
   *     </li>
   *
   *     <li>A handler for {@link ComponentRemoveSync} that:
   *         <ul>
   *             <li>Finds the referenced player.</li>
   *             <li>Removes the component locally via {@link ComponentManager}.</li>
   *         </ul>
   *     </li>
   * </ul>
   *
   * <p>The method assumes that it is executed on the client and that
   * {@link Minecraft#getInstance()} is available.
   */
  @SuppressWarnings("unchecked")
  public static void init() {

    RegistryManager.registerValue(
        SyncRegistry.class,
        ComponentAddSync.class,
        (Consumer<ComponentAddSync>) sync -> {
          Player player = null;
          if (Minecraft.getInstance().level != null) {
            player = Minecraft.getInstance().level.getPlayerByUUID(sync.holder());
          }
          if (player == null) {
            return;
          }

          ComponentManager.modify(null, sync.component().id(), original -> {
            CompoundTag data = new CompoundTag();
            sync.component().toNbt(data, false);
            original.fromNbt(data, false);
          });
        }
    );

    RegistryManager.registerValue(
        SyncRegistry.class,
        ComponentRemoveSync.class,
        (Consumer<ComponentRemoveSync>) sync -> {
          Player player = null;
          if (Minecraft.getInstance().level != null) {
            player = Minecraft.getInstance().level.getPlayerByUUID(sync.holder());
          }

          if (player == null) {
            return;
          }

          ComponentManager.removeComponent(player, sync.componentId());
        }
    );
  }
}
