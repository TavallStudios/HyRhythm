package com.hypixel.hytale.server.core.plugin;

public enum PluginState {
   NONE,
   SETUP,
   START,
   ENABLED,
   SHUTDOWN,
   DISABLED,
   FAILED;

   public boolean isInactive() {
      boolean var10000;
      switch (this.ordinal()) {
         case 0:
         case 5:
         case 6:
            var10000 = true;
            break;
         default:
            var10000 = false;
      }

      return var10000;
   }
}
