package com.hypixel.hytale.server.npc.asset.builder;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import javax.annotation.Nullable;

public class SlotMapper {
   public static final int NO_SLOT = -2147483648;
   private final Object2IntMap<String> mappings;
   @Nullable
   private final Int2ObjectMap<String> nameMap;
   private int nextSlot;

   public SlotMapper() {
      this(false);
   }

   public SlotMapper(boolean trackNames) {
      this.mappings = new Object2IntOpenHashMap();
      this.nameMap = trackNames ? new Int2ObjectOpenHashMap() : null;
      this.mappings.defaultReturnValue(-2147483648);
   }

   public int getSlot(String name) {
      int slot = this.mappings.getInt(name);
      if (slot == -2147483648) {
         slot = this.nextSlot++;
         this.mappings.put(name, slot);
         if (this.nameMap != null) {
            this.nameMap.put(slot, name);
         }
      }

      return slot;
   }

   public int slotCount() {
      return this.mappings.size();
   }

   @Nullable
   public Object2IntMap<String> getSlotMappings() {
      return this.mappings.isEmpty() ? null : this.mappings;
   }

   @Nullable
   public Int2ObjectMap<String> getNameMap() {
      return this.nameMap;
   }
}
