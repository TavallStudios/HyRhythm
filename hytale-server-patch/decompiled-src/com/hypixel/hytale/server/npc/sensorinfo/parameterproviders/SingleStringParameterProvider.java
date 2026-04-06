package com.hypixel.hytale.server.npc.sensorinfo.parameterproviders;

import javax.annotation.Nullable;

public class SingleStringParameterProvider extends SingleParameterProvider implements StringParameterProvider {
   @Nullable
   private String value;

   public SingleStringParameterProvider(int parameter) {
      super(parameter);
   }

   @Nullable
   public String getStringParameter() {
      return this.value;
   }

   public void clear() {
      this.value = null;
   }

   public void overrideString(String value) {
      this.value = value;
   }
}
