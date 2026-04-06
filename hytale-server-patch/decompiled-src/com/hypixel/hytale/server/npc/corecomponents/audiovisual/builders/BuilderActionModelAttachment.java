package com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringValidator;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.ActionModelAttachment;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import javax.annotation.Nonnull;

public class BuilderActionModelAttachment extends BuilderActionBase {
   protected final StringHolder slot = new StringHolder();
   protected final StringHolder attachment = new StringHolder();

   @Nonnull
   public ActionModelAttachment build(@Nonnull BuilderSupport builderSupport) {
      return new ActionModelAttachment(this, builderSupport);
   }

   @Nonnull
   public BuilderActionModelAttachment readConfig(@Nonnull JsonElement data) {
      this.requireString(data, "Slot", this.slot, StringNotEmptyValidator.get(), BuilderDescriptorState.Stable, "The attachment slot to set", (String)null);
      this.requireString(data, "Attachment", this.attachment, (StringValidator)null, BuilderDescriptorState.Stable, "The attachment to set, or empty to remove", (String)null);
      return this;
   }

   @Nonnull
   public String getShortDescription() {
      return "Set an attachment on the current NPC model";
   }

   @Nonnull
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   public String getSlot(@Nonnull BuilderSupport support) {
      return this.slot.get(support.getExecutionContext());
   }

   public String getAttachment(@Nonnull BuilderSupport support) {
      return this.attachment.get(support.getExecutionContext());
   }
}
