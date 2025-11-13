package com.dragonminez.core.common;

import net.minecraftforge.fml.loading.FMLEnvironment;

public enum Env {
  CLIENT,
  SERVER;

  public static boolean isDebug() {
    return !FMLEnvironment.production;
  }
}
