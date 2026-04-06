package com.hypixel.hytale.server.npc.corecomponents.entity.filters;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponent;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponentCollection;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class EntityFilterMany extends EntityFilterBase implements IAnnotatedComponentCollection {
   @Nonnull
   protected final IEntityFilter[] filters;
   protected final int cost;

   public EntityFilterMany(@Nonnull List<IEntityFilter> filters) {
      if (filters == null) {
         throw new IllegalArgumentException("Filter list can't be null");
      } else {
         this.filters = (IEntityFilter[])filters.toArray((x$0) -> new IEntityFilter[x$0]);

         for(IEntityFilter filter : this.filters) {
            if (filter == null) {
               throw new IllegalArgumentException("Filter cannot be null in filter list");
            }
         }

         IEntityFilter.prioritiseFilters(this.filters);
         int cost = 0;

         for(int i = 0; i < this.filters.length; ++i) {
            cost = (int)((double)cost + (double)this.filters[i].cost() * (1.0 / Math.pow(2.0, (double)i)));
         }

         this.cost = cost;
      }
   }

   public int cost() {
      return this.cost;
   }

   public void registerWithSupport(Role role) {
      for(IEntityFilter filter : this.filters) {
         filter.registerWithSupport(role);
      }

   }

   public void motionControllerChanged(@Nullable Ref<EntityStore> ref, @Nonnull NPCEntity npcComponent, MotionController motionController, @Nullable ComponentAccessor<EntityStore> componentAccessor) {
      for(IEntityFilter filter : this.filters) {
         filter.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
      }

   }

   public void loaded(Role role) {
      for(IEntityFilter filter : this.filters) {
         filter.loaded(role);
      }

   }

   public void spawned(Role role) {
      for(IEntityFilter filter : this.filters) {
         filter.spawned(role);
      }

   }

   public void unloaded(Role role) {
      for(IEntityFilter filter : this.filters) {
         filter.unloaded(role);
      }

   }

   public void removed(Role role) {
      for(IEntityFilter filter : this.filters) {
         filter.removed(role);
      }

   }

   public void teleported(Role role, World from, World to) {
      for(IEntityFilter filter : this.filters) {
         filter.teleported(role, from, to);
      }

   }

   public int componentCount() {
      return this.filters.length;
   }

   public IAnnotatedComponent getComponent(int index) {
      return this.filters[index];
   }

   public void setContext(IAnnotatedComponent parent, int index) {
      super.setContext(parent, index);

      for(int i = 0; i < this.filters.length; ++i) {
         this.filters[i].setContext(this, i);
      }

   }
}
