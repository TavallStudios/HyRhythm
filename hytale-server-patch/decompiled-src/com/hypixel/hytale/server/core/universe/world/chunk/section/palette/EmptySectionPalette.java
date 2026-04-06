package com.hypixel.hytale.server.core.universe.world.chunk.section.palette;

import com.hypixel.hytale.protocol.packets.world.PaletteType;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.ints.Int2ShortMap;
import it.unimi.dsi.fastutil.ints.Int2ShortOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.function.IntConsumer;
import java.util.function.ToIntFunction;
import javax.annotation.Nonnull;

public class EmptySectionPalette implements ISectionPalette {
   public static final int EMPTY_ID = 0;
   public static final EmptySectionPalette INSTANCE = new EmptySectionPalette();

   private EmptySectionPalette() {
   }

   @Nonnull
   public PaletteType getPaletteType() {
      return PaletteType.Empty;
   }

   @Nonnull
   public ISectionPalette.SetResult set(int index, int id) {
      return id == 0 ? ISectionPalette.SetResult.UNCHANGED : ISectionPalette.SetResult.REQUIRES_PROMOTE;
   }

   public int get(int index) {
      return 0;
   }

   public boolean shouldDemote() {
      return false;
   }

   public ISectionPalette demote() {
      throw new UnsupportedOperationException("Cannot demote empty chunk section!");
   }

   @Nonnull
   public ISectionPalette promote() {
      return new HalfByteSectionPalette();
   }

   public boolean contains(int id) {
      return id == 0;
   }

   public boolean containsAny(@Nonnull IntList ids) {
      return ids.contains(0);
   }

   public boolean isSolid(int id) {
      return id == 0;
   }

   public int count() {
      return 1;
   }

   public int count(int id) {
      return id == 0 ? '耀' : 0;
   }

   @Nonnull
   public IntSet values() {
      IntSet set = new IntOpenHashSet();
      set.add(0);
      return set;
   }

   public void forEachValue(@Nonnull IntConsumer consumer) {
      consumer.accept(0);
   }

   @Nonnull
   public Int2ShortMap valueCounts() {
      Int2ShortMap map = new Int2ShortOpenHashMap();
      map.put(0, (short)-32768);
      return map;
   }

   public void find(@Nonnull IntList ids, IntSet internalIdHolder, @Nonnull IntConsumer indexConsumer) {
      if (ids.contains(0)) {
         for(int i = 0; i < 32768; ++i) {
            indexConsumer.accept(i);
         }
      }

   }

   public void serializeForPacket(ByteBuf buf) {
   }

   public void serialize(ISectionPalette.KeySerializer keySerializer, ByteBuf buf) {
   }

   public void deserialize(ToIntFunction<ByteBuf> deserializer, ByteBuf buf, int version) {
   }
}
