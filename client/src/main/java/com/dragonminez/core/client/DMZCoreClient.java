package com.dragonminez.core.client;

import com.dragonminez.core.common.DMZCoreCommon;
import com.dragonminez.core.common.Reference;
import com.dragonminez.core.common.Env;
import net.minecraftforge.fml.common.Mod;

@Mod(Reference.MOD_ID + "_client")
public final class DMZCoreClient {

    public DMZCoreClient() {
        DMZCoreCommon.init(Env.CLIENT);
    }
}
