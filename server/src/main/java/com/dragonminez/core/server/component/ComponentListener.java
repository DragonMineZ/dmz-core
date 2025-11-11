package com.dragonminez.core.server.component;

import com.dragonminez.core.common.Reference;
import com.dragonminez.core.common.component.event.ComponentModifiedEvent;
import com.dragonminez.core.common.component.model.Component;
import com.dragonminez.core.common.component.model.ComponentDescriptor;
import com.dragonminez.core.common.component.registry.ComponentHolderRegistry;
import com.dragonminez.core.common.component.registry.ComponentRegistry;
import com.dragonminez.core.common.registry.RegistryManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Handles loading, saving, cloning, and cleanup of component data for players. Each player is
 * associated with a {@link ComponentRegistry} containing instances of all components that are
 * marked as auto-applied. These component instances are stored inside the global
 * {@link ComponentHolderRegistry}.
 *
 * <p>Component data is saved using the player's persistent NBT storage under
 * {@link Reference#MOD_ID} and restored on login or clone events.
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ComponentListener {

  /**
   * Initializes a fresh {@link ComponentRegistry} for the loading player, instantiates all
   * auto-applied components, registers them, and then loads their stored data from the player's
   * persistent NBT.
   */
  @SubscribeEvent
  public static void onPlayerLoad(PlayerEvent.LoadFromFile event) {
    if (!(event.getEntity() instanceof ServerPlayer player)) {
      return;
    }

    final ComponentRegistry registry = new ComponentRegistry();
    RegistryManager.registry(ComponentRegistry.class)
        .values()
        .forEach(component -> {
          final Component<?> instance = component.create();
          if (!instance.autoApply()) {
            return;
          }
          registry.register(instance.id(), instance);
        });

    RegistryManager.registerValue(
        ComponentHolderRegistry.class,
        player.getUUID(),
        registry,
        holder -> holder.load(
            player.getUUID(),
            player.getPersistentData().getCompound(Reference.MOD_ID)
        )
    );
  }

  /**
   * Called when a player starts tracking another entity (comes in range). This ensures the tracked
   * player receives the component data of the target player.
   */
  @SubscribeEvent
  public static void onPlayerStartTracking(PlayerEvent.StartTracking event) {
    if (!(event.getEntity() instanceof ServerPlayer tracker)) {
      return;
    }

    if (!(event.getTarget() instanceof ServerPlayer target)) {
      return;
    }

    ComponentSyncManager.sendAllComponentsToPlayer(target, tracker, false);
  }

  /**
   * Called when a player stops tracking another entity (goes out of range). This is used to clean
   * up any temporary tracking-specific data if needed.
   */
  @SubscribeEvent
  public static void onPlayerStopTracking(PlayerEvent.StopTracking event) {
    if (!(event.getEntity() instanceof ServerPlayer tracker)) {
      return;
    }
    if (!(event.getTarget() instanceof ServerPlayer target)) {
      return;
    }

    ComponentSyncManager.sendAllComponentsToPlayer(target, tracker, true);
  }

  /**
   * Handles component modification events by syncing the modified component to all tracking players
   * and the player themselves.
   */
  @SubscribeEvent
  public static void onComponentModified(ComponentModifiedEvent event) {
    if (!(event.player() instanceof ServerPlayer player)) {
      return;
    }
    final ComponentDescriptor<?, ?> descriptor = event.descriptor();
    if (descriptor != null && descriptor.isPublic()) {
      ComponentSyncManager.sendComponentToTracking(player, event.component(), false);
    }
    ComponentSyncManager.sendComponentToPlayer(player, event.component(), player, false);
  }

  /**
   * Gathers all component data associated with the player and writes the resulting NBT into the
   * player's persistent data under the mod's root tag.
   */
  @SubscribeEvent
  public static void onPlayerSave(PlayerEvent.SaveToFile event) {
    if (!(event.getEntity() instanceof ServerPlayer player)) {
      return;
    }

    final ComponentHolderRegistry provider = RegistryManager.registry(
        ComponentHolderRegistry.class);
    final CompoundTag data = new CompoundTag();
    provider.save(player.getUUID(), data);
    player.getPersistentData().put(Reference.MOD_ID, data);
  }

  /**
   * Transfers component data when the player is cloned (death, dimension change). If the old player
   * has stored data, it is copied. Otherwise, a new default component registry is assigned.
   */
  @SubscribeEvent
  public static void onPlayerClone(PlayerEvent.Clone event) {
    if (!(event.getOriginal() instanceof ServerPlayer oldPlayer)) {
      return;
    }
    if (!(event.getEntity() instanceof ServerPlayer newPlayer)) {
      return;
    }

    final ComponentHolderRegistry provider = RegistryManager.registry(
        ComponentHolderRegistry.class);

    if (provider.get(oldPlayer.getUUID()).isPresent()) {
      final CompoundTag data = new CompoundTag();
      provider.save(oldPlayer.getUUID(), data);
      provider.load(newPlayer.getUUID(), data);
      return;
    }

    final ComponentRegistry defaults = new ComponentRegistry();
    RegistryManager.registry(ComponentRegistry.class)
        .values()
        .forEach(component -> {
          final Component<?> instance = component.create();
          defaults.register(instance.id(), instance);
        });

    provider.register(newPlayer.getUUID(), defaults);
  }

  /**
   * Removes the player’s component data from memory on logout to prevent memory growth. Persistent
   * data remains stored in the player file.
   */
  @SubscribeEvent
  public static void onPlayerLogout(PlayerLoggedOutEvent event) {
    if (!(event.getEntity() instanceof ServerPlayer player)) {
      return;
    }
    RegistryManager.removeValue(ComponentHolderRegistry.class, player.getUUID());
  }
}

