package com.hypixel.hytale.builtin.adventure.objectives.config.completion;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class ClearObjectiveItemsCompletionAsset extends ObjectiveCompletionAsset {
   @Nonnull
   public static final BuilderCodec<ClearObjectiveItemsCompletionAsset> CODEC;

   protected ClearObjectiveItemsCompletionAsset() {
   }

   @Nonnull
   public String toString() {
      return "ClearObjectiveItemsCompletionAsset{} " + super.toString();
   }

   static {
      CODEC = BuilderCodec.builder(ClearObjectiveItemsCompletionAsset.class, ClearObjectiveItemsCompletionAsset::new, BASE_CODEC).build();
   }
}
