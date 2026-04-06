package com.hypixel.hytale.server.npc.corecomponents.builders;

import com.hypixel.hytale.server.npc.instructions.BodyMotion;
import javax.annotation.Nonnull;

public abstract class BuilderBodyMotionBase extends BuilderMotionBase<BodyMotion> {
   @Nonnull
   public final Class<BodyMotion> category() {
      return BodyMotion.class;
   }
}
