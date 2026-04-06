package com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ItemExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.EntityFilterItemInHand;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderEntityFilterItemInHand extends BuilderEntityFilterBase {
   protected final AssetArrayHolder items = new AssetArrayHolder();
   protected final EnumHolder<EntityFilterItemInHand.WieldingHand> hand = new EnumHolder<EntityFilterItemInHand.WieldingHand>();

   @Nonnull
   public EntityFilterItemInHand build(@Nonnull BuilderSupport builderSupport) {
      return new EntityFilterItemInHand(this, builderSupport);
   }

   @Nonnull
   public String getShortDescription() {
      return "Check if entity is holding an item";
   }

   @Nonnull
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
      this.requireAssetArray(data, "Items", this.items, 0, 2147483647, ItemExistsValidator.withConfig(EnumSet.of(AssetValidator.Config.MATCHER)), BuilderDescriptorState.Stable, "A list of glob item patterns to match", (String)null);
      this.getEnum(data, "Hand", this.hand, EntityFilterItemInHand.WieldingHand.class, EntityFilterItemInHand.WieldingHand.Both, BuilderDescriptorState.Stable, "Which hand to check", (String)null);
      return this;
   }

   @Nullable
   public String[] getItems(@Nonnull BuilderSupport support) {
      return this.items.get(support.getExecutionContext());
   }

   public EntityFilterItemInHand.WieldingHand getHand(@Nonnull BuilderSupport support) {
      return this.hand.get(support.getExecutionContext());
   }
}
