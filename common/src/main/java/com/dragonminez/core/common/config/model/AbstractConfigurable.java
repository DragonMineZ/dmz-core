package com.dragonminez.core.common.config.model;

/**
 * Base class for all configurable objects that can be loaded and saved as JSON files.
 * <p>
 * Each implementation must define a unique identifier and a folder name that determines
 * where its configuration file is stored inside the mod's config directory.
 * <p>
 * These objects can be automatically registered into a {@code ConfigRegistry} and managed
 * through {@code ConfigManager}.
 */
public abstract class AbstractConfigurable {

  /**
   * Returns the unique identifier of this configurable object.
   * <p>
   * This ID is typically used as the file name (with a .json extension) and as a registry key.
   *
   * @return the unique string ID
   */
  public abstract String getId();

  /**
   * Returns the folder name inside the mod's config directory where this configuration is stored.
   * <p>
   * Example: if this method returns {@code "abilities"}, the file path will be
   * {@code config/<modid>/abilities/<id>.json}.
   *
   * @return the folder name for this configuration
   */
  public abstract String folder();

  /**
   * Determines whether this object can be overridden by configurations loaded from disk.
   * <p>
   * Returning {@code false} ensures that existing in-memory definitions cannot be replaced
   * by those loaded from JSON files.
   *
   * @return {@code true} if the configuration can be overridden, otherwise {@code false}
   */
  public boolean allowOverrideByConfig() {
    return true;
  }
}
