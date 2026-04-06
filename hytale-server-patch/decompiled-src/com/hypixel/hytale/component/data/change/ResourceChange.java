package com.hypixel.hytale.component.data.change;

import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import javax.annotation.Nonnull;

public class ResourceChange<ECS_TYPE, T extends Resource<ECS_TYPE>> implements DataChange {
   private final ChangeType type;
   private final ResourceType<ECS_TYPE, T> resourceType;

   public ResourceChange(ChangeType type, ResourceType<ECS_TYPE, T> resourceType) {
      this.type = type;
      this.resourceType = resourceType;
   }

   public ChangeType getType() {
      return this.type;
   }

   public ResourceType<ECS_TYPE, T> getResourceType() {
      return this.resourceType;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.type);
      return "ResourceChange{type=" + var10000 + ", resourceChange=" + String.valueOf(this.resourceType) + "}";
   }
}
