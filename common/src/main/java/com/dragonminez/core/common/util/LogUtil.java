package com.dragonminez.core.common.util;

import com.dragonminez.core.common.Env;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Utility class for centralized logging within the mod.
 *
 * <p>
 * Provides simplified static helper methods for logging messages at different
 * levels (info, warn, error, and debug) using Log4j. Supports parameterized
 * messages through varargs, and automatically prefixes messages with the
 * provided execution environment ({@link Env}).
 * </p>
 *
 * <p>
 * This class is not instantiable and is intended to be used statically.
 * </p>
 */
public final class LogUtil {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Private constructor to prevent instantiation.
     */
    private LogUtil() {
    }

    /**
     * Logs an informational message with optional parameters.
     *
     * @param env     the execution environment used as prefix (e.g., CLIENT or SERVER)
     * @param message the message string, possibly containing placeholders
     * @param args    optional arguments referenced by placeholders in the message
     */
    public static void info(Env env, String message, Object... args) {
        LOGGER.info(prefixedMessage(env, message), args);
    }

    /**
     * Logs a warning message with optional parameters.
     *
     * @param env     the execution environment used as prefix (e.g., CLIENT or SERVER)
     * @param message the warning message string, possibly containing placeholders
     * @param args    optional arguments referenced by placeholders in the message
     */
    public static void warn(Env env, String message, Object... args) {
        LOGGER.warn(prefixedMessage(env, message), args);
    }

    /**
     * Logs an error message with optional parameters.
     *
     * @param env     the execution environment used as prefix (e.g., CLIENT or SERVER)
     * @param message the error message string, possibly containing placeholders
     * @param args    optional arguments referenced by placeholders in the message
     */
    public static void error(Env env, String message, Object... args) {
        LOGGER.error(prefixedMessage(env, message), args);
    }

    /**
     * Logs a debug message with optional parameters.
     * <p>
     * Debug output is only visible when debug logging is enabled in the runtime
     * configuration.
     * </p>
     *
     * @param env     the execution environment used as prefix (e.g., CLIENT or SERVER)
     * @param message the debug message string, possibly containing placeholders
     * @param args    optional arguments referenced by placeholders in the message
     */
    public static void debug(Env env, String message, Object... args) {
        LOGGER.debug(prefixedMessage(env, message), args);
    }

    /**
     * Generates a formatted log message prefixed with the given execution
     * environment.
     *
     * @param env     the execution environment used as prefix
     * @param message the original message
     * @return the formatted message including the environment prefix
     */
    private static String prefixedMessage(Env env, String message) {
        return "[" + env.name() + "] " + message;
    }
}
