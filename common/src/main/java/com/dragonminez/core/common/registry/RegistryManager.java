package com.dragonminez.core.common.registry;

import com.dragonminez.core.common.registry.impl.Registry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global manager responsible for creating, storing, and managing registry instances.
 * <p>
 * This class ensures that each registry type exists only once at runtime. It provides
 * methods to retrieve existing registries, automatically create them on first access,
 * and register values into them without manual instantiation.
 * <p>
 * Example usage:
 * <pre>{@code
 * ConfigRegistry registry = RegistryManager.INSTANCE.registry(ConfigRegistry.class);
 * RegistryManager.INSTANCE.registerValue(ConfigRegistry.class, "example_id", configObject);
 * }</pre>
 */
public final class RegistryManager {

  /** Singleton instance of the registry manager. */
  public static final RegistryManager INSTANCE = new RegistryManager();

  /** Stores all instantiated registries, mapped by their class. */
  private final Map<Class<?>, Object> registries = new ConcurrentHashMap<>();

  private RegistryManager() {}

  /**
   * Retrieves an existing registry instance or creates it if it doesn't exist yet.
   *
   * @param type the class type of the registry
   * @param <T>  the registry type
   * @return the existing or newly created registry instance
   * @throws IllegalStateException if the registry cannot be instantiated
   */
  @SuppressWarnings("unchecked")
  public <T> T registry(Class<T> type) {
    return (T) registries.computeIfAbsent(type, t -> {
      try {
        return t.getDeclaredConstructor().newInstance();
      } catch (Exception e) {
        throw new IllegalStateException("Failed to create registry for " + t.getName(), e);
      }
    });
  }

  /**
   * Registers a value into a given registry type. The registry will be automatically
   * created and cached if it doesn't exist yet.
   *
   * @param type  the registry class type
   * @param key   the unique key under which the value is registered
   * @param value the value to register
   * @param <T>   the value type stored in the registry
   */
  public <K, T> void registerValue(Class<? extends Registry<K, T>> type, K key, T value) {
    final Registry<K, T> registry = registry(type);
    registry.register(key, value);
    this.registries.put(type, registry);
  }

  /**
   * Removes a registry instance from the manager.
   *
   * @param type the registry class to remove
   * @return {@code true} if a registry was removed, {@code false} otherwise
   */
  public boolean removeRegistry(Class<?> type) {
    return registries.remove(type) != null;
  }
}
