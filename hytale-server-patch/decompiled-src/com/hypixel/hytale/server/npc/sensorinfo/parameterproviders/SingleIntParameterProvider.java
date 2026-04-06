package com.hypixel.hytale.server.npc.sensorinfo.parameterproviders;

public class SingleIntParameterProvider extends SingleParameterProvider implements IntParameterProvider {
   private int value;

   public SingleIntParameterProvider(int parameter) {
      super(parameter);
   }

   public int getIntParameter() {
      return this.value;
   }

   public void clear() {
      this.value = -2147483648;
   }

   public void overrideInt(int value) {
      this.value = value;
   }
}
