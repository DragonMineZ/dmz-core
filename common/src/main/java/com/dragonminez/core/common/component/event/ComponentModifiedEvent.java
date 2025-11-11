package com.dragonminez.core.common.component.event;

import com.dragonminez.core.common.component.model.Component;
import com.dragonminez.core.common.component.model.ComponentDescriptor;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Fired whenever a {@link Component} on a {@link Player} is modified.
 * <p>
 * This event can carry an optional {@link ComponentDescriptor} to indicate
 * which specific value or aspect of the component changed.
 * <p>
 * Listeners can use this event to react to component updates,
 * such as syncing data to the client or triggering abilities.
 */
public class ComponentModifiedEvent extends Event {

  private final Player player;
  private final Component<?> component;
  private final ComponentDescriptor<?, ?> descriptor;

  /**
   * Creates a new ComponentModifiedEvent for a specific player and component.
   *
   * @param player     The player whose component was modified.
   * @param component  The component that was modified.
   * @param descriptor The specific descriptor within the component that changed,
   *                   or {@code null} if the entire component changed.
   */
  public ComponentModifiedEvent(Player player, Component<?> component,
      @Nullable ComponentDescriptor<?, ?> descriptor) {
    this.player = player;
    this.component = component;
    this.descriptor = descriptor;
  }

  /**
   * Creates a new ComponentModifiedEvent for a specific player and component
   * when no specific descriptor is associated with the change.
   *
   * @param player    The player whose component was modified.
   * @param component The component that was modified.
   */
  public ComponentModifiedEvent(Player player, Component<?> component) {
    this(player, component, null);
  }

  /** @return The player whose component was modified. */
  public Player player() {
    return player;
  }

  /** @return The component that was modified. */
  public Component<?> component() {
    return component;
  }

  /**
   * @return The descriptor that was modified, or {@code null} if the entire component changed.
   */
  @Nullable
  public ComponentDescriptor<?, ?> descriptor() {
    return descriptor;
  }
}
