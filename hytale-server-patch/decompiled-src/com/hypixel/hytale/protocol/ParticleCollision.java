package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ParticleCollision {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 3;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 3;
   public static final int MAX_SIZE = 3;
   @Nonnull
   public ParticleCollisionBlockType blockType;
   @Nonnull
   public ParticleCollisionAction action;
   @Nonnull
   public ParticleRotationInfluence particleRotationInfluence;

   public ParticleCollision() {
      this.blockType = ParticleCollisionBlockType.None;
      this.action = ParticleCollisionAction.Expire;
      this.particleRotationInfluence = ParticleRotationInfluence.None;
   }

   public ParticleCollision(@Nonnull ParticleCollisionBlockType blockType, @Nonnull ParticleCollisionAction action, @Nonnull ParticleRotationInfluence particleRotationInfluence) {
      this.blockType = ParticleCollisionBlockType.None;
      this.action = ParticleCollisionAction.Expire;
      this.particleRotationInfluence = ParticleRotationInfluence.None;
      this.blockType = blockType;
      this.action = action;
      this.particleRotationInfluence = particleRotationInfluence;
   }

   public ParticleCollision(@Nonnull ParticleCollision other) {
      this.blockType = ParticleCollisionBlockType.None;
      this.action = ParticleCollisionAction.Expire;
      this.particleRotationInfluence = ParticleRotationInfluence.None;
      this.blockType = other.blockType;
      this.action = other.action;
      this.particleRotationInfluence = other.particleRotationInfluence;
   }

   @Nonnull
   public static ParticleCollision deserialize(@Nonnull ByteBuf buf, int offset) {
      ParticleCollision obj = new ParticleCollision();
      obj.blockType = ParticleCollisionBlockType.fromValue(buf.getByte(offset + 0));
      obj.action = ParticleCollisionAction.fromValue(buf.getByte(offset + 1));
      obj.particleRotationInfluence = ParticleRotationInfluence.fromValue(buf.getByte(offset + 2));
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 3;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.blockType.getValue());
      buf.writeByte(this.action.getValue());
      buf.writeByte(this.particleRotationInfluence.getValue());
   }

   public int computeSize() {
      return 3;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 3 ? ValidationResult.error("Buffer too small: expected at least 3 bytes") : ValidationResult.OK;
   }

   public ParticleCollision clone() {
      ParticleCollision copy = new ParticleCollision();
      copy.blockType = this.blockType;
      copy.action = this.action;
      copy.particleRotationInfluence = this.particleRotationInfluence;
      return copy;
   }

   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (!(obj instanceof ParticleCollision)) {
         return false;
      } else {
         ParticleCollision other = (ParticleCollision)obj;
         return Objects.equals(this.blockType, other.blockType) && Objects.equals(this.action, other.action) && Objects.equals(this.particleRotationInfluence, other.particleRotationInfluence);
      }
   }

   public int hashCode() {
      return Objects.hash(new Object[]{this.blockType, this.action, this.particleRotationInfluence});
   }
}
