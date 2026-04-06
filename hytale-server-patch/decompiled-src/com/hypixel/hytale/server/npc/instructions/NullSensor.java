package com.hypixel.hytale.server.npc.instructions;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.ComponentInfo;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponent;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NullSensor implements Sensor {
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      return true;
   }

   public InfoProvider getSensorInfo() {
      return null;
   }

   public boolean processDelay(float dt) {
      return true;
   }

   public void clearOnce() {
   }

   public void setOnce() {
   }

   public boolean isTriggered() {
      return false;
   }

   public void getInfo(Role role, ComponentInfo holder) {
   }

   public void setContext(IAnnotatedComponent parent, int index) {
   }

   @Nullable
   public IAnnotatedComponent getParent() {
      return null;
   }

   public int getIndex() {
      return 0;
   }
}
