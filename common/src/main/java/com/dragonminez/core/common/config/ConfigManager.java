package com.dragonminez.core.common.config;

import com.dragonminez.core.common.Reference;
import com.dragonminez.core.common.config.model.AbstractConfigurable;
import com.dragonminez.core.common.registry.RegistryManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Handles loading and saving of {@link AbstractConfigurable} objects to and from JSON files.
 * <p>
 * Each configuration object defines its own folder and ID, and is automatically
 * stored in the {@link ConfigRegistry} via the {@link RegistryManager}.
 * <p>
 * Files are placed under <code>config/&lt;modid&gt;/</code>, and each config
 * is represented as a <code>.json</code> file.
 */
public final class ConfigManager {

  private static final Path ROOT = Paths.get("config", Reference.MOD_ID);
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  private ConfigManager() {}

  /**
   * Loads all configuration files of the given type from the specified folder.
   * <p>
   * Each valid <code>.json</code> file will be parsed, converted to an instance
   * of {@code type}, and registered in the {@link ConfigRegistry}.
   *
   * @param type   the configuration class type
   * @param folder the subfolder under the mod config directory
   * @param <T>    the config type
   */
  public static <T extends AbstractConfigurable> void loadFromFolder(Class<T> type, String folder) {
    Path dir = ROOT.resolve(folder);
    if (!Files.isDirectory(dir)) {
      return;
    }

    try (var stream = Files.list(dir)) {
      for (Path path : stream.toList()) {
        if (path.getFileName().toString().endsWith(".json")) {
          loadFile(path, type);
        }
      }
    } catch (Exception e) {
      e.printStackTrace(System.out);
    }
  }

  /**
   * Loads a single configuration file and registers it.
   * <p>
   * If the file exists and contains valid JSON, it is deserialized into an instance
   * of {@code type}. The resulting object is added to the {@link ConfigRegistry},
   * unless a duplicate entry exists and the config disallows overrides.
   *
   * @param file the file path
   * @param type the configuration class type
   * @param <T>  the config type
   * @return the loaded configuration object, or {@code null} if loading failed
   */
  public static <T extends AbstractConfigurable> T loadFile(Path file, Class<T> type) {
    try {
      if (!Files.exists(file) || !Files.isRegularFile(file)) {
        return null;
      }

      String json = Files.readString(file);
      T obj = GSON.fromJson(json, type);
      if (obj == null) {
        return null;
      }

      ConfigRegistry registry = RegistryManager.registry(ConfigRegistry.class);
      if (!obj.allowOverrideByConfig() && registry.contains(obj.getId())) {
        return null;
      }

      RegistryManager.registerValue(ConfigRegistry.class, obj.getId(), obj);
      return obj;
    } catch (Throwable t) {
      t.printStackTrace(System.out);
      return null;
    }
  }

  /**
   * Loads a single configuration file by its ID, automatically converting the ID
   * to the expected file name.
   *
   * @param folder the config folder
   * @param id     the configuration ID
   * @param type   the configuration class type
   * @param <T>    the config type
   * @return the loaded configuration object, or {@code null} if not found
   */
  public static <T extends AbstractConfigurable> T loadById(String folder, String id, Class<T> type) {
    Path file = ROOT.resolve(folder).resolve(idToFilename(id));
    return loadFile(file, type);
  }

  /**
   * Saves a configuration object to disk and registers it in the {@link ConfigRegistry}.
   * <p>
   * The file will be created or replaced under its designated folder using the config’s ID.
   *
   * @param obj the configuration object to save
   * @return true if the save succeeded, false otherwise
   */
  public static boolean save(AbstractConfigurable obj) {
    try {
      Path dir = ROOT.resolve(obj.folder());
      if (!Files.exists(dir)) {
        Files.createDirectories(dir);
      }

      Path file = dir.resolve(idToFilename(obj.getId()));
      String json = GSON.toJson(obj);
      Files.writeString(file, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

      RegistryManager.registerValue(ConfigRegistry.class, obj.getId(), obj);
      return true;
    } catch (Throwable t) {
      t.printStackTrace(System.out);
      return false;
    }
  }

  /**
   * Converts a config ID into its corresponding file name.
   * <p>
   * For example, <code>modid:example</code> becomes <code>example.json</code>.
   *
   * @param id the configuration ID
   * @return the file name corresponding to the given ID
   */
  private static String idToFilename(String id) {
    if (id == null) {
      return "unknown.json";
    }
    String name = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
    return name + ".json";
  }
}
