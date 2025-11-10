package com.dragonminez.core.common.registry;

import com.dragonminez.core.common.registry.impl.Registry;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Central manager responsible for creating, storing, and accessing registry instances.
 *
 * <p>This class acts as the global registry hub of the system. Each registry type is
 * instantiated lazily (on first access) and cached so that only one instance of each
 * registry exists at runtime.
 *
 * <p>The manager provides:
 * <ul>
 *     <li>Automatic creation of registry instances</li>
 *     <li>Manual injection of custom registry implementations</li>
 *     <li>Convenience methods for registering entries into registries</li>
 *     <li>Safe lookup of registered values</li>
 * </ul>
 *
 * <p>Example usage:
 * <pre>{@code
 * ConfigRegistry config = RegistryManager.registry(ConfigRegistry.class);
 *
 * RegistryManager.registerValue(
 *         ConfigRegistry.class,
 *         "example",
 *         new ConfigObject()
 * );
 *
 * Optional<ConfigObject> value =
 *         RegistryManager.getValue(ConfigRegistry.class, "example");
 * }</pre>
 *
 * <p>All methods are static, so there is no need to create an instance of this class.
 */
public final class RegistryManager {

    private RegistryManager() {}

    /** Internal cache of all instantiated registries, mapped by their class type. */
    private static final Map<Class<?>, Object> REGISTRIES = new ConcurrentHashMap<>();

    /**
     * Injects a registry instance into the manager.
     * <p>
     * If a registry of the same type already exists, the provided instance is ignored.
     *
     * @param type     the registry class type
     * @param registry the instance to insert
     * @param <K>      the key type of the registry
     * @param <T>      the value type of the registry
     */
    public static <K, T> void register(Class<? extends Registry<K, T>> type, Registry<K, T> registry) {
        REGISTRIES.putIfAbsent(type, registry);
    }

    /**
     * Removes the registry instance associated with the given type.
     *
     * @param type the registry class to remove
     * @return {@code true} if a registry was removed, {@code false} otherwise
     */
    public static boolean remove(Class<?> type) {
        return REGISTRIES.remove(type) != null;
    }

    /**
     * Removes a value from the specified registry.
     *
     * @param type the registry class type
     * @param key  the key of the value to remove
     * @param <K>  the key type used by the registry
     * @param <T>  the value type stored in the registry
     */
    public static <K, T> void removeValue(Class<? extends Registry<K, T>> type, K key) {
        final Registry<K, T> registry = registry(type);
        registry.remove(key);
    }

    /**
     * Retrieves the registry instance associated with the given type, creating it if
     * necessary.
     *
     * @param type the registry class to obtain
     * @param <T>  the registry type
     * @return the existing or newly created registry instance
     * @throws IllegalStateException if instantiation of the registry fails
     */
    @SuppressWarnings("unchecked")
    public static <T> T registry(Class<T> type) {
        return (T) REGISTRIES.computeIfAbsent(type, t -> {
            try {
                return t.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create registry for " + t.getName(), e);
            }
        });
    }

    /**
     * Registers a value into the given registry type, executing a preprocessing step
     * before the registration occurs.
     *
     * <p>This method enables advanced behavior such as:
     * <ul>
     *     <li>initializing registry internals</li>
     *     <li>validating its state prior to insertion</li>
     *     <li>injecting dependencies into the registry instance</li>
     * </ul>
     *
     * @param type           the registry class type
     * @param key            the identifier for the value
     * @param value          the object to register
     * @param beforeRegister a consumer invoked with the registry instance before insertion
     * @param <R>            the registry implementation type
     * @param <K>            the key type stored in the registry
     * @param <T>            the value type stored in the registry
     */
    @SuppressWarnings("unchecked")
    public static <R extends Registry<K, T>, K, T> void registerValue(
            Class<R> type,
            K key,
            T value,
            Consumer<R> beforeRegister
    ) {
        final Registry<K, T> registry = registry(type);
        beforeRegister.accept((R) registry);
        registry.register(key, value);
        REGISTRIES.put(type, registry);
    }

    /**
     * Registers a value into the given registry type.
     *
     * <p>This is the standard convenience overload, equivalent to calling
     * {@link #registerValue(Class, Object, Object, Consumer)} with a no-op preprocessing step.
     *
     * @param type  the registry class type
     * @param key   the identifier under which the value is stored
     * @param value the value to insert
     * @param <K>   the key type of the registry
     * @param <T>   the value type of the registry
     */
    public static <K, T> void registerValue(Class<? extends Registry<K, T>> type, K key, T value) {
        registerValue(type, key, value, r -> {});
    }

    /**
     * Retrieves a value registered in the specified registry.
     *
     * @param type the registry class type
     * @param key  the key of the desired value
     * @param <K>  the key type used by the registry
     * @param <T>  the value type stored in the registry
     * @return an {@link Optional} containing the value if it exists, otherwise empty
     */
    public static <K, T> Optional<T> getValue(Class<? extends Registry<K, T>> type, K key) {
        final Registry<K, T> registry = registry(type);
        return registry.get(key);
    }
}
