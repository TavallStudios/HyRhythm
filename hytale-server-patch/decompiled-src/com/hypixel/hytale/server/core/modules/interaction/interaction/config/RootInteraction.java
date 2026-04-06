package com.hypixel.hytale.server.core.modules.interaction.interaction.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.codecs.map.EnumMapCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.codec.validation.validator.FloatArrayValidator;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.protocol.InteractionCooldown;
import com.hypixel.hytale.protocol.RootInteractionSettings;
import com.hypixel.hytale.protocol.WaitForDataFrom;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.Operation;
import com.hypixel.hytale.server.core.modules.interaction.interaction.operation.OperationsBuilder;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RootInteraction implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, RootInteraction>>, NetworkSerializable<com.hypixel.hytale.protocol.RootInteraction> {
   @Nonnull
   public static final BuilderCodec<InteractionCooldown> COOLDOWN_CODEC;
   @Nonnull
   public static final AssetBuilderCodec<String, RootInteraction> CODEC;
   @Nonnull
   public static final ValidatorCache<String> VALIDATOR_CACHE;
   @Nonnull
   public static final ContainedAssetCodec<String, RootInteraction, ?> CHILD_ASSET_CODEC;
   @Nonnull
   public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY;
   @Nonnull
   public static final MapCodec<String, HashMap<String, String>> CHILD_ASSET_CODEC_MAP;
   private static AssetStore<String, RootInteraction, IndexedLookupTableAssetMap<String, RootInteraction>> ASSET_STORE;
   protected String id;
   protected AssetExtraInfo.Data data;
   @Nonnull
   protected String[] interactionIds;
   @Nullable
   protected InteractionCooldown cooldown;
   @Nonnull
   protected Map<GameMode, RootInteractionSettings> settings;
   protected boolean requireNewClick;
   protected float clickQueuingTimeout;
   @Nonnull
   protected InteractionRules rules;
   protected Operation[] operations;
   protected boolean needsRemoteSync;

   public RootInteraction() {
      this.interactionIds = ArrayUtil.EMPTY_STRING_ARRAY;
      this.settings = Collections.emptyMap();
      this.rules = InteractionRules.DEFAULT_RULES;
   }

   public RootInteraction(@Nonnull String id, @Nonnull String... interactionIds) {
      this.interactionIds = ArrayUtil.EMPTY_STRING_ARRAY;
      this.settings = Collections.emptyMap();
      this.rules = InteractionRules.DEFAULT_RULES;
      this.id = id;
      this.interactionIds = interactionIds;
      this.data = new AssetExtraInfo.Data(RootInteraction.class, id, (Object)null);
   }

   public RootInteraction(@Nonnull String id, @Nullable InteractionCooldown cooldown, @Nonnull String... interactionIds) {
      this.interactionIds = ArrayUtil.EMPTY_STRING_ARRAY;
      this.settings = Collections.emptyMap();
      this.rules = InteractionRules.DEFAULT_RULES;
      this.id = id;
      this.cooldown = cooldown;
      this.interactionIds = interactionIds;
      this.data = new AssetExtraInfo.Data(RootInteraction.class, id, (Object)null);
   }

   @Nonnull
   public static AssetStore<String, RootInteraction, IndexedLookupTableAssetMap<String, RootInteraction>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.<String, RootInteraction, IndexedLookupTableAssetMap<String, RootInteraction>>getAssetStore(RootInteraction.class);
      }

      return ASSET_STORE;
   }

   @Nonnull
   public static IndexedLookupTableAssetMap<String, RootInteraction> getAssetMap() {
      return (IndexedLookupTableAssetMap)getAssetStore().getAssetMap();
   }

   public String getId() {
      return this.id;
   }

   public boolean needsRemoteSync() {
      return this.needsRemoteSync;
   }

   public boolean resetCooldownOnStart() {
      return this.cooldown == null || !this.cooldown.skipCooldownReset;
   }

   @Nullable
   public Operation getOperation(int index) {
      return index >= this.operations.length ? null : this.operations[index];
   }

   public int getOperationMax() {
      return this.operations.length;
   }

   public String[] getInteractionIds() {
      return this.interactionIds;
   }

   @Nonnull
   public Map<GameMode, RootInteractionSettings> getSettings() {
      return this.settings;
   }

   public float getClickQueuingTimeout() {
      return this.clickQueuingTimeout;
   }

   @Nonnull
   public InteractionRules getRules() {
      return this.rules;
   }

   @Nullable
   public InteractionCooldown getCooldown() {
      return this.cooldown;
   }

   public AssetExtraInfo.Data getData() {
      return this.data;
   }

   public void build(@Nonnull Set<String> modifiedInteractions) {
      if (this.operations != null) {
         this.build();
      }
   }

   public void build() {
      if (this.interactionIds != null) {
         OperationsBuilder builder = new OperationsBuilder();
         boolean needsSyncRemote = false;

         for(String interactionId : this.interactionIds) {
            Interaction interaction = (Interaction)Interaction.getAssetMap().getAsset(interactionId);
            if (interaction != null) {
               interaction.compile(builder);
               needsSyncRemote |= interaction.needsRemoteSync();
            }
         }

         this.operations = builder.build();
         this.needsRemoteSync = needsSyncRemote;

         for(Operation op : this.operations) {
            op = op.getInnerOperation();
            if (op instanceof Interaction) {
               Interaction inter = (Interaction)op;
               if (inter.getWaitForDataFrom() == WaitForDataFrom.Client && !inter.needsRemoteSync()) {
                  throw new IllegalArgumentException(String.valueOf(inter) + " needs client data but isn't marked as requiring syncing to remote clients");
               }
            }
         }

      }
   }

   @Nonnull
   public com.hypixel.hytale.protocol.RootInteraction toPacket() {
      com.hypixel.hytale.protocol.RootInteraction packet = new com.hypixel.hytale.protocol.RootInteraction();
      packet.id = this.id;
      packet.interactions = new int[this.interactionIds.length];

      for(int i = 0; i < this.interactionIds.length; ++i) {
         packet.interactions[i] = Interaction.getInteractionIdOrUnknown(this.interactionIds[i]);
      }

      packet.clickQueuingTimeout = this.clickQueuingTimeout;
      packet.requireNewClick = this.requireNewClick;
      packet.rules = this.rules.toPacket();
      packet.settings = this.settings;
      packet.cooldown = this.cooldown;
      if (this.data != null) {
         packet.tags = this.data.getTags().keySet().toIntArray();
      }

      return packet;
   }

   /** @deprecated */
   @Nullable
   @Deprecated
   public static RootInteraction getRootInteractionOrUnknown(@Nonnull String id) {
      return (RootInteraction)getAssetMap().getAsset(getRootInteractionIdOrUnknown(id));
   }

   /** @deprecated */
   @Deprecated
   public static int getRootInteractionIdOrUnknown(@Nullable String id) {
      if (id == null) {
         return -2147483648;
      } else {
         IndexedLookupTableAssetMap<String, RootInteraction> assetMap = getAssetMap();
         int interactionId = assetMap.getIndex(id);
         if (interactionId == -2147483648) {
            HytaleLogger.getLogger().at(Level.WARNING).log("Missing root interaction %s", id);
            getAssetStore().loadAssets("Hytale:Hytale", List.of(new RootInteraction(id, new String[0])));
            int index = assetMap.getIndex(id);
            if (index == -2147483648) {
               throw new IllegalArgumentException("Unknown key! " + id);
            }

            interactionId = index;
         }

         return interactionId;
      }
   }

   @Nonnull
   public String toString() {
      String var10000 = this.id;
      return "RootInteraction{id='" + var10000 + "', interactionIds=" + Arrays.toString(this.interactionIds) + ", settings=" + String.valueOf(this.settings) + ", requireNewClick=" + this.requireNewClick + ", clickQueuingTimeout=" + this.clickQueuingTimeout + ", rules=" + String.valueOf(this.rules) + ", operations=" + Arrays.toString(this.operations) + ", needsRemoteSync=" + this.needsRemoteSync + "}";
   }

   static {
      COOLDOWN_CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(InteractionCooldown.class, InteractionCooldown::new).appendInherited(new KeyedCodec("Id", Codec.STRING), (interactionCooldown, s) -> interactionCooldown.cooldownId = s, (interactionCooldown) -> interactionCooldown.cooldownId, (interactionCooldown, parent) -> interactionCooldown.cooldownId = parent.cooldownId).documentation("The Id for the cooldown.\n\nCooldowns can be used on different interactions but share a cooldown.").add()).appendInherited(new KeyedCodec("Cooldown", Codec.FLOAT), (interactionCooldown, s) -> interactionCooldown.cooldown = s, (interactionCooldown) -> interactionCooldown.cooldown, (interactionCooldown, parent) -> interactionCooldown.cooldown = parent.cooldown).addValidator(Validators.greaterThanOrEqual(0.0F)).documentation("The time in seconds this cooldown should last for.").add()).appendInherited(new KeyedCodec("Charges", Codec.FLOAT_ARRAY), (interactionCharges, s) -> interactionCharges.chargeTimes = s, (interactionCharges) -> interactionCharges.chargeTimes, (interactionCharges, parent) -> interactionCharges.chargeTimes = parent.chargeTimes).addValidator(new FloatArrayValidator(Validators.greaterThanOrEqual(0.0F))).documentation("The charge times available for this interaction.").add()).appendInherited(new KeyedCodec("SkipCooldownReset", Codec.BOOLEAN), (interactionCharges, s) -> interactionCharges.skipCooldownReset = s, (interactionCharges) -> interactionCharges.skipCooldownReset, (interactionCharges, parent) -> interactionCharges.skipCooldownReset = parent.skipCooldownReset).documentation("Determines whether resetting cooldown should be skipped.").add()).appendInherited(new KeyedCodec("InterruptRecharge", Codec.BOOLEAN), (interactionCharges, s) -> interactionCharges.interruptRecharge = s, (interactionCharges) -> interactionCharges.interruptRecharge, (interactionCharges, parent) -> interactionCharges.interruptRecharge = parent.interruptRecharge).documentation("Determines whether recharge is interrupted by use.").add()).appendInherited(new KeyedCodec("ClickBypass", Codec.BOOLEAN), (interactionCooldown, s) -> interactionCooldown.clickBypass = s, (interactionCooldown) -> interactionCooldown.clickBypass, (interactionCooldown, parent) -> interactionCooldown.clickBypass = parent.clickBypass).documentation("Whether this cooldown can be bypassed by clicking.").add()).build();
      CODEC = ((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)AssetBuilderCodec.builder(RootInteraction.class, RootInteraction::new, Codec.STRING, (o, i) -> o.id = i, (o) -> o.id, (o, i) -> o.data = i, (o) -> o.data).documentation("A **RootInteraction** serves as an entry point into a set of **Interaction**s.\n\nIn order to start an interaction chain a **RootInteraction** is required.\nA basic **RootInteraction** can simply contain a reference to single interaction within _Interactions_ field which will be the entire chain. More complex cases can configure the other fields.\n\nMost fields configured here apply to all **Interaction**s contained the root and any **Interaction**s they contain as well. Systems that look at tags for interactions may also check the root interaction as well reducing the need to duplicate them on all nested interactions.")).appendInherited(new KeyedCodec("Interactions", Interaction.CHILD_ASSET_CODEC_ARRAY), (o, i) -> o.interactionIds = i, (o) -> o.interactionIds, (o, p) -> o.interactionIds = p.interactionIds).addValidator(Validators.nonNull()).addValidator(Validators.nonEmptyArray()).addValidatorLate(() -> Interaction.VALIDATOR_CACHE.getArrayValidator().late()).documentation("The list of interactions that will be run when starting a chain with this root interaction. Interactions in this list will be run in sequence.").add()).appendInherited(new KeyedCodec("Cooldown", COOLDOWN_CODEC), (o, i) -> o.cooldown = i, (o) -> o.cooldown, (o, p) -> o.cooldown = p.cooldown).documentation("Cooldowns are used to prevent an interaction from running repeatedly too quickly.\n\nDuring a cooldown attempting to run an interaction with the same cooldown id will fail.").add()).appendInherited(new KeyedCodec("Rules", InteractionRules.CODEC), (o, i) -> o.rules = i, (o) -> o.rules, (o, p) -> o.rules = p.rules).documentation("A set of rules that control when this root interaction can run or what interactions this root being active prevents.").addValidator(Validators.nonNull()).add()).appendInherited(new KeyedCodec("Settings", new EnumMapCodec(GameMode.class, ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(RootInteractionSettings.class, RootInteractionSettings::new).appendInherited(new KeyedCodec("Cooldown", COOLDOWN_CODEC), (o, i) -> o.cooldown = i, (o) -> o.cooldown, (o, p) -> o.cooldown = p.cooldown).documentation("Cooldowns are used to prevent an interaction from running repeatedly too quickly.\n\nDuring a cooldown attempting to run an interaction with the same cooldown id will fail.").add()).appendInherited(new KeyedCodec("AllowSkipChainOnClick", Codec.BOOLEAN), (o, i) -> o.allowSkipChainOnClick = i, (o) -> o.allowSkipChainOnClick, (o, p) -> o.allowSkipChainOnClick = p.allowSkipChainOnClick).documentation("Whether to skip the whole interaction chain when another click is sent.").add()).build())), (o, i) -> o.settings = i, (o) -> o.settings, (o, p) -> o.settings = p.settings).documentation("Per a gamemode settings.").add()).appendInherited(new KeyedCodec("ClickQueuingTimeout", Codec.FLOAT), (interaction, s) -> interaction.clickQueuingTimeout = s, (interaction) -> interaction.clickQueuingTimeout, (interaction, parent) -> interaction.clickQueuingTimeout = parent.clickQueuingTimeout).documentation("Controls the amount of time this root interaction can remain in the click queue before being discarded.").add()).appendInherited(new KeyedCodec("RequireNewClick", Codec.BOOLEAN), (o, i) -> o.requireNewClick = i, (o) -> o.requireNewClick, (o, p) -> o.requireNewClick = p.requireNewClick).documentation("Requires the user to click again before running another root interaction of the same type.").add()).build();
      VALIDATOR_CACHE = new ValidatorCache<String>(new AssetKeyValidator(RootInteraction::getAssetStore));
      CHILD_ASSET_CODEC = new ContainedAssetCodec(RootInteraction.class, CODEC);
      CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<String[]>(CHILD_ASSET_CODEC, (x$0) -> new String[x$0]);
      CHILD_ASSET_CODEC_MAP = new MapCodec<String, HashMap<String, String>>(CHILD_ASSET_CODEC, HashMap::new);
   }
}
