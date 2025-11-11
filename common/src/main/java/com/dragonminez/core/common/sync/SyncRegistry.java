package com.dragonminez.core.common.sync;

import com.dragonminez.core.common.network.model.ISerializable;
import com.dragonminez.core.common.registry.model.Registry;
import java.util.function.Consumer;

public class SyncRegistry<T extends ISerializable<T>> extends Registry<Class<T>, Consumer<T>> {
}
