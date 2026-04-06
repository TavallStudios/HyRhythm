package com.hypixel.hytale.server.core.modules.interaction.interaction.config.none;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.Collector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.CollectorTag;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ParallelInteraction extends Interaction {
   public static final BuilderCodec<ParallelInteraction> CODEC;
   protected String[] interactions;

   @Nonnull
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.None;
   }

   protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      IndexedLookupTableAssetMap<String, RootInteraction> assetMap = RootInteraction.getAssetMap();
      context.execute(RootInteraction.getRootInteractionOrUnknown(this.interactions[0]));

      for(int i = 1; i < this.interactions.length; ++i) {
         String interaction = this.interactions[i];
         context.fork(context.duplicate(), RootInteraction.getRootInteractionOrUnknown(interaction), true);
      }

      context.getState().state = InteractionState.Finished;
   }

   protected void simulateTick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      IndexedLookupTableAssetMap<String, RootInteraction> assetMap = RootInteraction.getAssetMap();
      context.execute(RootInteraction.getRootInteractionOrUnknown(this.interactions[0]));
      context.getState().state = InteractionState.Finished;
   }

   public boolean walk(@Nonnull Collector collector, @Nonnull InteractionContext context) {
      for(int i = 0; i < this.interactions.length; ++i) {
         String root = this.interactions[i];
         if (InteractionManager.walkInteractions(collector, context, ParallelInteraction.ParallelTag.of(i), RootInteraction.getRootInteractionOrUnknown(root).getInteractionIds())) {
            return true;
         }
      }

      return false;
   }

   public boolean needsRemoteSync() {
      return true;
   }

   @Nonnull
   protected com.hypixel.hytale.protocol.Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.ParallelInteraction();
   }

   protected void configurePacket(com.hypixel.hytale.protocol.Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.ParallelInteraction p = (com.hypixel.hytale.protocol.ParallelInteraction)packet;
      int[] chainingNext = p.next = new int[this.interactions.length];

      for(int i = 0; i < this.interactions.length; ++i) {
         chainingNext[i] = RootInteraction.getRootInteractionIdOrUnknown(this.interactions[i]);
      }

   }

   @Nonnull
   public String toString() {
      String var10000 = Arrays.toString(this.interactions);
      return "ParallelInteraction{interactions=" + var10000 + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ParallelInteraction.class, ParallelInteraction::new, Interaction.ABSTRACT_CODEC).documentation("Runs the provided interactions in parallel to this interaction chain.")).appendInherited(new KeyedCodec("Interactions", new ArrayCodec(RootInteraction.CHILD_ASSET_CODEC, (x$0) -> new String[x$0])), (i, s) -> i.interactions = s, (i) -> i.interactions, (i, parent) -> i.interactions = parent.interactions).documentation("The collection of interaction roots to run in parallel via forks.").addValidator(Validators.nonNull()).addValidatorLate(() -> RootInteraction.VALIDATOR_CACHE.getArrayValidator().late()).addValidator(Validators.arraySizeRange(2, 2147483647)).add()).build();
   }

   private static class ParallelTag implements CollectorTag {
      private final int index;

      private ParallelTag(int index) {
         this.index = index;
      }

      public int getIndex() {
         return this.index;
      }

      public boolean equals(@Nullable Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            ParallelTag that = (ParallelTag)o;
            return this.index == that.index;
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.index;
      }

      @Nonnull
      public String toString() {
         return "ParallelTag{index=" + this.index + "}";
      }

      @Nonnull
      public static ParallelTag of(int index) {
         return new ParallelTag(index);
      }
   }
}
