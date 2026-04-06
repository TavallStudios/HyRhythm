package com.hypixel.hytale.server.npc.asset.builder.validators;

import com.hypixel.hytale.builtin.tagset.config.NPCGroup;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class TagSetExistsValidator extends AssetValidator {
   private static final TagSetExistsValidator DEFAULT_INSTANCE = new TagSetExistsValidator();

   private TagSetExistsValidator() {
   }

   private TagSetExistsValidator(EnumSet<AssetValidator.Config> config) {
      super(config);
   }

   @Nonnull
   public String getDomain() {
      return "TagSet";
   }

   public boolean test(String value) {
      return NPCGroup.getAssetMap().getIndex(value) != -2147483648;
   }

   @Nonnull
   public String errorMessage(String value, String attribute) {
      return "The NPC group tag set with the name \"" + value + "\" does not exist in attribute \"" + attribute + "\"";
   }

   @Nonnull
   public String getAssetName() {
      return NPCGroup.class.getSimpleName();
   }

   public static TagSetExistsValidator required() {
      return DEFAULT_INSTANCE;
   }

   @Nonnull
   public static TagSetExistsValidator withConfig(EnumSet<AssetValidator.Config> config) {
      return new TagSetExistsValidator(config);
   }
}
