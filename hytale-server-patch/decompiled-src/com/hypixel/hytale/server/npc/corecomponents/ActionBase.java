package com.hypixel.hytale.server.npc.corecomponents;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;

public abstract class ActionBase extends AnnotatedComponentBase implements Action {
   protected boolean once;
   protected boolean triggered;
   protected boolean active;

   public ActionBase(@Nonnull BuilderActionBase builderActionBase) {
      this.once = builderActionBase.isOnce();
   }

   public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      return !this.once || !this.triggered;
   }

   public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
      this.setOnce();
      return true;
   }

   public void activate(Role role, InfoProvider infoProvider) {
      this.active = true;
   }

   public void deactivate(Role role, InfoProvider infoProvider) {
      this.active = false;
   }

   public boolean isActivated() {
      return this.active;
   }

   public boolean isTriggered() {
      return this.triggered;
   }

   public void clearOnce() {
      this.triggered = false;
   }

   public void setOnce() {
      this.triggered = true;
   }

   public boolean processDelay(float dt) {
      return true;
   }
}
