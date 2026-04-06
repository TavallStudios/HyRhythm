package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server.combat;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import javax.annotation.Nonnull;

public abstract class Knockback {
   public static final CodecMapCodec<Knockback> CODEC = new CodecMapCodec<Knockback>("Type", true);
   public static final BuilderCodec<Knockback> BASE_CODEC;
   protected float force;
   protected float duration;
   protected ChangeVelocityType velocityType;
   private VelocityConfig velocityConfig;

   protected Knockback() {
      this.velocityType = ChangeVelocityType.Add;
   }

   public float getForce() {
      return this.force;
   }

   public float getDuration() {
      return this.duration;
   }

   public ChangeVelocityType getVelocityType() {
      return this.velocityType;
   }

   public VelocityConfig getVelocityConfig() {
      return this.velocityConfig;
   }

   public abstract Vector3d calculateVector(Vector3d var1, float var2, Vector3d var3);

   @Nonnull
   public String toString() {
      float var10000 = this.force;
      return "Knockback{, force=" + var10000 + ", duration=" + this.duration + ", velocityType=" + String.valueOf(this.velocityType) + ", velocityConfig=" + String.valueOf(this.velocityConfig) + "}";
   }

   static {
      BASE_CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.abstractBuilder(Knockback.class).append(new KeyedCodec("Force", Codec.DOUBLE), (knockbackAttachment, d) -> knockbackAttachment.force = d.floatValue(), (knockbackAttachment) -> (double)knockbackAttachment.force).add()).append(new KeyedCodec("Duration", Codec.FLOAT), (knockbackAttachment, f) -> knockbackAttachment.duration = f, (knockbackAttachment) -> knockbackAttachment.duration).addValidator(Validators.greaterThanOrEqual(0.0F)).documentation("The duration for which the knockback force should be continuously applied. If 0, force is applied once.").add()).append(new KeyedCodec("VelocityType", ProtocolCodecs.CHANGE_VELOCITY_TYPE_CODEC), (knockbackAttachment, d) -> knockbackAttachment.velocityType = d, (knockbackAttachment) -> knockbackAttachment.velocityType).add()).appendInherited(new KeyedCodec("VelocityConfig", VelocityConfig.CODEC), (o, i) -> o.velocityConfig = i, (o) -> o.velocityConfig, (o, p) -> o.velocityConfig = p.velocityConfig).add()).build();
   }
}
