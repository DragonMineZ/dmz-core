package com.dragonminez.core.server;

import com.dragonminez.core.common.DMZCoreCommon;
import com.dragonminez.core.common.Env;
import com.dragonminez.core.common.Reference;
import com.dragonminez.core.server.registry.SyncRegister;
import net.minecraftforge.fml.common.Mod;

@Mod(Reference.MOD_ID + "_server")
public final class DMZCoreServer {

  public DMZCoreServer() {
    DMZCoreCommon.init(Env.SERVER);
    SyncRegister.init();
  }
}
