package com.hypixel.hytale.server.core.asset.type.entityeffect.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.protocol.AnimationSet;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ModelOverride implements NetworkSerializable<com.hypixel.hytale.protocol.ModelOverride> {
   @Nonnull
   public static final BuilderCodec<ModelOverride> CODEC;
   @Nullable
   protected String model;
   @Nullable
   protected String texture;
   @Nonnull
   protected Map<String, ModelAsset.AnimationSet> animationSetMap = Collections.emptyMap();

   protected ModelOverride() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.ModelOverride toPacket() {
      com.hypixel.hytale.protocol.ModelOverride packet = new com.hypixel.hytale.protocol.ModelOverride();
      packet.model = this.model;
      packet.texture = this.texture;
      if (!this.animationSetMap.isEmpty()) {
         Map<String, AnimationSet> map = new Object2ObjectOpenHashMap(this.animationSetMap.size());

         for(Map.Entry<String, ModelAsset.AnimationSet> entry : this.animationSetMap.entrySet()) {
            map.put((String)entry.getKey(), ((ModelAsset.AnimationSet)entry.getValue()).toPacket((String)entry.getKey()));
         }

         packet.animationSets = map;
      }

      return packet;
   }

   @Nonnull
   public String toString() {
      String var10000 = this.model;
      return "ModelOverride{model='" + var10000 + "', texture='" + this.texture + "', animationSetMap=" + String.valueOf(this.animationSetMap) + "}";
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ModelOverride.class, ModelOverride::new).appendInherited(new KeyedCodec("Model", Codec.STRING, true), (modelOverride, s) -> modelOverride.model = s, (modelOverride) -> modelOverride.model, (modelOverride, parent) -> modelOverride.model = parent.model).addValidator(CommonAssetValidator.MODEL_CHARACTER).add()).appendInherited(new KeyedCodec("Texture", Codec.STRING, true), (modelOverride, s) -> modelOverride.texture = s, (modelOverride) -> modelOverride.texture, (modelOverride, parent) -> modelOverride.texture = parent.texture).addValidator(CommonAssetValidator.TEXTURE_CHARACTER).add()).appendInherited(new KeyedCodec("AnimationSets", new MapCodec(ModelAsset.AnimationSet.CODEC, HashMap::new)), (modelOverride, m) -> modelOverride.animationSetMap = m, (modelOverride) -> modelOverride.animationSetMap, (modelOverride, parent) -> modelOverride.animationSetMap = parent.animationSetMap).add()).build();
   }
}
