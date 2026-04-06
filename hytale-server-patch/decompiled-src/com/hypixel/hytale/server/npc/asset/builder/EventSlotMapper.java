package com.hypixel.hytale.server.npc.asset.builder;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class EventSlotMapper<EventType extends Enum<EventType>> {
   @Nonnull
   private final Map<EventType, IntSet> eventSets;
   @Nonnull
   private final Map<EventType, Int2IntMap> eventSlotMappings;
   private final Int2DoubleMap eventSlotRanges = new Int2DoubleOpenHashMap();
   private int nextEventSlot;

   public EventSlotMapper(Class<EventType> classType, EventType[] types) {
      this.eventSets = new EnumMap(classType);
      this.eventSlotMappings = new EnumMap(classType);
   }

   @Nonnull
   public Map<EventType, IntSet> getEventSets() {
      return this.eventSets;
   }

   @Nonnull
   public Map<EventType, Int2IntMap> getEventSlotMappings() {
      return this.eventSlotMappings;
   }

   @Nonnull
   public Int2DoubleMap getEventSlotRanges() {
      return this.eventSlotRanges;
   }

   public int getEventSlotCount() {
      return this.nextEventSlot;
   }

   public int getEventSlot(EventType type, int set, double maxRange) {
      ((IntSet)this.eventSets.computeIfAbsent(type, (k) -> new IntOpenHashSet())).add(set);
      Int2IntMap typeSlots = (Int2IntMap)this.eventSlotMappings.computeIfAbsent(type, (k) -> {
         Int2IntOpenHashMap m = new Int2IntOpenHashMap();
         m.defaultReturnValue(-2147483648);
         return m;
      });
      int slot = typeSlots.get(set);
      if (slot == -2147483648) {
         slot = this.nextEventSlot++;
         typeSlots.put(set, slot);
      }

      double currentRange = this.eventSlotRanges.getOrDefault(slot, 4.9E-324);
      if (currentRange == 4.9E-324 || currentRange < maxRange) {
         this.eventSlotRanges.put(slot, maxRange);
      }

      return slot;
   }
}
