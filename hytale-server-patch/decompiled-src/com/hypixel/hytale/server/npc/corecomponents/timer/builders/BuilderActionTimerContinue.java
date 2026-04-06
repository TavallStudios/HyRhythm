package com.hypixel.hytale.server.npc.corecomponents.timer.builders;

import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.timer.ActionTimer;
import com.hypixel.hytale.server.npc.util.Timer;
import javax.annotation.Nonnull;

public class BuilderActionTimerContinue extends BuilderActionTimer {
   @Nonnull
   public ActionTimer build(@Nonnull BuilderSupport builderSupport) {
      return new ActionTimer(this, builderSupport);
   }

   @Nonnull
   public String getShortDescription() {
      return "Continue a timer";
   }

   @Nonnull
   public String getLongDescription() {
      return "Continue a timer";
   }

   @Nonnull
   public Timer.TimerAction getTimerAction() {
      return Timer.TimerAction.CONTINUE;
   }
}
