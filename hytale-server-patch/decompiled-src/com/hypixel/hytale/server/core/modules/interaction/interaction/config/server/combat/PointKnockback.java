package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class PointKnockback extends Knockback {
   public static final BuilderCodec<PointKnockback> CODEC;
   protected float velocityY;
   protected int rotateY;
   protected int offsetX;
   protected int offsetZ;

   @Nonnull
   public Vector3d calculateVector(@Nonnull Vector3d source, float yaw, @Nonnull Vector3d target) {
      Vector3d from = source;
      if (this.offsetX != 0 || this.offsetZ != 0) {
         from = new Vector3d((double)this.offsetX, 0.0, (double)this.offsetZ);
         from.rotateY(yaw * 57.295776F);
         from.add(source);
      }

      Vector3d vector = Vector3d.directionTo(from, target).normalize();
      if (this.rotateY != 0) {
         vector.rotateY((float)this.rotateY);
      }

      double x = vector.getX() * (double)this.force;
      double z = vector.getZ() * (double)this.force;
      double y = (double)this.velocityY;
      return new Vector3d(x, y, z);
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(PointKnockback.class, PointKnockback::new, Knockback.BASE_CODEC).append(new KeyedCodec("VelocityY", Codec.DOUBLE), (knockbackAttachment, d) -> knockbackAttachment.velocityY = d.floatValue(), (knockbackAttachment) -> (double)knockbackAttachment.velocityY).add()).append(new KeyedCodec("RotateY", Codec.INTEGER), (knockbackAttachment, i) -> knockbackAttachment.rotateY = i, (knockbackAttachment) -> knockbackAttachment.rotateY).add()).append(new KeyedCodec("OffsetX", Codec.INTEGER), (knockbackAttachment, i) -> knockbackAttachment.offsetX = i, (knockbackAttachment) -> knockbackAttachment.offsetX).add()).append(new KeyedCodec("OffsetZ", Codec.INTEGER), (knockbackAttachment, i) -> knockbackAttachment.offsetZ = i, (knockbackAttachment) -> knockbackAttachment.offsetZ).add()).build();
   }
}
