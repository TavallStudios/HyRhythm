package com.hypixel.hytale.server.core.inventory.container;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.event.EventRegistration;
import com.hypixel.hytale.function.consumer.ShortObjectConsumer;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterActionType;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterType;
import com.hypixel.hytale.server.core.inventory.container.filter.SlotFilter;
import com.hypixel.hytale.server.core.inventory.transaction.ClearTransaction;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class EmptyItemContainer extends ItemContainer {
   public static final EmptyItemContainer INSTANCE = new EmptyItemContainer();
   public static final BuilderCodec<EmptyItemContainer> CODEC = BuilderCodec.builder(EmptyItemContainer.class, () -> INSTANCE).build();
   private static final EventRegistration<Void, ItemContainer.ItemContainerChangeEvent> EVENT_REGISTRATION = new EventRegistration<Void, ItemContainer.ItemContainerChangeEvent>(ItemContainer.ItemContainerChangeEvent.class, () -> false, () -> {
   });

   protected EmptyItemContainer() {
   }

   public short getCapacity() {
      return 0;
   }

   @Nonnull
   public ClearTransaction clear() {
      return ClearTransaction.EMPTY;
   }

   public void forEach(ShortObjectConsumer<ItemStack> action) {
   }

   protected <V> V readAction(@Nonnull Supplier<V> action) {
      return (V)action.get();
   }

   protected <X, V> V readAction(@Nonnull Function<X, V> action, X x) {
      return (V)action.apply(x);
   }

   protected <V> V writeAction(@Nonnull Supplier<V> action) {
      return (V)action.get();
   }

   protected <X, V> V writeAction(@Nonnull Function<X, V> action, X x) {
      return (V)action.apply(x);
   }

   @Nonnull
   protected ClearTransaction internal_clear() {
      return ClearTransaction.EMPTY;
   }

   protected ItemStack internal_getSlot(short slot) {
      throw new UnsupportedOperationException("getSlot(int) is not supported in EmptyItemContainer");
   }

   protected ItemStack internal_setSlot(short slot, ItemStack itemStack) {
      throw new UnsupportedOperationException("setSlot(int, ItemStack) is not supported in EmptyItemContainer");
   }

   protected ItemStack internal_removeSlot(short slot) {
      throw new UnsupportedOperationException("removeSlot(int) is not supported in EmptyItemContainer");
   }

   protected boolean cantAddToSlot(short slot, ItemStack itemStack, ItemStack slotItemStack) {
      return false;
   }

   protected boolean cantRemoveFromSlot(short slot) {
      return false;
   }

   protected boolean cantDropFromSlot(short slot) {
      return false;
   }

   protected boolean cantMoveToSlot(ItemContainer fromContainer, short slotFrom) {
      return false;
   }

   @Nonnull
   public List<ItemStack> removeAllItemStacks() {
      return Collections.emptyList();
   }

   @Nonnull
   public Map<Integer, ItemWithAllMetadata> toProtocolMap() {
      return Collections.emptyMap();
   }

   public EmptyItemContainer clone() {
      return INSTANCE;
   }

   public EventRegistration registerChangeEvent(short priority, Consumer<ItemContainer.ItemContainerChangeEvent> consumer) {
      return EVENT_REGISTRATION;
   }

   public void setGlobalFilter(FilterType globalFilter) {
   }

   public void setSlotFilter(FilterActionType actionType, short slot, SlotFilter filter) {
      validateSlotIndex(slot, 0);
   }
}
