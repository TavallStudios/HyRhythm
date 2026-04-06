package com.hypixel.hytale.server.npc.corecomponents.world.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.world.SensorCanPlace;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorCanPlace extends BuilderSensorBase {
   protected final EnumHolder<SensorCanPlace.Direction> direction = new EnumHolder<SensorCanPlace.Direction>();
   protected final EnumHolder<SensorCanPlace.Offset> offset = new EnumHolder<SensorCanPlace.Offset>();
   protected final DoubleHolder retryDelay = new DoubleHolder();
   protected final BooleanHolder allowEmptyMaterials = new BooleanHolder();

   @Nonnull
   public String getShortDescription() {
      return "Test if the currently set block can be placed at the relative position given direction and offset";
   }

   @Nonnull
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorCanPlace(this, builderSupport);
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.getEnum(data, "Direction", this.direction, SensorCanPlace.Direction.class, SensorCanPlace.Direction.Forward, BuilderDescriptorState.Stable, "The direction to place relative to heading", (String)null);
      this.getEnum(data, "Offset", this.offset, SensorCanPlace.Offset.class, SensorCanPlace.Offset.BodyPosition, BuilderDescriptorState.Stable, "The offset to place at", (String)null);
      this.getDouble(data, "RetryDelay", this.retryDelay, 5.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "The amount of time to delay if a placement fails before trying to place something again", (String)null);
      this.getBoolean(data, "AllowEmptyMaterials", this.allowEmptyMaterials, false, BuilderDescriptorState.Stable, "Whether it should be possible to replace blocks that have empty material", (String)null);
      this.provideFeature(Feature.Position);
      return this;
   }

   public SensorCanPlace.Direction getDirection(@Nonnull BuilderSupport support) {
      return this.direction.get(support.getExecutionContext());
   }

   public SensorCanPlace.Offset getOffset(@Nonnull BuilderSupport support) {
      return this.offset.get(support.getExecutionContext());
   }

   public double getRetryDelay(@Nonnull BuilderSupport support) {
      return this.retryDelay.get(support.getExecutionContext());
   }

   public boolean isAllowEmptyMaterials(@Nonnull BuilderSupport support) {
      return this.allowEmptyMaterials.get(support.getExecutionContext());
   }
}
