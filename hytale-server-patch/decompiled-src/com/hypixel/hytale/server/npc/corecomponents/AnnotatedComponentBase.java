package com.hypixel.hytale.server.npc.corecomponents;

import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.util.ComponentInfo;
import com.hypixel.hytale.server.npc.util.IAnnotatedComponent;

public abstract class AnnotatedComponentBase implements IAnnotatedComponent {
   protected IAnnotatedComponent parent;
   protected int index;

   public void getInfo(Role role, ComponentInfo holder) {
   }

   public void setContext(IAnnotatedComponent parent, int index) {
      this.parent = parent;
      this.index = index;
   }

   public IAnnotatedComponent getParent() {
      return this.parent;
   }

   public int getIndex() {
      return this.index;
   }
}
