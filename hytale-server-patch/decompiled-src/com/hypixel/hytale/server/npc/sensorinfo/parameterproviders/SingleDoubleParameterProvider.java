package com.hypixel.hytale.server.npc.sensorinfo.parameterproviders;

public class SingleDoubleParameterProvider extends SingleParameterProvider implements DoubleParameterProvider {
   private double value;

   public SingleDoubleParameterProvider(int parameter) {
      super(parameter);
   }

   public double getDoubleParameter() {
      return this.value;
   }

   public void clear() {
      this.value = -1.7976931348623157E308;
   }

   public void overrideDouble(double value) {
      this.value = value;
   }
}
