package com.hypixel.hytale.server.core.cosmetics;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public class PlayerSkin {
   private final PlayerSkinPartId bodyCharacteristic;
   private final PlayerSkinPartId underwear;
   private final String face;
   private final String ears;
   private final String mouth;
   @Nullable
   private final PlayerSkinPartId eyes;
   @Nullable
   private final PlayerSkinPartId facialHair;
   @Nullable
   private final PlayerSkinPartId haircut;
   @Nullable
   private final PlayerSkinPartId eyebrows;
   @Nullable
   private final PlayerSkinPartId pants;
   @Nullable
   private final PlayerSkinPartId overpants;
   @Nullable
   private final PlayerSkinPartId undertop;
   @Nullable
   private final PlayerSkinPartId overtop;
   @Nullable
   private final PlayerSkinPartId shoes;
   @Nullable
   private final PlayerSkinPartId headAccessory;
   @Nullable
   private final PlayerSkinPartId faceAccessory;
   @Nullable
   private final PlayerSkinPartId earAccessory;
   @Nullable
   private final PlayerSkinPartId skinFeature;
   @Nullable
   private final PlayerSkinPartId gloves;
   @Nullable
   private final PlayerSkinPartId cape;

   public PlayerSkin(@Nonnull BsonDocument doc) {
      this.bodyCharacteristic = getId(doc, "bodyCharacteristic");
      this.underwear = getId(doc, "underwear");
      this.face = doc.getString("face").getValue();
      this.ears = doc.getString("ears").getValue();
      this.mouth = doc.getString("mouth").getValue();
      this.eyes = PlayerSkin.PlayerSkinPartId.fromString(doc.getString("eyes").getValue());
      this.facialHair = getId(doc, "facialHair");
      this.haircut = getId(doc, "haircut");
      this.eyebrows = getId(doc, "eyebrows");
      this.pants = getId(doc, "pants");
      this.overpants = getId(doc, "overpants");
      this.undertop = getId(doc, "undertop");
      this.overtop = getId(doc, "overtop");
      this.shoes = getId(doc, "shoes");
      this.headAccessory = getId(doc, "headAccessory");
      this.faceAccessory = getId(doc, "faceAccessory");
      this.earAccessory = getId(doc, "earAccessory");
      this.skinFeature = getId(doc, "skinFeature");
      this.gloves = getId(doc, "gloves");
      this.cape = getId(doc, "cape");
   }

   public PlayerSkin(PlayerSkinPartId bodyCharacteristic, PlayerSkinPartId underwear, String face, String ears, String mouth, PlayerSkinPartId eyes, PlayerSkinPartId facialHair, PlayerSkinPartId haircut, PlayerSkinPartId eyebrows, PlayerSkinPartId pants, PlayerSkinPartId overpants, PlayerSkinPartId undertop, PlayerSkinPartId overtop, PlayerSkinPartId shoes, PlayerSkinPartId headAccessory, PlayerSkinPartId faceAccessory, PlayerSkinPartId earAccessory, PlayerSkinPartId skinFeature, PlayerSkinPartId gloves, PlayerSkinPartId cape) {
      this.bodyCharacteristic = bodyCharacteristic;
      this.underwear = underwear;
      this.face = face;
      this.ears = ears;
      this.mouth = mouth;
      this.eyes = eyes;
      this.facialHair = facialHair;
      this.haircut = haircut;
      this.eyebrows = eyebrows;
      this.pants = pants;
      this.overpants = overpants;
      this.undertop = undertop;
      this.overtop = overtop;
      this.shoes = shoes;
      this.headAccessory = headAccessory;
      this.faceAccessory = faceAccessory;
      this.earAccessory = earAccessory;
      this.skinFeature = skinFeature;
      this.gloves = gloves;
      this.cape = cape;
   }

   public PlayerSkin(String bodyCharacteristic, String underwear, String face, String ears, String mouth, @Nullable String eyes, @Nullable String facialHair, @Nullable String haircut, @Nullable String eyebrows, @Nullable String pants, @Nullable String overpants, @Nullable String undertop, @Nullable String overtop, @Nullable String shoes, @Nullable String headAccessory, @Nullable String faceAccessory, @Nullable String earAccessory, @Nullable String skinFeature, @Nullable String gloves, @Nullable String cape) {
      this.bodyCharacteristic = bodyCharacteristic != null ? PlayerSkin.PlayerSkinPartId.fromString(bodyCharacteristic) : null;
      this.underwear = underwear != null ? PlayerSkin.PlayerSkinPartId.fromString(underwear) : null;
      this.face = face;
      this.ears = ears;
      this.mouth = mouth;
      this.eyes = eyes != null ? PlayerSkin.PlayerSkinPartId.fromString(eyes) : null;
      this.facialHair = facialHair != null ? PlayerSkin.PlayerSkinPartId.fromString(facialHair) : null;
      this.haircut = haircut != null ? PlayerSkin.PlayerSkinPartId.fromString(haircut) : null;
      this.eyebrows = eyebrows != null ? PlayerSkin.PlayerSkinPartId.fromString(eyebrows) : null;
      this.pants = pants != null ? PlayerSkin.PlayerSkinPartId.fromString(pants) : null;
      this.overpants = overpants != null ? PlayerSkin.PlayerSkinPartId.fromString(overpants) : null;
      this.undertop = undertop != null ? PlayerSkin.PlayerSkinPartId.fromString(undertop) : null;
      this.overtop = overtop != null ? PlayerSkin.PlayerSkinPartId.fromString(overtop) : null;
      this.shoes = shoes != null ? PlayerSkin.PlayerSkinPartId.fromString(shoes) : null;
      this.headAccessory = headAccessory != null ? PlayerSkin.PlayerSkinPartId.fromString(headAccessory) : null;
      this.faceAccessory = faceAccessory != null ? PlayerSkin.PlayerSkinPartId.fromString(faceAccessory) : null;
      this.earAccessory = earAccessory != null ? PlayerSkin.PlayerSkinPartId.fromString(earAccessory) : null;
      this.skinFeature = skinFeature != null ? PlayerSkin.PlayerSkinPartId.fromString(skinFeature) : null;
      this.gloves = gloves != null ? PlayerSkin.PlayerSkinPartId.fromString(gloves) : null;
      this.cape = cape != null ? PlayerSkin.PlayerSkinPartId.fromString(cape) : null;
   }

   @Nullable
   private static PlayerSkinPartId getId(@Nonnull BsonDocument doc, String key) {
      BsonValue bsonValue = doc.get(key);
      return bsonValue != null && !bsonValue.isNull() ? PlayerSkin.PlayerSkinPartId.fromString(bsonValue.asString().getValue()) : null;
   }

   public PlayerSkinPartId getBodyCharacteristic() {
      return this.bodyCharacteristic;
   }

   public PlayerSkinPartId getUnderwear() {
      return this.underwear;
   }

   public String getFace() {
      return this.face;
   }

   @Nullable
   public PlayerSkinPartId getEyes() {
      return this.eyes;
   }

   @Nonnull
   public String getEars() {
      return this.ears;
   }

   @Nonnull
   public String getMouth() {
      return this.mouth;
   }

   @Nullable
   public PlayerSkinPartId getFacialHair() {
      return this.facialHair;
   }

   @Nullable
   public PlayerSkinPartId getHaircut() {
      return this.haircut;
   }

   @Nullable
   public PlayerSkinPartId getEyebrows() {
      return this.eyebrows;
   }

   @Nullable
   public PlayerSkinPartId getPants() {
      return this.pants;
   }

   @Nullable
   public PlayerSkinPartId getOverpants() {
      return this.overpants;
   }

   @Nullable
   public PlayerSkinPartId getUndertop() {
      return this.undertop;
   }

   @Nullable
   public PlayerSkinPartId getOvertop() {
      return this.overtop;
   }

   @Nullable
   public PlayerSkinPartId getShoes() {
      return this.shoes;
   }

   @Nullable
   public PlayerSkinPartId getHeadAccessory() {
      return this.headAccessory;
   }

   @Nullable
   public PlayerSkinPartId getFaceAccessory() {
      return this.faceAccessory;
   }

   @Nullable
   public PlayerSkinPartId getEarAccessory() {
      return this.earAccessory;
   }

   @Nullable
   public PlayerSkinPartId getSkinFeature() {
      return this.skinFeature;
   }

   @Nullable
   public PlayerSkinPartId getGloves() {
      return this.gloves;
   }

   @Nullable
   public PlayerSkinPartId getCape() {
      return this.cape;
   }

   public static class PlayerSkinPartId {
      public final String assetId;
      public final String textureId;
      public final String variantId;

      public PlayerSkinPartId(String assetId, String textureId, String variantId) {
         this.assetId = assetId;
         this.textureId = textureId;
         this.variantId = variantId;
      }

      @Nonnull
      public static PlayerSkinPartId fromString(@Nonnull String stringId) {
         String[] idParts = stringId.split("\\.");
         return new PlayerSkinPartId(idParts[0], idParts.length > 1 ? idParts[1] : null, idParts.length > 2 ? idParts[2] : null);
      }

      public String getAssetId() {
         return this.assetId;
      }

      public String getTextureId() {
         return this.textureId;
      }

      public String getVariantId() {
         return this.variantId;
      }

      @Nonnull
      public String toString() {
         return "CharacterPartId{assetId='" + this.assetId + "', textureId='" + this.textureId + "', variantId='" + this.variantId + "'}";
      }
   }
}
