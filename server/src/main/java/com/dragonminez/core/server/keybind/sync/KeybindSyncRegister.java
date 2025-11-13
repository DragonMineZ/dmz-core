package com.dragonminez.core.server.keybind.sync;

import com.dragonminez.core.common.keybind.KeybindManager;
import com.dragonminez.core.common.keybind.network.KeybindHandlerSync;
import com.dragonminez.core.common.sync.SyncRegistry;

public class KeybindSyncRegister {

  public static void init() {
    SyncRegistry.register(KeybindHandlerSync.class, (sender, handler)
        -> KeybindManager.onPress(sender, handler.keybindId(), handler.heldDown(),
        true));
  }
}
