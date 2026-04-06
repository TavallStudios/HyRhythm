package com.hypixel.hytale.event;

import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IEventBus extends IEventRegistry {
   default <KeyType, EventType extends IEvent<KeyType>> EventType dispatch(@Nonnull Class<EventType> eventClass) {
      return (EventType)(this.dispatchFor(eventClass, (Object)null).dispatch((IBaseEvent)null));
   }

   default <EventType extends IAsyncEvent<Void>> CompletableFuture<EventType> dispatchAsync(@Nonnull Class<EventType> eventClass) {
      return (CompletableFuture)this.dispatchForAsync(eventClass).dispatch((IBaseEvent)null);
   }

   default <KeyType, EventType extends IEvent<KeyType>> IEventDispatcher<EventType, EventType> dispatchFor(@Nonnull Class<? super EventType> eventClass) {
      return this.dispatchFor(eventClass, (Object)null);
   }

   <KeyType, EventType extends IEvent<KeyType>> IEventDispatcher<EventType, EventType> dispatchFor(@Nonnull Class<? super EventType> var1, @Nullable KeyType var2);

   default <KeyType, EventType extends IAsyncEvent<KeyType>> IEventDispatcher<EventType, CompletableFuture<EventType>> dispatchForAsync(@Nonnull Class<? super EventType> eventClass) {
      return this.dispatchForAsync(eventClass, (Object)null);
   }

   <KeyType, EventType extends IAsyncEvent<KeyType>> IEventDispatcher<EventType, CompletableFuture<EventType>> dispatchForAsync(@Nonnull Class<? super EventType> var1, @Nullable KeyType var2);
}
