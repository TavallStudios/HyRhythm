package com.hypixel.hytale.server.core.asset.type.blocktick.config;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import java.util.Objects;
import java.util.SplittableRandom;

public abstract class TickProcedure {
   public static final CodecMapCodec<TickProcedure> CODEC = new CodecMapCodec<TickProcedure>("Type");
   public static final BuilderCodec<TickProcedure> BASE_CODEC = BuilderCodec.abstractBuilder(TickProcedure.class).build();
   protected static final SplittableRandom BASE_RANDOM = new SplittableRandom();
   protected static final ThreadLocal<SplittableRandom> RANDOM;

   protected SplittableRandom getRandom() {
      return (SplittableRandom)RANDOM.get();
   }

   public abstract BlockTickStrategy onTick(World var1, WorldChunk var2, int var3, int var4, int var5, int var6);

   static {
      SplittableRandom var10000 = BASE_RANDOM;
      Objects.requireNonNull(var10000);
      RANDOM = ThreadLocal.withInitial(var10000::split);
   }
}
