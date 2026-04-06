package com.hypixel.hytale.builtin.hytalegenerator.material;

import java.util.Objects;
import javax.annotation.Nonnull;

public final class Material {
   @Nonnull
   private final SolidMaterial solid;
   @Nonnull
   private final FluidMaterial fluid;
   private Hash hashCode;
   private Hash materialIdsHash;

   public Material(@Nonnull SolidMaterial solid, @Nonnull FluidMaterial fluid) {
      this.solid = solid;
      this.fluid = fluid;
      this.hashCode = new Hash();
      this.materialIdsHash = new Hash();
   }

   public boolean equals(Object o) {
      if (!(o instanceof Material material)) {
         return false;
      } else {
         return Objects.equals(this.solid, material.solid) && Objects.equals(this.fluid, material.fluid);
      }
   }

   public int hashCode() {
      if (this.hashCode.isCalculated) {
         return this.hashCode.value;
      } else {
         this.hashCode.value = hashCode(this.solid, this.fluid);
         this.hashCode.isCalculated = true;
         return this.hashCode.value;
      }
   }

   public int hashMaterialIds() {
      if (this.materialIdsHash.isCalculated) {
         return this.materialIdsHash.value;
      } else {
         this.materialIdsHash.value = hashMaterialIds(this.solid, this.fluid);
         this.materialIdsHash.isCalculated = true;
         return this.materialIdsHash.value;
      }
   }

   public static int hashCode(@Nonnull SolidMaterial solid, @Nonnull FluidMaterial fluid) {
      int result = solid.hashCode();
      result = 31 * result + fluid.hashCode();
      return result;
   }

   public static int hashMaterialIds(@Nonnull SolidMaterial solid, @Nonnull FluidMaterial fluid) {
      return Objects.hash(new Object[]{solid.blockId, fluid.fluidId});
   }

   @Nonnull
   public SolidMaterial solid() {
      return this.solid;
   }

   @Nonnull
   public FluidMaterial fluid() {
      return this.fluid;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.solid);
      return "Material[solid=" + var10000 + ", fluid=" + String.valueOf(this.fluid) + "]";
   }

   private class Hash {
      int value = 0;
      boolean isCalculated = false;
   }
}
