package com.hypixel.hytale.builtin.adventure.shop;

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
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.server.core.entity.entities.player.pages.choices.ChoiceElement;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class ShopAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, ShopAsset>> {
   @Nonnull
   public static final AssetBuilderCodec<String, ShopAsset> CODEC;
   @Nonnull
   public static final ValidatorCache<String> VALIDATOR_CACHE;
   private static AssetStore<String, ShopAsset, DefaultAssetMap<String, ShopAsset>> ASSET_STORE;
   protected AssetExtraInfo.Data extraData;
   protected String id;
   protected ChoiceElement[] elements;

   @Nonnull
   public static AssetStore<String, ShopAsset, DefaultAssetMap<String, ShopAsset>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.<String, ShopAsset, DefaultAssetMap<String, ShopAsset>>getAssetStore(ShopAsset.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, ShopAsset> getAssetMap() {
      return (DefaultAssetMap)getAssetStore().getAssetMap();
   }

   public ShopAsset(String id, ChoiceElement[] elements) {
      this.id = id;
      this.elements = elements;
   }

   protected ShopAsset() {
   }

   public String getId() {
      return this.id;
   }

   public ChoiceElement[] getElements() {
      return this.elements;
   }

   @Nonnull
   public String toString() {
      String var10000 = this.id;
      return "ShopAsset{id='" + var10000 + "', elements=" + Arrays.toString(this.elements) + "}";
   }

   static {
      CODEC = ((AssetBuilderCodec.Builder)AssetBuilderCodec.builder(ShopAsset.class, ShopAsset::new, Codec.STRING, (shopAsset, s) -> shopAsset.id = s, (shopAsset) -> shopAsset.id, (shopAsset, data) -> shopAsset.extraData = data, (shopAsset) -> shopAsset.extraData).addField(new KeyedCodec("Content", new ArrayCodec(ChoiceElement.CODEC, (x$0) -> new ChoiceElement[x$0])), (shopAsset, choiceElements) -> shopAsset.elements = choiceElements, (shopAsset) -> shopAsset.elements)).build();
      VALIDATOR_CACHE = new ValidatorCache<String>(new AssetKeyValidator(ShopAsset::getAssetStore));
   }
}
