package com.dragonminez.core.common.registry.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * A registry that defers the actual registration of objects until explicitly applied.
 * <p>
 * Instead of inserting entries immediately, this class accumulates registration actions
 * that can later be executed on a target {@link Registry} instance using {@link #registerAll()}.
 * <p>
 * Typical usage:
 * <pre>
 *   Registry<Item> itemRegistry = new Registry<>();
 *   DeferredRegistry<Item> deferred = new DeferredRegistry<>(itemRegistry);
 *
 *   deferred.register("example_item", () -> new Item(...)); // not yet registered
 *   deferred.registerAll(); // now applies everything to the target registry
 * </pre>
 *
 * <p>This design is inspired by systems such as Forge's {@code DeferredRegister}, where
 * registration is collected early (e.g., during static initialization) and only applied
 * at a safe time in the mod lifecycle.
 *
 * @param <T> The type of object stored in the target registry.
 */
public class DeferredRegistry<K, T> extends Registry<K, T> {

  /** List of registration actions to apply later. */
  private final List<Runnable> registrations = Collections.synchronizedList(new ArrayList<>());

  /** The registry where entries will ultimately be stored. */
  private final Registry<K, T> target;

  /**
   * Creates a new deferred registry that applies registrations to the given target.
   *
   * @param target The registry that will receive the actual entries when {@link #registerAll()} is called.
   * @throws NullPointerException if the target is null.
   */
  public DeferredRegistry(Registry<K, T> target) {
    this.target = Objects.requireNonNull(target, "target");
  }

  /**
   * Defers a direct value registration.
   * <p>
   * The given value will not be inserted into the target registry immediately,
   * but rather when {@link #registerAll()} is executed.
   *
   * @param key   The unique string key for the entry.
   * @param value The instance to be registered.
   * @throws IllegalStateException If this registry is locked.
   */
  @Override
  public void register(K key, T value) {
    checkLocked();
    registrations.add(() -> target.register(key, value));
  }

  /**
   * Defers a supplier-based registration.
   * <p>
   * When {@link #registerAll()} is executed, the supplier will be invoked and
   * its result will be registered in the target registry.
   *
   * @param key      The unique string key for the entry.
   * @param supplier A supplier that produces the object to be registered.
   * @throws IllegalStateException If this registry is locked.
   */
  public void register(K key, Supplier<? extends T> supplier) {
    checkLocked();
    registrations.add(() -> target.register(key, supplier.get()));
  }

  /**
   * Applies all deferred registrations in order of insertion.
   * <p>
   * Each stored registration action is executed exactly once, and the internal
   * list of deferred entries is cleared afterward.
   * <p>
   * If any registration throws an exception, it is collected and rethrown as a
   * combined {@link RuntimeException} after all actions have been processed.
   */
  public void registerAll() {
    List<Runnable> toApply;
    synchronized (registrations) {
      if (registrations.isEmpty()) return;
      toApply = new ArrayList<>(registrations);
      registrations.clear();
    }

    RuntimeException firstEx = null;
    for (Runnable r : toApply) {
      try {
        r.run();
      } catch (RuntimeException ex) {
        if (firstEx == null) firstEx = ex;
        else firstEx.addSuppressed(ex);
      }
    }
    if (firstEx != null) throw firstEx;
  }
}
