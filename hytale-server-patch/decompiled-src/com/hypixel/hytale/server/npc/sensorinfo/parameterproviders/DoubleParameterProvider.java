package com.hypixel.hytale.server.npc.sensorinfo.parameterproviders;

public interface DoubleParameterProvider extends ParameterProvider {
   double NOT_PROVIDED = -1.7976931348623157E308;

   double getDoubleParameter();
}
