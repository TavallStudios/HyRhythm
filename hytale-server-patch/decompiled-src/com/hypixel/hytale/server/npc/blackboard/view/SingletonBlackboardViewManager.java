package com.hypixel.hytale.server.npc.blackboard.view;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.blackboard.Blackboard;
import java.util.function.Consumer;
import javax.annotation.Nonnull;

public class SingletonBlackboardViewManager<View extends IBlackboardView<View>> implements IBlackboardViewManager<View> {
   private final View view;

   public SingletonBlackboardViewManager(View view) {
      this.view = view;
   }

   public View get(Ref<EntityStore> ref, Blackboard blackboard, ComponentAccessor<EntityStore> componentAccessor) {
      return this.view;
   }

   public View get(Vector3d position, Blackboard blackboard) {
      return this.view;
   }

   public View get(int chunkX, int chunkZ, Blackboard blackboard) {
      return this.view;
   }

   public View get(long index, Blackboard blackboard) {
      return this.view;
   }

   public View getIfExists(long index) {
      return this.view;
   }

   public void cleanup() {
      this.view.cleanup();
   }

   public void onWorldRemoved() {
      this.view.onWorldRemoved();
   }

   public void forEachView(@Nonnull Consumer<View> consumer) {
      consumer.accept(this.view);
   }

   public void clear() {
   }
}
