package com.dragonminez.core.client.keybind.model;

import com.dragonminez.core.common.Reference;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

/**
 * Extended {@link KeyMapping} with extra metadata used by the mod:
 * <ul>
 *   <li>{@code id}: logical keybind id used on client/server.</li>
 *   <li>{@code canBeHeldDown}: whether holding this key is allowed (used by anticheat).</li>
 *   <li>{@code notifyServer}: whether this keybind should send packets to the server.</li>
 *   <li>{@code ctrlRequired}: whether Ctrl must be held to consider the key "active".</li>
 *   <li>{@code isDebugKey}: whether this keybind is intended only for debug/dev features.</li>
 * </ul>
 *
 * <p>Use the various {@code create(...)} factory methods to construct instances with
 * different combinations of options.</p>
 */
public class Keybind extends KeyMapping {

  /**
   * Logical identifier for this keybind (also used in translation key).
   */
  private final String id;

  /**
   * Whether this keybind is allowed to be held down.
   * If {@code false}, the server-side handler may treat long holds as suspicious.
   */
  private final boolean canBeHeldDown;

  /**
   * Whether this keybind should notify the server when pressed.
   * If {@code false}, the key is handled purely client-side.
   */
  private final boolean notifyServer;

  /**
   * If {@code true}, Ctrl must be held for {@link #isActive()} to return {@code true}.
   */
  private final boolean ctrlRequired;

  /**
   * Marks this key as a debug key (for dev / debug-only functionality).
   */
  private final boolean isDebugKey;

  /**
   * Full constructor used internally by the static factory methods.
   *
   * @param id            logical keybind id (used in registry and translation key).
   * @param type          input type (e.g. {@link InputConstants.Type#KEYSYM}).
   * @param key           default key code.
   * @param category      translation key for the keybind category.
   * @param canBeHeldDown whether holding this key is allowed.
   * @param notifyServer  whether this keybind sends events to the server.
   * @param ctrlRequired  whether Ctrl must be pressed for the keybind to be active.
   * @param isDebugKey    whether this keybind is debug-only.
   */
  private Keybind(String id,
      InputConstants.Type type,
      int key,
      String category,
      boolean canBeHeldDown,
      boolean notifyServer,
      boolean ctrlRequired,
      boolean isDebugKey) {
    super("key." + Reference.MOD_ID + "." + id, type, key, category);
    this.id = id;
    this.canBeHeldDown = canBeHeldDown;
    this.notifyServer = notifyServer;
    this.ctrlRequired = ctrlRequired;
    this.isDebugKey = isDebugKey;
  }

  /**
   * Creates a keybind with all flags explicitly specified.
   *
   * @see #canBeHeldDown()
   * @see #notifyServer()
   * @see #isCtrlRequired()
   * @see #isDebugKey()
   */
  public static Keybind create(String id,
      InputConstants.Type type,
      int key,
      String category,
      boolean canBeHeldDown,
      boolean notifyServer,
      boolean ctrlRequired,
      boolean isDebugKey) {
    return new Keybind(id, type, key, category, canBeHeldDown, notifyServer, ctrlRequired,
        isDebugKey);
  }

  /**
   * Creates a keybind with explicit {@code notifyServer}, {@code ctrlRequired} and {@code isDebugKey},
   * but that cannot be held down.
   */
  public static Keybind create(String id,
      InputConstants.Type type,
      int key,
      String category,
      boolean notifyServer,
      boolean ctrlRequired,
      boolean isDebugKey) {
    return create(id, type, key, category, false, notifyServer, ctrlRequired,
        isDebugKey);
  }

  /**
   * Creates a keybind that does not notify the server and cannot be held down,
   * but has explicit {@code ctrlRequired} and {@code isDebugKey}.
   */
  public static Keybind create(String id,
      InputConstants.Type type,
      int key,
      String category,
      boolean ctrlRequired,
      boolean isDebugKey) {
    return create(id, type, key, category, false, false, ctrlRequired,
        isDebugKey);
  }

  /**
   * Creates a keybind that is local-only, cannot be held down, and has no Ctrl requirement,
   * but is marked as a debug key or not.
   */
  public static Keybind create(String id,
      InputConstants.Type type,
      int key,
      String category,
      boolean isDebugKey) {
    return create(id, type, key, category, false, false, false,
        isDebugKey);
  }

  /**
   * Creates a basic keybind with default flags:
   * not holdable, no server notification, no Ctrl requirement, not a debug key.
   */
  public static Keybind create(String id,
      InputConstants.Type type,
      int key,
      String category) {
    return create(id, type, key, category, false, false, false,
        false);
  }

  /**
   * Convenience overloads using {@link InputConstants.Type#KEYSYM} as the type.
   */

  public static Keybind create(String id,
      int key,
      String category,
      boolean canBeHeldDown,
      boolean notifyServer,
      boolean ctrlRequired,
      boolean isDebugKey) {
    return create(id, InputConstants.Type.KEYSYM, key, category, canBeHeldDown, notifyServer,
        ctrlRequired, isDebugKey);
  }

  public static Keybind create(String id,
      int key,
      String category,
      boolean notifyServer,
      boolean ctrlRequired,
      boolean isDebugKey) {
    return create(id, InputConstants.Type.KEYSYM, key, category, false, notifyServer,
        ctrlRequired,
        isDebugKey);
  }

  public static Keybind create(String id,
      int key,
      String category,
      boolean ctrlRequired,
      boolean isDebugKey) {
    return create(id, InputConstants.Type.KEYSYM, key, category, false,
        false, ctrlRequired,
        isDebugKey);
  }

  public static Keybind create(String id,
      int key,
      String category,
      boolean isDebugKey) {
    return create(id, InputConstants.Type.KEYSYM, key, category, false,
        false, false, isDebugKey);
  }

  public static Keybind create(String id,
      int key,
      String category) {
    return create(id, InputConstants.Type.KEYSYM, key, category, false,
        false, false, false);
  }

  /**
   * @return the logical id of this keybind.
   */
  public String id() {
    return this.id;
  }

  /**
   * @return {@code true} if this keybind is allowed to be held down.
   */
  public boolean canBeHeldDown() {
    return this.canBeHeldDown;
  }

  /**
   * @return {@code true} if this keybind should send presses to the server.
   */
  public boolean notifyServer() {
    return this.notifyServer;
  }

  /**
   * Checks if the Ctrl key is currently pressed in the client window.
   */
  private boolean isCtrlDown() {
    final long window = Minecraft.getInstance().getWindow().getWindow();
    return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
        || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
  }

  /**
   * Returns whether this keybind is currently active.
   * <p>
   * A keybind is active if the underlying key is down and, if {@link #isCtrlRequired()},
   * Ctrl is pressed as well.
   *
   * @return {@code true} if the key is considered active, {@code false} otherwise.
   */
  public boolean isActive() {
    return this.isDown() && (!isCtrlRequired() || isCtrlDown());
  }

  /**
   * @return {@code true} if Ctrl is required for this keybind to be considered active.
   */
  public boolean isCtrlRequired() {
    return ctrlRequired;
  }

  /**
   * @return {@code true} if this is a debug-only keybind.
   */
  public boolean isDebugKey() {
    return isDebugKey;
  }

  /**
   * Shows a nicer key label in controls menu when Ctrl is required,
   * e.g. {@code "Ctrl + R"} instead of just {@code "R"}.
   */
  @Override
  public @NotNull Component getTranslatedKeyMessage() {
    final Component base = super.getTranslatedKeyMessage();
    return isCtrlRequired() ? Component.literal("Ctrl + ").append(base) : base;
  }
}
