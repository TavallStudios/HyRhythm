package com.hypixel.hytale.server.npc.asset.builder;

import com.google.gson.JsonElement;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderTemplateInteractionVars extends BuilderCodecObjectHelper<Map<String, String>> {
   public BuilderTemplateInteractionVars() {
      super(RootInteraction.class, RootInteraction.CHILD_ASSET_CODEC_MAP, (Validator)null);
   }

   public Map<String, String> build() {
      throw new UnsupportedOperationException();
   }

   public void readConfig(@Nonnull JsonElement data, @Nonnull ExtraInfo extraInfo) {
      super.readConfig(data, extraInfo);
   }

   @Nullable
   public Map<String, String> build(@Nonnull ExecutionContext context) {
      Map<String, String> override = context.getInteractionVars();
      return override != null ? override : (Map)this.value;
   }
}
