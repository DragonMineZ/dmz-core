package com.dragonminez.core.common.registry.impl;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * A generic registry system that maps string identifiers to values of type {@code T}.
 * <p>
 * This class provides a simple thread-safe registration and lookup mechanism, ensuring that:
 * <ul>
 *   <li>Each key can only be registered once.</li>
 *   <li>Registrations can be locked to prevent further modifications.</li>
 * </ul>
 * <p>
 * It is designed as a base class for other registry systems, such as
 * {@link DeferredRegistry}, which implements deferred (lazy) registration.
 *
 * @param <T> The type of objects stored in this registry.
 */
public class Registry<T> {

  /** Thread-safe map of registered objects, keyed by their unique string identifiers. */
  protected final Map<String, T> map = new ConcurrentHashMap<>();

  /** Whether this registry is locked (disallowing further registrations). */
  private volatile boolean locked = false;

  /**
   * Registers a new value under the given key.
   *
   * @param key   The unique string identifier for the object.
   * @param value The object to register.
   * @throws IllegalStateException If the registry is locked or the key is already registered.
   */
  public void register(String key, T value) {
    checkLocked();
    Object prev = map.putIfAbsent(key, value);
    if (prev != null) {
      throw new IllegalStateException("Key already registered: " + key);
    }
  }

  /**
   * Retrieves a value from the registry.
   *
   * @param key The string key of the registered object.
   * @return An {@link Optional} containing the value, or empty if not found.
   */
  public Optional<T> get(String key) {
    return Optional.ofNullable(map.get(key));
  }

  /**
   * Checks whether a given key exists in the registry.
   *
   * @param key The string key to check.
   * @return {@code true} if the key exists, {@code false} otherwise.
   */
  public boolean contains(String key) {
    return map.containsKey(key);
  }

  /**
   * Returns a stream of all registered keys.
   *
   * @return A stream of string keys.
   */
  public Stream<String> keys() {
    return map.keySet().stream();
  }

  /**
   * Returns a stream of all registered values.
   *
   * @return A stream of registered values.
   */
  public Stream<T> values() {
    return map.values().stream();
  }

  /**
   * Clears all entries from the registry.
   */
  public void clear() {
    map.clear();
  }

  /**
   * Locks the registry, preventing further registrations.
   */
  public void lock() {
    this.locked = true;
  }

  /**
   * Unlocks the registry, allowing new registrations.
   * <p>
   * Use this carefully — unlocking after initial setup is generally discouraged.
   */
  public void unlock() {
    this.locked = false;
  }

  /**
   * Ensures the registry is not locked before allowing modifications.
   *
   * @throws IllegalStateException If the registry is locked.
   */
  protected void checkLocked() {
    if (locked) {
      throw new IllegalStateException("Registry locked (no further registrations allowed)");
    }
  }
}
