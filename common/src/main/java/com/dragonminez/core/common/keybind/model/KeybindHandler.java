package com.dragonminez.core.common.keybind.model;

import net.minecraft.world.entity.player.Player;

/**
 * Base class for keybind handlers executed on both client and server.
 *
 * <p>A {@code KeybindHandler} defines the behavior of a keybind each time it is activated.
 * The handler may be invoked:
 * <ul>
 *   <li><b>Client-side</b> — when the physical key is pressed locally.</li>
 *   <li><b>Server-side</b> — when the client notifies the server and the packet is validated.</li>
 * </ul>
 *
 * <h3>Anticheat Note</h3>
 * <p><b>The anticheat checks are performed ONLY on the server.</b>
 * The client is untrusted and may lie (e.g. always reporting {@code heldDown = false}). Therefore:
 * <ul>
 *   <li>The server independently validates if a keybind may be held.</li>
 *   <li>The server rate-limits tap presses for keybinds that cannot be held.</li>
 *   <li>If the packet fails validation, the server kicks the player.</li>
 * </ul>
 * The client never runs nor enforces anticheat logic.</p>
 *
 * <p>The handler itself does not enforce security — it simply reacts to the event. The
 * {@code KeybindManager} performs all anticheat checks before invoking the server-side handler.</p>
 */
public abstract class KeybindHandler {

  /**
   * Whether this keybind is intended to support continuous hold behavior.
   *
   * <p>This flag:
   * <ul>
   *   <li>informs the server's anticheat whether held-down usage is allowed,</li>
   *   <li>does NOT prevent the client from reporting heldDown = true (clients can lie),</li>
   *   <li>is available to client-side logic, but only enforced on the server.</li>
   * </ul>
   */
  private final boolean canBeHeldDown;

  /**
   * Creates a new keybind handler.
   *
   * @param canBeHeldDown {@code true} if the keybind is allowed to generate repeated events while held,
   *                      {@code false} if it is meant to be press-only.
   */
  public KeybindHandler(boolean canBeHeldDown) {
    this.canBeHeldDown = canBeHeldDown;
  }

  /**
   * Called whenever the keybind is activated on either client or server.
   *
   * <p>The meaning of the parameters depends on where this method is called:
   *
   * <h3>Client-side invocation</h3>
   * <ul>
   *   <li>{@code fromServer = false}</li>
   *   <li>{@code heldDown} is based on the client's local key state.</li>
   *   <li>Used for client-only features: rendering, local actions, UI updates, etc.</li>
   *   <li>No anticheat is performed on the client.</li>
   * </ul>
   *
   * <h3>Server-side invocation</h3>
   * <ul>
   *   <li>{@code fromServer = true}</li>
   *   <li>{@code heldDown} comes from a network packet — but the server does NOT trust it.</li>
   *   <li>Before this method is called, the server's <b>KeybindManager</b> performs:
   *     <ul>
   *       <li>illegal hold detection (if key cannot be held)</li>
   *       <li>fast-tap spam detection (for non-holdable keys)</li>
   *       <li>player kick if validation fails</li>
   *     </ul>
   *   </li>
   * </ul>
   *
   * <p>By the time this method is invoked on the server, the input is considered validated.</p>
   *
   * @param player     the player who pressed the key
   * @param heldDown   whether the client reported the key as held
   * @param fromServer {@code true} when running on server from a validated packet; {@code false} when running on client
   */
  public abstract void onPress(Player player, boolean heldDown, boolean fromServer);

  /**
   * Whether this keybind is intended to support hold behavior.
   *
   * <p>Used by the <b>server</b> to enforce anticheat rules. The client does not enforce this.</p>
   */
  public boolean canBeHeldDown() {
    return this.canBeHeldDown;
  }
}
