package com.hypixel.hytale.server.core.modules.interaction.interaction.config.client;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.InteractionState;
import com.hypixel.hytale.protocol.InteractionSyncData;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.modules.interaction.InteractionModule;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.Collector;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.data.CollectorTag;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.Label;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.OperationsBuilder;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChainingInteraction extends Interaction {
   @Nonnull
   public static final BuilderCodec<ChainingInteraction> CODEC;
   protected String chainId;
   protected float chainingAllowance;
   protected String[] next;
   @Nullable
   protected Map<String, String> flags;
   @Nullable
   protected Object2IntMap<String> flagIndex;
   private String[] sortedFlagKeys;

   @Nonnull
   public WaitForDataFrom getWaitForDataFrom() {
      return WaitForDataFrom.Client;
   }

   protected void tick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      InteractionSyncData clientState = context.getClientState();

      assert clientState != null;

      InteractionSyncData state = context.getState();
      if (clientState.flagIndex != -1) {
         state.state = InteractionState.Finished;
         context.jump(context.getLabel(this.next.length + clientState.flagIndex));
      } else if (clientState.chainingIndex == -1) {
         state.state = InteractionState.NotFinished;
      } else {
         state.state = InteractionState.Finished;
         context.jump(context.getLabel(clientState.chainingIndex));
      }
   }

   protected void simulateTick0(boolean firstRun, float time, @Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      if (firstRun) {
         CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();

         assert commandBuffer != null;

         Ref<EntityStore> ref = context.getEntity();
         Data dataComponent = (Data)commandBuffer.getComponent(ref, ChainingInteraction.Data.getComponentType());
         if (dataComponent != null) {
            InteractionSyncData state = context.getState();
            String id = this.chainId == null ? this.id : this.chainId;
            Object2IntMap<String> map = this.chainId == null ? dataComponent.map : dataComponent.namedMap;
            int lastSequenceIndex = map.getInt(id);
            ++lastSequenceIndex;
            if (lastSequenceIndex >= this.next.length) {
               lastSequenceIndex = 0;
            }

            if (this.chainingAllowance > 0.0F && dataComponent.getTimeSinceLastAttackInSeconds() > this.chainingAllowance) {
               lastSequenceIndex = 0;
            }

            map.put(id, lastSequenceIndex);
            state.chainingIndex = lastSequenceIndex;
            state.state = InteractionState.Finished;
            context.jump(context.getLabel(lastSequenceIndex));
            dataComponent.lastAttack = System.nanoTime();
         }
      }
   }

   public void compile(@Nonnull OperationsBuilder builder) {
      int len = this.next.length + (this.sortedFlagKeys != null ? this.sortedFlagKeys.length : 0);
      Label[] labels = new Label[len];

      for(int i = 0; i < labels.length; ++i) {
         labels[i] = builder.createUnresolvedLabel();
      }

      builder.addOperation(this, labels);
      Label end = builder.createUnresolvedLabel();

      for(int i = 0; i < this.next.length; ++i) {
         builder.resolveLabel(labels[i]);
         Interaction interaction = Interaction.getInteractionOrUnknown(this.next[i]);
         interaction.compile(builder);
         builder.jump(end);
      }

      if (this.flags != null) {
         for(int i = 0; i < this.sortedFlagKeys.length; ++i) {
            String flag = this.sortedFlagKeys[i];
            builder.resolveLabel(labels[this.next.length + i]);
            Interaction interaction = Interaction.getInteractionOrUnknown((String)this.flags.get(flag));
            interaction.compile(builder);
            builder.jump(end);
         }
      }

      builder.resolveLabel(end);
   }

   public boolean walk(@Nonnull Collector collector, @Nonnull InteractionContext context) {
      for(int i = 0; i < this.next.length; ++i) {
         if (InteractionManager.walkInteraction(collector, context, ChainingInteraction.ChainingTag.of(i), this.next[i])) {
            return true;
         }
      }

      return false;
   }

   @Nonnull
   protected com.hypixel.hytale.protocol.Interaction generatePacket() {
      return new com.hypixel.hytale.protocol.ChainingInteraction();
   }

   protected void configurePacket(com.hypixel.hytale.protocol.Interaction packet) {
      super.configurePacket(packet);
      com.hypixel.hytale.protocol.ChainingInteraction p = (com.hypixel.hytale.protocol.ChainingInteraction)packet;
      p.chainingAllowance = this.chainingAllowance;
      int[] chainingNext = p.chainingNext = new int[this.next.length];

      for(int i = 0; i < this.next.length; ++i) {
         chainingNext[i] = Interaction.getInteractionIdOrUnknown(this.next[i]);
      }

      if (this.flags != null) {
         p.flags = new Object2IntOpenHashMap();

         for(Map.Entry<String, String> e : this.flags.entrySet()) {
            p.flags.put((String)e.getKey(), Interaction.getInteractionIdOrUnknown((String)e.getValue()));
         }
      }

      p.chainId = this.chainId;
   }

   public boolean needsRemoteSync() {
      return true;
   }

   @Nonnull
   public String toString() {
      float var10000 = this.chainingAllowance;
      return "ChainingInteraction{chainingAllowance=" + var10000 + ", next=" + Arrays.toString(this.next) + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ChainingInteraction.class, ChainingInteraction::new, Interaction.ABSTRACT_CODEC).documentation("Runs one of the entries in `Next` based on how many times this interaction was run before the `ChainingAllowance` timer was reset.")).appendInherited(new KeyedCodec("ChainingAllowance", Codec.DOUBLE), (chainingInteraction, d) -> chainingInteraction.chainingAllowance = d.floatValue(), (chainingInteraction) -> (double)chainingInteraction.chainingAllowance, (chainingInteraction, parent) -> chainingInteraction.chainingAllowance = parent.chainingAllowance).documentation("Time in seconds that the user has to run this interaction again in order to hit the next chain entry.\nResets the timer each time the interaction is reached.").add()).appendInherited(new KeyedCodec("Next", new ArrayCodec(Interaction.CHILD_ASSET_CODEC, (x$0) -> new String[x$0])), (interaction, s) -> interaction.next = s, (interaction) -> interaction.next, (interaction, parent) -> interaction.next = parent.next).addValidator(Validators.nonNull()).addValidator(Validators.nonNullArrayElements()).addValidatorLate(() -> Interaction.VALIDATOR_CACHE.getArrayValidator().late()).add()).appendInherited(new KeyedCodec("ChainId", Codec.STRING), (o, i) -> o.chainId = i, (o) -> o.chainId, (o, p) -> o.chainId = p.chainId).add()).appendInherited(new KeyedCodec("Flags", new MapCodec(CHILD_ASSET_CODEC, HashMap::new)), (o, i) -> o.flags = i, (o) -> o.flags, (o, p) -> o.flags = p.flags).addValidatorLate(() -> Interaction.VALIDATOR_CACHE.getMapValueValidator().late()).add()).afterDecode((o) -> {
         if (o.flags != null) {
            String[] sortedFlagKeys = o.sortedFlagKeys = (String[])o.flags.keySet().toArray((x$0) -> new String[x$0]);
            Arrays.sort(sortedFlagKeys);
            o.flagIndex = new Object2IntOpenHashMap();

            for(int i = 0; i < sortedFlagKeys.length; ++i) {
               o.flagIndex.put(sortedFlagKeys[i], i);
            }
         }

      })).build();
   }

   public static class Data implements Component<EntityStore> {
      private final Object2IntMap<String> map = new Object2IntOpenHashMap();
      private final Object2IntMap<String> namedMap = new Object2IntOpenHashMap();
      private long lastAttack;

      public static ComponentType<EntityStore, Data> getComponentType() {
         return InteractionModule.get().getChainingDataComponent();
      }

      public float getTimeSinceLastAttackInSeconds() {
         if (this.lastAttack == 0L) {
            return 0.0F;
         } else {
            long diff = System.nanoTime() - this.lastAttack;
            return (float)diff / 1.0E9F;
         }
      }

      @Nonnull
      public Object2IntMap<String> getNamedMap() {
         return this.namedMap;
      }

      @Nonnull
      public Component<EntityStore> clone() {
         Data c = new Data();
         c.map.putAll(this.map);
         c.lastAttack = this.lastAttack;
         return c;
      }
   }

   private static class ChainingTag implements CollectorTag {
      private final int index;

      private ChainingTag(int index) {
         this.index = index;
      }

      public int getIndex() {
         return this.index;
      }

      public boolean equals(@Nullable Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            ChainingTag that = (ChainingTag)o;
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
         return "ChainingTag{index=" + this.index + "}";
      }

      @Nonnull
      public static ChainingTag of(int index) {
         return new ChainingTag(index);
      }
   }
}
