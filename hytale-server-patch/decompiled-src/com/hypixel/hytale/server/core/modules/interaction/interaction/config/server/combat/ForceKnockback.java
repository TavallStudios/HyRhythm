package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class ForceKnockback extends Knockback {
   public static final BuilderCodec<ForceKnockback> CODEC;
   private Vector3d direction;

   public ForceKnockback() {
      this.direction = Vector3d.UP;
   }

   @Nonnull
   public Vector3d calculateVector(Vector3d source, float yaw, Vector3d target) {
      Vector3d vel = this.direction.clone();
      vel.rotateY(yaw);
      vel.scale((double)this.force);
      return vel;
   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.direction);
      return "ForceKnockback{direction=" + var10000 + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(ForceKnockback.class, ForceKnockback::new, Knockback.BASE_CODEC).appendInherited(new KeyedCodec("Direction", Vector3d.CODEC), (o, i) -> o.direction = i, (o) -> o.direction, (o, p) -> o.direction = p.direction).addValidator(Validators.nonNull()).add()).afterDecode((i) -> i.direction.normalize())).build();
   }
}
