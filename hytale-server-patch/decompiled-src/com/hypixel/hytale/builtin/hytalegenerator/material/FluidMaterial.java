package com.hypixel.hytale.builtin.hytalegenerator.material;

import java.util.Objects;
import javax.annotation.Nonnull;

public class FluidMaterial {
   @Nonnull
   private final MaterialCache materialCache;
   public final int fluidId;
   public final byte fluidLevel;

   FluidMaterial(@Nonnull MaterialCache materialCache, int fluidId, byte fluidLevel) {
      this.materialCache = materialCache;
      this.fluidId = fluidId;
      this.fluidLevel = fluidLevel;
   }

   @Nonnull
   public MaterialCache getVoxelCache() {
      return this.materialCache;
   }

   public final boolean equals(Object o) {
      if (!(o instanceof FluidMaterial that)) {
         return false;
      } else {
         return this.fluidId == that.fluidId && this.fluidLevel == that.fluidLevel && this.materialCache.equals(that.materialCache);
      }
   }

   public int hashCode() {
      return contentHash(this.fluidId, this.fluidLevel);
   }

   public static int contentHash(int blockId, byte fluidLevel) {
      return Objects.hash(new Object[]{blockId, fluidLevel});
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.materialCache);
      return "FluidMaterial{materialCache=" + var10000 + ", fluidId=" + this.fluidId + ", fluidLevel=" + this.fluidLevel + "}";
   }
}
