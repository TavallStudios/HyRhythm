package com.hypixel.hytale.server.core.cosmetics;

import com.hypixel.hytale.common.plugin.PluginManifest;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.common.util.RandomUtil;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Options;
import com.hypixel.hytale.server.core.asset.AssetModule;
import com.hypixel.hytale.server.core.asset.LoadAssetEvent;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelAsset;
import com.hypixel.hytale.server.core.cosmetics.commands.EmoteCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CosmeticsModule extends JavaPlugin {
   public static final PluginManifest MANIFEST = PluginManifest.corePlugin(CosmeticsModule.class).build();
   private static CosmeticsModule INSTANCE;
   private CosmeticRegistry registry;

   public CosmeticsModule(@Nonnull JavaPluginInit init) {
      super(init);
      INSTANCE = this;
   }

   protected void setup() {
      this.registry = new CosmeticRegistry(AssetModule.get().getBaseAssetPack());
      this.getCommandRegistry().registerCommand(new EmoteCommand());
      if (Options.getOptionSet().has(Options.VALIDATE_ASSETS)) {
         this.getEventRegistry().register((short)64, LoadAssetEvent.class, this::validateGeneratedSkin);
      }

   }

   public CosmeticRegistry getRegistry() {
      return this.registry;
   }

   private void validateGeneratedSkin(@Nonnull LoadAssetEvent eventType) {
      for(int i = 0; i < 10; ++i) {
         com.hypixel.hytale.protocol.PlayerSkin skin = this.generateRandomSkin(new Random((long)i));

         try {
            this.validateSkin(skin);
         } catch (InvalidSkinException e) {
            eventType.failed(true, e.getMessage());
            return;
         }
      }

   }

   @Nullable
   public Model createRandomModel(@Nonnull Random random) {
      com.hypixel.hytale.protocol.PlayerSkin skin = this.generateRandomSkin(random);
      return get().createModel(skin);
   }

   @Nullable
   public Model createModel(@Nonnull com.hypixel.hytale.protocol.PlayerSkin skin) {
      return this.createModel(skin, 1.0F);
   }

   @Nullable
   public Model createModel(@Nonnull com.hypixel.hytale.protocol.PlayerSkin skin, float scale) {
      try {
         this.validateSkin(skin);
      } catch (InvalidSkinException e) {
         ((HytaleLogger.Api)this.getLogger().at(Level.WARNING).withCause(e)).log("Was passed an invalid skin %s", skin);
         return null;
      }

      ModelAsset modelAsset = (ModelAsset)ModelAsset.getAssetMap().getAsset("Player");
      return Model.createScaledModel(modelAsset, scale, (Map)null);
   }

   public void validateSkin(@Nonnull com.hypixel.hytale.protocol.PlayerSkin skin) throws InvalidSkinException {
      if (skin == null) {
         throw new InvalidSkinException("skin", (String)null);
      } else if (skin.face != null && this.registry.getFaces().containsKey(skin.face)) {
         if (skin.ears != null && this.registry.getEars().containsKey(skin.ears)) {
            if (skin.mouth != null && this.registry.getMouths().containsKey(skin.mouth)) {
               if (!this.isValidAttachment(this.registry.getBodyCharacteristics(), skin.bodyCharacteristic, true)) {
                  throw new InvalidSkinException("body", skin.bodyCharacteristic);
               } else if (!this.isValidAttachment(this.registry.getUnderwear(), skin.underwear, true)) {
                  throw new InvalidSkinException("underwear", skin.underwear);
               } else if (!this.isValidAttachment(this.registry.getEyes(), skin.eyes, true)) {
                  throw new InvalidSkinException("eyes", skin.eyes);
               } else if (!this.isValidAttachment(this.registry.getSkinFeatures(), skin.skinFeature)) {
                  throw new InvalidSkinException("skin feature", skin.skinFeature);
               } else if (!this.isValidAttachment(this.registry.getEyebrows(), skin.eyebrows)) {
                  throw new InvalidSkinException("eyebrows", skin.eyebrows);
               } else if (!this.isValidAttachment(this.registry.getPants(), skin.pants)) {
                  throw new InvalidSkinException("pants", skin.pants);
               } else if (!this.isValidAttachment(this.registry.getOverpants(), skin.overpants)) {
                  throw new InvalidSkinException("overpants", skin.overpants);
               } else if (!this.isValidAttachment(this.registry.getShoes(), skin.shoes)) {
                  throw new InvalidSkinException("shoes", skin.shoes);
               } else if (!this.isValidAttachment(this.registry.getUndertops(), skin.undertop)) {
                  throw new InvalidSkinException("undertop", skin.undertop);
               } else if (!this.isValidAttachment(this.registry.getOvertops(), skin.overtop)) {
                  throw new InvalidSkinException("overtop", skin.overtop);
               } else if (!this.isValidAttachment(this.registry.getGloves(), skin.gloves)) {
                  throw new InvalidSkinException("gloves", skin.gloves);
               } else if (!this.isValidAttachment(this.registry.getHeadAccessories(), skin.headAccessory)) {
                  throw new InvalidSkinException("head accessory", skin.headAccessory);
               } else if (!this.isValidAttachment(this.registry.getFaceAccessories(), skin.faceAccessory)) {
                  throw new InvalidSkinException("face accessory", skin.faceAccessory);
               } else if (!this.isValidAttachment(this.registry.getEarAccessories(), skin.earAccessory)) {
                  throw new InvalidSkinException("ear accessory", skin.earAccessory);
               } else if (!this.isValidHaircutAttachment(skin.haircut, skin.headAccessory)) {
                  throw new InvalidSkinException("haircut", skin.haircut);
               } else if (!this.isValidAttachment(this.registry.getFacialHairs(), skin.facialHair)) {
                  throw new InvalidSkinException("facial hair", skin.facialHair);
               } else if (!this.isValidAttachment(this.registry.getCapes(), skin.cape)) {
                  throw new InvalidSkinException("cape", skin.cape);
               }
            } else {
               throw new InvalidSkinException("mouth", skin.mouth);
            }
         } else {
            throw new InvalidSkinException("ears", skin.ears);
         }
      } else {
         throw new InvalidSkinException("face", skin.face);
      }
   }

   private boolean isValidAttachment(@Nonnull Map<String, PlayerSkinPart> map, String id) {
      return this.isValidAttachment(map, id, false);
   }

   private boolean isValidTexture(@Nonnull PlayerSkinPart part, String variantId, String textureId) {
      if (part.getGradientSet() != null && ((PlayerSkinGradientSet)this.registry.getGradientSets().get(part.getGradientSet())).getGradients().containsKey(textureId)) {
         return true;
      } else {
         return part.getVariants() != null ? ((PlayerSkinPart.Variant)part.getVariants().get(variantId)).getTextures().containsKey(textureId) : part.getTextures().containsKey(textureId);
      }
   }

   private boolean isValidAttachment(@Nonnull Map<String, PlayerSkinPart> map, @Nullable String id, boolean required) {
      if (id == null) {
         return !required;
      } else {
         String[] idParts = id.split("\\.");
         PlayerSkinPart skinPart = (PlayerSkinPart)map.get(idParts[0]);
         if (skinPart == null) {
            return false;
         } else {
            String variantId = idParts.length > 2 && !idParts[2].isEmpty() ? idParts[2] : null;
            return skinPart.getVariants() != null && !skinPart.getVariants().containsKey(variantId) ? false : this.isValidTexture(skinPart, variantId, idParts[1]);
         }
      }
   }

   private boolean isValidHaircutAttachment(@Nullable String haircutId, @Nullable String headAccessoryId) {
      if (haircutId == null) {
         return true;
      } else {
         Map<String, PlayerSkinPart> haircuts = this.registry.getHaircuts();
         String[] idParts = haircutId.split("\\.");
         String haircutAssetId = idParts[0];
         String haircutAssetTextureId = idParts.length > 1 && !idParts[1].isEmpty() ? idParts[1] : null;
         if (headAccessoryId != null) {
            idParts = headAccessoryId.split("\\.");
            String headAccessoryAssetId = idParts[0];
            PlayerSkinPart headAccessoryPart = (PlayerSkinPart)this.registry.getHeadAccessories().get(headAccessoryAssetId);
            if (headAccessoryPart != null) {
               switch (headAccessoryPart.getHeadAccessoryType()) {
                  case HalfCovering:
                     PlayerSkinPart haircutPart = (PlayerSkinPart)haircuts.get(haircutAssetId);
                     if (haircutPart == null) {
                        return false;
                     }

                     if (haircutPart.doesRequireGenericHaircut()) {
                        PlayerSkinPart baseHaircutPart = (PlayerSkinPart)haircuts.get("Generic" + String.valueOf(haircutPart.getHairType()));
                        return this.isValidAttachment(haircuts, baseHaircutPart.getId() + "." + haircutAssetTextureId, false);
                     }
                     break;
                  case FullyCovering:
                     return this.isValidAttachment(haircuts, haircutId);
               }
            }
         }

         return this.isValidAttachment(haircuts, haircutId);
      }
   }

   public static CosmeticsModule get() {
      return INSTANCE;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.PlayerSkin generateRandomSkin(@Nonnull Random random) {
      String bodyCharacteristic = this.randomSkinPart(this.registry.getBodyCharacteristics(), true, random);
      String underwear = this.randomSkinPart(this.registry.getUnderwear(), true, random);
      String face = this.randomSkinPart(this.registry.getFaces(), true, false, random);
      String ears = this.randomSkinPart(this.registry.getEars(), true, false, random);
      String mouth = this.randomSkinPart(this.registry.getMouths(), true, false, random);
      String eyes = this.randomSkinPart(this.registry.getEyes(), true, random);
      String facialHair = null;
      if (random.nextInt(10) > 4) {
         facialHair = this.randomSkinPart(this.registry.getFacialHairs(), random);
      }

      String haircut = this.randomSkinPart(this.registry.getHaircuts(), random);
      String eyebrows = this.randomSkinPart(this.registry.getEyebrows(), random);
      String pants = this.randomSkinPart(this.registry.getPants(), random);
      String overpants = null;
      String undertop = this.randomSkinPart(this.registry.getUndertops(), random);
      String overtop = this.randomSkinPart(this.registry.getOvertops(), random);
      String shoes = this.randomSkinPart(this.registry.getShoes(), random);
      String headAccessory = null;
      if (random.nextInt(10) > 8) {
         headAccessory = this.randomSkinPart(this.registry.getHeadAccessories(), random);
      }

      String faceAccessory = null;
      if (random.nextInt(10) > 8) {
         faceAccessory = this.randomSkinPart(this.registry.getFaceAccessories(), random);
      }

      String earAccessory = null;
      if (random.nextInt(10) > 8) {
         earAccessory = this.randomSkinPart(this.registry.getEarAccessories(), random);
      }

      String skinFeature = null;
      if (random.nextInt(10) > 8) {
         skinFeature = this.randomSkinPart(this.registry.getSkinFeatures(), random);
      }

      String gloves = null;
      return new com.hypixel.hytale.protocol.PlayerSkin(bodyCharacteristic, underwear, face, eyes, ears, mouth, facialHair, haircut, eyebrows, pants, overpants, undertop, overtop, shoes, headAccessory, faceAccessory, earAccessory, skinFeature, gloves, (String)null);
   }

   @Nullable
   private String randomSkinPart(@Nonnull Map<String, PlayerSkinPart> map, @Nonnull Random random) {
      return this.randomSkinPart(map, false, random);
   }

   @Nullable
   private String randomSkinPart(@Nonnull Map<String, PlayerSkinPart> map, boolean required, @Nonnull Random random) {
      return this.randomSkinPart(map, required, true, random);
   }

   @Nullable
   private String randomSkinPart(@Nonnull Map<String, PlayerSkinPart> map, boolean required, boolean color, @Nonnull Random random) {
      PlayerSkinPart[] arr = (PlayerSkinPart[])map.values().toArray((x$0) -> new PlayerSkinPart[x$0]);
      PlayerSkinPart part = required ? (PlayerSkinPart)RandomUtil.selectRandom(arr, random) : (PlayerSkinPart)RandomUtil.selectRandomOrNull(arr, random);
      if (part == null) {
         return null;
      } else if (!color) {
         return part.getId();
      } else {
         String[] colors = ArrayUtil.EMPTY_STRING_ARRAY;
         if (part.getGradientSet() != null) {
            colors = (String[])((PlayerSkinGradientSet)this.registry.getGradientSets().get(part.getGradientSet())).getGradients().keySet().toArray((x$0) -> new String[x$0]);
         }

         Map<String, PlayerSkinPartTexture> textures = part.getTextures();
         String variantId = null;
         if (part.getVariants() != null) {
            variantId = (String)RandomUtil.selectRandom((String[])part.getVariants().keySet().toArray((x$0) -> new String[x$0]), random);
            textures = ((PlayerSkinPart.Variant)part.getVariants().get(variantId)).getTextures();
         }

         if (textures != null) {
            colors = (String[])ArrayUtil.combine(colors, (String[])textures.keySet().toArray((x$0) -> new String[x$0]));
         }

         String colorId = (String)RandomUtil.selectRandom(colors, random);
         if (variantId == null) {
            String var10000 = part.getId();
            return var10000 + "." + colorId;
         } else {
            return part.getId() + "." + colorId + "." + variantId;
         }
      }
   }

   public static class InvalidSkinException extends Exception {
      private final String partType;
      private final String partId;

      public InvalidSkinException(String partType, @Nullable String partId) {
         super(formatMessage(partType, partId));
         this.partType = partType;
         this.partId = partId;
      }

      private static String formatMessage(String partType, @Nullable String partId) {
         return partId == null ? "Missing required " + partType : "Unknown " + partType + ": " + partId;
      }

      public String getPartType() {
         return this.partType;
      }

      @Nullable
      public String getPartId() {
         return this.partId;
      }
   }
}
