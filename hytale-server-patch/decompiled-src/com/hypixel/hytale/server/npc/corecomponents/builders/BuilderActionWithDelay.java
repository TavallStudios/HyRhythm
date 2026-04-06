package com.hypixel.hytale.server.npc.corecomponents.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;

public abstract class BuilderActionWithDelay extends BuilderActionBase {
   public static final double[] DEFAULT_TIMEOUT_RANGE = new double[]{1.0, 1.0};
   protected final NumberArrayHolder delayRange = new NumberArrayHolder();

   @Nonnull
   public Builder<Action> readCommonConfig(@Nonnull JsonElement data) {
      super.readCommonConfig(data);
      this.getDoubleRange(data, "Delay", this.delayRange, this.getDefaultTimeoutRange(), DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, 1.7976931348623157E308), BuilderDescriptorState.Stable, "Range of time to delay in seconds", (String)null);
      return this;
   }

   public double[] getDelayRange(@Nonnull BuilderSupport support) {
      return this.delayRange.get(support.getExecutionContext());
   }

   protected double[] getDefaultTimeoutRange() {
      return DEFAULT_TIMEOUT_RANGE;
   }
}
