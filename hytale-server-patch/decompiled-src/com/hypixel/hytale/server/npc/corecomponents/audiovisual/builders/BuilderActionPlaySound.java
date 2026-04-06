package com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.SoundEventExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.ActionPlaySound;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import javax.annotation.Nonnull;

public class BuilderActionPlaySound extends BuilderActionBase {
   protected final AssetHolder soundEventId = new AssetHolder();

   @Nonnull
   public ActionPlaySound build(@Nonnull BuilderSupport builderSupport) {
      return new ActionPlaySound(this, builderSupport);
   }

   @Nonnull
   public String getShortDescription() {
      return "Plays a sound to players within a specified range.";
   }

   @Nonnull
   public String getLongDescription() {
      return "Plays a sound to players within a specified range.";
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionPlaySound readConfig(@Nonnull JsonElement data) {
      this.requireAsset(data, "SoundEventId", this.soundEventId, SoundEventExistsValidator.required(), BuilderDescriptorState.Stable, "The sound event to play", (String)null);
      return this;
   }

   public String getSoundEventId(@Nonnull BuilderSupport support) {
      return this.soundEventId.get(support.getExecutionContext());
   }

   public int getSoundEventIndex(@Nonnull BuilderSupport support) {
      String key = this.soundEventId.get(support.getExecutionContext());
      int index = SoundEvent.getAssetMap().getIndex(key);
      if (index == -2147483648) {
         throw new IllegalArgumentException("Unknown key! " + key);
      } else {
         return index;
      }
   }
}
