package com.hypixel.hytale.server.core.modules.entity;

import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ComponentRegistry;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

/** @deprecated */
@Deprecated
public class AllLegacyEntityTypesQuery implements Query<EntityStore> {
   @Nonnull
   public static final AllLegacyEntityTypesQuery INSTANCE = new AllLegacyEntityTypesQuery();

   public boolean test(@Nonnull Archetype<EntityStore> archetype) {
      return EntityUtils.hasEntity(archetype);
   }

   public boolean requiresComponentType(ComponentType<EntityStore, ?> componentType) {
      return false;
   }

   public void validateRegistry(@Nonnull ComponentRegistry<EntityStore> registry) {
   }

   public void validate() {
   }
}
