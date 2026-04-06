package com.hypixel.hytale.component.query;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.ComponentType;
import javax.annotation.Nonnull;

public class AnyQuery<ECS_TYPE> implements Query<ECS_TYPE> {
   @Nonnull
   static final AnyQuery<?> INSTANCE = new AnyQuery();

   public boolean test(Archetype<ECS_TYPE> archetype) {
      return true;
   }

   public boolean requiresComponentType(ComponentType<ECS_TYPE, ?> componentType) {
      return false;
   }

   public void validateRegistry(@Nonnull ComponentRegistry<ECS_TYPE> registry) {
   }

   public void validate() {
   }
}
