package com.dragonminez.core.common;

import com.dragonminez.core.common.network.NetworkManager;
import com.dragonminez.core.common.util.LogUtil;
import net.minecraftforge.fml.common.Mod;

@Mod(Reference.MOD_ID + "_common")
public final class DMZCoreCommon {

    public DMZCoreCommon() {
        NetworkManager.init();
    }

    public static void init(Env env) {
        LogUtil.info(env, "DMZCore initialized!");
    }
}
