package com.hypixel.hytale.server.npc.corecomponents.world.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.IntHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.IntSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.RelationalOperator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderBodyMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.world.BodyMotionPath;
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.Nonnull;

public class BuilderBodyMotionPath extends BuilderBodyMotionBase {
   public static final double[] DEFAULT_DELAY_SCALE_RANGE = new double[]{0.2, 0.4};
   public static final double[] DEFAULT_PERCENT_DELAY_RANGE = new double[]{0.0, 0.2};
   protected final EnumHolder<BodyMotionPath.Shape> shape = new EnumHolder<BodyMotionPath.Shape>();
   protected final NumberArrayHolder delayScaleRange = new NumberArrayHolder();
   protected final NumberArrayHolder percentDelayRange = new NumberArrayHolder();
   protected final IntHolder viewSegments = new IntHolder();
   protected double pathWidth;
   protected double nodeWidth;
   protected double minRelativeSpeed;
   protected double maxRelativeSpeed;
   protected double minWalkDistance;
   protected double maxWalkDistance;
   protected boolean startAtNearestNode;
   protected BodyMotionPath.Direction direction;
   protected double minNodeDelay;
   protected double maxNodeDelay;
   protected boolean useNodeViewDirection;
   protected boolean pickRandomAngle;

   @Nonnull
   public BodyMotionPath build(@Nonnull BuilderSupport builderSupport) {
      return new BodyMotionPath(this, builderSupport);
   }

   @Nonnull
   public String getShortDescription() {
      return "Walk along a path";
   }

   @Nonnull
   public String getLongDescription() {
      return "Walk along a path.";
   }

   public void registerTags(@Nonnull Set<String> tags) {
      super.registerTags(tags);
      tags.add("path");
   }

   @Nonnull
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderBodyMotionPath readConfig(@Nonnull JsonElement data) {
      this.getBoolean(data, "StartAtNearestNode", (b) -> this.startAtNearestNode = b, true, BuilderDescriptorState.Stable, "Start at closest warp point", (String)null);
      this.getDouble(data, "PathWidth", (w) -> this.pathWidth = w, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Experimental, "Walking corridor width", (String)null);
      this.getDouble(data, "NodeWidth", (w) -> this.nodeWidth = w, 0.2, DoubleSingleValidator.greater0(), BuilderDescriptorState.Experimental, "Radius of warp point node", (String)null);
      this.getDouble(data, "MinRelSpeed", (s) -> this.minRelativeSpeed = s, 0.5, DoubleRangeValidator.fromExclToIncl(0.0, 1.0), BuilderDescriptorState.Stable, "Minimum relative walk speed", (String)null);
      this.getDouble(data, "MaxRelSpeed", (s) -> this.maxRelativeSpeed = s, 0.5, DoubleRangeValidator.fromExclToIncl(0.0, 1.0), BuilderDescriptorState.Stable, "Maximum relative walk speed", (String)null);
      this.getDouble(data, "MinWalkDistance", (d) -> this.minWalkDistance = d, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Experimental, "Minimum walk distance when PathWidth greater 0", (String)null);
      this.getDouble(data, "MaxWalkDistance", (d) -> this.maxWalkDistance = d, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Experimental, "Maximum walk distance when PathWidth greater 0", (String)null);
      this.getEnum(data, "Direction", (d) -> this.direction = d, BodyMotionPath.Direction.class, BodyMotionPath.Direction.FORWARD, BuilderDescriptorState.Stable, "Walking direction relative to order of nodes", (String)null);
      this.getEnum(data, "Shape", this.shape, BodyMotionPath.Shape.class, BodyMotionPath.Shape.LOOP, BuilderDescriptorState.Stable, "Shape of Path", (String)null);
      this.getDouble(data, "MinNodeDelay", (d) -> this.minNodeDelay = d, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Minimum resting time at a node", (String)null);
      this.getDouble(data, "MaxNodeDelay", (d) -> this.maxNodeDelay = d, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Maximum resting time at a node", (String)null);
      this.getBoolean(data, "UseNodeViewDirection", (b) -> this.useNodeViewDirection = b, false, BuilderDescriptorState.Stable, "Look into next node direction at node", (String)null);
      this.getDoubleRange(data, "NodePauseScaleRange", this.delayScaleRange, DEFAULT_DELAY_SCALE_RANGE, DoubleSequenceValidator.fromExclToInclWeaklyMonotonic(0.0, 1.7976931348623157E308), BuilderDescriptorState.Stable, "The range from which to pick a value that defines the portion of the total node pause time that should be spent facing a direction before turning", (String)null);
      this.getDoubleRange(data, "NodePauseExtraPercentRange", this.percentDelayRange, DEFAULT_PERCENT_DELAY_RANGE, DoubleSequenceValidator.between01WeaklyMonotonic(), BuilderDescriptorState.Stable, "A range from which to pick the additional percentage of the directional pause time to add to it", (String)null);
      this.getBoolean(data, "PickRandomAngle", (b) -> this.pickRandomAngle = b, false, BuilderDescriptorState.Stable, "Whether to sweep left and right using the observation angle, or pick a random angle within the sector each time", (String)null);
      this.getInt(data, "ViewSegments", this.viewSegments, 1, IntSingleValidator.greater0(), BuilderDescriptorState.Stable, "The number of distinct segments to stop at when sweeping from left to right using the observation angle", (String)null);
      this.validateDoubleRelation("MinRelativeSpeed", this.minRelativeSpeed, RelationalOperator.LessEqual, "MaxRelativeSpeed", this.maxRelativeSpeed);
      this.validateDoubleRelation("MinWalkDistance", this.minWalkDistance, RelationalOperator.LessEqual, "MaxWalkDistance", this.maxWalkDistance);
      this.validateDoubleRelation("MinNodeDelay", this.minNodeDelay, RelationalOperator.LessEqual, "MaxNodeDelay", this.maxNodeDelay);
      this.requireFeature(EnumSet.of(Feature.Path));
      return this;
   }

   public double getPathWidth() {
      return this.pathWidth;
   }

   public double getNodeWidth() {
      return this.nodeWidth;
   }

   public double getMinRelativeSpeed() {
      return this.minRelativeSpeed;
   }

   public double getMaxRelativeSpeed() {
      return this.maxRelativeSpeed;
   }

   public double getMinWalkDistance() {
      return this.minWalkDistance;
   }

   public double getMaxWalkDistance() {
      return this.maxWalkDistance;
   }

   public boolean isStartAtNearestNode() {
      return this.startAtNearestNode;
   }

   public BodyMotionPath.Direction getDirection() {
      return this.direction;
   }

   public BodyMotionPath.Shape getShape(@Nonnull BuilderSupport support) {
      return this.shape.get(support.getExecutionContext());
   }

   public double getMinNodeDelay() {
      return this.minNodeDelay;
   }

   public double getMaxNodeDelay() {
      return this.maxNodeDelay;
   }

   public boolean isUseNodeViewDirection() {
      return this.useNodeViewDirection;
   }

   public double[] getDelayScaleRange(@Nonnull BuilderSupport support) {
      return this.delayScaleRange.get(support.getExecutionContext());
   }

   public double[] getPercentDelayRange(@Nonnull BuilderSupport support) {
      return this.percentDelayRange.get(support.getExecutionContext());
   }

   public boolean isPickRandomAngle() {
      return this.pickRandomAngle;
   }

   public int getViewSegments(@Nonnull BuilderSupport support) {
      return this.viewSegments.get(support.getExecutionContext());
   }
}
