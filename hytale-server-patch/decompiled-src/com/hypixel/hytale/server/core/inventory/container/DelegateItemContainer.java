package com.hypixel.hytale.server.core.inventory.container;

import com.hypixel.fastutil.ints.Int2ObjectConcurrentHashMap;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterType;
import com.hypixel.hytale.server.core.inventory.container.filter.SlotFilter;
import com.hypixel.hytale.server.core.inventory.transaction.ClearTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.Transaction;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DelegateItemContainer<T extends ItemContainer> extends ItemContainer {
   private T delegate;
   private final Map<FilterActionType, Int2ObjectConcurrentHashMap<SlotFilter>> slotFilters = new ConcurrentHashMap();
   @Nonnull
   private FilterType globalFilter;

   public DelegateItemContainer(T delegate) {
      this.globalFilter = FilterType.ALLOW_ALL;
      Objects.requireNonNull(delegate, "Delegate can't be null!");
      this.delegate = delegate;
   }

   public T getDelegate() {
      return this.delegate;
   }

   protected <V> V readAction(Supplier<V> action) {
      return (V)this.delegate.readAction(action);
   }

   protected <X, V> V readAction(Function<X, V> action, X x) {
      return (V)this.delegate.readAction(action, x);
   }

   protected <V> V writeAction(Supplier<V> action) {
      return (V)this.delegate.writeAction(action);
   }

   protected <X, V> V writeAction(Function<X, V> action, X x) {
      return (V)this.delegate.writeAction(action, x);
   }

   protected ClearTransaction internal_clear() {
      return this.delegate.internal_clear();
   }

   protected ItemStack internal_getSlot(short slot) {
      return this.delegate.internal_getSlot(slot);
   }

   protected ItemStack internal_setSlot(short slot, ItemStack itemStack) {
      return this.delegate.internal_setSlot(slot, itemStack);
   }

   protected ItemStack internal_removeSlot(short slot) {
      return this.delegate.internal_removeSlot(slot);
   }

   protected boolean cantAddToSlot(short slot, ItemStack itemStack, ItemStack slotItemStack) {
      if (!this.globalFilter.allowInput()) {
         return true;
      } else {
         return this.testFilter(FilterActionType.ADD, slot, itemStack) ? true : this.delegate.cantAddToSlot(slot, itemStack, slotItemStack);
      }
   }

   protected boolean cantRemoveFromSlot(short slot) {
      if (!this.globalFilter.allowOutput()) {
         return true;
      } else {
         return this.testFilter(FilterActionType.REMOVE, slot, (ItemStack)null) ? true : this.delegate.cantRemoveFromSlot(slot);
      }
   }

   protected boolean cantDropFromSlot(short slot) {
      return this.testFilter(FilterActionType.DROP, slot, (ItemStack)null) ? true : this.delegate.cantDropFromSlot(slot);
   }

   protected boolean cantMoveToSlot(ItemContainer fromContainer, short slotFrom) {
      return this.delegate.cantMoveToSlot(fromContainer, slotFrom);
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
      return this.delegate.getCapacity();
   }

   public ClearTransaction clear() {
      return this.delegate.clear();
   }

   @Nonnull
   public DelegateItemContainer<T> clone() {
      return new DelegateItemContainer<T>(this.delegate);
   }

   public boolean isEmpty() {
      return this.delegate.isEmpty();
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

   @Nonnull
   public EventRegistration registerChangeEvent(short priority, @Nonnull Consumer<ItemContainer.ItemContainerChangeEvent> consumer) {
      EventRegistration thisRegistration = super.registerChangeEvent(priority, consumer);
      EventRegistration[] delegateRegistration = new EventRegistration[]{this.delegate.internalChangeEventRegistry.register(priority, (Object)null, (event) -> consumer.accept(new ItemContainer.ItemContainerChangeEvent(this, event.transaction().toParent(this, (short)0, this.delegate))))};
      return EventRegistration.combine(thisRegistration, delegateRegistration);
   }

   protected void sendUpdate(@Nonnull Transaction transaction) {
      if (transaction.succeeded()) {
         super.sendUpdate(transaction);
         this.delegate.externalChangeEventRegistry.dispatchFor((Object)null).dispatch(new ItemContainer.ItemContainerChangeEvent(this.delegate, transaction));
      }
   }

   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         DelegateItemContainer<?> that = (DelegateItemContainer)o;
         if (this.delegate != null) {
            if (!this.delegate.equals(that.delegate)) {
               return false;
            }
         } else if (that.delegate != null) {
            return false;
         }

         if (this.slotFilters != null) {
            if (!this.slotFilters.equals(that.slotFilters)) {
               return false;
            }
         } else if (that.slotFilters != null) {
            return false;
         }

         return this.globalFilter == that.globalFilter;
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.delegate != null ? this.delegate.hashCode() : 0;
      result = 31 * result + (this.slotFilters != null ? this.slotFilters.hashCode() : 0);
      result = 31 * result + (this.globalFilter != null ? this.globalFilter.hashCode() : 0);
      return result;
   }
}
