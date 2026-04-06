package com.hypixel.hytale.server.npc.corecomponents.utility;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.builders.BuilderSensorMany;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.sensorinfo.WrappedInfoProvider;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponent;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponentCollection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class SensorMany extends SensorBase implements IAnnotatedComponentCollection {
   @Nonnull
   protected final Sensor[] sensors;
   protected final int autoUnlockTargetSlot;
   protected final WrappedInfoProvider infoProvider;

   public SensorMany(@Nonnull BuilderSensorMany builder, @Nonnull BuilderSupport support, @Nonnull List<Sensor> sensors) {
      super(builder);
      if (sensors == null) {
         throw new IllegalArgumentException("Sensor list can't be null");
      } else {
         this.sensors = (Sensor[])sensors.toArray((x$0) -> new Sensor[x$0]);

         for(Sensor sensor : this.sensors) {
            if (sensor == null) {
               throw new IllegalArgumentException("Sensor in sensor list can't be null");
            }
         }

         this.autoUnlockTargetSlot = builder.getAutoUnlockedTargetSlot(support);
         this.infoProvider = this.createInfoProvider();
      }
   }

   public void done() {
      for(Sensor s : this.sensors) {
         s.done();
      }

   }

   public void registerWithSupport(Role role) {
      for(Sensor sensor : this.sensors) {
         sensor.registerWithSupport(role);
      }

   }

   public void motionControllerChanged(@Nullable Ref<EntityStore> ref, @Nonnull NPCEntity npcComponent, MotionController motionController, @Nullable ComponentAccessor<EntityStore> componentAccessor) {
      for(Sensor sensor : this.sensors) {
         sensor.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
      }

   }

   public void loaded(Role role) {
      for(Sensor sensor : this.sensors) {
         sensor.loaded(role);
      }

   }

   public void spawned(Role role) {
      for(Sensor sensor : this.sensors) {
         sensor.spawned(role);
      }

   }

   public void unloaded(Role role) {
      for(Sensor sensor : this.sensors) {
         sensor.unloaded(role);
      }

   }

   public void removed(Role role) {
      for(Sensor sensor : this.sensors) {
         sensor.removed(role);
      }

   }

   public void teleported(Role role, World from, World to) {
      for(Sensor sensor : this.sensors) {
         sensor.teleported(role, from, to);
      }

   }

   public InfoProvider getSensorInfo() {
      return this.infoProvider;
   }

   public int componentCount() {
      return this.sensors.length;
   }

   public IAnnotatedComponent getComponent(int index) {
      return this.sensors[index];
   }

   public void setContext(IAnnotatedComponent parent, int index) {
      super.setContext(parent, index);

      for(int i = 0; i < this.sensors.length; ++i) {
         this.sensors[i].setContext(this, i);
      }

   }

   protected abstract WrappedInfoProvider createInfoProvider();
}
