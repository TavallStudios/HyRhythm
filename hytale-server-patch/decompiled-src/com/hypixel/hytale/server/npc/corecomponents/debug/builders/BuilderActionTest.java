package com.hypixel.hytale.server.npc.corecomponents.debug.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumSetHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.FloatHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.IntHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleArrayValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.IntValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringArrayValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ModelExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.debug.ActionTest;
import com.hypixel.hytale.server.npc.role.RoleDebugFlags;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderActionTest extends BuilderActionBase {
   protected final BooleanHolder booleanHolder = new BooleanHolder();
   protected final DoubleHolder doubleHolder = new DoubleHolder();
   protected final FloatHolder floatHolder = new FloatHolder();
   protected final IntHolder intHolder = new IntHolder();
   protected final StringHolder stringHolder = new StringHolder();
   protected final EnumHolder<RoleDebugFlags> enumHolder = new EnumHolder<RoleDebugFlags>();
   protected final EnumSetHolder<RoleDebugFlags> enumSetHolder = new EnumSetHolder<RoleDebugFlags>();
   protected final AssetHolder assetHolder = new AssetHolder();
   protected final NumberArrayHolder numberArrayHolder = new NumberArrayHolder();
   protected final StringArrayHolder stringArrayHolder = new StringArrayHolder();

   @Nonnull
   public String getShortDescription() {
      return "Test action to exercise attribute evaluation (DO NOT USE)";
   }

   @Nonnull
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Experimental;
   }

   @Nonnull
   public ActionTest build(@Nonnull BuilderSupport builderSupport) {
      return new ActionTest(this, builderSupport);
   }

   @Nonnull
   public BuilderActionTest readConfig(@Nonnull JsonElement data) {
      this.getBoolean(data, "Boolean", this.booleanHolder, true, BuilderDescriptorState.Deprecated, "Boolean True", (String)null);
      this.getDouble(data, "Double", this.doubleHolder, 0.0, (DoubleValidator)null, BuilderDescriptorState.Deprecated, "Double 0", (String)null);
      this.getFloat(data, "Float", this.floatHolder, 0.0, (DoubleValidator)null, BuilderDescriptorState.Deprecated, "Float 0", (String)null);
      this.getInt(data, "Int", this.intHolder, 0, (IntValidator)null, BuilderDescriptorState.Deprecated, "Int 0", (String)null);
      this.getString(data, "String", this.stringHolder, "Test", (StringValidator)null, BuilderDescriptorState.Deprecated, "String Test", (String)null);
      this.getEnum(data, "Enum", this.enumHolder, RoleDebugFlags.class, RoleDebugFlags.Collisions, BuilderDescriptorState.Deprecated, "Enum RoleDebugFlags Collisions", (String)null);
      this.getEnumSet(data, "EnumSet", this.enumSetHolder, RoleDebugFlags.class, EnumSet.of(RoleDebugFlags.Collisions, RoleDebugFlags.Flock), BuilderDescriptorState.Deprecated, "EnumSet Collisions Flock", (String)null);
      this.getAsset(data, "Asset", this.assetHolder, "Sheep", ModelExistsValidator.withConfig(EnumSet.of(AssetValidator.Config.MATCHER)), BuilderDescriptorState.Deprecated, "Asset Sheep", (String)null);
      this.getDoubleArray(data, "DoubleArray", this.numberArrayHolder, new double[]{1.0, 2.0}, 0, 10, (DoubleArrayValidator)null, BuilderDescriptorState.Deprecated, "DoubleArray [1,2] 0-10", (String)null);
      this.getStringArray(data, "StringArray", this.stringArrayHolder, new String[]{"a", "b"}, 0, 10, (StringArrayValidator)null, BuilderDescriptorState.Deprecated, "StringArray [a,b] 0-10", (String)null);
      return this;
   }

   public boolean getBoolean(@Nonnull BuilderSupport support) {
      return this.booleanHolder.get(support.getExecutionContext());
   }

   public double getDouble(@Nonnull BuilderSupport support) {
      return this.doubleHolder.get(support.getExecutionContext());
   }

   public float getFloat(@Nonnull BuilderSupport support) {
      return this.floatHolder.get(support.getExecutionContext());
   }

   public int getInt(@Nonnull BuilderSupport support) {
      return this.intHolder.get(support.getExecutionContext());
   }

   public String getString(@Nonnull BuilderSupport support) {
      return this.stringHolder.get(support.getExecutionContext());
   }

   public RoleDebugFlags getEnum(@Nonnull BuilderSupport support) {
      return this.enumHolder.get(support.getExecutionContext());
   }

   public EnumSet<RoleDebugFlags> getEnumSet(@Nonnull BuilderSupport support) {
      return this.enumSetHolder.get(support.getExecutionContext());
   }

   public String getAsset(@Nonnull BuilderSupport support) {
      return this.assetHolder.get(support.getExecutionContext());
   }

   public double[] getNumberArray(@Nonnull BuilderSupport support) {
      return this.numberArrayHolder.get(support.getExecutionContext());
   }

   @Nullable
   public String[] getStringArray(@Nonnull BuilderSupport support) {
      return this.stringArrayHolder.get(support.getExecutionContext());
   }
}
