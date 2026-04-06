package com.hypixel.hytale.builtin.hytalegenerator.framework.shaders;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class RelationalShader<T> implements Shader<T> {
   @Nonnull
   private final Map<T, Shader<T>> relations;
   @Nonnull
   private final Shader<T> onMissingKey;

   public RelationalShader(@Nonnull Shader<T> onMissingKey) {
      this.onMissingKey = onMissingKey;
      this.relations = new HashMap(1);
   }

   @Nonnull
   public RelationalShader<T> addRelation(@Nonnull T key, @Nonnull Shader<T> value) {
      this.relations.put(key, value);
      return this;
   }

   public T shade(T current, long seed) {
      return (T)(!this.relations.containsKey(current) ? this.onMissingKey.shade(current, seed) : ((Shader)this.relations.get(current)).shade(current, seed));
   }

   public T shade(T current, long seedA, long seedB) {
      return (T)(!this.relations.containsKey(current) ? this.onMissingKey.shade(current, seedA, seedB) : ((Shader)this.relations.get(current)).shade(current, seedA, seedB));
   }

   public T shade(T current, long seedA, long seedB, long seedC) {
      return (T)(!this.relations.containsKey(current) ? this.onMissingKey.shade(current, seedA, seedB, seedC) : ((Shader)this.relations.get(current)).shade(current, seedA, seedB, seedC));
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.relations);
      return "RelationalShader{relations=" + var10000 + ", onMissingKey=" + String.valueOf(this.onMissingKey) + "}";
   }
}
