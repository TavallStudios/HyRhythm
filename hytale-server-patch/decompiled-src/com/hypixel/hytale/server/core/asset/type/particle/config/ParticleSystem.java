package com.hypixel.hytale.server.core.asset.type.particle.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.schema.metadata.ui.UIDefaultCollapsedState;
import com.hypixel.hytale.codec.schema.metadata.ui.UITypeIcon;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.lang.ref.SoftReference;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class ParticleSystem implements JsonAssetWithMap<String, DefaultAssetMap<String, ParticleSystem>>, NetworkSerializable<com.hypixel.hytale.protocol.ParticleSystem> {
   public static final AssetBuilderCodec<String, ParticleSystem> CODEC;
   public static final ValidatorCache<String> VALIDATOR_CACHE;
   private static AssetStore<String, ParticleSystem, DefaultAssetMap<String, ParticleSystem>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected float lifeSpan;
   protected ParticleSpawnerGroup[] spawners;
   protected float cullDistance;
   protected float boundingRadius;
   protected boolean isImportant;
   private SoftReference<com.hypixel.hytale.protocol.ParticleSystem> cachedPacket;

   public static AssetStore<String, ParticleSystem, DefaultAssetMap<String, ParticleSystem>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.<String, ParticleSystem, DefaultAssetMap<String, ParticleSystem>>getAssetStore(ParticleSystem.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, ParticleSystem> getAssetMap() {
      return (DefaultAssetMap)getAssetStore().getAssetMap();
   }

   public ParticleSystem(String id, float lifeSpan, ParticleSpawnerGroup[] spawners, float cullDistance, float boundingRadius, boolean isImportant) {
      this.id = id;
      this.lifeSpan = lifeSpan;
      this.spawners = spawners;
      this.cullDistance = cullDistance;
      this.boundingRadius = boundingRadius;
      this.isImportant = isImportant;
   }

   protected ParticleSystem() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ParticleSystem toPacket() {
      com.hypixel.hytale.protocol.ParticleSystem cached = this.cachedPacket == null ? null : (com.hypixel.hytale.protocol.ParticleSystem)this.cachedPacket.get();
      if (cached != null) {
         return cached;
      } else {
         com.hypixel.hytale.protocol.ParticleSystem packet = new com.hypixel.hytale.protocol.ParticleSystem();
         packet.id = this.id;
         packet.lifeSpan = this.lifeSpan;
         if (this.spawners != null && this.spawners.length > 0) {
            packet.spawners = (com.hypixel.hytale.protocol.ParticleSpawnerGroup[])ArrayUtil.copyAndMutate(this.spawners, ParticleSpawnerGroup::toPacket, (x$0) -> new com.hypixel.hytale.protocol.ParticleSpawnerGroup[x$0]);
         }

         packet.cullDistance = this.cullDistance;
         packet.boundingRadius = this.boundingRadius;
         packet.isImportant = this.isImportant;
         this.cachedPacket = new SoftReference(packet);
         return packet;
      }
   }

   public String getId() {
      return this.id;
   }

   public float getLifeSpan() {
      return this.lifeSpan;
   }

   public ParticleSpawnerGroup[] getSpawners() {
      return this.spawners;
   }

   public float getCullDistance() {
      return this.cullDistance;
   }

   public float getBoundingRadius() {
      return this.boundingRadius;
   }

   public boolean isImportant() {
      return this.isImportant;
   }

   @Nonnull
   public String toString() {
      String var10000 = this.id;
      return "ParticleSystem{id='" + var10000 + "', lifeSpan=" + this.lifeSpan + ", spawners=" + Arrays.toString(this.spawners) + ", cullDistance=" + this.cullDistance + ", boundingRadius=" + this.boundingRadius + ", isImportant=" + this.isImportant + "}";
   }

   static {
      CODEC = ((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)((AssetBuilderCodec.Builder)AssetBuilderCodec.builder(ParticleSystem.class, ParticleSystem::new, Codec.STRING, (particleSystem, s) -> particleSystem.id = s, (particleSystem) -> particleSystem.id, (asset, data) -> asset.data = data, (asset) -> asset.data).metadata(new UITypeIcon("ParticleSystem.png"))).appendInherited(new KeyedCodec("Spawners", new ArrayCodec(ParticleSpawnerGroup.CODEC, (x$0) -> new ParticleSpawnerGroup[x$0])), (particleSystem, o) -> particleSystem.spawners = o, (particleSystem) -> particleSystem.spawners, (particleSystem, parent) -> particleSystem.spawners = parent.spawners).metadata(UIDefaultCollapsedState.UNCOLLAPSED).addValidator(Validators.nonEmptyArray()).add()).appendInherited(new KeyedCodec("LifeSpan", Codec.FLOAT), (particleSystem, f) -> particleSystem.lifeSpan = f, (particleSystem) -> particleSystem.lifeSpan, (particleSystem, parent) -> particleSystem.lifeSpan = parent.lifeSpan).add()).appendInherited(new KeyedCodec("CullDistance", Codec.FLOAT), (particleSystem, f) -> particleSystem.cullDistance = f, (particleSystem) -> particleSystem.cullDistance, (particleSystem, parent) -> particleSystem.cullDistance = parent.cullDistance).add()).appendInherited(new KeyedCodec("BoundingRadius", Codec.FLOAT), (particleSystem, f) -> particleSystem.boundingRadius = f, (particleSystem) -> particleSystem.boundingRadius, (particleSystem, parent) -> particleSystem.boundingRadius = parent.boundingRadius).add()).appendInherited(new KeyedCodec("IsImportant", Codec.BOOLEAN), (particleSystem, b) -> particleSystem.isImportant = b, (particleSystem) -> particleSystem.isImportant, (particleSystem, parent) -> particleSystem.isImportant = parent.isImportant).add()).build();
      VALIDATOR_CACHE = new ValidatorCache<String>(new AssetKeyValidator(ParticleSystem::getAssetStore));
   }
}
