package com.dragonminez.core.common.keybind;

import com.dragonminez.core.common.keybind.model.KeybindHandler;
import com.dragonminez.core.common.registry.RegistryManager;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * Central manager for keybind presses, including basic anticheat checks.
 *
 * <p>This class is responsible for:
 * <ul>
 *   <li>Looking up the corresponding {@link KeybindHandler} for a keybind id.</li>
 *   <li>Enforcing server-side rules such as:
 *     <ul>
 *       <li>Disallowing held-down usage if the handler does not support it.</li>
 *       <li>Detecting suspiciously fast repeated taps on <b>non-holdable</b> keybinds and
 *           kicking the player. This also covers the case where a hacked client lies about
 *           {@code heldDown} and always sends {@code false} while physically holding the key.</li>
 *     </ul>
 *   </li>
 *   <li>Forwarding valid presses to the associated {@link KeybindHandler}.</li>
 * </ul>
 *
 * <p>The method {@link #onPress(Player, String, boolean, boolean)} is intended to be called
 * whenever a keybind press is received, typically from a network packet.</p>
 */
public class KeybindManager {

  /**
   * Minimum allowed time between non-held ("tap") presses for the same keybind per player, in
   * milliseconds.
   *
   * <p>If a new tap arrives sooner than this threshold after the previous one for a
   * <b>non-holdable</b> keybind, it is treated as suspicious and the player may be kicked by
   * the anticheat. This helps catch clients that spoof {@code heldDown = false} while
   * actually holding the key.</p>
   */
  private static final long MIN_TAP_INTERVAL_MS = 75L;

  /**
   * Last press timestamps per player and keybind id.
   *
   * <p>Outer map is keyed by player UUID, inner map by keybind id.</p>
   */
  private static final Map<UUID, Map<String, Long>> LAST_PRESS_TIMES = new ConcurrentHashMap<>();

  /**
   * Handles a keybind press and applies anticheat checks before delegating to the handler.
   *
   * <p>Behaviour:
   * <ul>
   *   <li>Looks up the {@link KeybindHandler} registered under {@code keybindId} in
   *       {@link KeybindHandlerRegistry} via {@link RegistryManager}.</li>
   *   <li>If {@code fromServer} is {@code true} and {@code heldDown} is {@code true} while
   *       {@link KeybindHandler#canBeHeldDown()} is {@code false}, the player is kicked
   *       (illegal hold usage).</li>
   *   <li>If {@code fromServer} is {@code true}, {@code heldDown} is {@code false} and the
   *       handler is <b>not</b> holdable, but the last tap for this player+keybind was too recent
   *       (under {@link #MIN_TAP_INTERVAL_MS}), the player is kicked. This is intended to
   *       detect spammy input or clients that never report {@code heldDown = true}.</li>
   *   <li>Otherwise, the press is forwarded to
   *       {@link KeybindHandler#onPress(Player, boolean, boolean)}.</li>
   * </ul>
   *
   * @param player     the player who pressed the key.
   * @param keybindId  the logical id of the keybind.
   * @param heldDown   {@code true} if the key is being held, {@code false} if it is a tap
   *                   (as reported by the client; not necessarily trustworthy).</param>
   * @param fromServer {@code true} if the event comes from a validated server-side context; used to
   *                   decide whether to enforce anticheat.
   */
  public static void onPress(Player player, String keybindId, boolean heldDown,
      boolean fromServer) {

    final Optional<KeybindHandler> optHandler =
        RegistryManager.getValue(KeybindHandlerRegistry.class, keybindId);

    optHandler.ifPresent(handler -> {
      // Disallow illegal held usage for non-holdable keybinds
      if (fromServer && heldDown && !handler.canBeHeldDown()) {
        kickForAnticheat(player,
            "Disconnected by anticheat: illegal held keybind usage (" + keybindId + ")");
        return;
      }

      // Detect suspiciously fast taps on non-holdable keybinds.
      // This also covers hacked clients that always send heldDown = false while holding.
      if (fromServer && !heldDown && !handler.canBeHeldDown() && isTooSoon(player, keybindId)) {
        kickForAnticheat(player,
            "Disconnected by anticheat: suspicious rapid keybind usage (" + keybindId + ")");
        return;
      }

      // Delegate to the handler if all checks pass
      handler.onPress(player, heldDown, fromServer);
    });
  }

  /**
   * Checks whether the given player's tap on the given keybind happened too soon after the previous
   * one.
   *
   * @param player    the player who pressed the key.
   * @param keybindId the keybind id.
   * @return {@code true} if the interval since the last tap is below {@link #MIN_TAP_INTERVAL_MS},
   *         {@code false} otherwise.
   */
  private static boolean isTooSoon(Player player, String keybindId) {
    if (!(player instanceof ServerPlayer)) {
      // Only enforce timing rules on the logical server
      return false;
    }

    final long now = System.currentTimeMillis();
    final UUID uuid = player.getUUID();

    final Map<String, Long> perPlayer = LAST_PRESS_TIMES.computeIfAbsent(
        uuid,
        __ -> new ConcurrentHashMap<>()
    );

    Long last = perPlayer.get(keybindId);
    perPlayer.put(keybindId, now);

    if (last == null) {
      return false;
    }

    return (now - last) < MIN_TAP_INTERVAL_MS;
  }

  /**
   * Disconnects the player from the server with a given anticheat message.
   *
   * @param player  the player to disconnect.
   * @param message the kick message to show to the player.
   */
  private static void kickForAnticheat(Player player, String message) {
    if (player instanceof ServerPlayer serverPlayer) {
      serverPlayer.connection.disconnect(Component.literal(message));
    }
  }
}
