package com.dragonminez.core.client.keybind;

import com.dragonminez.core.client.keybind.model.Keybind;
import com.dragonminez.core.common.Env;
import com.dragonminez.core.common.Reference;
import com.dragonminez.core.common.registry.RegistryManager;
import com.dragonminez.core.common.registry.model.Registry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

/**
 * Client-side registry for {@link Keybind} instances.
 *
 * <p>This registry stores all mod keybinds by their logical id ({@link String}) and
 * automatically registers them with Forge during the {@link RegisterKeyMappingsEvent}.</p>
 *
 * <p>Only non-debug keybinds are registered in production builds. Keys marked as
 * {@linkplain Keybind#isDebugKey() debug keys} are only registered when the environment
 * is in debug mode ({@link Env#isDebug()}).</p>
 *
 * <p>Keybinds are expected to be registered into this registry via
 * {@link RegistryManager#registerValue(Class, Object, Object)} during
 * client setup. Once that is done, this class takes care of registering them with
 * the actual Minecraft key mapping system.</p>
 */
@EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT, bus = Bus.MOD)
public class KeybindRegistry extends Registry<String, Keybind> {

  /**
   * Forge event handler that registers all stored {@link Keybind} instances as
   * {@link net.minecraft.client.KeyMapping}s.
   *
   * <p>The method:
   * <ol>
   *   <li>Retrieves all keybinds stored in {@link KeybindRegistry} via
   *       {@link RegistryManager#getValues(Class)}.</li>
   *   <li>Skips debug-only keybinds if the environment is not in debug mode.</li>
   *   <li>Registers the remaining keybinds with the given {@link RegisterKeyMappingsEvent}.</li>
   * </ol>
   *
   * <p>This is subscribed to the MOD event bus on the client side, so it runs
   * automatically during the appropriate Forge setup phase.</p>
   *
   * @param event the Forge key-mapping registration event.
   */
  @SubscribeEvent
  public static void onKeyRegistry(RegisterKeyMappingsEvent event) {
    for (Keybind value : RegistryManager.getValues(KeybindRegistry.class)) {
      if (value.isDebugKey() && !Env.isDebug()) {
        continue;
      }
      event.register(value);
    }
  }
}
