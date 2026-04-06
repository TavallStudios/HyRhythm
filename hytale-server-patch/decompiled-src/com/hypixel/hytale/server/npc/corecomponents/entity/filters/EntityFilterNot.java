package com.hypixel.hytale.server.npc.corecomponents.entity.filters;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.EntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponent;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponentCollection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityFilterNot extends EntityFilterBase implements IAnnotatedComponentCollection {
   protected final IEntityFilter filter;

   public EntityFilterNot(IEntityFilter filter) {
      this.filter = filter;
   }

   public boolean matchesEntity(@Nonnull Ref<EntityStore> ref, @Nonnull Ref<EntityStore> targetRef, @Nonnull Role role, @Nonnull Store<EntityStore> store) {
      return !this.filter.matchesEntity(ref, targetRef, role, store);
   }

   public int cost() {
      return this.filter.cost();
   }

   public void registerWithSupport(Role role) {
      this.filter.registerWithSupport(role);
   }

   public void motionControllerChanged(@Nullable Ref<EntityStore> ref, @Nonnull NPCEntity npcComponent, MotionController motionController, @Nullable ComponentAccessor<EntityStore> componentAccessor) {
      this.filter.motionControllerChanged(ref, npcComponent, motionController, componentAccessor);
   }

   public void loaded(Role role) {
      this.filter.loaded(role);
   }

   public void spawned(Role role) {
      this.filter.spawned(role);
   }

   public void unloaded(Role role) {
      this.filter.unloaded(role);
   }

   public void removed(Role role) {
      this.filter.removed(role);
   }

   public void teleported(Role role, World from, World to) {
      this.filter.teleported(role, from, to);
   }

   public int componentCount() {
      return 1;
   }

   public IAnnotatedComponent getComponent(int index) {
      if (index >= this.componentCount()) {
         throw new IndexOutOfBoundsException();
      } else {
         return this.filter;
      }
   }

   public void setContext(IAnnotatedComponent parent, int index) {
      super.setContext(parent, index);
      this.filter.setContext(this, index);
   }
}
