package com.dragonminez.core.common.component.registry;

import com.dragonminez.core.common.registry.model.Registry;
import net.minecraft.nbt.CompoundTag;

import java.util.Optional;
import java.util.UUID;

/**
 * A registry that maps a holder UUID (typically a player or entity) to its
 * associated {@link ComponentRegistry}. Each holder maintains its own
 * collection of components.
 *
 * <p>Provides methods to load and save all components belonging to a holder
 * using a shared {@link CompoundTag}. Each component decides how to read or
 * write its own data.
 */
public class ComponentHolderRegistry extends Registry<UUID, ComponentRegistry> {

    /**
     * Loads all components associated with the given holder UUID from the
     * provided NBT tag. Each component reads its own data by calling
     * {@code fromNbt} with {@code shared = true}.
     *
     * @param holder the UUID of the component holder
     * @param origin the NBT tag containing the stored component data
     */
    public void load(UUID holder, CompoundTag origin) {
        final Optional<ComponentRegistry> optRegistry = this.get(holder);
        if (optRegistry.isEmpty()) return;
        optRegistry.get()
                .values()
                .forEach(component -> component.fromNbt(origin, true));
    }

    /**
     * Saves all components associated with the given holder UUID into the
     * provided NBT tag. Each component writes its own data by calling
     * {@code toNbt} with {@code shared = true}.
     *
     * @param holder the UUID of the component holder
     * @param origin the NBT tag into which component data is written
     */
    public void save(UUID holder, CompoundTag origin) {
        final Optional<ComponentRegistry> optRegistry = this.get(holder);
        if (optRegistry.isEmpty()) return;
        optRegistry.get()
                .values()
                .forEach(component -> component.toNbt(origin, true));
    }
}

