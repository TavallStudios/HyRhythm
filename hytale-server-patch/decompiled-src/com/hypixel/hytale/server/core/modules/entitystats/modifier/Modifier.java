package com.hypixel.hytale.server.core.modules.entitystats.modifier;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class Modifier implements NetworkSerializable<com.hypixel.hytale.protocol.Modifier> {
   public static final CodecMapCodec<Modifier> CODEC = new CodecMapCodec<Modifier>();
   protected static final BuilderCodec<Modifier> BASE_CODEC;
   protected ModifierTarget target;

   public Modifier() {
      this.target = Modifier.ModifierTarget.MAX;
   }

   public Modifier(ModifierTarget target) {
      this.target = Modifier.ModifierTarget.MAX;
      this.target = target;
   }

   public abstract float apply(float var1);

   public ModifierTarget getTarget() {
      return this.target;
   }

   @Nonnull
   public com.hypixel.hytale.protocol.Modifier toPacket() {
      if (!(this instanceof StaticModifier)) {
         throw new UnsupportedOperationException("Only static modifiers supported on the client currently.");
      } else {
         com.hypixel.hytale.protocol.Modifier packet = new com.hypixel.hytale.protocol.Modifier();
         com.hypixel.hytale.protocol.ModifierTarget var10001;
         switch (this.target.ordinal()) {
            case 0 -> var10001 = com.hypixel.hytale.protocol.ModifierTarget.Min;
            case 1 -> var10001 = com.hypixel.hytale.protocol.ModifierTarget.Max;
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         packet.target = var10001;
         return packet;
      }
   }

   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Modifier modifier = (Modifier)o;
         return this.target == modifier.target;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.target != null ? this.target.hashCode() : 0;
   }

   @Nonnull
   public String toString() {
      return "Modifier{target=" + String.valueOf(this.target) + "}";
   }

   static {
      BASE_CODEC = ((BuilderCodec.Builder)BuilderCodec.abstractBuilder(Modifier.class).append(new KeyedCodec("Target", new EnumCodec(ModifierTarget.class, EnumCodec.EnumStyle.LEGACY)), (regenerating, value) -> regenerating.target = value, (regenerating) -> regenerating.target).add()).build();
   }

   public static enum ModifierTarget {
      MIN,
      MAX;

      public static final ModifierTarget[] VALUES = values();
   }
}
