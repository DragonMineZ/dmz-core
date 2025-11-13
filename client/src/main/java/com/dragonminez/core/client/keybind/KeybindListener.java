package com.dragonminez.core.client.keybind;

import com.dragonminez.core.client.keybind.model.Keybind;
import com.dragonminez.core.common.Reference;
import com.dragonminez.core.common.keybind.KeybindManager;
import com.dragonminez.core.common.keybind.network.KeybindHandlerSync;
import com.dragonminez.core.common.registry.RegistryManager;
import com.dragonminez.core.common.sync.SyncManager;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side keybind polling system.
 *
 * <p>This class runs every client tick and checks the state of all registered
 * {@link Keybind} instances. Based on whether a keybind is active, it dispatches events either
 * locally or to the server depending on the keybind's settings.</p>
 *
 * <h3>Responsibilities of this class</h3>
 * <ul>
 *   <li>Poll keybinds every tick.</li>
 *   <li>Determine whether a keybind is being tapped or held.</li>
 *   <li>Invoke the client-side handler, if applicable.</li>
 *   <li>Send network packets to the server for keybinds that request it.</li>
 * </ul>
 */
@Mod.EventBusSubscriber(modid = Reference.MOD_ID, value = Dist.CLIENT,
    bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeybindListener {

  /**
   * Tracks the keybind's active state from the previous tick. Used to detect tap (press) events for
   * keys that do not support being held.
   */
  private static final ConcurrentHashMap<String, Boolean> keyStateMap =
      new ConcurrentHashMap<>();

  /**
   * Checks all keybind states once per client tick.
   *
   * <p>Logic:</p>
   * <ul>
   *   <li>If the physical key is down and the keybind supports holding,
   *       then the handler is triggered every tick while it remains held.</li>
   *   <li>If the keybind does not support holding,
   *       then the handler is triggered only on initial press (rising edge).</li>
   *   <li>If the keybind is configured to notify the server,
   *       then a packet is sent instead of running locally.</li>
   * </ul>
   */
  @SubscribeEvent
  public static void onClientTick(TickEvent.ClientTickEvent event) {
    if (event.phase != TickEvent.Phase.END) {
      return;
    }

    final LocalPlayer player = Minecraft.getInstance().player;
    if (player == null) {
      return;
    }

    for (Keybind keybind : RegistryManager.getValues(KeybindRegistry.class)) {
      final boolean isActive = keybind.isActive();
      final boolean wasActive = keyStateMap.getOrDefault(keybind.id(), false);

      // Key is physically pressed
      if (isActive) {
        if (keybind.canBeHeldDown()) {
          // Trigger continuously while held
          KeybindListener.onPress(player, keybind, true);
          continue;
        }

        // For non-holdable keybinds, trigger only once per press
        if (!wasActive) {
          KeybindListener.onPress(player, keybind, false);
        }
      }

      keyStateMap.put(keybind.id(), isActive);
    }
  }

  /**
   * Dispatches a keybind press from the client.
   *
   * <p>Behavior:</p>
   * <ul>
   *   <li>If {@link Keybind#notifyServer()} is true:
   *     <ul>
   *       <li>a packet is sent to the server</li>
   *       <li>no client-side handler is invoked</li>
   *     </ul>
   *   </li>
   *   <li>If false:
   *     <ul>
   *       <li>the keybind is handled locally using {@link KeybindManager}</li>
   *       <li>the call is marked as {@code fromServer = false}</li>
   *     </ul>
   *   </li>
   * </ul>
   *
   * @param player   the local client player
   * @param keybind  the keybind being triggered
   * @param heldDown whether the key is being held this tick
   */
  private static void onPress(Player player, Keybind keybind, boolean heldDown) {
    if (keybind.notifyServer()) {
      SyncManager.sendToServer(new KeybindHandlerSync(keybind.id(), heldDown));
      return;
    }
    // Handle client-only keybinds locally
    KeybindManager.onPress(player, keybind.id(), heldDown, false);
  }
}
