package com.hypixel.hytale.builtin.npccombatactionevaluator.evaluator.combatactions;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.npccombatactionevaluator.evaluator.CombatActionEvaluator;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.decisionmaker.core.Option;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.valuestore.ValueStore;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class CombatActionOption extends Option implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, CombatActionOption>> {
   @Nonnull
   public static final AssetCodecMapCodec<String, CombatActionOption> CODEC;
   @Nonnull
   public static final BuilderCodec<CombatActionOption> BASE_CODEC;
   @Nonnull
   public static final Codec<String> CHILD_ASSET_CODEC;
   @Nonnull
   public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY;
   @Nonnull
   public static final ValidatorCache<String> VALIDATOR_CACHE;
   private static AssetStore<String, CombatActionOption, IndexedLookupTableAssetMap<String, CombatActionOption>> ASSET_STORE;
   protected AssetExtraInfo.Data extraData;
   protected String id;
   protected Target actionTarget;
   protected double[] postExecuteDistanceRange;

   @Nonnull
   public static AssetStore<String, CombatActionOption, IndexedLookupTableAssetMap<String, CombatActionOption>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.<String, CombatActionOption, IndexedLookupTableAssetMap<String, CombatActionOption>>getAssetStore(CombatActionOption.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, CombatActionOption> getAssetMap() {
      return (IndexedLookupTableAssetMap)getAssetStore().getAssetMap();
   }

   protected CombatActionOption() {
   }

   public String getId() {
      return this.id;
   }

   public Target getActionTarget() {
      return this.actionTarget;
   }

   @Nullable
   public double[] getPostExecuteDistanceRange() {
      return this.postExecuteDistanceRange;
   }

   public abstract void execute(int var1, ArchetypeChunk<EntityStore> var2, CommandBuffer<EntityStore> var3, Role var4, CombatActionEvaluator var5, ValueStore var6);

   public abstract boolean isBasicAttackAllowed(int var1, ArchetypeChunk<EntityStore> var2, CommandBuffer<EntityStore> var3, CombatActionEvaluator var4);

   public boolean cancelBasicAttackOnSelect() {
      return true;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.extraData);
      return "CombatActionOption{extraData=" + var10000 + ", id='" + this.id + "', actionTarget=" + String.valueOf(this.actionTarget) + ", postExecuteDistanceRange=" + Arrays.toString(this.postExecuteDistanceRange) + "}" + super.toString();
   }

   @Nonnull
   public static CombatActionOption getNothingFor(String id) {
      return new Nothing(id);
   }

   static {
      CODEC = new AssetCodecMapCodec<String, CombatActionOption>(Codec.STRING, (t, k) -> t.id = k, (t) -> t.id, (t, data) -> t.extraData = data, (t) -> t.extraData);
      BASE_CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.abstractBuilder(CombatActionOption.class, Option.ABSTRACT_CODEC).appendInherited(new KeyedCodec("Target", CombatActionOption.Target.CODEC), (option, e) -> option.actionTarget = e, (option) -> option.actionTarget, (option, parent) -> option.actionTarget = parent.actionTarget).addValidator(Validators.nonNull()).documentation("The target type this action applies to.").add()).appendInherited(new KeyedCodec("PostExecuteDistanceRange", Codec.DOUBLE_ARRAY), (option, o) -> option.postExecuteDistanceRange = o, (option) -> option.postExecuteDistanceRange, (option, parent) -> option.postExecuteDistanceRange = parent.postExecuteDistanceRange).addValidator(Validators.doubleArraySize(2)).addValidator(Validators.weaklyMonotonicSequentialDoubleArrayValidator()).documentation("An optional range the NPC will try to maintain from the target after executing the combat action.").add()).build();
      CHILD_ASSET_CODEC = new ContainedAssetCodec(CombatActionOption.class, CODEC);
      CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<String[]>(CHILD_ASSET_CODEC, (x$0) -> new String[x$0]);
      VALIDATOR_CACHE = new ValidatorCache<String>(new AssetKeyValidator(CombatActionOption::getAssetStore));
      CODEC.register("State", StateCombatAction.class, StateCombatAction.CODEC);
      CODEC.register("Ability", AbilityCombatAction.class, AbilityCombatAction.CODEC);
      CODEC.register("SelectBasicAttackTarget", BasicAttackTargetCombatAction.class, BasicAttackTargetCombatAction.CODEC);
   }

   public static enum Target {
      Self,
      Hostile,
      Friendly;

      @Nonnull
      public static final EnumCodec<Target> CODEC = (new EnumCodec<Target>(Target.class)).documentKey(Self, "Action targets self.").documentKey(Hostile, "Action targets any hostile target.").documentKey(Friendly, "Action targets any friendly target.");
   }

   private static class Nothing extends CombatActionOption {
      private final String id;

      public Nothing(String id) {
         this.id = id;
         this.actionTarget = CombatActionOption.Target.Self;
      }

      public String getId() {
         return this.id;
      }

      public void execute(int index, ArchetypeChunk<EntityStore> archetypeChunk, CommandBuffer<EntityStore> commandBuffer, Role role, CombatActionEvaluator evaluator, ValueStore valueStore) {
      }

      public boolean isBasicAttackAllowed(int selfIndex, ArchetypeChunk<EntityStore> archetypeChunk, CommandBuffer<EntityStore> commandBuffer, CombatActionEvaluator evaluator) {
         return true;
      }
   }
}
