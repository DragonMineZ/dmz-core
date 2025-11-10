package com.dragonminez.core.common.component.registry;

import com.dragonminez.core.common.component.model.Component;
import com.dragonminez.core.common.registry.impl.Registry;

/**
 * Registry responsible for storing all component definitions.
 *
 * <p>Each entry is mapped by its unique string identifier and contains a
 * {@link Component} instance describing the component’s structure and behavior.
 *
 * <p>This registry represents the global catalog of component types available
 * in the system. Individual players receive their own instances of these
 * components through the {@link ComponentHolderRegistry}.
 */
public class ComponentRegistry extends Registry<String, Component<?>> {
}
