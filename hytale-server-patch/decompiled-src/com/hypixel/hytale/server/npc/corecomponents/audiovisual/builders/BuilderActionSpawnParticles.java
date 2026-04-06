package com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleArrayValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ParticleSystemExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.ActionSpawnParticles;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BuilderActionSpawnParticles extends BuilderActionBase {
   protected final AssetHolder particleSystem = new AssetHolder();
   protected final DoubleHolder range = new DoubleHolder();
   protected final NumberArrayHolder offset = new NumberArrayHolder();
   protected final StringHolder targetNodeName = new StringHolder();
   protected final BooleanHolder isDetachedFromModel = new BooleanHolder();

   @Nonnull
   public ActionSpawnParticles build(@Nonnull BuilderSupport builderSupport) {
      return new ActionSpawnParticles(this, builderSupport);
   }

   @Nonnull
   public String getShortDescription() {
      return "Spawn particle system visible within a given range with an offset relative to npc heading";
   }

   @Nonnull
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.WorkInProgress;
   }

   @Nonnull
   public BuilderActionSpawnParticles readConfig(@Nonnull JsonElement data) {
      this.requireAsset(data, "ParticleSystem", this.particleSystem, ParticleSystemExistsValidator.required(), BuilderDescriptorState.Stable, "Particle system to spawn", (String)null);
      this.getDouble(data, "Range", this.range, 75.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum visibility range", (String)null);
      this.getVector3d(data, "Offset", this.offset, (double[])null, (DoubleArrayValidator)null, BuilderDescriptorState.Stable, "Offset relative to footpoint in view direction of NPC", (String)null);
      this.getString(data, "TargetNodeName", this.targetNodeName, (String)null, (StringValidator)null, BuilderDescriptorState.Stable, "Target node name to position particles at", (String)null);
      this.getBoolean(data, "IsDetachedFromModel", this.isDetachedFromModel, false, BuilderDescriptorState.Stable, "Whether to attach particles to the model", (String)null);
      return this;
   }

   public String getParticleSystem(@Nonnull BuilderSupport support) {
      return this.particleSystem.get(support.getExecutionContext());
   }

   public double getRange(BuilderSupport support) {
      return this.range.get(support.getExecutionContext());
   }

   public Vector3d getOffset(BuilderSupport support) {
      double[] var10000 = this.offset.get(support.getExecutionContext());
      Vector3d var10001 = Vector3d.ZERO;
      Objects.requireNonNull(var10001);
      return createVector3d(var10000, var10001::clone);
   }

   public String getTargetNodeName(BuilderSupport support) {
      return this.targetNodeName.get(support.getExecutionContext());
   }

   public boolean isDetachedFromModel(BuilderSupport support) {
      return this.isDetachedFromModel.get(support.getExecutionContext());
   }
}
