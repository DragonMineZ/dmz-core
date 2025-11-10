package com.dragonminez.core.common.component;

import com.dragonminez.core.common.component.model.Component;
import com.dragonminez.core.common.component.model.ComponentDescriptor;
import com.dragonminez.core.common.component.registry.ComponentHolderRegistry;
import com.dragonminez.core.common.registry.RegistryManager;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Central manager for accessing and modifying player components.
 *
 * <p>This class provides a simplified API for interacting with {@link Component} instances
 * associated with a particular holder (usually a player, referenced by their {@link UUID}).</p>
 *
 * <p>The manager is responsible for reading and writing component fields via
 * {@link ComponentDescriptor} objects and is also intended to be the central place for
 * propagating changes to clients.</p>
 *
 * <p>All modifications should ideally go through this manager to ensure proper
 * synchronization, validation, and consistent access to component data.</p>
 *
 * <h2>Notes on Generics</h2>
 * <ul>
 *     <li>{@code T} is always a subtype of {@link Component<T>}.</li>
 *     <li>{@code V} represents the type of a value inside a {@link ComponentDescriptor}.</li>
 *     <li>Unchecked casts are used internally to safely convert from generic {@link Component} types
 *     to the expected types when using descriptors or modifying components.</li>
 * </ul>
 */
public final class ComponentManager {

    private ComponentManager() {
    }

    /**
     * Retrieves a component instance for a given holder and component ID.
     *
     * @param holderId    The UUID of the holder (player or entity) owning the component.
     * @param componentId The string ID of the component.
     * @param <T>         The type of the component.
     * @return An Optional containing the component if present.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Component<T>> Optional<T> getComponent(UUID holderId, String componentId) {
        ComponentHolderRegistry holder = RegistryManager.registry(ComponentHolderRegistry.class);
        return holder.get(holderId)
                .flatMap(registry -> registry.get(componentId))
                .map(c -> (T) c);
    }

    /**
     * Modifies a component via a {@link Consumer} lambda.
     *
     * <p>This ensures that all changes pass through a centralized place for
     * potential network propagation or other validation hooks.</p>
     *
     * @param holderId    The UUID of the holder.
     * @param componentId The component ID to modify.
     * @param modifier    A Consumer that accepts the component and modifies it.
     * @param <T>         The type of the component.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Component<T>> void modify(UUID holderId, String componentId,
                                                       Consumer<? super T> modifier) {
        getComponent(holderId, componentId).ifPresent(component -> modifier.accept((T) component));
    }

    /**
     * Retrieves the value of a specific descriptor inside a component.
     *
     * @param holderId    The UUID of the holder.
     * @param componentId The component ID.
     * @param descriptor  The descriptor to read.
     * @param <T>         The type of the component.
     * @param <V>         The type of the value inside the descriptor.
     * @return An Optional containing the descriptor's value, if available.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Component<T>, V> Optional<V> get(UUID holderId, String componentId,
                                                              ComponentDescriptor<T, V> descriptor) {
        return getComponent(holderId, componentId)
                .map(component -> (V) descriptor.get((T) component));
    }

    /**
     * Sets the value of a specific descriptor inside a component.
     *
     * @param holderId    The UUID of the holder.
     * @param componentId The component ID.
     * @param descriptor  The descriptor to write to.
     * @param value       The value to set.
     * @param <T>         The type of the component.
     * @param <V>         The type of the value inside the descriptor.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Component<T>, V> void set(UUID holderId, String componentId,
                                                       ComponentDescriptor<T, V> descriptor, V value) {
        getComponent(holderId, componentId)
                .ifPresent(component -> descriptor.set((T) component, value));
    }

    /**
     * Retrieves the value of a descriptor, or returns a fallback if not present.
     *
     * @param holderId    The UUID of the holder.
     * @param componentId The component ID.
     * @param descriptor  The descriptor to read.
     * @param fallback    Supplier to provide a fallback value if the descriptor is missing.
     * @param <T>         The type of the component.
     * @param <V>         The type of the value inside the descriptor.
     * @return The descriptor's value, or the fallback if missing.
     */
    public static <T extends Component<T>, V> V getOr(UUID holderId, String componentId,
                                                      ComponentDescriptor<T, V> descriptor, Supplier<V> fallback) {
        return get(holderId, componentId, descriptor).orElseGet(fallback);
    }

    /**
     * Sends all components of the reference player to a single target player.
     *
     * @param holderUUID The UUID of the player whose components are being sent.
     * @param target     The player who will receive the components.
     */
    public static void sendAllComponentsToPlayer(UUID holderUUID, ServerPlayer target) {
        // TODO: Implement sending all components from holderUUID to target
    }

    /**
     * Sends all components of the reference player to all tracking players around them.
     *
     * @param holderUUID The UUID of the player whose components are being broadcasted.
     */
    public static void sendAllComponentsToTracking(UUID holderUUID) {
        // TODO: Implement sending all components from holderUUID to all tracking players
    }

    /**
     * Sends a single component of the reference player to a single target player.
     *
     * @param holderUUID  The UUID of the player whose component is being sent.
     * @param componentId The ID of the component to send.
     * @param target      The player who will receive the component.
     */
    public static void sendComponentToPlayer(UUID holderUUID, String componentId, ServerPlayer target) {
        // TODO: Implement sending a single component from holderUUID to target
    }

    /**
     * Sends a single component of the reference player to all tracking players around them.
     *
     * @param holderUUID  The UUID of the player whose component is being broadcasted.
     * @param componentId The ID of the component to send.
     */
    public static void sendComponentToTracking(UUID holderUUID, String componentId) {
        // TODO: Implement sending a single component from holderUUID to all tracking players
    }
}
