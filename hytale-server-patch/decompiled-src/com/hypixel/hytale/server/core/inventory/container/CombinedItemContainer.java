package com.hypixel.hytale.server.core.inventory.container;

import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterType;
import com.hypixel.hytale.server.core.inventory.container.filter.SlotFilter;
import com.hypixel.hytale.server.core.inventory.transaction.ClearTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.Transaction;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CombinedItemContainer extends ItemContainer {
   protected final ItemContainer[] containers;

   public CombinedItemContainer(ItemContainer... containers) {
      this.containers = containers;
   }

   public ItemContainer getContainer(int index) {
      return this.containers[index];
   }

   public int getContainersSize() {
      return this.containers.length;
   }

   @Nullable
   public ItemContainer getContainerForSlot(short slot) {
      for(ItemContainer container : this.containers) {
         short capacity = container.getCapacity();
         if (slot < capacity) {
            return container;
         }

         slot -= capacity;
      }

      return null;
   }

   protected <V> V readAction(@Nonnull Supplier<V> action) {
      return (V)this.readAction0(0, action);
   }

   private <V> V readAction0(int i, @Nonnull Supplier<V> action) {
      return (V)(i >= this.containers.length ? action.get() : this.containers[i].readAction(() -> this.readAction0(i + 1, action)));
   }

   protected <X, V> V readAction(@Nonnull Function<X, V> action, X x) {
      return (V)this.readAction0(0, action, x);
   }

   private <X, V> V readAction0(int i, @Nonnull Function<X, V> action, X x) {
      return (V)(i >= this.containers.length ? action.apply(x) : this.containers[i].readAction(() -> this.readAction0(i + 1, action, x)));
   }

   protected <V> V writeAction(@Nonnull Supplier<V> action) {
      return (V)this.writeAction0(0, action);
   }

   private <V> V writeAction0(int i, @Nonnull Supplier<V> action) {
      return (V)(i >= this.containers.length ? action.get() : this.containers[i].writeAction(() -> this.writeAction0(i + 1, action)));
   }

   protected <X, V> V writeAction(@Nonnull Function<X, V> action, X x) {
      return (V)this.writeAction0(0, action, x);
   }

   private <X, V> V writeAction0(int i, @Nonnull Function<X, V> action, X x) {
      return (V)(i >= this.containers.length ? action.apply(x) : this.containers[i].writeAction(() -> this.writeAction0(i + 1, action, x)));
   }

   @Nonnull
   protected ClearTransaction internal_clear() {
      ItemStack[] itemStacks = new ItemStack[this.getCapacity()];
      short start = 0;

      for(ItemContainer container : this.containers) {
         ClearTransaction clear = container.internal_clear();
         ItemStack[] items = clear.getItems();

         for(short slot = 0; slot < itemStacks.length; ++slot) {
            itemStacks[(short)(start + slot)] = items[slot];
         }

         start += container.getCapacity();
      }

      return new ClearTransaction(true, (short)0, itemStacks);
   }

   @Nullable
   protected ItemStack internal_getSlot(short slot) {
      for(ItemContainer container : this.containers) {
         short capacity = container.getCapacity();
         if (slot < capacity) {
            return container.internal_getSlot(slot);
         }

         slot -= capacity;
      }

      return null;
   }

   @Nullable
   protected ItemStack internal_setSlot(short slot, ItemStack itemStack) {
      if (ItemStack.isEmpty(itemStack)) {
         return this.internal_removeSlot(slot);
      } else {
         for(ItemContainer container : this.containers) {
            short capacity = container.getCapacity();
            if (slot < capacity) {
               return container.internal_setSlot(slot, itemStack);
            }

            slot -= capacity;
         }

         return null;
      }
   }

   @Nullable
   protected ItemStack internal_removeSlot(short slot) {
      for(ItemContainer container : this.containers) {
         short capacity = container.getCapacity();
         if (slot < capacity) {
            return container.internal_removeSlot(slot);
         }

         slot -= capacity;
      }

      return null;
   }

   protected boolean cantAddToSlot(short slot, ItemStack itemStack, ItemStack slotItemStack) {
      for(ItemContainer container : this.containers) {
         short capacity = container.getCapacity();
         if (slot < capacity) {
            return container.cantAddToSlot(slot, itemStack, slotItemStack);
         }

         slot -= capacity;
      }

      return true;
   }

   protected boolean cantRemoveFromSlot(short slot) {
      for(ItemContainer container : this.containers) {
         short capacity = container.getCapacity();
         if (slot < capacity) {
            return container.cantRemoveFromSlot(slot);
         }

         slot -= capacity;
      }

      return true;
   }

   protected boolean cantDropFromSlot(short slot) {
      for(ItemContainer container : this.containers) {
         short capacity = container.getCapacity();
         if (slot < capacity) {
            return container.cantDropFromSlot(slot);
         }

         slot -= capacity;
      }

      return true;
   }

   protected boolean cantMoveToSlot(ItemContainer fromContainer, short slotFrom) {
      for(ItemContainer container : this.containers) {
         boolean cantMoveToSlot = container.cantMoveToSlot(fromContainer, slotFrom);
         if (cantMoveToSlot) {
            return true;
         }
      }

      return false;
   }

   public short getCapacity() {
      short capacity = 0;

      for(ItemContainer container : this.containers) {
         capacity += container.getCapacity();
      }

      return capacity;
   }

   public CombinedItemContainer clone() {
      throw new UnsupportedOperationException("clone() is not supported for CombinedItemContainer");
   }

   @Nonnull
   public EventRegistration registerChangeEvent(short priority, @Nonnull Consumer<ItemContainer.ItemContainerChangeEvent> consumer) {
      EventRegistration thisRegistration = super.registerChangeEvent(priority, consumer);
      EventRegistration[] containerRegistrations = new EventRegistration[this.containers.length];
      short start = 0;

      for(int i = 0; i < this.containers.length; ++i) {
         ItemContainer container = this.containers[i];
         containerRegistrations[i] = container.internalChangeEventRegistry.register(priority, (Object)null, (event) -> consumer.accept(new ItemContainer.ItemContainerChangeEvent(this, event.transaction().toParent(this, start, container))));
         start += container.getCapacity();
      }

      return EventRegistration.combine(thisRegistration, containerRegistrations);
   }

   protected void sendUpdate(@Nonnull Transaction transaction) {
      if (transaction.succeeded()) {
         super.sendUpdate(transaction);
         short start = 0;

         for(ItemContainer container : this.containers) {
            Transaction containerTransaction = transaction.fromParent(this, start, container);
            if (containerTransaction != null) {
               if (!containerTransaction.succeeded()) {
                  start += container.getCapacity();
                  continue;
               }

               container.sendUpdate(containerTransaction);
            }

            start += container.getCapacity();
         }

      }
   }

   public boolean containsContainer(ItemContainer itemContainer) {
      if (itemContainer == this) {
         return true;
      } else {
         for(ItemContainer container : this.containers) {
            if (container.containsContainer(itemContainer)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o instanceof CombinedItemContainer) {
         CombinedItemContainer that = (CombinedItemContainer)o;
         short capacity = this.getCapacity();
         return capacity != that.getCapacity() ? false : (Boolean)this.readAction((_that) -> (Boolean)_that.readAction((_that2) -> {
               for(short i = 0; i < capacity; ++i) {
                  if (!Objects.equals(this.internal_getSlot(i), _that2.internal_getSlot(i))) {
                     return false;
                  }
               }

               return true;
            }, _that), that);
      } else {
         return false;
      }
   }

   public int hashCode() {
      short capacity = this.getCapacity();
      int result = (Integer)this.readAction(() -> {
         int hash = 0;

         for(short i = 0; i < capacity; ++i) {
            ItemStack itemStack = this.internal_getSlot(i);
            hash = 31 * hash + (itemStack != null ? itemStack.hashCode() : 0);
         }

         return hash;
      });
      result = 31 * result + capacity;
      return result;
   }

   public void setGlobalFilter(FilterType globalFilter) {
      throw new UnsupportedOperationException("setGlobalFilter(FilterType) is not supported in CombinedItemContainer");
   }

   public void setSlotFilter(FilterActionType actionType, short slot, SlotFilter filter) {
      for(ItemContainer container : this.containers) {
         short capacity = container.getCapacity();
         if (slot < capacity) {
            container.setSlotFilter(actionType, slot, filter);
            return;
         }

         slot -= capacity;
      }

   }
}
