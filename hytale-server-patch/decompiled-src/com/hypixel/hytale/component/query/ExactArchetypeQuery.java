package com.hypixel.hytale.component.query;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.ComponentType;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ExactArchetypeQuery<ECS_TYPE> implements Query<ECS_TYPE> {
   @Nonnull
   private final Archetype<ECS_TYPE> archetype;

   public ExactArchetypeQuery(@Nonnull Archetype<ECS_TYPE> archetype) {
      this.archetype = archetype;
      Objects.requireNonNull(archetype, "Archetype for ExactArchetypeQuery cannot be null");
   }

   public Archetype<ECS_TYPE> getArchetype() {
      return this.archetype;
   }

   public boolean test(@Nonnull Archetype<ECS_TYPE> archetype) {
      return archetype.equals(this.archetype);
   }

   public boolean requiresComponentType(@Nonnull ComponentType<ECS_TYPE, ?> componentType) {
      return this.archetype.requiresComponentType(componentType);
   }

   public void validateRegistry(@Nonnull ComponentRegistry<ECS_TYPE> registry) {
      this.archetype.validateRegistry(registry);
   }

   public void validate() {
      this.archetype.validate();
   }
}
