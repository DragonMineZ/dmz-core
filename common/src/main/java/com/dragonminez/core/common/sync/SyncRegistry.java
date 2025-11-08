package com.dragonminez.core.common.sync;

import com.dragonminez.core.common.registry.impl.Registry;

import java.util.function.Consumer;

public class SyncRegistry<T> extends Registry<Class<?>, Consumer<T>> {
}
