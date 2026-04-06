package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class DirectionalKnockback extends Knockback {
   public static final BuilderCodec<DirectionalKnockback> CODEC;
   protected float relativeX;
   protected float velocityY;
   protected float relativeZ;

   @Nonnull
   public Vector3d calculateVector(@Nonnull Vector3d source, float yaw, @Nonnull Vector3d target) {
      Vector3d vector = source.clone().subtract(target);
      if (vector.squaredLength() <= 1.0E-8) {
         Vector3d lookVector = new Vector3d(0.0, 0.0, -1.0);
         lookVector.rotateY(yaw);
         vector.assign(lookVector);
      } else {
         vector.normalize();
      }

      if (this.relativeX != 0.0F || this.relativeZ != 0.0F) {
         Vector3d rotation = new Vector3d((double)this.relativeX, 0.0, (double)this.relativeZ);
         rotation.rotateY(yaw);
         vector.add(rotation);
      }

      double x = vector.getX() * (double)this.force;
      double z = vector.getZ() * (double)this.force;
      double y = (double)this.velocityY;
      return new Vector3d(x, y, z);
   }

   @Nonnull
   public String toString() {
      return "DirectionalKnockback{relativeX=" + this.relativeX + ", relativeZ=" + this.relativeZ + "}";
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(DirectionalKnockback.class, DirectionalKnockback::new, Knockback.BASE_CODEC).append(new KeyedCodec("RelativeX", Codec.DOUBLE), (knockbackAttachment, d) -> knockbackAttachment.relativeX = d.floatValue(), (knockbackAttachment) -> (double)knockbackAttachment.relativeX).add()).append(new KeyedCodec("VelocityY", Codec.DOUBLE), (knockbackAttachment, d) -> knockbackAttachment.velocityY = d.floatValue(), (knockbackAttachment) -> (double)knockbackAttachment.velocityY).add()).append(new KeyedCodec("RelativeZ", Codec.DOUBLE), (knockbackAttachment, d) -> knockbackAttachment.relativeZ = d.floatValue(), (knockbackAttachment) -> (double)knockbackAttachment.relativeZ).add()).build();
   }
}
