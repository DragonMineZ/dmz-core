package com.dragonminez.core.server;

import com.dragonminez.core.common.Reference;
import com.dragonminez.core.common.Env;
import net.minecraftforge.fml.common.Mod;

import com.dragonminez.core.common.DMZCoreCommon;

@Mod(Reference.MOD_ID + "_server")
public final class DMZCoreServer {

    public DMZCoreServer() {
        DMZCoreCommon.init(Env.SERVER);
    }
}
