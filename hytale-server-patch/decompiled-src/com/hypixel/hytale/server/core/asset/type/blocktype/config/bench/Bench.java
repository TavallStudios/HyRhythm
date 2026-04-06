package com.hypixel.hytale.server.core.asset.type.blocktype.config.bench;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.lookup.ObjectCodecMapCodec;
import com.hypixel.hytale.protocol.BenchType;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.soundevent.validator.SoundEventValidators;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Bench implements NetworkSerializable<com.hypixel.hytale.protocol.Bench> {
   public static final ObjectCodecMapCodec<BenchType, Bench> CODEC = new ObjectCodecMapCodec<BenchType, Bench>("Type", new EnumCodec(BenchType.class));
   public static final BuilderCodec<Bench> BASE_CODEC;
   /** @deprecated */
   @Deprecated(
      forRemoval = true
   )
   protected static final Map<BenchType, RootInteraction> BENCH_INTERACTIONS;
   @Nonnull
   protected BenchType type;
   protected String id;
   protected String descriptiveLabel;
   protected BenchTierLevel[] tierLevels;
   @Nullable
   protected String localOpenSoundEventId;
   protected transient int localOpenSoundEventIndex;
   @Nullable
   protected String localCloseSoundEventId;
   protected transient int localCloseSoundEventIndex;
   @Nullable
   protected String completedSoundEventId;
   protected transient int completedSoundEventIndex;
   @Nullable
   protected String failedSoundEventId;
   protected transient int failedSoundEventIndex;
   @Nullable
   protected String benchUpgradeSoundEventId;
   protected transient int benchUpgradeSoundEventIndex;
   @Nullable
   protected String benchUpgradeCompletedSoundEventId;
   protected transient int benchUpgradeCompletedSoundEventIndex;

   public Bench() {
      this.type = BenchType.Crafting;
      this.localOpenSoundEventId = null;
      this.localOpenSoundEventIndex = 0;
      this.localCloseSoundEventId = null;
      this.localCloseSoundEventIndex = 0;
      this.completedSoundEventId = null;
      this.completedSoundEventIndex = 0;
      this.failedSoundEventId = null;
      this.failedSoundEventIndex = 0;
      this.benchUpgradeSoundEventId = null;
      this.benchUpgradeSoundEventIndex = 0;
      this.benchUpgradeCompletedSoundEventId = null;
      this.benchUpgradeCompletedSoundEventIndex = 0;
   }

   public BenchType getType() {
      return this.type;
   }

   public String getId() {
      return this.id;
   }

   public String getDescriptiveLabel() {
      return this.descriptiveLabel;
   }

   public BenchTierLevel getTierLevel(int tierLevel) {
      return this.tierLevels != null && tierLevel >= 1 && tierLevel <= this.tierLevels.length ? this.tierLevels[tierLevel - 1] : null;
   }

   public BenchUpgradeRequirement getUpgradeRequirement(int tierLevel) {
      BenchTierLevel currentTierLevel = this.getTierLevel(tierLevel);
      return currentTierLevel == null ? null : currentTierLevel.upgradeRequirement;
   }

   @Nullable
   public String getLocalOpenSoundEventId() {
      return this.localOpenSoundEventId;
   }

   public int getLocalOpenSoundEventIndex() {
      return this.localOpenSoundEventIndex;
   }

   @Nullable
   public String getLocalCloseSoundEventId() {
      return this.localCloseSoundEventId;
   }

   public int getLocalCloseSoundEventIndex() {
      return this.localCloseSoundEventIndex;
   }

   @Nullable
   public String getCompletedSoundEventId() {
      return this.completedSoundEventId;
   }

   public int getCompletedSoundEventIndex() {
      return this.completedSoundEventIndex;
   }

   @Nullable
   public String getFailedSoundEventId() {
      return this.failedSoundEventId;
   }

   public int getFailedSoundEventIndex() {
      return this.failedSoundEventIndex;
   }

   @Nullable
   public String getBenchUpgradeSoundEventId() {
      return this.benchUpgradeSoundEventId;
   }

   public int getBenchUpgradeSoundEventIndex() {
      return this.benchUpgradeSoundEventIndex;
   }

   @Nullable
   public String getBenchUpgradeCompletedSoundEventId() {
      return this.benchUpgradeCompletedSoundEventId;
   }

   public int getBenchUpgradeCompletedSoundEventIndex() {
      return this.benchUpgradeCompletedSoundEventIndex;
   }

   @Nullable
   public RootInteraction getRootInteraction() {
      return (RootInteraction)BENCH_INTERACTIONS.get(this.type);
   }

   public com.hypixel.hytale.protocol.Bench toPacket() {
      com.hypixel.hytale.protocol.Bench packet = new com.hypixel.hytale.protocol.Bench();
      if (this.tierLevels != null && this.tierLevels.length > 0) {
         packet.benchTierLevels = new com.hypixel.hytale.protocol.BenchTierLevel[this.tierLevels.length];

         for(int i = 0; i < this.tierLevels.length; ++i) {
            packet.benchTierLevels[i] = this.tierLevels[i].toPacket();
         }
      }

      return packet;
   }

   public boolean equals(Object o) {
      if (o != null && this.getClass() == o.getClass()) {
         Bench bench = (Bench)o;
         return this.localOpenSoundEventIndex == bench.localOpenSoundEventIndex && this.localCloseSoundEventIndex == bench.localCloseSoundEventIndex && this.completedSoundEventIndex == bench.completedSoundEventIndex && this.benchUpgradeSoundEventIndex == bench.benchUpgradeSoundEventIndex && this.benchUpgradeCompletedSoundEventIndex == bench.benchUpgradeCompletedSoundEventIndex && this.type == bench.type && Objects.equals(this.id, bench.id) && Objects.equals(this.descriptiveLabel, bench.descriptiveLabel) && Objects.deepEquals(this.tierLevels, bench.tierLevels) && Objects.equals(this.localOpenSoundEventId, bench.localOpenSoundEventId) && Objects.equals(this.localCloseSoundEventId, bench.localCloseSoundEventId) && Objects.equals(this.completedSoundEventId, bench.completedSoundEventId) && Objects.equals(this.failedSoundEventId, bench.failedSoundEventId) && Objects.equals(this.benchUpgradeSoundEventId, bench.benchUpgradeSoundEventId) && Objects.equals(this.benchUpgradeCompletedSoundEventId, bench.benchUpgradeCompletedSoundEventId);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.type, this.id, this.descriptiveLabel, Arrays.hashCode(this.tierLevels), this.localOpenSoundEventId, this.localOpenSoundEventIndex, this.localCloseSoundEventId, this.localCloseSoundEventIndex, this.completedSoundEventId, this.completedSoundEventIndex, this.failedSoundEventId, this.failedSoundEventIndex, this.benchUpgradeSoundEventId, this.benchUpgradeSoundEventIndex, this.benchUpgradeCompletedSoundEventId, this.benchUpgradeCompletedSoundEventIndex});
   }

   public String toString() {
      String var10000 = String.valueOf(this.type);
      return "Bench{type=" + var10000 + ", id='" + this.id + "', descriptiveLabel='" + this.descriptiveLabel + "', tierLevels=" + Arrays.toString(this.tierLevels) + ", localOpenSoundEventId='" + this.localOpenSoundEventId + "', localOpenSoundEventIndex=" + this.localOpenSoundEventIndex + ", localCloseSoundEventId='" + this.localCloseSoundEventId + "', localCloseSoundEventIndex=" + this.localCloseSoundEventIndex + ", completedSoundEventId='" + this.completedSoundEventId + "', completedSoundEventIndex=" + this.completedSoundEventIndex + ", failedSoundEventId='" + this.failedSoundEventId + "', failedSoundEventIndex=" + this.failedSoundEventIndex + ", benchUpgradeSoundEventId='" + this.benchUpgradeSoundEventId + "', benchUpgradeSoundEventIndex=" + this.benchUpgradeSoundEventIndex + ", benchUpgradeCompletedSoundEventId='" + this.benchUpgradeCompletedSoundEventId + "', benchUpgradeCompletedSoundEventIndex=" + this.benchUpgradeCompletedSoundEventIndex + "}";
   }

   /** @deprecated */
   @Deprecated(
      forRemoval = true
   )
   public static void registerRootInteraction(BenchType benchType, RootInteraction interaction) {
      BENCH_INTERACTIONS.put(benchType, interaction);
   }

   static {
      BASE_CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.abstractBuilder(Bench.class).addField(new KeyedCodec("Id", Codec.STRING), (bench, s) -> bench.id = s, (bench) -> bench.id)).addField(new KeyedCodec("DescriptiveLabel", Codec.STRING), (bench, s) -> bench.descriptiveLabel = s, (bench) -> bench.descriptiveLabel)).appendInherited(new KeyedCodec("TierLevels", new ArrayCodec(BenchTierLevel.CODEC, (x$0) -> new BenchTierLevel[x$0])), (bench, u) -> bench.tierLevels = u, (bench) -> bench.tierLevels, (bench, parent) -> bench.tierLevels = parent.tierLevels).add()).append(new KeyedCodec("LocalOpenSoundEventId", Codec.STRING), (bench, s) -> bench.localOpenSoundEventId = s, (bench) -> bench.localOpenSoundEventId).addValidator(SoundEvent.VALIDATOR_CACHE.getValidator()).addValidator(SoundEventValidators.ONESHOT).add()).append(new KeyedCodec("LocalCloseSoundEventId", Codec.STRING), (bench, s) -> bench.localCloseSoundEventId = s, (bench) -> bench.localCloseSoundEventId).addValidator(SoundEvent.VALIDATOR_CACHE.getValidator()).addValidator(SoundEventValidators.ONESHOT).add()).append(new KeyedCodec("CompletedSoundEventId", Codec.STRING), (bench, s) -> bench.completedSoundEventId = s, (bench) -> bench.completedSoundEventId).addValidator(SoundEvent.VALIDATOR_CACHE.getValidator()).addValidator(SoundEventValidators.MONO).addValidator(SoundEventValidators.ONESHOT).add()).append(new KeyedCodec("FailedSoundEventId", Codec.STRING), (bench, s) -> bench.failedSoundEventId = s, (bench) -> bench.failedSoundEventId).addValidator(SoundEvent.VALIDATOR_CACHE.getValidator()).addValidator(SoundEventValidators.MONO).addValidator(SoundEventValidators.ONESHOT).add()).append(new KeyedCodec("BenchUpgradeSoundEventId", Codec.STRING), (bench, s) -> bench.benchUpgradeSoundEventId = s, (bench) -> bench.benchUpgradeSoundEventId).addValidator(SoundEvent.VALIDATOR_CACHE.getValidator()).addValidator(SoundEventValidators.MONO).addValidator(SoundEventValidators.ONESHOT).add()).append(new KeyedCodec("BenchUpgradeCompletedSoundEventId", Codec.STRING), (bench, s) -> bench.benchUpgradeCompletedSoundEventId = s, (bench) -> bench.benchUpgradeCompletedSoundEventId).addValidator(SoundEvent.VALIDATOR_CACHE.getValidator()).addValidator(SoundEventValidators.MONO).addValidator(SoundEventValidators.ONESHOT).add()).afterDecode((bench) -> {
         bench.type = (BenchType)CODEC.getIdFor(bench.getClass());
         if (bench.localOpenSoundEventId != null) {
            bench.localOpenSoundEventIndex = SoundEvent.getAssetMap().getIndex(bench.localOpenSoundEventId);
         }

         if (bench.localCloseSoundEventId != null) {
            bench.localCloseSoundEventIndex = SoundEvent.getAssetMap().getIndex(bench.localCloseSoundEventId);
         }

         if (bench.completedSoundEventId != null) {
            bench.completedSoundEventIndex = SoundEvent.getAssetMap().getIndex(bench.completedSoundEventId);
         }

         if (bench.benchUpgradeSoundEventId != null) {
            bench.benchUpgradeSoundEventIndex = SoundEvent.getAssetMap().getIndex(bench.benchUpgradeSoundEventId);
         }

         if (bench.benchUpgradeCompletedSoundEventId != null) {
            bench.benchUpgradeCompletedSoundEventIndex = SoundEvent.getAssetMap().getIndex(bench.benchUpgradeCompletedSoundEventId);
         }

         if (bench.failedSoundEventId != null) {
            bench.failedSoundEventIndex = SoundEvent.getAssetMap().getIndex(bench.failedSoundEventId);
         }

      })).build();
      BENCH_INTERACTIONS = new EnumMap(BenchType.class);
   }

   public static class BenchSlot {
      public static final BuilderCodec<BenchSlot> CODEC;
      protected String icon;

      protected BenchSlot() {
      }

      public String getIcon() {
         return this.icon;
      }

      public boolean equals(@Nullable Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            BenchSlot benchSlot = (BenchSlot)o;
            return this.icon != null ? this.icon.equals(benchSlot.icon) : benchSlot.icon == null;
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.icon != null ? this.icon.hashCode() : 0;
      }

      @Nonnull
      public String toString() {
         return "BenchSlot{icon='" + this.icon + "'}";
      }

      static {
         CODEC = ((BuilderCodec.Builder)BuilderCodec.builder(BenchSlot.class, BenchSlot::new).addField(new KeyedCodec("Icon", Codec.STRING), (benchSlot, s) -> benchSlot.icon = s, (benchSlot) -> benchSlot.icon)).build();
      }
   }
}
