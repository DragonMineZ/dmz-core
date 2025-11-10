package com.dragonminez.core.common.component.model;

import net.minecraft.nbt.CompoundTag;

import java.util.List;

/**
 * Represents a data-bearing component that can be attached to a player or other holders.
 * <p>
 * A component defines:
 * <ul>
 *   <li>A unique {@code id()} used for registration and NBT storage.</li>
 *   <li>A list of {@link ComponentDescriptor} objects describing its internal values.</li>
 *   <li>A factory method {@link #create()} used to generate fresh instances.</li>
 *   <li>Automatic or manual application control via {@link #autoApply()}.</li>
 * </ul>
 * <p>
 * Each component can serialize and deserialize its state using the provided
 * NBT helpers {@link #toNbt(CompoundTag, boolean)} and
 * {@link #fromNbt(CompoundTag, boolean)}. These methods rely entirely on the
 * component’s descriptors to encode and decode each field.
 *
 * @param <T> The concrete component type.
 */
public interface Component<T extends Component<T>> {

    /**
     * Returns the unique string identifier for this component.
     * <p>
     * This ID is used both for registry storage and as the NBT root key for
     * the component's data. It must be globally unique.
     *
     * @return the component ID.
     */
    String id();

    /**
     * Returns the list of descriptors defining all values tracked by this component.
     * <p>
     * Each descriptor manages its own getter, setter, and NBT read/write logic.
     *
     * @return the list of component descriptors.
     */
    List<ComponentDescriptor<T, ?>> descriptors();

    /**
     * Creates a fresh instance of this component.
     * <p>
     * This is used when attaching components to new players or when cloning.
     *
     * @return a new component instance.
     */
    T create();

    /**
     * Determines whether this component should be automatically attached when a player loads.
     * <p>
     * Returning {@code true} causes the component to be instantiated for all players.
     * Returning {@code false} requires manual attachment.
     *
     * @return {@code true} to auto-apply, otherwise {@code false}.
     */
    default boolean autoApply() {
        return true;
    }

    /**
     * Serializes this component's values into the provided NBT structure.
     * <p>
     * Only descriptors marked as persistent will be included if
     * {@code onlyPersistent} is set to {@code true}.
     * <p>
     * The final structure will be stored under a sub-tag matching the component ID.
     *
     * @param origin         the target tag where this component should write its data.
     * @param onlyPersistent whether to write only persistent descriptors.
     */
    @SuppressWarnings("unchecked")
    default void toNbt(CompoundTag origin, boolean onlyPersistent) {
        final CompoundTag node = new CompoundTag();
        for (ComponentDescriptor<T, ?> desc : descriptors()) {
            if (onlyPersistent && !desc.isPersistent()) continue;
            desc.writeNbt((T) this, node);
        }
        origin.put(id(), node);
    }

    /**
     * Deserializes this component's values from the given NBT structure.
     * <p>
     * If the NBT does not contain a sub-tag matching this component’s ID,
     * the method silently does nothing.
     * <p>
     * Only descriptors marked as persistent will be read if
     * {@code onlyPersistent} is set to {@code true}.
     *
     * @param tag            the NBT tag holding all component data.
     * @param onlyPersistent whether to read only persistent descriptors.
     */
    @SuppressWarnings("unchecked")
    default void fromNbt(CompoundTag tag, boolean onlyPersistent) {
        if (!tag.contains(id())) return;
        CompoundTag node = tag.getCompound(id());
        for (ComponentDescriptor<T, ?> desc : descriptors()) {
            if (onlyPersistent && !desc.isPersistent()) continue;
            desc.fromNbt((T) this, node);
        }
    }
}
