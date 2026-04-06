package com.hypixel.hytale.server.core.cosmetics;

import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.schema.config.StringSchema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CosmeticAssetValidator implements Validator<String> {
   private final CosmeticType type;

   public CosmeticAssetValidator(CosmeticType type) {
      this.type = type;
   }

   public void accept(@Nullable String asset, @Nonnull ValidationResults results) {
      if (asset != null) {
         CosmeticRegistry reg = CosmeticsModule.get().getRegistry();
         Map<String, ?> toCheck = reg.getByType(this.type);
         if (!toCheck.containsKey(asset)) {
            String var10001 = String.valueOf(this.type);
            results.fail("Cosmetic Asset (" + var10001 + ") '" + asset + "' doesn't exist!");
         }

      }
   }

   public void updateSchema(SchemaContext context, @Nonnull Schema target) {
      ((StringSchema)target).setHytaleCosmeticAsset(EnumCodec.EnumStyle.LEGACY.formatCamelCase(this.type.name()));
   }
}
