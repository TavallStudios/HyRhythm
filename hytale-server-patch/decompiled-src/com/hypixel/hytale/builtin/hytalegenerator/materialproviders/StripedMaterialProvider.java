package com.hypixel.hytale.builtin.hytalegenerator.materialproviders;

import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StripedMaterialProvider<V> extends MaterialProvider<V> {
   @Nonnull
   private final MaterialProvider<V> materialProvider;
   @Nonnull
   private final Stripe[] stripes;

   public StripedMaterialProvider(@Nonnull MaterialProvider<V> materialProvider, @Nonnull List<Stripe> stripes) {
      this.materialProvider = materialProvider;
      this.stripes = new Stripe[stripes.size()];

      for(int i = 0; i < stripes.size(); ++i) {
         Stripe s = (Stripe)stripes.get(i);
         this.stripes[i] = s;
      }

   }

   @Nullable
   public V getVoxelTypeAt(@Nonnull MaterialProvider.Context context) {
      for(Stripe s : this.stripes) {
         if (s.contains(context.position.y)) {
            return this.materialProvider.getVoxelTypeAt(context);
         }
      }

      return null;
   }

   public static class Stripe {
      private final int topY;
      private final int bottomY;

      public Stripe(int topY, int bottomY) {
         this.topY = topY;
         this.bottomY = bottomY;
      }

      public boolean contains(int y) {
         return y >= this.bottomY && y <= this.topY;
      }
   }
}
