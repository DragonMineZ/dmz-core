package com.dragonminez.core.server.component;

import static com.dragonminez.core.common.component.ComponentManager.getAll;

import com.dragonminez.core.common.component.model.Component;
import com.dragonminez.core.common.component.network.model.ComponentAddSync;
import com.dragonminez.core.common.component.network.model.ComponentRemoveSync;
import com.dragonminez.core.common.network.model.ISerializable;
import com.dragonminez.core.common.sync.SyncManager;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

/**
 * Handles the synchronization of {@link Component}s between server players.
 * <p>
 * Provides utilities to send individual components or all components of a player to a target player
 * or to all players tracking the reference player.
 * <p>
 * This class is server-side only and ensures that changes in a player's components are correctly
 * reflected across connected clients.
 */
public final class ComponentSyncManager {

  private ComponentSyncManager() {
  }

  /**
   * Creates a network packet to either add or remove a component for a player.
   *
   * @param holder    The UUID of the player whose component is being synced.
   * @param component The component to add or remove.
   * @param remove    Whether the packet is for removal ({@code true}) or addition ({@code false}).
   * @return The packet object that can be sent via {@link SyncManager}.
   */
  @SuppressWarnings("unchecked")
  private static <T extends ISerializable<T>> T createComponentPacket(UUID holder,
      Component<?> component, boolean onlyPublic, boolean remove) {
    if (remove) {
      return (T) new ComponentRemoveSync(holder, component.id());
    }
    return (T) new ComponentAddSync(holder, component, onlyPublic);
  }

  /**
   * Sends a single component of a player to a specific target player.
   *
   * @param holder    The player whose component is being sent.
   * @param component The component to send.
   * @param target    The player who will receive the component packet.
   * @param remove    Whether this packet represents a removal of the component.
   */
  public static <T extends ISerializable<T>> void sendComponentToPlayer(ServerPlayer holder,
      Component<?> component,
      ServerPlayer target, boolean remove) {
    final T packet = createComponentPacket(holder.getUUID(), component, !holder.is(target), remove);
    SyncManager.sendTo(packet, target);
  }

  /**
   * Broadcasts a single component of a player to all players tracking them.
   *
   * @param holder    The player whose component is being broadcasted.
   * @param component The component to send.
   * @param remove    Whether this packet represents a removal of the component.
   */
  public static <T extends ISerializable<T>> void sendComponentToTracking(ServerPlayer holder,
      Component<?> component, boolean remove) {
    final T packet = createComponentPacket(holder.getUUID(), component, true, remove);
    SyncManager.sendTracking(packet, holder);
  }

  /**
   * Sends all components of a player to a specific target player.
   *
   * @param holder The player whose components are being sent.
   * @param target The player who will receive all component packets.
   * @param remove Whether these packets represent removal of the components.
   */
  public static void sendAllComponentsToPlayer(ServerPlayer holder, ServerPlayer target,
      boolean remove) {
    for (Component<?> component : getAll(holder)) {
      sendComponentToPlayer(holder, component, target, remove);
    }
  }
}
