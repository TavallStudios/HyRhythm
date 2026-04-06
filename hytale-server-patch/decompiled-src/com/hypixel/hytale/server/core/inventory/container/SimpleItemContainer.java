package com.hypixel.hytale.server.core.inventory.container;

import com.hypixel.fastutil.ints.Int2ObjectConcurrentHashMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.Short2ObjectMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterType;
import com.hypixel.hytale.server.core.inventory.container.filter.SlotFilter;
import com.hypixel.hytale.server.core.inventory.transaction.ClearTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackSlotTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SimpleItemContainer extends ItemContainer {
   public static final BuilderCodec<SimpleItemContainer> CODEC;
   protected short capacity;
   protected final ReadWriteLock lock = new ReentrantReadWriteLock();
   protected Short2ObjectMap<ItemStack> items;
   private final Map<FilterActionType, Int2ObjectConcurrentHashMap<SlotFilter>> slotFilters = new ConcurrentHashMap();
   private FilterType globalFilter;

   protected SimpleItemContainer() {
      this.globalFilter = FilterType.ALLOW_ALL;
   }

   public SimpleItemContainer(short capacity) {
      this.globalFilter = FilterType.ALLOW_ALL;
      if (capacity <= 0) {
         throw new IllegalArgumentException("Capacity is less than or equal zero! " + capacity + " <= 0");
      } else {
         this.capacity = capacity;
         this.items = new Short2ObjectOpenHashMap(capacity);
      }
   }

   public SimpleItemContainer(@Nonnull SimpleItemContainer other) {
      this.globalFilter = FilterType.ALLOW_ALL;
      this.capacity = other.capacity;
      other.lock.readLock().lock();

      try {
         this.items = new Short2ObjectOpenHashMap(other.items);
      } finally {
         other.lock.readLock().unlock();
      }

      this.slotFilters.putAll(other.slotFilters);
      this.globalFilter = other.globalFilter;
   }

   protected <V> V readAction(@Nonnull Supplier<V> action) {
      this.lock.readLock().lock();

      Object var2;
      try {
         var2 = action.get();
      } finally {
         this.lock.readLock().unlock();
      }

      return (V)var2;
   }

   protected <X, V> V readAction(@Nonnull Function<X, V> action, X x) {
      this.lock.readLock().lock();

      Object var3;
      try {
         var3 = action.apply(x);
      } finally {
         this.lock.readLock().unlock();
      }

      return (V)var3;
   }

   protected <V> V writeAction(@Nonnull Supplier<V> action) {
      this.lock.writeLock().lock();

      Object var2;
      try {
         var2 = action.get();
      } finally {
         this.lock.writeLock().unlock();
      }

      return (V)var2;
   }

   protected <X, V> V writeAction(@Nonnull Function<X, V> action, X x) {
      this.lock.writeLock().lock();

      Object var3;
      try {
         var3 = action.apply(x);
      } finally {
         this.lock.writeLock().unlock();
      }

      return (V)var3;
   }

   protected ItemStack internal_getSlot(short slot) {
      return (ItemStack)this.items.get(slot);
   }

   protected ItemStack internal_setSlot(short slot, ItemStack itemStack) {
      return ItemStack.isEmpty(itemStack) ? this.internal_removeSlot(slot) : (ItemStack)this.items.put(slot, itemStack);
   }

   protected ItemStack internal_removeSlot(short slot) {
      return (ItemStack)this.items.remove(slot);
   }

   protected boolean cantAddToSlot(short slot, ItemStack itemStack, ItemStack slotItemStack) {
      return !this.globalFilter.allowInput() ? true : this.testFilter(FilterActionType.ADD, slot, itemStack);
   }

   protected boolean cantRemoveFromSlot(short slot) {
      return !this.globalFilter.allowOutput() ? true : this.testFilter(FilterActionType.REMOVE, slot, (ItemStack)null);
   }

   protected boolean cantDropFromSlot(short slot) {
      return this.testFilter(FilterActionType.DROP, slot, (ItemStack)null);
   }

   protected boolean cantMoveToSlot(ItemContainer fromContainer, short slotFrom) {
      return false;
   }

   private boolean testFilter(FilterActionType actionType, short slot, ItemStack itemStack) {
      Int2ObjectConcurrentHashMap<SlotFilter> map = (Int2ObjectConcurrentHashMap)this.slotFilters.get(actionType);
      if (map == null) {
         return false;
      } else {
         SlotFilter filter = map.get(slot);
         if (filter == null) {
            return false;
         } else {
            return !filter.test(actionType, this, slot, itemStack);
         }
      }
   }

   public short getCapacity() {
      return this.capacity;
   }

   @Nonnull
   protected ClearTransaction internal_clear() {
      ItemStack[] itemStacks = new ItemStack[this.getCapacity()];

      for(short i = 0; i < itemStacks.length; ++i) {
         itemStacks[i] = (ItemStack)this.items.get(i);
      }

      this.items.clear();
      return new ClearTransaction(true, (short)0, itemStacks);
   }

   @Nonnull
   public SimpleItemContainer clone() {
      return new SimpleItemContainer(this);
   }

   public boolean isEmpty() {
      this.lock.readLock().lock();

      boolean var1;
      try {
         if (!this.items.isEmpty()) {
            return super.isEmpty();
         }

         var1 = true;
      } finally {
         this.lock.readLock().unlock();
      }

      return var1;
   }

   public void setGlobalFilter(@Nonnull FilterType globalFilter) {
      this.globalFilter = (FilterType)Objects.requireNonNull(globalFilter);
   }

   public void setSlotFilter(FilterActionType actionType, short slot, @Nullable SlotFilter filter) {
      validateSlotIndex(slot, this.getCapacity());
      if (filter != null) {
         ((Int2ObjectConcurrentHashMap)this.slotFilters.computeIfAbsent(actionType, (k) -> new Int2ObjectConcurrentHashMap())).put(slot, filter);
      } else {
         this.slotFilters.computeIfPresent(actionType, (k, map) -> {
            map.remove(slot);
            return map.isEmpty() ? null : map;
         });
      }

   }

   @Nullable
   public ItemStack getItemStack(short slot) {
      validateSlotIndex(slot, this.getCapacity());
      this.lock.readLock().lock();

      ItemStack var2;
      try {
         var2 = this.internal_getSlot(slot);
      } finally {
         this.lock.readLock().unlock();
      }

      return var2;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o instanceof SimpleItemContainer) {
         SimpleItemContainer that = (SimpleItemContainer)o;
         if (this.capacity != that.capacity) {
            return false;
         } else {
            this.lock.readLock().lock();

            boolean var3;
            try {
               var3 = this.items.equals(that.items);
            } finally {
               this.lock.readLock().unlock();
            }

            return var3;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      this.lock.readLock().lock();

      int result;
      try {
         result = this.items.hashCode();
      } finally {
         this.lock.readLock().unlock();
      }

      result = 31 * result + this.capacity;
      return result;
   }

   public static ItemContainer getNewContainer(short capacity) {
      return ItemContainer.getNewContainer(capacity, SimpleItemContainer::new);
   }

   public static boolean addOrDropItemStack(@Nonnull ComponentAccessor<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull ItemContainer itemContainer, @Nonnull ItemStack itemStack) {
      ItemStackTransaction transaction = itemContainer.addItemStack(itemStack);
      ItemStack remainder = transaction.getRemainder();
      if (!ItemStack.isEmpty(remainder)) {
         ItemUtils.dropItem(ref, remainder, store);
         return true;
      } else {
         return false;
      }
   }

   public static boolean addOrDropItemStack(@Nonnull ComponentAccessor<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull ItemContainer itemContainer, short slot, @Nonnull ItemStack itemStack) {
      ItemStackSlotTransaction transaction = itemContainer.addItemStackToSlot(slot, itemStack);
      ItemStack remainder = transaction.getRemainder();
      return !ItemStack.isEmpty(remainder) ? addOrDropItemStack(store, ref, itemContainer, itemStack) : false;
   }

   public static boolean addOrDropItemStacks(@Nonnull ComponentAccessor<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull ItemContainer itemContainer, List<ItemStack> itemStacks) {
      ListTransaction<ItemStackTransaction> transaction = itemContainer.addItemStacks(itemStacks);
      boolean droppedItem = false;

      for(ItemStackTransaction stackTransaction : transaction.getList()) {
         ItemStack remainder = stackTransaction.getRemainder();
         if (!ItemStack.isEmpty(remainder)) {
            ItemUtils.dropItem(ref, remainder, store);
            droppedItem = true;
         }
      }

      return droppedItem;
   }

   public static boolean tryAddOrderedOrDropItemStacks(@Nonnull ComponentAccessor<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull ItemContainer itemContainer, List<ItemStack> itemStacks) {
      ListTransaction<ItemStackSlotTransaction> transaction = itemContainer.addItemStacksOrdered(itemStacks);
      List<ItemStack> remainderItemStacks = null;

      for(ItemStackSlotTransaction stackTransaction : transaction.getList()) {
         ItemStack remainder = stackTransaction.getRemainder();
         if (!ItemStack.isEmpty(remainder)) {
            if (remainderItemStacks == null) {
               remainderItemStacks = new ObjectArrayList();
            }

            remainderItemStacks.add(remainder);
         }
      }

      return addOrDropItemStacks(store, ref, itemContainer, remainderItemStacks);
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(SimpleItemContainer.class, SimpleItemContainer::new).append(new KeyedCodec("Capacity", Codec.SHORT), (o, i) -> o.capacity = i, (o) -> o.capacity).addValidator(Validators.greaterThanOrEqual(Short.valueOf((short)0))).add()).append(new KeyedCodec("Items", new Short2ObjectMapCodec(ItemStack.CODEC, Short2ObjectOpenHashMap::new, false)), (o, i) -> o.items = i, (o) -> o.items).add()).afterDecode((i) -> {
         if (i.items == null) {
            i.items = new Short2ObjectOpenHashMap(i.capacity);
         }

         i.items.short2ObjectEntrySet().removeIf((e) -> e.getShortKey() < 0 || e.getShortKey() >= i.capacity || ItemStack.isEmpty((ItemStack)e.getValue()));
      })).build();
   }
}
