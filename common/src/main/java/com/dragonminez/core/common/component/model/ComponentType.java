package com.dragonminez.core.common.component.model;

/**
 * Represents the set of supported data types for fields inside a {@link Component}.
 * <p>
 * Each {@link ComponentDescriptor} uses a {@code ComponentType} to determine how
 * its value should be encoded into NBT and decoded back.
 * <p>
 * The types included here cover all primitive-like values normally required for
 * gameplay components (stats, flags, timers, attributes, lists, etc.).
 *
 * <p>Supported types:
 *
 * <ul>
 *   <li>{@link #INTEGER} – 32-bit signed integer fields.</li>
 *   <li>{@link #LONG} – 64-bit signed long fields.</li>
 *   <li>{@link #DOUBLE} – double-precision floating-point fields.</li>
 *   <li>{@link #FLOAT} – single-precision floating-point fields.</li>
 *   <li>{@link #BOOLEAN} – boolean flags.</li>
 *   <li>{@link #STRING} – UTF-8 string fields.</li>
 *   <li>{@link #LIST_STRING} – list of strings (stored as an NBT ListTag).</li>
 * </ul>
 *
 * <p>This enum is used exclusively by the generic component system to ensure that
 * serialization and deserialization of component fields is consistent and type-safe
 * without needing reflection.</p>
 */
public enum ComponentType {
    /** A 32-bit integer value. */
    INTEGER,

    /** A 64-bit long integer value. */
    LONG,

    /** A double-precision floating-point value. */
    DOUBLE,

    /** A single-precision floating-point value. */
    FLOAT,

    /** A boolean (true/false) value. */
    BOOLEAN,

    /** A UTF-8 string value. */
    STRING,

    /** A list of UTF-8 strings stored as an NBT ListTag. */
    LIST_STRING
}
