package com.dragonminez.core.common.component.model;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;

import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Represents a single field inside a {@link Component}. A descriptor defines:
 *
 * <ul>
 *   <li>The field's unique ID within the component.</li>
 *   <li>The field's data type (int, boolean, string, etc.).</li>
 *   <li>A getter and setter used to read and modify the value on the component instance.</li>
 *   <li>Whether the field is publicly visible to other players.</li>
 *   <li>Whether the field should persist when saving to NBT.</li>
 *   <li>An optional abbreviation used for debugging or command displays.</li>
 * </ul>
 *
 * <p>This class also handles reading and writing the field to NBT automatically,
 * based on its {@link ComponentType}.</p>
 *
 * <p>Descriptors are the backbone of the component system: they enable completely
 * generic saving, loading, syncing, and reflection-like behavior without storing
 * raw field references.</p>
 *
 * @param <C> The component class that owns this descriptor.
 * @param <V> The type of the value represented by this descriptor.
 */
public final class ComponentDescriptor<C, V> {

    /** The internal lowercase ID used in NBT. */
    private final String id;

    /** The original field name before lowercase conversion (for UI or debugging). */
    private final String displayName;

    /** The data type used for encoding and decoding this field. */
    private final ComponentType type;

    /** A setter function used to update the component's field. */
    private final BiConsumer<C, V> setter;

    /** A getter used to retrieve the field's current value from the component. */
    private final Function<C, V> getter;

    /**
     * Whether this field is considered public information.
     *
     * <p>If true, the value may be synchronized to other players (e.g., for UI, targeting,
     * or status display). If false, the value is private to the owning player and should
     * never be visible to other clients. This is used for hiding sensitive attributes like
     * health, cooldowns, internal resources, etc.</p>
     */
    private final boolean isPublic;

    /**
     * Whether this field should be included when saving or loading from persistent NBT.
     *
     * <p>If false, the value resets every session or is runtime-only.</p>
     */
    private final boolean isPersistent;

    /** Optional short label used for commands or debug printing. */
    private final String abbrev;

    /**
     * Creates a new descriptor for a component field.
     *
     * @param id           The field ID (lowercased automatically).
     * @param type         The data type used for NBT encoding.
     * @param setter       Consumer used to mutate the component's value.
     * @param getter       Function used to read the component's value.
     * @param abbrev       Optional abbreviation for debug contexts.
     * @param isPublic     Whether other players may see this field.
     * @param isPersistent Whether this field should be saved to NBT.
     */
    public ComponentDescriptor(String id, ComponentType type,
                               BiConsumer<C, V> setter, Function<C, V> getter,
                               String abbrev, boolean isPublic, boolean isPersistent) {
        this.id = id.toLowerCase(Locale.ROOT);
        this.displayName = id;
        this.type = type;
        this.setter = setter;
        this.getter = getter;
        this.isPublic = isPublic;
        this.isPersistent = isPersistent;
        this.abbrev = abbrev == null ? "" : abbrev;
    }

    /**
     * Factory method for creating a descriptor with an abbreviation.
     */
    public static <C, V> ComponentDescriptor<C, V> of(String id, ComponentType type,
                                                      BiConsumer<C, V> setter, Function<C, V> getter,
                                                      String abbrev,
                                                      boolean isPublic, boolean isPersistent) {
        return new ComponentDescriptor<>(id, type, setter, getter, abbrev, isPublic, isPersistent);
    }

    /**
     * Factory method for creating a descriptor without an abbreviation.
     */
    public static <C, V> ComponentDescriptor<C, V> of(String id, ComponentType type,
                                                      BiConsumer<C, V> setter, Function<C, V> getter,
                                                      boolean isPublic, boolean isPersistent) {
        return ComponentDescriptor.of(id, type, setter, getter, "", isPublic, isPersistent);
    }

    /** @return The lowercase ID of this descriptor. */
    public String id() {
        return id;
    }

    /** @return The original name of the descriptor (useful for UI). */
    public String displayName() {
        return displayName;
    }

    /** @return A short optional abbreviation. */
    public String abbrev() {
        return abbrev;
    }

    /** @return The data type handled by this descriptor. */
    public ComponentType type() {
        return type;
    }

    /** @return Whether this value should be visible to other players. */
    public boolean isPublic() {
        return isPublic;
    }

    /** @return Whether this value is saved/loaded from persistent NBT. */
    public boolean isPersistent() {
        return isPersistent;
    }

    /**
     * Sets the value on the component using the descriptor's setter.
     *
     * @param holder The component instance.
     * @param value  The raw value (cast internally).
     */
    @SuppressWarnings("unchecked")
    public void set(C holder, Object value) {
        setter.accept(holder, (V) value);
    }

    /**
     * Retrieves the value from the component using the descriptor's getter.
     *
     * @param holder The component instance.
     * @return The raw value.
     */
    public Object get(C holder) {
        return getter.apply(holder);
    }

    /**
     * Writes this field's value into the provided NBT tag, based on the descriptor type.
     *
     * <p>Non-persistent fields are filtered at the component level, not here.</p>
     *
     * @param holder The component instance owning this value.
     * @param tag    The NBT structure to write into.
     */
    public void writeNbt(C holder, CompoundTag tag) {
        final Object val = get(holder);
        if (val == null) return;

        switch (type) {
            case INTEGER -> tag.putInt(id, ((Number) val).intValue());
            case LONG -> tag.putLong(id, ((Number) val).longValue());
            case FLOAT -> tag.putFloat(id, ((Number) val).floatValue());
            case DOUBLE -> tag.putDouble(id, ((Number) val).doubleValue());
            case BOOLEAN -> tag.putBoolean(id, (Boolean) val);
            case STRING -> tag.putString(id, String.valueOf(val));
            case LIST_STRING -> {
                @SuppressWarnings("unchecked")
                final List<String> list = (List<String>) val;
                ListTag listTag = new ListTag();
                for (String s : list) listTag.add(StringTag.valueOf(s));
                tag.put(id, listTag);
            }
            default -> {
            }
        }
    }

    /**
     * Reads this descriptor's value from NBT and applies it using the setter.
     *
     * @param holder The component instance to apply the loaded value to.
     * @param tag    The source NBT tag.
     */
    public void fromNbt(C holder, CompoundTag tag) {
        if (!tag.contains(id)) return;

        switch (type) {
            case INTEGER -> set(holder, tag.getInt(id));
            case LONG -> set(holder, tag.getLong(id));
            case FLOAT -> set(holder, tag.getFloat(id));
            case DOUBLE -> set(holder, tag.getDouble(id));
            case BOOLEAN -> set(holder, tag.getBoolean(id));
            case STRING -> set(holder, tag.getString(id));
            case LIST_STRING -> {
                final var list = tag.getList(id, 8);
                java.util.List<String> out = new java.util.ArrayList<>(list.size());
                list.forEach(t -> out.add(t.getAsString()));
                set(holder, out);
            }
            default -> {
            }
        }
    }
}
