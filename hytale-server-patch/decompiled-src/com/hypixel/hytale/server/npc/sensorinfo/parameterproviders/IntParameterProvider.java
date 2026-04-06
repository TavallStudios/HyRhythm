package com.hypixel.hytale.server.npc.sensorinfo.parameterproviders;

public interface IntParameterProvider extends ParameterProvider {
   int NOT_PROVIDED = -2147483648;

   int getIntParameter();
}
