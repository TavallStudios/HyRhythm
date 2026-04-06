package com.hypixel.hytale.component.data.change;

import com.hypixel.hytale.component.SystemGroup;
import javax.annotation.Nonnull;

public class SystemGroupChange<ECS_TYPE> implements DataChange {
   private final ChangeType type;
   private final SystemGroup<ECS_TYPE> systemGroup;

   public SystemGroupChange(ChangeType type, SystemGroup<ECS_TYPE> systemGroup) {
      this.type = type;
      this.systemGroup = systemGroup;
   }

   public ChangeType getType() {
      return this.type;
   }

   public SystemGroup<ECS_TYPE> getSystemGroup() {
      return this.systemGroup;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.type);
      return "SystemGroupChange{type=" + var10000 + ", systemGroup=" + String.valueOf(this.systemGroup) + "}";
   }
}
