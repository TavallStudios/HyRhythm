package com.hypixel.hytale.builtin.hytalegenerator.framework.shaders;

import com.hypixel.hytale.builtin.hytalegenerator.framework.math.SeedGenerator;
import java.util.function.Predicate;
import javax.annotation.Nonnull;

public class MaskShader<T> implements Shader<T> {
   private final Shader<T> childShader;
   private final Predicate<T> mask;
   private SeedGenerator seedGenerator;

   private MaskShader(Predicate<T> mask, Shader<T> childShader, long seed) {
      this.mask = mask;
      this.childShader = childShader;
      this.seedGenerator = new SeedGenerator(seed);
   }

   @Nonnull
   public static <T> Builder<T> builder(@Nonnull Class<T> dataType) {
      return new Builder<T>();
   }

   public T shade(T current, long seed) {
      return (T)(!this.mask.test(current) ? current : this.childShader.shade(current, seed));
   }

   public T shade(T current, long seedA, long seedB) {
      return (T)this.shade(current, 0L);
   }

   public T shade(T current, long seedA, long seedB, long seedC) {
      return (T)this.shade(current, 0L);
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.childShader);
      return "MaskShader{childShader=" + var10000 + ", mask=" + String.valueOf(this.mask) + ", seedGenerator=" + String.valueOf(this.seedGenerator) + "}";
   }

   public static class Builder<T> {
      private Shader<T> childShader;
      private Predicate<T> mask;
      private long seed = System.nanoTime();

      private Builder() {
      }

      @Nonnull
      public MaskShader<T> build() {
         if (this.childShader != null && this.mask != null) {
            return new MaskShader<T>(this.mask, this.childShader, this.seed);
         } else {
            throw new IllegalStateException("incomplete builder");
         }
      }

      @Nonnull
      public Builder<T> withSeed(long seed) {
         this.seed = seed;
         return this;
      }

      @Nonnull
      public Builder<T> withMask(@Nonnull Predicate<T> mask) {
         this.mask = mask;
         return this;
      }

      @Nonnull
      public Builder<T> withChildShader(@Nonnull Shader<T> shader) {
         this.childShader = shader;
         return this;
      }
   }
}
