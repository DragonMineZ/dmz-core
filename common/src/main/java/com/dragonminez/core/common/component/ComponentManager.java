package com.dragonminez.core.common.component;

import com.dragonminez.core.common.component.event.ComponentModifiedEvent;
import com.dragonminez.core.common.component.model.Component;
import com.dragonminez.core.common.component.model.ComponentDescriptor;
import com.dragonminez.core.common.component.registry.ComponentHolderRegistry;
import com.dragonminez.core.common.component.registry.ComponentRegistry;
import com.dragonminez.core.common.registry.RegistryManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;

/**
 * Central manager for accessing and modifying player components.
 *
 * <p>This class provides a simplified API for interacting with {@link Component} instances
 * associated with a particular holder (usually a player, referenced by their {@link UUID}).</p>
 *
 * <p>The manager is responsible for reading and writing component fields via
 * {@link ComponentDescriptor} objects and is also intended to be the central place for propagating
 * changes to clients.</p>
 *
 * <p>All modifications should ideally go through this manager to ensure proper
 * synchronization, validation, and consistent access to component data.</p>
 *
 * <h2>Notes on Generics</h2>
 * <ul>
 *     <li>{@code T} is always a subtype of {@link Component}.</li>
 *     <li>{@code V} represents the type of a value inside a {@link ComponentDescriptor}.</li>
 *     <li>Unchecked casts are used internally to safely convert from generic {@link Component} types
 *     to the expected types when using descriptors or modifying components.</li>
 * </ul>
 */
public final class ComponentManager {

  private ComponentManager() {
  }

  /**
   * Modifies a component via a {@link Consumer} lambda.
   *
   * <p>This ensures that all changes pass through a centralized place for
   * potential network propagation or other validation hooks.</p>
   *
   * @param player      The holder
   * @param componentId The component ID to modify.
   * @param modifier    A Consumer that accepts the component and modifies it.
   * @param <T>         The type of the component.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Component<T>> void modify(Player player, String componentId,
      Consumer<? super T> modifier) {
    getComponent(player, componentId).ifPresent(component -> {
      modifier.accept((T) component);
      MinecraftForge.EVENT_BUS.post(new ComponentModifiedEvent(player, component));
    });
  }

  /**
   * Sets the value of a specific descriptor inside a component.
   *
   * @param player      The holder
   * @param componentId The component ID.
   * @param descriptor  The descriptor to write to.
   * @param value       The value to set.
   * @param <T>         The type of the component.
   * @param <V>         The type of the value inside the descriptor.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Component<T>, V> void set(Player player, String componentId,
      ComponentDescriptor<T, V> descriptor, V value) {
    getComponent(player, componentId)
        .ifPresent(component -> {
          descriptor.set((T) component, value);
          MinecraftForge.EVENT_BUS.post(new ComponentModifiedEvent(player, component, descriptor));
        });
  }

  /**
   * Removes a component from a given holder.
   *
   * @param player      The holder
   * @param componentId The string ID of the component to remove.
   */
  public static void removeComponent(Player player, String componentId) {
    final ComponentHolderRegistry holder = RegistryManager
        .registry(ComponentHolderRegistry.class);
    final Optional<ComponentRegistry> optRegistry = holder.get(player.getUUID());
    if (optRegistry.isEmpty()) {
      return;
    }
    final ComponentRegistry registry = optRegistry.get();
    registry.remove(componentId);
  }

  /**
   * Retrieves a component instance for a given holder and component ID.
   *
   * @param player      The holder
   * @param componentId The string ID of the component.
   * @param <T>         The type of the component.
   * @return An Optional containing the component if present.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Component<T>> Optional<T> getComponent(Player player,
      String componentId) {
    ComponentHolderRegistry holder = RegistryManager.registry(ComponentHolderRegistry.class);
    return holder.get(player.getUUID())
        .flatMap(registry -> registry.get(componentId))
        .map(c -> (T) c);
  }

  /**
   * Retrieves the value of a specific descriptor inside a component.
   *
   * @param player      The holder
   * @param componentId The component ID.
   * @param descriptor  The descriptor to read.
   * @param <T>         The type of the component.
   * @param <V>         The type of the value inside the descriptor.
   * @return An Optional containing the descriptor's value, if available.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Component<T>, V> Optional<V> get(Player player, String componentId,
      ComponentDescriptor<T, V> descriptor) {
    return getComponent(player, componentId)
        .map(component -> (V) descriptor.get((T) component));
  }

  /**
   * Retrieves the value of a descriptor, or returns a fallback if not present.
   *
   * @param player      The holder
   * @param componentId The component ID.
   * @param descriptor  The descriptor to read.
   * @param fallback    Supplier to provide a fallback value if the descriptor is missing.
   * @param <T>         The type of the component.
   * @param <V>         The type of the value inside the descriptor.
   * @return The descriptor's value, or the fallback if missing.
   */
  public static <T extends Component<T>, V> V getOr(Player player, String componentId,
      ComponentDescriptor<T, V> descriptor, Supplier<V> fallback) {
    return get(player, componentId, descriptor).orElseGet(fallback);
  }

  /**
   * Retrieves all components associated with a given holder.
   *
   * <p>This returns a map of component IDs to their corresponding {@link Component} instances.
   * The returned map is unmodifiable to prevent accidental modification of the internal
   * registry.</p>
   *
   * @param player The holder
   * @return An unmodifiable map of component IDs to {@link Component} instances. Returns an empty
   * map if no components exist for the holder.
   */
  public static List<Component<?>> getAll(Player player) {
    final Optional<ComponentRegistry> optComponent = RegistryManager.getValue(
        ComponentHolderRegistry.class, player.getUUID());
    if (optComponent.isEmpty()) {
      return List.of();
    }
    final ComponentRegistry components = optComponent.get();
    return components.values().toList();
  }
}
