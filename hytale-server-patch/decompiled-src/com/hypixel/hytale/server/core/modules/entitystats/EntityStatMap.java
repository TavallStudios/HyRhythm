package com.hypixel.hytale.server.core.modules.entitystats;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.ChangeStatBehaviour;
import com.hypixel.hytale.protocol.EntityStatOp;
import com.hypixel.hytale.protocol.EntityStatUpdate;
import com.hypixel.hytale.protocol.ValueType;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityStatMap implements Component<EntityStore> {
   public static final int VERSION = 5;
   public static final BuilderCodec<EntityStatMap> CODEC;
   private Map<String, EntityStatValue> unknown;
   @Nonnull
   private EntityStatValue[] values;
   float[] tempRegenerationValues;
   public final Int2ObjectMap<List<EntityStatUpdate>> selfUpdates;
   public final Int2ObjectMap<FloatList> selfStatValues;
   public final Int2ObjectMap<List<EntityStatUpdate>> otherUpdates;
   protected boolean isSelfNetworkOutdated;
   protected boolean isNetworkOutdated;

   public static ComponentType<EntityStore, EntityStatMap> getComponentType() {
      return EntityStatsModule.get().getEntityStatMapComponentType();
   }

   public EntityStatMap() {
      this.values = EntityStatValue.EMPTY_ARRAY;
      this.tempRegenerationValues = ArrayUtil.EMPTY_FLOAT_ARRAY;
      this.selfUpdates = new Int2ObjectOpenHashMap();
      this.selfStatValues = new Int2ObjectOpenHashMap();
      this.otherUpdates = new Int2ObjectOpenHashMap();
   }

   public int size() {
      return this.values.length;
   }

   @Nullable
   public EntityStatValue get(int index) {
      return index >= this.values.length ? null : this.values[index];
   }

   /** @deprecated */
   @Deprecated
   @Nullable
   public EntityStatValue get(String entityStat) {
      return this.get(EntityStatType.getAssetMap().getIndex(entityStat));
   }

   public void update() {
      IndexedLookupTableAssetMap<String, EntityStatType> assetMap = EntityStatType.getAssetMap();

      for(int index = 0; index < this.values.length; ++index) {
         EntityStatType asset = assetMap.getAsset(index);
         EntityStatValue value = this.values[index];
         if (value != null) {
            if (asset.isUnknown()) {
               if (this.unknown == null) {
                  this.unknown = new Object2ObjectOpenHashMap();
               }

               this.unknown.put(asset.getId(), value);
               this.values[index] = new EntityStatValue(index, asset);
            } else if (value.synchronizeAsset(index, asset)) {
               this.addInitChange(index, value);
            }
         }
      }

      int assetCount = assetMap.getNextIndex();
      int oldLength = this.values.length;
      if (oldLength <= assetCount) {
         this.values = (EntityStatValue[])Arrays.copyOf(this.values, assetCount);

         for(int index = oldLength; index < assetCount; ++index) {
            EntityStatType asset = assetMap.getAsset(index);
            if (asset.isUnknown()) {
               EntityStatValue value = this.values[index] = new EntityStatValue(index, asset);
               this.addInitChange(index, value);
            } else {
               EntityStatValue value = this.unknown == null ? null : (EntityStatValue)this.unknown.remove(asset.getId());
               if (value != null) {
                  value.synchronizeAsset(index, asset);
                  this.values[index] = value;
                  this.addInitChange(index, value);
               } else {
                  value = this.values[index] = new EntityStatValue(index, asset);
                  this.addInitChange(index, value);
               }
            }
         }
      }

   }

   @Nullable
   public Modifier getModifier(int index, String key) {
      EntityStatValue entityStatValue = this.get(index);
      if (entityStatValue == null) {
         HytaleLogger.getLogger().at(Level.WARNING).log("No EntityStatValue found for index: " + index);
         return null;
      } else {
         return entityStatValue.getModifiers() != null ? (Modifier)entityStatValue.getModifiers().get(key) : null;
      }
   }

   @Nullable
   public Modifier putModifier(int index, String key, Modifier modifier) {
      return this.putModifier(EntityStatMap.Predictable.NONE, index, key, modifier);
   }

   @Nullable
   public Modifier putModifier(Predictable predictable, int index, String key, Modifier modifier) {
      EntityStatValue entityStatValue = this.get(index);
      if (entityStatValue == null) {
         HytaleLogger.getLogger().at(Level.WARNING).log("No EntityStatValue found for index: " + index);
         return null;
      } else {
         float previousValue = entityStatValue.get();
         Modifier previous = entityStatValue.putModifier(key, modifier);
         this.addChange(predictable, index, EntityStatOp.PutModifier, previousValue, key, modifier);
         return previous;
      }
   }

   @Nullable
   public Modifier removeModifier(int index, String key) {
      return this.removeModifier(EntityStatMap.Predictable.NONE, index, key);
   }

   @Nullable
   public Modifier removeModifier(Predictable predictable, int index, String key) {
      EntityStatValue entityStatValue = this.get(index);
      if (entityStatValue == null) {
         HytaleLogger.getLogger().at(Level.WARNING).log("No EntityStatValue found for index: " + index);
         return null;
      } else {
         float previousValue = entityStatValue.get();
         Modifier previous = entityStatValue.removeModifier(key);
         if (previous != null) {
            this.addChange(predictable, index, EntityStatOp.RemoveModifier, previousValue, key, (Modifier)null);
         }

         return previous;
      }
   }

   public float setStatValue(int index, float newValue) {
      return this.setStatValue(EntityStatMap.Predictable.NONE, index, newValue);
   }

   public float setStatValue(Predictable predictable, int index, float newValue) {
      EntityStatValue entityStatValue = this.get(index);
      if (entityStatValue == null) {
         HytaleLogger.getLogger().at(Level.WARNING).log("No EntityStatValue found for index: " + index);
         return 0.0F;
      } else {
         float currentValue = entityStatValue.get();
         float ret = entityStatValue.set(newValue);
         if (predictable != EntityStatMap.Predictable.NONE || newValue != currentValue) {
            this.addChange(predictable, index, EntityStatOp.Set, currentValue, newValue);
         }

         return ret;
      }
   }

   public float addStatValue(int index, float amount) {
      return this.addStatValue(EntityStatMap.Predictable.NONE, index, amount);
   }

   public float addStatValue(Predictable predictable, int index, float amount) {
      EntityStatValue entityStatValue = this.get(index);
      if (entityStatValue == null) {
         HytaleLogger.getLogger().at(Level.WARNING).log("No EntityStatValue found for index: " + index);
         return 0.0F;
      } else {
         float currentValue = entityStatValue.get();
         float ret = entityStatValue.set(currentValue + amount);
         if (predictable != EntityStatMap.Predictable.NONE || ret != currentValue) {
            this.addChange(predictable, index, EntityStatOp.Add, currentValue, amount);
         }

         return ret;
      }
   }

   public float subtractStatValue(int index, float amount) {
      return this.addStatValue(index, -amount);
   }

   public float subtractStatValue(Predictable predictable, int index, float amount) {
      return this.addStatValue(predictable, index, -amount);
   }

   public float minimizeStatValue(int index) {
      return this.minimizeStatValue(EntityStatMap.Predictable.NONE, index);
   }

   public float minimizeStatValue(Predictable predictable, int index) {
      EntityStatValue entityStatValue = this.get(index);
      if (entityStatValue == null) {
         HytaleLogger.getLogger().at(Level.WARNING).log("No EntityStatValue found for index: " + index);
         return 0.0F;
      } else {
         float previousValue = entityStatValue.get();
         float ret = entityStatValue.set(entityStatValue.getMin());
         this.addChange(predictable, index, EntityStatOp.Minimize, previousValue, 0.0F);
         return ret;
      }
   }

   public float maximizeStatValue(int index) {
      return this.maximizeStatValue(EntityStatMap.Predictable.NONE, index);
   }

   public float maximizeStatValue(Predictable predictable, int index) {
      EntityStatValue entityStatValue = this.get(index);
      if (entityStatValue == null) {
         HytaleLogger.getLogger().at(Level.WARNING).log("No EntityStatValue found for index: " + index);
         return 0.0F;
      } else {
         float previousValue = entityStatValue.get();
         float ret = entityStatValue.set(entityStatValue.getMax());
         this.addChange(predictable, index, EntityStatOp.Maximize, previousValue, 0.0F);
         return ret;
      }
   }

   public float resetStatValue(int index) {
      return this.resetStatValue(EntityStatMap.Predictable.NONE, index);
   }

   public float resetStatValue(Predictable predictable, int index) {
      EntityStatType entityStatType = (EntityStatType)EntityStatType.getAssetMap().getAsset(index);
      if (entityStatType == null) {
         HytaleLogger.getLogger().at(Level.WARNING).log("No EntityStatType found for index: " + index);
         return 0.0F;
      } else {
         EntityStatValue entityStatValue = this.get(index);
         if (entityStatValue == null) {
            HytaleLogger.getLogger().at(Level.WARNING).log("No EntityStatValue found for index: " + index);
            return 0.0F;
         } else {
            float previousValue = entityStatValue.get();
            float ret;
            switch (entityStatType.getResetBehavior()) {
               case InitialValue -> ret = entityStatValue.set(entityStatType.getInitialValue());
               case MaxValue -> ret = entityStatValue.set(entityStatValue.getMax());
               default -> ret = 0.0F;
            }

            this.addChange(predictable, index, EntityStatOp.Reset, previousValue, 0.0F);
            return ret;
         }
      }
   }

   @Nonnull
   public Int2ObjectMap<List<EntityStatUpdate>> getSelfUpdates() {
      return this.selfUpdates;
   }

   @Nonnull
   public Int2ObjectMap<FloatList> getSelfStatValues() {
      return this.selfStatValues;
   }

   @Nonnull
   public Int2ObjectMap<EntityStatUpdate[]> consumeSelfUpdates() {
      return this.updatesToProtocol(this.selfUpdates);
   }

   public void clearUpdates() {
      this.selfUpdates.values().forEach(List::clear);
      this.selfStatValues.values().forEach(List::clear);
      this.otherUpdates.values().forEach(List::clear);
   }

   @Nonnull
   public Int2ObjectMap<EntityStatUpdate[]> consumeOtherUpdates() {
      return this.updatesToProtocol(this.otherUpdates);
   }

   @Nonnull
   private Int2ObjectOpenHashMap<EntityStatUpdate[]> updatesToProtocol(@Nonnull Int2ObjectMap<List<EntityStatUpdate>> localUpdates) {
      Int2ObjectOpenHashMap<EntityStatUpdate[]> updates = new Int2ObjectOpenHashMap(localUpdates.size());
      ObjectIterator<Int2ObjectMap.Entry<List<EntityStatUpdate>>> iterator = Int2ObjectMaps.fastIterator(localUpdates);

      while(iterator.hasNext()) {
         Int2ObjectMap.Entry<List<EntityStatUpdate>> e = (Int2ObjectMap.Entry)iterator.next();
         if (!((List)e.getValue()).isEmpty()) {
            updates.put(e.getIntKey(), (EntityStatUpdate[])((List)e.getValue()).toArray((x$0) -> new EntityStatUpdate[x$0]));
         }
      }

      return updates;
   }

   @Nonnull
   public Int2ObjectMap<EntityStatUpdate[]> createInitUpdate(boolean all) {
      Int2ObjectOpenHashMap<EntityStatUpdate[]> updates = new Int2ObjectOpenHashMap(this.size());

      for(int i = 0; i < this.size(); ++i) {
         EntityStatValue stat = this.get(i);
         if (stat != null && (((EntityStatType)EntityStatType.getAssetMap().getAsset(i)).isShared() || all)) {
            updates.put(i, new EntityStatUpdate[]{makeInitChange(stat)});
         }
      }

      return updates;
   }

   public boolean consumeSelfNetworkOutdated() {
      boolean temp = this.isSelfNetworkOutdated;
      this.isSelfNetworkOutdated = false;
      return temp;
   }

   public boolean consumeNetworkOutdated() {
      boolean temp = this.isNetworkOutdated;
      this.isNetworkOutdated = false;
      return temp;
   }

   private void addInitChange(int index, @Nonnull EntityStatValue value) {
      this.addChange(EntityStatMap.Predictable.NONE, index, EntityStatOp.Init, value.get(), value.get(), value.getModifiers());
   }

   private void addChange(Predictable predictable, int index, @Nonnull EntityStatOp op, float previousValue, float value) {
      this.addChange(predictable, index, op, previousValue, value, (Map)null);
   }

   private void addChange(Predictable predictable, int index, @Nonnull EntityStatOp op, float previousValue, float value, Map<String, Modifier> modifierMap) {
      EntityStatType statType = (EntityStatType)EntityStatType.getAssetMap().getAsset(index);
      if (statType.isShared()) {
         boolean isPredictable = predictable == EntityStatMap.Predictable.ALL;
         List<EntityStatUpdate> other = (List)this.otherUpdates.computeIfAbsent(index, (v) -> new ObjectArrayList());
         this.tryMergeUpdate(other, op, value, modifierMap, isPredictable);
         this.isNetworkOutdated = true;
      }

      boolean isPredictable = predictable != EntityStatMap.Predictable.NONE;
      List<EntityStatUpdate> self = (List)this.selfUpdates.computeIfAbsent(index, (v) -> new ObjectArrayList());
      FloatList values = (FloatList)this.selfStatValues.computeIfAbsent(index, (v) -> new FloatArrayList());
      if (this.tryMergeUpdate(self, op, value, modifierMap, isPredictable)) {
         values.set(values.size() - 1, this.get(index).get());
      } else {
         values.add(previousValue);
         values.add(this.get(index).get());
         this.isSelfNetworkOutdated = true;
      }
   }

   private void addChange(Predictable predictable, int index, EntityStatOp op, float previousValue, String key, @Nullable Modifier modifier) {
      EntityStatType statType = (EntityStatType)EntityStatType.getAssetMap().getAsset(index);
      com.hypixel.hytale.protocol.Modifier modifierPacket = modifier != null ? modifier.toPacket() : null;
      if (statType.isShared()) {
         boolean isPredictable = predictable == EntityStatMap.Predictable.ALL;
         List<EntityStatUpdate> other = (List)this.otherUpdates.computeIfAbsent(index, (v) -> new ObjectArrayList());
         other.add(new EntityStatUpdate(op, isPredictable, 0.0F, (Map)null, key, modifierPacket));
         this.isNetworkOutdated = true;
      }

      boolean isPredictable = predictable != EntityStatMap.Predictable.NONE;
      List<EntityStatUpdate> self = (List)this.selfUpdates.computeIfAbsent(index, (v) -> new ObjectArrayList());
      self.add(new EntityStatUpdate(op, isPredictable, 0.0F, (Map)null, key, modifierPacket));
      FloatList values = (FloatList)this.selfStatValues.computeIfAbsent(index, (v) -> new FloatArrayList());
      values.add(previousValue);
      values.add(this.get(index).get());
      this.isSelfNetworkOutdated = true;
   }

   private boolean tryMergeUpdate(@Nonnull List<EntityStatUpdate> updates, @Nonnull EntityStatOp op, float value, @Nullable Map<String, Modifier> modifierMap, boolean isPredictable) {
      EntityStatUpdate last = updates.isEmpty() ? null : (EntityStatUpdate)updates.getLast();
      switch (op) {
         case Init:
            if (!isPredictable && last != null && !last.predictable && last.op == EntityStatOp.Init) {
               last.value = value;
               return true;
            }

            Map<String, com.hypixel.hytale.protocol.Modifier> modifiers = null;
            if (modifierMap != null) {
               modifiers = new Object2ObjectOpenHashMap();

               for(Map.Entry<String, Modifier> e : modifierMap.entrySet()) {
                  modifiers.put((String)e.getKey(), ((Modifier)e.getValue()).toPacket());
               }
            }

            updates.add(new EntityStatUpdate(op, isPredictable, value, modifiers, (String)null, (com.hypixel.hytale.protocol.Modifier)null));
            return false;
         case Remove:
            updates.add(new EntityStatUpdate(op, isPredictable, 0.0F, (Map)null, (String)null, (com.hypixel.hytale.protocol.Modifier)null));
         default:
            return false;
         case Add:
            if (isPredictable || last == null || last.predictable || last.op != EntityStatOp.Init && last.op != EntityStatOp.Add && last.op != EntityStatOp.Set) {
               updates.add(new EntityStatUpdate(op, isPredictable, value, (Map)null, (String)null, (com.hypixel.hytale.protocol.Modifier)null));
               return false;
            }

            last.value += value;
            return true;
         case Set:
         case Minimize:
         case Maximize:
         case Reset:
            if (!isPredictable && last != null && !last.predictable && last.op != EntityStatOp.Remove) {
               if (last.op != EntityStatOp.Init) {
                  last.op = op;
               }

               last.value = value;
               return true;
            } else {
               updates.add(new EntityStatUpdate(op, isPredictable, value, (Map)null, (String)null, (com.hypixel.hytale.protocol.Modifier)null));
               return false;
            }
      }
   }

   public void processStatChanges(Predictable predictable, @Nonnull Int2FloatMap entityStats, ValueType valueType, @Nonnull ChangeStatBehaviour changeStatBehaviour) {
      ObjectIterator var5 = entityStats.int2FloatEntrySet().iterator();

      while(var5.hasNext()) {
         Int2FloatMap.Entry entry = (Int2FloatMap.Entry)var5.next();
         int statIndex = entry.getIntKey();
         float amount = entry.getFloatValue();
         if (valueType == ValueType.Percent) {
            EntityStatValue stat = this.get(statIndex);
            if (stat == null) {
               continue;
            }

            amount = amount * (stat.getMax() - stat.getMin()) / 100.0F;
         }

         switch (changeStatBehaviour) {
            case Set:
               this.setStatValue(predictable, statIndex, amount);
               break;
            case Add:
               this.addStatValue(predictable, statIndex, amount);
         }
      }

   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.unknown);
      return "EntityStatMap{unknown=" + var10000 + "values=" + Arrays.toString(this.values) + "}";
   }

   @Nonnull
   public EntityStatMap clone() {
      EntityStatMap map = new EntityStatMap();
      map.unknown = this.unknown;
      map.update();

      for(int i = 0; i < this.values.length; ++i) {
         if (this.values[i] != null) {
            EntityStatValue value = this.values[i];
            map.values[i].set(value.get());
            Map<String, Modifier> modifiers = value.getModifiers();
            if (modifiers != null) {
               for(Map.Entry<String, Modifier> entry : modifiers.entrySet()) {
                  map.values[i].putModifier((String)entry.getKey(), (Modifier)entry.getValue());
               }
            }
         }
      }

      ObjectIterator var7 = this.selfUpdates.int2ObjectEntrySet().iterator();

      while(var7.hasNext()) {
         Int2ObjectMap.Entry<List<EntityStatUpdate>> entry = (Int2ObjectMap.Entry)var7.next();
         map.selfUpdates.put(entry.getIntKey(), new ObjectArrayList((Collection)entry.getValue()));
      }

      var7 = this.selfStatValues.int2ObjectEntrySet().iterator();

      while(var7.hasNext()) {
         Int2ObjectMap.Entry<FloatList> entry = (Int2ObjectMap.Entry)var7.next();
         map.selfStatValues.put(entry.getIntKey(), new FloatArrayList((FloatList)entry.getValue()));
      }

      var7 = this.otherUpdates.int2ObjectEntrySet().iterator();

      while(var7.hasNext()) {
         Int2ObjectMap.Entry<List<EntityStatUpdate>> entry = (Int2ObjectMap.Entry)var7.next();
         map.otherUpdates.put(entry.getIntKey(), new ObjectArrayList((Collection)entry.getValue()));
      }

      return map;
   }

   @Nonnull
   private static EntityStatUpdate makeInitChange(@Nonnull EntityStatValue value) {
      Map<String, com.hypixel.hytale.protocol.Modifier> modifiers = null;
      if (value.getModifiers() != null) {
         modifiers = new Object2ObjectOpenHashMap();

         for(Map.Entry<String, Modifier> e : value.getModifiers().entrySet()) {
            modifiers.put((String)e.getKey(), ((Modifier)e.getValue()).toPacket());
         }
      }

      return new EntityStatUpdate(EntityStatOp.Init, false, value.get(), modifiers, (String)null, (com.hypixel.hytale.protocol.Modifier)null);
   }

   public static Int2ObjectMap<com.hypixel.hytale.protocol.Modifier[]> toPacket(@Nullable Int2ObjectMap<StaticModifier[]> modifiers) {
      if (modifiers == null) {
         return null;
      } else {
         Int2ObjectOpenHashMap<com.hypixel.hytale.protocol.Modifier[]> packet = new Int2ObjectOpenHashMap(modifiers.size());
         ObjectIterator var2 = modifiers.int2ObjectEntrySet().iterator();

         while(var2.hasNext()) {
            Int2ObjectMap.Entry<StaticModifier[]> e = (Int2ObjectMap.Entry)var2.next();
            com.hypixel.hytale.protocol.Modifier[] out = new com.hypixel.hytale.protocol.Modifier[((StaticModifier[])e.getValue()).length];

            for(int i = 0; i < out.length; ++i) {
               out[i] = ((StaticModifier[])e.getValue())[i].toPacket();
            }

            packet.put(e.getIntKey(), out);
         }

         return packet;
      }
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(EntityStatMap.class, EntityStatMap::new).legacyVersioned()).codecVersion(5)).addField(new KeyedCodec("Stats", new MapCodec(EntityStatValue.CODEC, HashMap::new, false)), (statMap, value) -> statMap.unknown = value, (statMap) -> {
         HashMap<String, EntityStatValue> outMap = new HashMap();
         if (statMap.unknown != null) {
            outMap.putAll(statMap.unknown);
         }

         for(EntityStatValue value : statMap.values) {
            if (value != null) {
               outMap.putIfAbsent(value.getId(), value);
            }
         }

         return outMap;
      })).afterDecode((map) -> {
         map.values = EntityStatValue.EMPTY_ARRAY;
         map.update();
      })).build();
   }

   public static enum Predictable {
      NONE,
      SELF,
      ALL;
   }
}
