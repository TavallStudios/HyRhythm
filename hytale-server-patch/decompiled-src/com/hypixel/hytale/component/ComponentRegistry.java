package com.hypixel.hytale.component;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.MapProvidedMapCodec;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.component.data.change.ChangeType;
import com.hypixel.hytale.component.data.change.ComponentChange;
import com.hypixel.hytale.component.data.change.DataChange;
import com.hypixel.hytale.component.data.change.ResourceChange;
import com.hypixel.hytale.component.data.change.SystemChange;
import com.hypixel.hytale.component.data.change.SystemGroupChange;
import com.hypixel.hytale.component.data.change.SystemTypeChange;
import com.hypixel.hytale.component.data.unknown.TempUnknownComponent;
import com.hypixel.hytale.component.data.unknown.UnknownComponents;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.event.EntityEventType;
import com.hypixel.hytale.component.event.WorldEventType;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.query.ReadWriteArchetypeQuery;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.component.spatial.SpatialStructure;
import com.hypixel.hytale.component.system.EcsEvent;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.ISystem;
import com.hypixel.hytale.component.system.QuerySystem;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.RefSystem;
import com.hypixel.hytale.component.system.System;
import com.hypixel.hytale.component.system.WorldEventSystem;
import com.hypixel.hytale.component.system.tick.ArchetypeTickingSystem;
import com.hypixel.hytale.component.system.tick.RunWhenPausedSystem;
import com.hypixel.hytale.component.system.tick.TickableSystem;
import com.hypixel.hytale.component.system.tick.TickingSystem;
import com.hypixel.hytale.logger.HytaleLogger;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;

public class ComponentRegistry<ECS_TYPE> implements IComponentRegistry<ECS_TYPE> {
   public static final int UNASSIGNED_INDEX = -2147483648;
   public static final int DEFAULT_INITIAL_SIZE = 16;
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   /** @deprecated */
   @Deprecated
   private static final KeyedCodec<Integer> VERSION;
   private static final AtomicInteger REFERENCE_THREAD_COUNTER;
   private boolean shutdown;
   private final StampedLock dataLock = new StampedLock();
   @Nonnull
   private final Object2IntMap<String> componentIdToIndex = new Object2IntOpenHashMap(16);
   private final BitSet componentIndexReuse = new BitSet();
   private int componentSize;
   @Nonnull
   private String[] componentIds = new String[16];
   @Nonnull
   private BuilderCodec<? extends Component<ECS_TYPE>>[] componentCodecs = new BuilderCodec[16];
   @Nonnull
   private Supplier<? extends Component<ECS_TYPE>>[] componentSuppliers = new Supplier[16];
   @Nonnull
   private ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>[] componentTypes = new ComponentType[16];
   @Nonnull
   private final Object2IntMap<String> resourceIdToIndex = new Object2IntOpenHashMap(16);
   private final BitSet resourceIndexReuse = new BitSet();
   private int resourceSize;
   @Nonnull
   private String[] resourceIds = new String[16];
   @Nonnull
   private BuilderCodec<? extends Resource<ECS_TYPE>>[] resourceCodecs = new BuilderCodec[16];
   @Nonnull
   private Supplier<? extends Resource<ECS_TYPE>>[] resourceSuppliers = new Supplier[16];
   @Nonnull
   private ResourceType<ECS_TYPE, ? extends Resource<ECS_TYPE>>[] resourceTypes = new ResourceType[16];
   @Nonnull
   private final Object2IntMap<Class<? extends ISystem<ECS_TYPE>>> systemTypeClassToIndex = new Object2IntOpenHashMap(16);
   @Nonnull
   private final Object2IntMap<Class<? extends EcsEvent>> entityEventTypeClassToIndex = new Object2IntOpenHashMap(16);
   @Nonnull
   private final Object2IntMap<Class<? extends EcsEvent>> worldEventTypeClassToIndex = new Object2IntOpenHashMap(16);
   private final BitSet systemTypeIndexReuse = new BitSet();
   private int systemTypeSize;
   @Nonnull
   private SystemType<ECS_TYPE, ? extends ISystem<ECS_TYPE>>[] systemTypes = new SystemType[16];
   private BitSet[] systemTypeToSystemIndex = new BitSet[16];
   private final BitSet systemGroupIndexReuse = new BitSet();
   private int systemGroupSize;
   @Nonnull
   private SystemGroup<ECS_TYPE>[] systemGroups = new SystemGroup[16];
   private int systemSize;
   @Nonnull
   private ISystem<ECS_TYPE>[] systems = new ISystem[16];
   @Nonnull
   private ISystem<ECS_TYPE>[] sortedSystems = new ISystem[16];
   @Nonnull
   private final Object2IntMap<Class<? extends ISystem<ECS_TYPE>>> systemClasses = new Object2IntOpenHashMap(16);
   @Nonnull
   private final Object2BooleanMap<Class<? extends ISystem<ECS_TYPE>>> systemBypassClassCheck = new Object2BooleanOpenHashMap(16);
   @Nonnull
   private final StampedLock storeLock = new StampedLock();
   private int storeSize;
   @Nonnull
   private Store<ECS_TYPE>[] stores = new Store[16];
   private final ReadWriteLock dataUpdateLock = new ReentrantReadWriteLock();
   private Data<ECS_TYPE> data;
   private final Set<Reference<Holder<ECS_TYPE>>> holders = ConcurrentHashMap.newKeySet();
   private final ReferenceQueue<Holder<ECS_TYPE>> holderReferenceQueue = new ReferenceQueue();
   @Nonnull
   private final Thread holderReferenceThread;
   @Nonnull
   private final ComponentType<ECS_TYPE, UnknownComponents<ECS_TYPE>> unknownComponentType;
   @Nonnull
   private final ComponentType<ECS_TYPE, NonTicking<ECS_TYPE>> nonTickingComponentType;
   @Nonnull
   private final ComponentType<ECS_TYPE, NonSerialized<ECS_TYPE>> nonSerializedComponentType;
   @Nonnull
   private final SystemType<ECS_TYPE, HolderSystem<ECS_TYPE>> holderSystemType;
   @Nonnull
   private final SystemType<ECS_TYPE, RefSystem<ECS_TYPE>> refSystemType;
   @Nonnull
   private final SystemType<ECS_TYPE, RefChangeSystem<ECS_TYPE, ?>> refChangeSystemType;
   @Nonnull
   private final SystemType<ECS_TYPE, QuerySystem<ECS_TYPE>> querySystemType;
   @Nonnull
   private final SystemType<ECS_TYPE, TickingSystem<ECS_TYPE>> tickingSystemType;
   @Nonnull
   private final SystemType<ECS_TYPE, TickableSystem<ECS_TYPE>> tickableSystemType;
   @Nonnull
   private final SystemType<ECS_TYPE, RunWhenPausedSystem<ECS_TYPE>> runWhenPausedSystemType;
   @Nonnull
   private final SystemType<ECS_TYPE, ArchetypeTickingSystem<ECS_TYPE>> archetypeTickingSystemType;

   public ComponentRegistry() {
      this.componentIdToIndex.defaultReturnValue(-2147483648);
      this.resourceIdToIndex.defaultReturnValue(-2147483648);
      this.systemTypeClassToIndex.defaultReturnValue(-2147483648);
      this.entityEventTypeClassToIndex.defaultReturnValue(-2147483648);
      this.worldEventTypeClassToIndex.defaultReturnValue(-2147483648);

      for(int i = 0; i < 16; ++i) {
         this.systemTypeToSystemIndex[i] = new BitSet();
      }

      this.data = new Data<ECS_TYPE>(this);
      this.unknownComponentType = this.registerComponent(UnknownComponents.class, "Unknown", UnknownComponents.CODEC);
      this.nonTickingComponentType = this.registerComponent(NonTicking.class, NonTicking::get);
      this.nonSerializedComponentType = this.registerComponent(NonSerialized.class, NonSerialized::get);
      this.holderSystemType = this.registerSystemType(HolderSystem.class);
      this.refSystemType = this.registerSystemType(RefSystem.class);
      this.refChangeSystemType = this.registerSystemType(RefChangeSystem.class);
      this.querySystemType = this.registerSystemType(QuerySystem.class);
      this.tickingSystemType = this.registerSystemType(TickingSystem.class);
      this.tickableSystemType = this.registerSystemType(TickableSystem.class);
      this.runWhenPausedSystemType = this.registerSystemType(RunWhenPausedSystem.class);
      this.archetypeTickingSystemType = this.registerSystemType(ArchetypeTickingSystem.class);
      this.holderReferenceThread = new Thread(() -> {
         try {
            while(!Thread.interrupted()) {
               this.holders.remove(this.holderReferenceQueue.remove());
            }
         } catch (InterruptedException var2) {
            Thread.currentThread().interrupt();
         }

      }, "EntityHolderReferenceThread-" + REFERENCE_THREAD_COUNTER.getAndIncrement());
      this.holderReferenceThread.setDaemon(true);
      this.holderReferenceThread.start();
   }

   public boolean isShutdown() {
      return this.shutdown;
   }

   public void shutdown() {
      this.shutdown0();
   }

   void shutdown0() {
      this.shutdown = true;
      this.holderReferenceThread.interrupt();
      long lock = this.storeLock.writeLock();

      try {
         for(int storeIndex = this.storeSize - 1; storeIndex >= 0; --storeIndex) {
            Store<ECS_TYPE> store = this.stores[storeIndex];
            if (store != null) {
               store.shutdown0(this.data);
            }
         }

         this.stores = Store.EMPTY_ARRAY;
      } finally {
         this.storeLock.unlockWrite(lock);
      }

   }

   @Nonnull
   public ReadWriteLock getDataUpdateLock() {
      return this.dataUpdateLock;
   }

   @Nonnull
   public ComponentType<ECS_TYPE, UnknownComponents<ECS_TYPE>> getUnknownComponentType() {
      return this.unknownComponentType;
   }

   @Nonnull
   public ComponentType<ECS_TYPE, NonTicking<ECS_TYPE>> getNonTickingComponentType() {
      return this.nonTickingComponentType;
   }

   @Nonnull
   public ComponentType<ECS_TYPE, NonSerialized<ECS_TYPE>> getNonSerializedComponentType() {
      return this.nonSerializedComponentType;
   }

   @Nonnull
   public SystemType<ECS_TYPE, HolderSystem<ECS_TYPE>> getHolderSystemType() {
      return this.holderSystemType;
   }

   @Nonnull
   public SystemType<ECS_TYPE, RefSystem<ECS_TYPE>> getRefSystemType() {
      return this.refSystemType;
   }

   @Nonnull
   public SystemType<ECS_TYPE, RefChangeSystem<ECS_TYPE, ?>> getRefChangeSystemType() {
      return this.refChangeSystemType;
   }

   @Nonnull
   public SystemType<ECS_TYPE, QuerySystem<ECS_TYPE>> getQuerySystemType() {
      return this.querySystemType;
   }

   @Nonnull
   public SystemType<ECS_TYPE, TickingSystem<ECS_TYPE>> getTickingSystemType() {
      return this.tickingSystemType;
   }

   @Nonnull
   public SystemType<ECS_TYPE, TickableSystem<ECS_TYPE>> getTickableSystemType() {
      return this.tickableSystemType;
   }

   @Nonnull
   public SystemType<ECS_TYPE, RunWhenPausedSystem<ECS_TYPE>> getRunWhenPausedSystemType() {
      return this.runWhenPausedSystemType;
   }

   @Nonnull
   public SystemType<ECS_TYPE, ArchetypeTickingSystem<ECS_TYPE>> getArchetypeTickingSystemType() {
      return this.archetypeTickingSystemType;
   }

   @Nonnull
   public <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(@Nonnull Class<? super T> tClass, @Nonnull Supplier<T> supplier) {
      return this.registerComponent(tClass, (String)null, (BuilderCodec)null, supplier, false);
   }

   @Nonnull
   public <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(@Nonnull Class<? super T> tClass, @Nonnull String id, @Nonnull BuilderCodec<T> codec) {
      Objects.requireNonNull(codec);
      return this.registerComponent(tClass, id, codec, codec::getDefaultValue, false);
   }

   /** @deprecated */
   @Deprecated
   @Nonnull
   public <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(@Nonnull Class<? super T> tClass, @Nonnull String id, @Nonnull BuilderCodec<T> codec, boolean skipValidation) {
      Objects.requireNonNull(codec);
      return this.registerComponent(tClass, id, codec, codec::getDefaultValue, skipValidation);
   }

   @Nonnull
   private <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent(@Nonnull Class<? super T> tClass, @Nullable String id, @Nullable BuilderCodec<T> codec, @Nonnull Supplier<T> supplier, boolean skipValidation) {
      if (this.shutdown) {
         throw new IllegalStateException("Registry has been shutdown");
      } else {
         if (codec != null && !skipValidation) {
            ExtraInfo extraInfo = (ExtraInfo)ExtraInfo.THREAD_LOCAL.get();
            codec.validateDefaults(extraInfo, new HashSet());
            extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER, "Default Asset Validation Failed!\n");
         }

         long lock = this.dataLock.writeLock();

         ComponentType var9;
         try {
            ComponentType<ECS_TYPE, T> componentType = this.registerComponent0(tClass, id, codec, supplier, new ComponentType());
            lock = this.dataLock.tryConvertToReadLock(lock);
            this.updateData0(new ComponentChange(ChangeType.REGISTERED, componentType));
            var9 = componentType;
         } finally {
            this.dataLock.unlock(lock);
         }

         return var9;
      }
   }

   public <T extends Component<ECS_TYPE>> void unregisterComponent(@Nonnull ComponentType<ECS_TYPE, T> componentType) {
      if (this.shutdown) {
         throw new IllegalStateException("Registry has been shutdown");
      } else {
         componentType.validateRegistry(this);
         componentType.validate();
         if (componentType.equals(this.unknownComponentType)) {
            throw new IllegalArgumentException("UnknownComponentType can not be unregistered!");
         } else {
            long lock = this.dataLock.writeLock();

            try {
               this.unregisterComponent0(componentType);
               List<DataChange> changes = new ObjectArrayList();
               changes.add(new ComponentChange(ChangeType.UNREGISTERED, componentType));

               for(int unsortedSystemIndex = this.systemSize - 1; unsortedSystemIndex >= 0; --unsortedSystemIndex) {
                  ISystem<ECS_TYPE> system = this.systems[unsortedSystemIndex];
                  if (system instanceof QuerySystem) {
                     QuerySystem<ECS_TYPE> archetypeSystem = (QuerySystem)system;
                     Query<ECS_TYPE> query = archetypeSystem.getQuery();
                     if (query != null && query.requiresComponentType(componentType)) {
                        this.unregisterSystem0(unsortedSystemIndex, system);
                        changes.add(new SystemChange(ChangeType.UNREGISTERED, system));
                     }
                  }
               }

               lock = this.dataLock.tryConvertToReadLock(lock);
               this.updateData0((DataChange[])changes.toArray((x$0) -> new DataChange[x$0]));
               componentType.invalidate();
            } finally {
               this.dataLock.unlock(lock);
            }

         }
      }
   }

   @Nonnull
   public <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> registerResource(@Nonnull Class<? super T> tClass, @Nonnull Supplier<T> supplier) {
      return this.registerResource(tClass, (String)null, (BuilderCodec)null, supplier);
   }

   @Nonnull
   public <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> registerResource(@Nonnull Class<? super T> tClass, @Nonnull String id, @Nonnull BuilderCodec<T> codec) {
      Objects.requireNonNull(codec);
      return this.registerResource(tClass, id, codec, codec::getDefaultValue);
   }

   @Nonnull
   private <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> registerResource(@Nonnull Class<? super T> tClass, @Nullable String id, @Nullable BuilderCodec<T> codec, @Nonnull Supplier<T> supplier) {
      if (this.shutdown) {
         throw new IllegalStateException("Registry has been shutdown");
      } else {
         if (codec != null) {
            ExtraInfo extraInfo = (ExtraInfo)ExtraInfo.THREAD_LOCAL.get();
            codec.validateDefaults(extraInfo, new HashSet());
            extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER, "Default Asset Validation Failed!\n");
         }

         long lock = this.dataLock.writeLock();

         ResourceType var8;
         try {
            ResourceType<ECS_TYPE, T> resourceType = this.registerResource0(tClass, id, codec, supplier, new ResourceType());
            lock = this.dataLock.tryConvertToReadLock(lock);
            this.updateData0(new ResourceChange(ChangeType.REGISTERED, resourceType));
            var8 = resourceType;
         } finally {
            this.dataLock.unlock(lock);
         }

         return var8;
      }
   }

   public <T extends Resource<ECS_TYPE>> void unregisterResource(@Nonnull ResourceType<ECS_TYPE, T> resourceType) {
      if (this.shutdown) {
         throw new IllegalStateException("Registry has been shutdown");
      } else {
         resourceType.validateRegistry(this);
         resourceType.validate();
         long lock = this.dataLock.writeLock();

         try {
            this.unregisterResource0(resourceType);
            lock = this.dataLock.tryConvertToReadLock(lock);
            this.updateData0(new ResourceChange(ChangeType.UNREGISTERED, resourceType));
            resourceType.invalidate();
         } finally {
            this.dataLock.unlock(lock);
         }

      }
   }

   @Nonnull
   public <T extends ISystem<ECS_TYPE>> SystemType<ECS_TYPE, T> registerSystemType(@Nonnull Class<? super T> systemTypeClass) {
      if (this.shutdown) {
         throw new IllegalStateException("Registry has been shutdown");
      } else if (!ISystem.class.isAssignableFrom(systemTypeClass)) {
         throw new IllegalArgumentException("systemTypeClass must extend ComponentSystem! " + String.valueOf(systemTypeClass));
      } else {
         long lock = this.dataLock.writeLock();

         SystemType var5;
         try {
            SystemType<ECS_TYPE, T> systemType = this.registerSystemType0(systemTypeClass);
            lock = this.dataLock.tryConvertToReadLock(lock);
            this.updateData0(new SystemTypeChange(ChangeType.REGISTERED, systemType));
            var5 = systemType;
         } finally {
            this.dataLock.unlock(lock);
         }

         return var5;
      }
   }

   public <T extends ISystem<ECS_TYPE>> void unregisterSystemType(@Nonnull SystemType<ECS_TYPE, T> systemType) {
      if (this.shutdown) {
         throw new IllegalStateException("Registry has been shutdown");
      } else {
         systemType.validate();
         long lock = this.dataLock.writeLock();

         try {
            this.unregisterSystemType0(systemType);
            lock = this.dataLock.tryConvertToReadLock(lock);
            this.updateData0(new SystemTypeChange(ChangeType.UNREGISTERED, systemType));
            systemType.invalidate();
         } finally {
            this.dataLock.unlock(lock);
         }

      }
   }

   @Nonnull
   public <T extends EcsEvent> EntityEventType<ECS_TYPE, T> registerEntityEventType(@Nonnull Class<? super T> eventTypeClass) {
      if (this.shutdown) {
         throw new IllegalStateException("Registry has been shutdown");
      } else if (!EcsEvent.class.isAssignableFrom(eventTypeClass)) {
         throw new IllegalArgumentException("eventTypeClass must extend EcsEvent! " + String.valueOf(eventTypeClass));
      } else {
         long lock = this.dataLock.writeLock();

         EntityEventType var5;
         try {
            EntityEventType<ECS_TYPE, T> systemType = this.registerEntityEventType0(eventTypeClass);
            lock = this.dataLock.tryConvertToReadLock(lock);
            this.updateData0(new SystemTypeChange(ChangeType.REGISTERED, systemType));
            var5 = systemType;
         } finally {
            this.dataLock.unlock(lock);
         }

         return var5;
      }
   }

   @Nonnull
   public <T extends EcsEvent> WorldEventType<ECS_TYPE, T> registerWorldEventType(@Nonnull Class<? super T> eventTypeClass) {
      if (this.shutdown) {
         throw new IllegalStateException("Registry has been shutdown");
      } else if (!EcsEvent.class.isAssignableFrom(eventTypeClass)) {
         throw new IllegalArgumentException("eventTypeClass must extend EcsEvent! " + String.valueOf(eventTypeClass));
      } else {
         long lock = this.dataLock.writeLock();

         WorldEventType var5;
         try {
            WorldEventType<ECS_TYPE, T> systemType = this.registerWorldEventType0(eventTypeClass);
            lock = this.dataLock.tryConvertToReadLock(lock);
            this.updateData0(new SystemTypeChange(ChangeType.REGISTERED, systemType));
            var5 = systemType;
         } finally {
            this.dataLock.unlock(lock);
         }

         return var5;
      }
   }

   public <T extends EcsEvent> void unregisterEntityEventType(@Nonnull EntityEventType<ECS_TYPE, T> eventType) {
      if (this.shutdown) {
         throw new IllegalStateException("Registry has been shutdown");
      } else {
         eventType.validate();
         long lock = this.dataLock.writeLock();

         try {
            this.unregisterEntityEventType0(eventType);
            lock = this.dataLock.tryConvertToReadLock(lock);
            this.updateData0(new SystemTypeChange(ChangeType.UNREGISTERED, eventType));
            eventType.invalidate();
         } finally {
            this.dataLock.unlock(lock);
         }

      }
   }

   public <T extends EcsEvent> void unregisterWorldEventType(@Nonnull WorldEventType<ECS_TYPE, T> eventType) {
      if (this.shutdown) {
         throw new IllegalStateException("Registry has been shutdown");
      } else {
         eventType.validate();
         long lock = this.dataLock.writeLock();

         try {
            this.unregisterWorldEventType0(eventType);
            lock = this.dataLock.tryConvertToReadLock(lock);
            this.updateData0(new SystemTypeChange(ChangeType.UNREGISTERED, eventType));
            eventType.invalidate();
         } finally {
            this.dataLock.unlock(lock);
         }

      }
   }

   @Nonnull
   public SystemGroup<ECS_TYPE> registerSystemGroup() {
      return this.registerSystemGroup(Collections.emptySet());
   }

   @Nonnull
   public SystemGroup<ECS_TYPE> registerSystemGroup(Set<Dependency<ECS_TYPE>> dependencies) {
      if (this.shutdown) {
         throw new IllegalStateException("Registry has been shutdown");
      } else {
         long lock = this.dataLock.writeLock();

         SystemGroup var5;
         try {
            SystemGroup<ECS_TYPE> systemGroup = this.registerSystemGroup0(dependencies);
            lock = this.dataLock.tryConvertToReadLock(lock);
            this.updateData0(new SystemGroupChange(ChangeType.REGISTERED, systemGroup));
            var5 = systemGroup;
         } finally {
            this.dataLock.unlock(lock);
         }

         return var5;
      }
   }

   public void unregisterSystemGroup(@Nonnull SystemGroup<ECS_TYPE> systemGroup) {
      if (this.shutdown) {
         throw new IllegalStateException("Registry has been shutdown");
      } else {
         systemGroup.validate();
         long lock = this.dataLock.writeLock();

         try {
            this.unregisterSystemGroup0(systemGroup);
            lock = this.dataLock.tryConvertToReadLock(lock);
            this.updateData0(new SystemGroupChange(ChangeType.UNREGISTERED, systemGroup));
            systemGroup.invalidate();
         } finally {
            this.dataLock.unlock(lock);
         }

      }
   }

   public void registerSystem(@Nonnull ISystem<ECS_TYPE> system) {
      this.registerSystem(system, false);
   }

   /** @deprecated */
   @Deprecated(
      forRemoval = true
   )
   public void registerSystem(@Nonnull ISystem<ECS_TYPE> system, boolean bypassClassCheck) {
      if (this.shutdown) {
         throw new IllegalStateException("Registry has been shutdown");
      } else {
         Class<? extends ISystem> systemClass = system.getClass();
         if (system instanceof QuerySystem) {
            QuerySystem<ECS_TYPE> archetypeSystem = (QuerySystem)system;
            Query<ECS_TYPE> query = archetypeSystem.getQuery();
            query.validateRegistry(this);
            query.validate();
            if (query instanceof ReadWriteArchetypeQuery) {
               ReadWriteArchetypeQuery<ECS_TYPE> readWriteQuery = (ReadWriteArchetypeQuery)query;
               if (readWriteQuery.getReadArchetype().equals(readWriteQuery.getWriteArchetype())) {
                  LOGGER.at(Level.WARNING).log("%s.getQuery() is using ReadWriteArchetypeEntityQuery with the same `Read` and `Modified` Archetype! This can be simplified by using the Archetype directly as the EntityQuery.", systemClass.getName());
               }
            }
         }

         long lock = this.dataLock.writeLock();

         try {
            if (!bypassClassCheck) {
               if (this.systemClasses.containsKey(systemClass)) {
                  throw new IllegalArgumentException("System of type " + systemClass.getName() + " is already registered!");
               }
            } else {
               this.systemBypassClassCheck.put(systemClass, true);
            }

            if (ArrayUtil.indexOf(this.systems, system) != -1) {
               throw new IllegalArgumentException("System is already registered!");
            }

            for(Dependency<ECS_TYPE> dependency : system.getDependencies()) {
               dependency.validate(this);
            }

            this.registerSystem0(system);
            List<DataChange> changes = new ObjectArrayList();
            changes.add(new SystemChange(ChangeType.REGISTERED, system));
            if (system instanceof System) {
               System<ECS_TYPE> theSystem = (System)system;

               for(ComponentRegistration<ECS_TYPE, ?> componentRegistration : theSystem.getComponentRegistrations()) {
                  ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>> componentType = this.registerComponent0(componentRegistration);
                  changes.add(new ComponentChange(ChangeType.REGISTERED, componentType));
               }

               for(ResourceRegistration<ECS_TYPE, ?> resourceRegistration : theSystem.getResourceRegistrations()) {
                  ResourceType<ECS_TYPE, ? extends Resource<ECS_TYPE>> resourceType = this.registerResource0(resourceRegistration);
                  changes.add(new ResourceChange(ChangeType.REGISTERED, resourceType));
               }
            }

            if (system instanceof EntityEventSystem) {
               EntityEventSystem<ECS_TYPE, ?> eventSystem = (EntityEventSystem)system;
               if (!this.entityEventTypeClassToIndex.containsKey(eventSystem.getEventType())) {
                  EntityEventType<ECS_TYPE, ?> eventType = this.registerEntityEventType0(eventSystem.getEventType());
                  changes.add(new SystemTypeChange(ChangeType.REGISTERED, eventType));
               }
            }

            if (system instanceof WorldEventSystem) {
               WorldEventSystem<ECS_TYPE, ?> eventSystem = (WorldEventSystem)system;
               if (!this.worldEventTypeClassToIndex.containsKey(eventSystem.getEventType())) {
                  WorldEventType<ECS_TYPE, ?> eventType = this.registerWorldEventType0(eventSystem.getEventType());
                  changes.add(new SystemTypeChange(ChangeType.REGISTERED, eventType));
               }
            }

            lock = this.dataLock.tryConvertToReadLock(lock);
            this.updateData0((DataChange[])changes.toArray((x$0) -> new DataChange[x$0]));
         } finally {
            this.dataLock.unlock(lock);
         }

      }
   }

   public void unregisterSystem(@Nonnull Class<? extends ISystem<ECS_TYPE>> systemClass) {
      if (this.shutdown) {
         throw new IllegalStateException("Registry has been shutdown");
      } else {
         long lock = this.dataLock.writeLock();

         try {
            int systemIndex = this.systemClasses.getInt(systemClass);
            ISystem<ECS_TYPE> system = this.systems[systemIndex];
            if (system != null) {
               if (system instanceof QuerySystem) {
                  QuerySystem<ECS_TYPE> archetypeSystem = (QuerySystem)system;
                  Query<ECS_TYPE> query = archetypeSystem.getQuery();
                  query.validateRegistry(this);
                  query.validate();
               }

               int unsortedSystemIndex = ArrayUtil.indexOf(this.systems, system);
               if (unsortedSystemIndex == -1) {
                  throw new IllegalArgumentException("System is not registered!");
               }

               this.unregisterSystem0(unsortedSystemIndex, system);
               List<DataChange> changes = new ObjectArrayList();
               changes.add(new SystemChange(ChangeType.UNREGISTERED, system));
               if (system instanceof System) {
                  System<ECS_TYPE> theSystem = (System)system;

                  for(ComponentRegistration<ECS_TYPE, ?> systemComponent : theSystem.getComponentRegistrations()) {
                     this.unregisterComponent0(systemComponent.componentType());
                     changes.add(new ComponentChange(ChangeType.UNREGISTERED, systemComponent.componentType()));
                  }

                  for(ResourceRegistration<ECS_TYPE, ?> systemResource : theSystem.getResourceRegistrations()) {
                     this.unregisterResource0(systemResource.resourceType());
                     changes.add(new ResourceChange(ChangeType.UNREGISTERED, systemResource.resourceType()));
                  }
               }

               lock = this.dataLock.tryConvertToReadLock(lock);
               this.updateData0((DataChange[])changes.toArray((x$0) -> new DataChange[x$0]));
               return;
            }
         } finally {
            this.dataLock.unlock(lock);
         }

      }
   }

   @Nonnull
   public ResourceType<ECS_TYPE, SpatialResource<Ref<ECS_TYPE>, ECS_TYPE>> registerSpatialResource(@Nonnull Supplier<SpatialStructure<Ref<ECS_TYPE>>> supplier) {
      return this.registerResource(SpatialResource.class, () -> new SpatialResource((SpatialStructure)supplier.get()));
   }

   @Nonnull
   public Store<ECS_TYPE> addStore(@Nonnull ECS_TYPE externalData, @Nonnull IResourceStorage resourceStorage) {
      if (this.shutdown) {
         throw new IllegalStateException("Registry has been shutdown");
      } else {
         return this.addStore0(externalData, resourceStorage, (_store) -> {
         });
      }
   }

   @Nonnull
   public Store<ECS_TYPE> addStore(@Nonnull ECS_TYPE externalData, @Nonnull IResourceStorage resourceStorage, Consumer<Store<ECS_TYPE>> consumer) {
      if (this.shutdown) {
         throw new IllegalStateException("Registry has been shutdown");
      } else {
         return this.addStore0(externalData, resourceStorage, consumer);
      }
   }

   public void removeStore(@Nonnull Store<ECS_TYPE> store) {
      if (this.shutdown) {
         throw new IllegalStateException("Registry has been shutdown");
      } else if (store.isShutdown()) {
         throw new IllegalStateException("Store is already shutdown!");
      } else {
         this.removeStore0(store);
      }
   }

   @Nonnull
   public Holder<ECS_TYPE> newHolder() {
      Holder<ECS_TYPE> holder = new Holder<ECS_TYPE>(this);
      this.holders.add(new WeakReference(holder, this.holderReferenceQueue));
      return holder;
   }

   @Nonnull
   public Holder<ECS_TYPE> newHolder(@Nonnull Archetype<ECS_TYPE> archetype, @Nonnull Component<ECS_TYPE>[] components) {
      Holder<ECS_TYPE> holder = new Holder<ECS_TYPE>(this, archetype, components);
      this.holders.add(new WeakReference(holder, this.holderReferenceQueue));
      return holder;
   }

   @Nonnull
   protected Holder<ECS_TYPE> _internal_newEntityHolder() {
      return new Holder<ECS_TYPE>();
   }

   protected Data<ECS_TYPE> _internal_getData() {
      return this.data;
   }

   public Data<ECS_TYPE> getData() {
      this.assertInStoreThread();
      return this.data;
   }

   @Nonnull
   public BuilderCodec<Holder<ECS_TYPE>> getEntityCodec() {
      return this.data.getEntityCodec();
   }

   public void assertInStoreThread() {
      long lock = this.storeLock.readLock();

      try {
         for(int i = 0; i < this.storeSize; ++i) {
            if (this.stores[i].isInThread()) {
               return;
            }
         }

         throw new AssertionError("Data can only be accessed from a store thread!");
      } finally {
         this.storeLock.unlockRead(lock);
      }
   }

   @Nullable
   public Holder<ECS_TYPE> deserialize(@Nonnull BsonDocument entityDocument) {
      Optional<Integer> version = VERSION.get(entityDocument);
      if (version.isPresent()) {
         return this.deserialize(entityDocument, (Integer)version.get());
      } else {
         ExtraInfo extraInfo = (ExtraInfo)ExtraInfo.THREAD_LOCAL.get();
         Holder<ECS_TYPE> holder = this.data.getEntityCodec().decode(entityDocument, extraInfo);
         extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER);
         return holder;
      }
   }

   /** @deprecated */
   @Nullable
   @Deprecated
   public Holder<ECS_TYPE> deserialize(@Nonnull BsonDocument entityDocument, int version) {
      ExtraInfo extraInfo = new ExtraInfo(version);
      Holder<ECS_TYPE> holder = this.data.getEntityCodec().decode(entityDocument, extraInfo);
      extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER);
      return holder;
   }

   public BsonDocument serialize(@Nonnull Holder<ECS_TYPE> holder) {
      ExtraInfo extraInfo = (ExtraInfo)ExtraInfo.THREAD_LOCAL.get();
      BsonDocument document = this.data.getEntityCodec().encode(holder, extraInfo).asDocument();
      extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER);
      return document;
   }

   public boolean hasSystem(@Nonnull ISystem<ECS_TYPE> system) {
      return ArrayUtil.indexOf(this.systems, system, 0, this.systemSize) != -1;
   }

   public <T extends ISystem<ECS_TYPE>> boolean hasSystemClass(@Nonnull Class<T> systemClass) {
      return this.systemClasses.containsKey(systemClass);
   }

   public <T extends ISystem<ECS_TYPE>> boolean hasSystemType(@Nonnull SystemType<ECS_TYPE, T> systemType) {
      return this.systemTypeClassToIndex.containsKey(systemType.getTypeClass());
   }

   public boolean hasSystemGroup(@Nonnull SystemGroup<ECS_TYPE> group) {
      return ArrayUtil.indexOf(this.systemGroups, group) != -1;
   }

   @Nonnull
   private <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent0(@Nonnull ComponentRegistration<ECS_TYPE, T> registration) {
      return this.registerComponent0(registration.typeClass(), registration.id(), registration.codec(), registration.supplier(), registration.componentType());
   }

   @Nonnull
   private <T extends Component<ECS_TYPE>> ComponentType<ECS_TYPE, T> registerComponent0(@Nonnull Class<? super T> tClass, @Nullable String id, @Nullable BuilderCodec<T> codec, @Nonnull Supplier<T> supplier, @Nonnull ComponentType<ECS_TYPE, T> componentType) {
      if (id != null && this.componentIdToIndex.containsKey(id)) {
         throw new IllegalArgumentException("id '" + id + "' already exists!");
      } else {
         int index;
         if (this.componentIndexReuse.isEmpty()) {
            index = this.componentSize++;
         } else {
            index = this.componentIndexReuse.nextSetBit(0);
            this.componentIndexReuse.clear(index);
         }

         if (this.componentIds.length <= index) {
            int newLength = ArrayUtil.grow(index);
            this.componentIds = (String[])Arrays.copyOf(this.componentIds, newLength);
            this.componentCodecs = (BuilderCodec[])Arrays.copyOf(this.componentCodecs, newLength);
            this.componentSuppliers = (Supplier[])Arrays.copyOf(this.componentSuppliers, newLength);
            this.componentTypes = (ComponentType[])Arrays.copyOf(this.componentTypes, newLength);
         }

         componentType.init(this, tClass, index);
         this.componentIdToIndex.put(id, index);
         this.componentIds[index] = id;
         this.componentCodecs[index] = codec;
         this.componentSuppliers[index] = supplier;
         this.componentTypes[index] = componentType;
         return componentType;
      }
   }

   private <T extends Component<ECS_TYPE>> void unregisterComponent0(@Nonnull ComponentType<ECS_TYPE, T> componentType) {
      int componentIndex = componentType.getIndex();
      if (componentIndex == this.componentSize - 1) {
         int highestUsedIndex = this.componentIndexReuse.previousClearBit(componentIndex - 1);
         this.componentSize = highestUsedIndex + 1;
         this.componentIndexReuse.clear(this.componentSize, componentIndex);
      } else {
         this.componentIndexReuse.set(componentIndex);
      }

      this.componentIdToIndex.removeInt(this.componentIds[componentIndex]);
      this.componentIds[componentIndex] = null;
      this.componentCodecs[componentIndex] = null;
      this.componentSuppliers[componentIndex] = null;
      this.componentTypes[componentIndex] = null;
   }

   @Nonnull
   private <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> registerResource0(@Nonnull ResourceRegistration<ECS_TYPE, T> registration) {
      return this.registerResource0(registration.typeClass(), registration.id(), registration.codec(), registration.supplier(), registration.resourceType());
   }

   @Nonnull
   private <T extends Resource<ECS_TYPE>> ResourceType<ECS_TYPE, T> registerResource0(@Nonnull Class<? super T> tClass, @Nullable String id, @Nullable BuilderCodec<T> codec, @Nonnull Supplier<T> supplier, @Nonnull ResourceType<ECS_TYPE, T> resourceType) {
      if (id != null && this.resourceIdToIndex.containsKey(id)) {
         throw new IllegalArgumentException("id '" + id + "' already exists!");
      } else {
         int index;
         if (this.resourceIndexReuse.isEmpty()) {
            index = this.resourceSize++;
         } else {
            index = this.resourceIndexReuse.nextSetBit(0);
            this.resourceIndexReuse.clear(index);
         }

         if (this.resourceIds.length <= index) {
            int newLength = ArrayUtil.grow(index);
            this.resourceIds = (String[])Arrays.copyOf(this.resourceIds, newLength);
            this.resourceCodecs = (BuilderCodec[])Arrays.copyOf(this.resourceCodecs, newLength);
            this.resourceSuppliers = (Supplier[])Arrays.copyOf(this.resourceSuppliers, newLength);
            this.resourceTypes = (ResourceType[])Arrays.copyOf(this.resourceTypes, newLength);
         }

         resourceType.init(this, tClass, index);
         this.resourceIdToIndex.put(id, index);
         this.resourceIds[index] = id;
         this.resourceCodecs[index] = codec;
         this.resourceSuppliers[index] = supplier;
         this.resourceTypes[index] = resourceType;
         return resourceType;
      }
   }

   private <T extends Resource<ECS_TYPE>> void unregisterResource0(@Nonnull ResourceType<ECS_TYPE, T> resourceType) {
      int resourceIndex = resourceType.getIndex();
      if (resourceIndex == this.resourceSize - 1) {
         int highestUsedIndex = this.resourceIndexReuse.previousClearBit(resourceIndex - 1);
         this.resourceSize = highestUsedIndex + 1;
         this.resourceIndexReuse.clear(this.resourceSize, resourceIndex);
      } else {
         this.resourceIndexReuse.set(resourceIndex);
      }

      this.resourceIdToIndex.removeInt(this.resourceIds[resourceIndex]);
      this.resourceIds[resourceIndex] = null;
      this.resourceCodecs[resourceIndex] = null;
      this.resourceSuppliers[resourceIndex] = null;
      this.resourceTypes[resourceIndex] = null;
   }

   @Nonnull
   private <T extends ISystem<ECS_TYPE>> SystemType<ECS_TYPE, T> registerSystemType0(@Nonnull Class<? super T> systemTypeClass) {
      if (this.systemTypeClassToIndex.containsKey(systemTypeClass)) {
         throw new IllegalArgumentException("system type '" + String.valueOf(systemTypeClass) + "' already exists!");
      } else {
         int systemTypeIndex;
         if (this.systemTypeIndexReuse.isEmpty()) {
            systemTypeIndex = this.systemTypeSize++;
         } else {
            systemTypeIndex = this.systemTypeIndexReuse.nextSetBit(0);
            this.systemTypeIndexReuse.clear(systemTypeIndex);
         }

         if (this.systemTypes.length <= systemTypeIndex) {
            this.systemTypes = (SystemType[])Arrays.copyOf(this.systemTypes, ArrayUtil.grow(systemTypeIndex));
            this.systemTypeToSystemIndex = (BitSet[])Arrays.copyOf(this.systemTypeToSystemIndex, ArrayUtil.grow(systemTypeIndex));
         }

         SystemType<ECS_TYPE, T> systemType = new SystemType<ECS_TYPE, T>(this, systemTypeClass, systemTypeIndex);
         this.systemTypeClassToIndex.put(systemTypeClass, systemTypeIndex);
         this.systemTypes[systemTypeIndex] = systemType;
         return systemType;
      }
   }

   private <T extends ISystem<ECS_TYPE>> void unregisterSystemType0(@Nonnull SystemType<ECS_TYPE, T> systemType) {
      int systemTypeIndex = systemType.getIndex();
      if (systemTypeIndex == this.systemTypeSize - 1) {
         int highestUsedIndex = this.systemTypeIndexReuse.previousClearBit(systemTypeIndex - 1);
         this.systemTypeSize = highestUsedIndex + 1;
         this.systemTypeIndexReuse.clear(this.systemTypeSize, systemTypeIndex);
      } else {
         this.systemTypeIndexReuse.set(systemTypeIndex);
      }

      this.systemTypeClassToIndex.removeInt(systemType.getTypeClass());
      this.systemTypes[systemTypeIndex] = null;
      this.systemTypeToSystemIndex[systemTypeIndex].clear();
   }

   @Nonnull
   private <T extends EcsEvent> EntityEventType<ECS_TYPE, T> registerEntityEventType0(@Nonnull Class<? super T> eventTypeClass) {
      if (this.entityEventTypeClassToIndex.containsKey(eventTypeClass)) {
         throw new IllegalArgumentException("event type '" + String.valueOf(eventTypeClass) + "' already exists!");
      } else {
         int systemTypeIndex;
         if (this.systemTypeIndexReuse.isEmpty()) {
            systemTypeIndex = this.systemTypeSize++;
         } else {
            systemTypeIndex = this.systemTypeIndexReuse.nextSetBit(0);
            this.systemTypeIndexReuse.clear(systemTypeIndex);
         }

         if (this.systemTypes.length <= systemTypeIndex) {
            this.systemTypes = (SystemType[])Arrays.copyOf(this.systemTypes, ArrayUtil.grow(systemTypeIndex));
            this.systemTypeToSystemIndex = (BitSet[])Arrays.copyOf(this.systemTypeToSystemIndex, ArrayUtil.grow(systemTypeIndex));
         }

         EntityEventType<ECS_TYPE, T> systemType = new EntityEventType<ECS_TYPE, T>(this, EntityEventSystem.class, eventTypeClass, systemTypeIndex);
         this.entityEventTypeClassToIndex.put(eventTypeClass, systemTypeIndex);
         this.systemTypes[systemTypeIndex] = systemType;
         return systemType;
      }
   }

   private <T extends EcsEvent> void unregisterEntityEventType0(@Nonnull EntityEventType<ECS_TYPE, T> eventType) {
      int systemTypeIndex = eventType.getIndex();
      if (systemTypeIndex == this.systemTypeSize - 1) {
         int highestUsedIndex = this.systemTypeIndexReuse.previousClearBit(systemTypeIndex - 1);
         this.systemTypeSize = highestUsedIndex + 1;
         this.systemTypeIndexReuse.clear(this.systemTypeSize, systemTypeIndex);
      } else {
         this.systemTypeIndexReuse.set(systemTypeIndex);
      }

      this.entityEventTypeClassToIndex.removeInt(eventType.getEventClass());
      this.systemTypes[systemTypeIndex] = null;
      this.systemTypeToSystemIndex[systemTypeIndex].clear();
   }

   @Nullable
   public <T extends EcsEvent> EntityEventType<ECS_TYPE, T> getEntityEventTypeForClass(Class<T> eClass) {
      int index = this.entityEventTypeClassToIndex.getInt(eClass);
      return index == -2147483648 ? null : (EntityEventType)this.systemTypes[index];
   }

   @Nonnull
   private <T extends EcsEvent> WorldEventType<ECS_TYPE, T> registerWorldEventType0(@Nonnull Class<? super T> eventTypeClass) {
      if (this.worldEventTypeClassToIndex.containsKey(eventTypeClass)) {
         throw new IllegalArgumentException("event type '" + String.valueOf(eventTypeClass) + "' already exists!");
      } else {
         int systemTypeIndex;
         if (this.systemTypeIndexReuse.isEmpty()) {
            systemTypeIndex = this.systemTypeSize++;
         } else {
            systemTypeIndex = this.systemTypeIndexReuse.nextSetBit(0);
            this.systemTypeIndexReuse.clear(systemTypeIndex);
         }

         if (this.systemTypes.length <= systemTypeIndex) {
            this.systemTypes = (SystemType[])Arrays.copyOf(this.systemTypes, ArrayUtil.grow(systemTypeIndex));
            this.systemTypeToSystemIndex = (BitSet[])Arrays.copyOf(this.systemTypeToSystemIndex, ArrayUtil.grow(systemTypeIndex));
         }

         WorldEventType<ECS_TYPE, T> systemType = new WorldEventType<ECS_TYPE, T>(this, WorldEventSystem.class, eventTypeClass, systemTypeIndex);
         this.worldEventTypeClassToIndex.put(eventTypeClass, systemTypeIndex);
         this.systemTypes[systemTypeIndex] = systemType;
         return systemType;
      }
   }

   private <T extends EcsEvent> void unregisterWorldEventType0(@Nonnull WorldEventType<ECS_TYPE, T> eventType) {
      int systemTypeIndex = eventType.getIndex();
      if (systemTypeIndex == this.systemTypeSize - 1) {
         int highestUsedIndex = this.systemTypeIndexReuse.previousClearBit(systemTypeIndex - 1);
         this.systemTypeSize = highestUsedIndex + 1;
         this.systemTypeIndexReuse.clear(this.systemTypeSize, systemTypeIndex);
      } else {
         this.systemTypeIndexReuse.set(systemTypeIndex);
      }

      this.worldEventTypeClassToIndex.removeInt(eventType.getEventClass());
      this.systemTypes[systemTypeIndex] = null;
      this.systemTypeToSystemIndex[systemTypeIndex].clear();
   }

   @Nullable
   public <T extends EcsEvent> WorldEventType<ECS_TYPE, T> getWorldEventTypeForClass(Class<T> eClass) {
      int index = this.worldEventTypeClassToIndex.getInt(eClass);
      return index == -2147483648 ? null : (WorldEventType)this.systemTypes[index];
   }

   @Nonnull
   private SystemGroup<ECS_TYPE> registerSystemGroup0(@Nonnull Set<Dependency<ECS_TYPE>> dependencies) {
      int systemGroupIndex;
      if (this.systemGroupIndexReuse.isEmpty()) {
         systemGroupIndex = this.systemGroupSize++;
      } else {
         systemGroupIndex = this.systemGroupIndexReuse.nextSetBit(0);
         this.systemGroupIndexReuse.clear(systemGroupIndex);
      }

      if (this.systemGroups.length <= systemGroupIndex) {
         this.systemGroups = (SystemGroup[])Arrays.copyOf(this.systemGroups, ArrayUtil.grow(systemGroupIndex));
      }

      SystemGroup<ECS_TYPE> systemGroup = new SystemGroup<ECS_TYPE>(this, systemGroupIndex, dependencies);
      this.systemGroups[systemGroupIndex] = systemGroup;
      return systemGroup;
   }

   private void unregisterSystemGroup0(@Nonnull SystemGroup<ECS_TYPE> systemType) {
      int systemGroupIndex = systemType.getIndex();
      if (systemGroupIndex == this.systemGroupSize - 1) {
         int highestUsedIndex = this.systemGroupIndexReuse.previousClearBit(systemGroupIndex - 1);
         this.systemGroupSize = highestUsedIndex + 1;
         this.systemGroupIndexReuse.clear(this.systemGroupSize, systemGroupIndex);
      } else {
         this.systemGroupIndexReuse.set(systemGroupIndex);
      }

      this.systemGroups[systemGroupIndex] = null;
   }

   private void registerSystem0(@Nonnull ISystem<ECS_TYPE> system) {
      int systemIndex = this.systemSize++;
      if (this.systems.length <= systemIndex) {
         this.systems = (ISystem[])Arrays.copyOf(this.systems, ArrayUtil.grow(systemIndex));
      }

      this.systems[systemIndex] = system;
      this.systemClasses.put(system.getClass(), systemIndex);
      system.onSystemRegistered();
   }

   private void unregisterSystem0(int systemIndex, @Nonnull ISystem<ECS_TYPE> system) {
      int lastIndex = this.systemSize - 1;
      if (systemIndex != lastIndex) {
         ISystem<ECS_TYPE> lastSystem = this.systems[lastIndex];
         this.systems[systemIndex] = lastSystem;
         this.systemClasses.put(lastSystem.getClass(), systemIndex);
      }

      this.systems[lastIndex] = null;
      this.systemSize = lastIndex;
      Class<? extends ISystem> systemClass = system.getClass();
      boolean bypassClassCheck = this.systemBypassClassCheck.getBoolean(systemClass);
      if (!bypassClassCheck && !this.systemClasses.remove(systemClass, systemIndex)) {
         String var10002 = systemClass.getName();
         throw new IllegalArgumentException("Failed to remove system " + var10002 + ", " + systemIndex);
      } else {
         system.onSystemUnregistered();
      }
   }

   @Nonnull
   private Store<ECS_TYPE> addStore0(@Nonnull ECS_TYPE externalData, @Nonnull IResourceStorage resourceStorage, Consumer<Store<ECS_TYPE>> consumer) {
      long lock = this.storeLock.writeLock();

      Store var8;
      try {
         int storeIndex = this.storeSize++;
         if (this.stores.length <= storeIndex) {
            this.stores = (Store[])Arrays.copyOf(this.stores, ArrayUtil.grow(storeIndex));
         }

         Store<ECS_TYPE> store = new Store<ECS_TYPE>(this, storeIndex, externalData, resourceStorage);
         this.stores[storeIndex] = store;
         consumer.accept(store);
         store.onAdd(this.data);
         var8 = store;
      } finally {
         this.storeLock.unlockWrite(lock);
      }

      return var8;
   }

   private void removeStore0(@Nonnull Store<ECS_TYPE> store) {
      store.shutdown0(this.data);

      long lock;
      do {
         Thread.onSpinWait();
         this.doDataUpdate();
         lock = this.storeLock.tryWriteLock();
      } while(!this.storeLock.validate(lock));

      try {
         int storeIndex = store.storeIndex;
         int lastIndex = this.storeSize - 1;
         if (storeIndex != lastIndex) {
            Store<ECS_TYPE> lastStore = this.stores[lastIndex];
            lastStore.storeIndex = storeIndex;
            this.stores[storeIndex] = lastStore;
         }

         this.stores[lastIndex] = null;
         this.storeSize = lastIndex;
      } finally {
         this.storeLock.unlockWrite(lock);
      }

   }

   Data<ECS_TYPE> doDataUpdate() {
      return this.data;
   }

   private void updateData0(@Nonnull DataChange... dataChanges) {
      boolean systemChanged = false;
      boolean systemTypeChanged = false;

      for(DataChange dataChange : dataChanges) {
         if (dataChange instanceof SystemChange) {
            systemChanged = true;
         }

         if (dataChange instanceof SystemTypeChange) {
            systemTypeChanged = true;
         }
      }

      if (systemChanged) {
         if (this.sortedSystems.length < this.systems.length) {
            this.sortedSystems = (ISystem[])Arrays.copyOf(this.systems, this.systems.length);
         } else {
            java.lang.System.arraycopy(this.systems, 0, this.sortedSystems, 0, this.systems.length);
         }

         ISystem.calculateOrder(this, this.sortedSystems, this.systemSize);
      }

      if (systemChanged || systemTypeChanged) {
         for(int systemTypeIndex = 0; systemTypeIndex < this.systemTypeSize; ++systemTypeIndex) {
            SystemType<ECS_TYPE, ? extends ISystem<ECS_TYPE>> systemType = this.systemTypes[systemTypeIndex];
            if (systemType != null) {
               BitSet bitSet = this.systemTypeToSystemIndex[systemTypeIndex] = new BitSet();

               for(int systemIndex = 0; systemIndex < this.systemSize; ++systemIndex) {
                  if (systemType.isType(this.sortedSystems[systemIndex])) {
                     bitSet.set(systemIndex);
                  }
               }
            }
         }
      }

      long lock = this.storeLock.readLock();
      this.dataUpdateLock.writeLock().lock();

      try {
         Data<ECS_TYPE> oldData = this.data;
         this.data = new Data<ECS_TYPE>(oldData.getVersion() + 1, this, dataChanges);

         for(int i = 0; i < this.storeSize; ++i) {
            this.stores[i].updateData(oldData, this.data);
         }

         for(Reference<Holder<ECS_TYPE>> holderReference : this.holders) {
            Holder<ECS_TYPE> holder = (Holder)holderReference.get();
            if (holder != null) {
               holder.updateData(oldData, this.data);
            }
         }
      } finally {
         this.dataUpdateLock.writeLock().unlock();
         this.storeLock.unlockRead(lock);
      }

   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.getClass());
      return "ComponentRegistry{super()=" + var10000 + "@" + this.hashCode() + ", shutdown=" + this.shutdown + ", dataLock=" + String.valueOf(this.dataLock) + ", idToIndex=" + String.valueOf(this.componentIdToIndex) + ", componentIndexReuse=" + String.valueOf(this.componentIndexReuse) + ", componentSize=" + this.componentSize + ", componentIds=" + Arrays.toString(this.componentIds) + ", componentCodecs=" + Arrays.toString(this.componentCodecs) + ", componentSuppliers=" + Arrays.toString(this.componentSuppliers) + ", componentTypes=" + Arrays.toString(this.componentTypes) + ", resourceIndexReuse=" + String.valueOf(this.resourceIndexReuse) + ", resourceSize=" + this.resourceSize + ", resourceIds=" + Arrays.toString(this.resourceIds) + ", resourceCodecs=" + Arrays.toString(this.resourceCodecs) + ", resourceSuppliers=" + Arrays.toString(this.resourceSuppliers) + ", resourceTypes=" + Arrays.toString(this.resourceTypes) + ", systemSize=" + this.systemSize + ", systems=" + Arrays.toString(this.systems) + ", sortedSystems=" + Arrays.toString(this.sortedSystems) + ", storeLock=" + String.valueOf(this.storeLock) + ", storeSize=" + this.storeSize + ", stores=" + Arrays.toString(this.stores) + ", data=" + String.valueOf(this.data) + "}";
   }

   public <T extends Component<ECS_TYPE>> T createComponent(@Nonnull ComponentType<ECS_TYPE, T> componentType) {
      long lock = this.dataLock.readLock();

      Component var4;
      try {
         var4 = this.data.createComponent(componentType);
      } finally {
         this.dataLock.unlockRead(lock);
      }

      return (T)var4;
   }

   static {
      VERSION = new KeyedCodec<Integer>("Version", Codec.INTEGER);
      REFERENCE_THREAD_COUNTER = new AtomicInteger();
   }

   public static class Data<ECS_TYPE> {
      private final int version;
      @Nonnull
      private final ComponentRegistry<ECS_TYPE> registry;
      private final Object2IntMap<String> componentIdToIndex;
      private final int componentSize;
      @Nonnull
      private final String[] componentIds;
      @Nonnull
      private final BuilderCodec<? extends Component<ECS_TYPE>>[] componentCodecs;
      @Nonnull
      private final Supplier<? extends Component<ECS_TYPE>>[] componentSuppliers;
      @Nonnull
      private final ComponentType<ECS_TYPE, ? extends Component<ECS_TYPE>>[] componentTypes;
      private final Object2IntMap<String> resourceIdToIndex;
      private final int resourceSize;
      @Nonnull
      private final String[] resourceIds;
      @Nonnull
      private final BuilderCodec<? extends Resource<ECS_TYPE>>[] resourceCodecs;
      @Nonnull
      private final Supplier<? extends Resource<ECS_TYPE>>[] resourceSuppliers;
      @Nonnull
      private final ResourceType<ECS_TYPE, ? extends Resource<ECS_TYPE>>[] resourceTypes;
      private final Object2IntMap<Class<? extends ISystem<ECS_TYPE>>> systemTypeClassToIndex;
      private final int systemTypeSize;
      @Nonnull
      private final SystemType<ECS_TYPE, ? extends ISystem<ECS_TYPE>>[] systemTypes;
      @Nonnull
      private final BitSet[] systemTypeToSystemIndex;
      private final int systemSize;
      @Nonnull
      private final ISystem<ECS_TYPE>[] sortedSystems;
      @Nonnull
      private final Map<String, Codec<Component<ECS_TYPE>>> codecMap;
      @Nonnull
      private final BuilderCodec<Holder<ECS_TYPE>> entityCodec;
      @Nullable
      private final DataChange[] dataChanges;

      private Data(@Nonnull ComponentRegistry<ECS_TYPE> registry) {
         this.version = 0;
         this.registry = registry;
         this.componentIdToIndex = Object2IntMaps.emptyMap();
         this.componentSize = 0;
         this.componentIds = ArrayUtil.EMPTY_STRING_ARRAY;
         this.componentCodecs = BuilderCodec.EMPTY_ARRAY;
         this.componentSuppliers = ArrayUtil.<Component<ECS_TYPE>>emptySupplierArray();
         this.componentTypes = ComponentType.EMPTY_ARRAY;
         this.resourceIdToIndex = Object2IntMaps.emptyMap();
         this.resourceSize = 0;
         this.resourceIds = ArrayUtil.EMPTY_STRING_ARRAY;
         this.resourceCodecs = BuilderCodec.EMPTY_ARRAY;
         this.resourceSuppliers = ArrayUtil.<Resource<ECS_TYPE>>emptySupplierArray();
         this.resourceTypes = ResourceType.EMPTY_ARRAY;
         this.systemTypeClassToIndex = Object2IntMaps.emptyMap();
         this.systemTypeSize = 0;
         this.systemTypes = SystemType.EMPTY_ARRAY;
         this.systemTypeToSystemIndex = ArrayUtil.EMPTY_BITSET_ARRAY;
         this.systemSize = 0;
         this.sortedSystems = ISystem.EMPTY_ARRAY;
         this.codecMap = Collections.emptyMap();
         this.entityCodec = this.createCodec();
         this.dataChanges = null;
      }

      private Data(int version, @Nonnull ComponentRegistry<ECS_TYPE> registry, DataChange... dataChanges) {
         this.version = version;
         this.registry = registry;
         this.componentIdToIndex = new Object2IntOpenHashMap(registry.componentIdToIndex);
         this.componentIdToIndex.defaultReturnValue(-2147483648);
         this.componentSize = registry.componentSize;
         this.componentIds = (String[])Arrays.copyOf(registry.componentIds, this.componentSize);
         this.componentCodecs = (BuilderCodec[])Arrays.copyOf(registry.componentCodecs, this.componentSize);
         this.componentSuppliers = (Supplier[])Arrays.copyOf(registry.componentSuppliers, this.componentSize);
         this.componentTypes = (ComponentType[])Arrays.copyOf(registry.componentTypes, this.componentSize);
         this.resourceIdToIndex = new Object2IntOpenHashMap(registry.resourceIdToIndex);
         this.resourceIdToIndex.defaultReturnValue(-2147483648);
         this.resourceSize = registry.resourceSize;
         this.resourceIds = (String[])Arrays.copyOf(registry.resourceIds, this.resourceSize);
         this.resourceCodecs = (BuilderCodec[])Arrays.copyOf(registry.resourceCodecs, this.resourceSize);
         this.resourceSuppliers = (Supplier[])Arrays.copyOf(registry.resourceSuppliers, this.resourceSize);
         this.resourceTypes = (ResourceType[])Arrays.copyOf(registry.resourceTypes, this.resourceSize);
         this.systemTypeClassToIndex = new Object2IntOpenHashMap(registry.systemTypeClassToIndex);
         this.systemTypeClassToIndex.defaultReturnValue(-2147483648);
         this.systemTypeSize = registry.systemTypeSize;
         this.systemTypes = (SystemType[])Arrays.copyOf(registry.systemTypes, this.systemTypeSize);
         this.systemTypeToSystemIndex = (BitSet[])Arrays.copyOf(registry.systemTypeToSystemIndex, this.systemTypeSize);
         this.systemSize = registry.systemSize;
         this.sortedSystems = (ISystem[])Arrays.copyOf(registry.sortedSystems, this.systemSize);
         Object2ObjectOpenHashMap<String, Codec<Component<ECS_TYPE>>> codecMap = new Object2ObjectOpenHashMap(this.componentSize);

         for(int i = 0; i < this.componentSize; ++i) {
            if (this.componentCodecs[i] != null) {
               codecMap.put(this.componentIds[i], this.componentCodecs[i]);
            }
         }

         this.codecMap = codecMap;
         this.entityCodec = this.createCodec();
         this.dataChanges = dataChanges;
      }

      @Nonnull
      private BuilderCodec<Holder<ECS_TYPE>> createCodec() {
         Function<Codec<Component<ECS_TYPE>>, Codec<Component<ECS_TYPE>>> function = (componentCodec) -> componentCodec != null ? componentCodec : TempUnknownComponent.COMPONENT_CODEC;
         ComponentRegistry var10001 = this.registry;
         Objects.requireNonNull(var10001);
         return BuilderCodec.builder(Holder.class, var10001::newHolder).append(new KeyedCodec("Components", new MapProvidedMapCodec(this.codecMap, function, LinkedHashMap::new, false)), (holder, map) -> holder.loadComponentsMap(this, map), (holder) -> holder.createComponentsMap(this)).add().build();
      }

      public int getVersion() {
         return this.version;
      }

      @Nonnull
      public ComponentRegistry<ECS_TYPE> getRegistry() {
         return this.registry;
      }

      @Nullable
      public ComponentType<ECS_TYPE, ?> getComponentType(String id) {
         int index = this.componentIdToIndex.getInt(id);
         return index == -2147483648 ? null : this.componentTypes[index];
      }

      public int getComponentSize() {
         return this.componentSize;
      }

      @Nullable
      public String getComponentId(@Nonnull ComponentType<ECS_TYPE, ?> componentType) {
         return this.componentIds[componentType.getIndex()];
      }

      @Nullable
      public <T extends Component<ECS_TYPE>> Codec<T> getComponentCodec(@Nonnull ComponentType<ECS_TYPE, T> componentType) {
         return this.componentCodecs[componentType.getIndex()];
      }

      public <T extends Component<ECS_TYPE>> T createComponent(@Nonnull ComponentType<ECS_TYPE, T> componentType) {
         componentType.validateRegistry(this.registry);
         componentType.validate();
         return (T)(this.componentSuppliers[componentType.getIndex()].get());
      }

      public ResourceType<ECS_TYPE, ?> getResourceType(int index) {
         return this.resourceTypes[index];
      }

      @Nullable
      public ResourceType<ECS_TYPE, ?> getResourceType(String id) {
         int index = this.resourceIdToIndex.getInt(id);
         return index == -2147483648 ? null : this.resourceTypes[index];
      }

      public int getResourceSize() {
         return this.resourceSize;
      }

      @Nullable
      public String getResourceId(@Nonnull ResourceType<ECS_TYPE, ?> resourceType) {
         return this.resourceIds[resourceType.getIndex()];
      }

      @Nullable
      public <T extends Resource<ECS_TYPE>> BuilderCodec<T> getResourceCodec(@Nonnull ResourceType<ECS_TYPE, T> resourceType) {
         return this.resourceCodecs[resourceType.getIndex()];
      }

      public <T extends Resource<ECS_TYPE>> T createResource(@Nonnull ResourceType<ECS_TYPE, T> resourceType) {
         resourceType.validateRegistry(this.registry);
         resourceType.validate();
         return (T)(this.resourceSuppliers[resourceType.getIndex()].get());
      }

      public int getSystemTypeSize() {
         return this.systemTypeSize;
      }

      @Nullable
      public <T extends ISystem<ECS_TYPE>> SystemType<ECS_TYPE, T> getSystemType(Class<? super T> systemTypeClass) {
         int systemTypeClassToIndexInt = this.systemTypeClassToIndex.getInt(systemTypeClass);
         return systemTypeClassToIndexInt == -2147483648 ? null : this.systemTypes[systemTypeClassToIndexInt];
      }

      public SystemType<ECS_TYPE, ? extends ISystem<ECS_TYPE>> getSystemType(int systemTypeIndex) {
         return this.systemTypes[systemTypeIndex];
      }

      public <T extends ISystem<ECS_TYPE>> BitSet getSystemIndexesForType(@Nonnull SystemType<ECS_TYPE, T> systemType) {
         return this.systemTypeToSystemIndex[systemType.getIndex()];
      }

      public int getSystemSize() {
         return this.systemSize;
      }

      public ISystem<ECS_TYPE> getSystem(int systemIndex) {
         return this.sortedSystems[systemIndex];
      }

      public <T extends ISystem<ECS_TYPE>> T getSystem(int systemIndex, SystemType<ECS_TYPE, T> systemType) {
         return (T)this.sortedSystems[systemIndex];
      }

      public int indexOf(ISystem<ECS_TYPE> system) {
         int systemIndex = -1;

         for(int i = 0; i < this.sortedSystems.length; ++i) {
            if (this.sortedSystems[i] == system) {
               systemIndex = i;
               break;
            }
         }

         return systemIndex;
      }

      @Nonnull
      public BuilderCodec<Holder<ECS_TYPE>> getEntityCodec() {
         return this.entityCodec;
      }

      public int getDataChangeCount() {
         return this.dataChanges.length;
      }

      public DataChange getDataChange(int index) {
         return this.dataChanges[index];
      }

      public boolean equals(@Nullable Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            Data<?> data = (Data)o;
            return this.version == data.version;
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.version;
      }

      @Nonnull
      public String toString() {
         int var10000 = this.version;
         return "Data{version=" + var10000 + ", componentSize=" + this.componentSize + ", componentSuppliers=" + Arrays.toString(this.componentSuppliers) + ", resourceSize=" + this.resourceSize + ", resourceSuppliers=" + Arrays.toString(this.resourceSuppliers) + ", systemSize=" + this.systemSize + ", sortedSystems=" + Arrays.toString(this.sortedSystems) + ", dataChanges=" + Arrays.toString(this.dataChanges) + "}";
      }

      public void appendDump(@Nonnull String prefix, @Nonnull StringBuilder sb) {
         sb.append(prefix).append("version=").append(this.version).append("\n");
         sb.append(prefix).append("componentSize=").append(this.componentSize).append("\n");
         sb.append(prefix).append("componentSuppliers=").append("\n");

         for(int i = 0; i < this.componentSize; ++i) {
            sb.append(prefix).append("\t- ").append(i).append("\t").append(this.componentSuppliers[i]).append("\n");
         }

         sb.append(prefix).append("resourceSuppliers=").append("\n");

         for(int i = 0; i < this.resourceSize; ++i) {
            sb.append(prefix).append("\t- ").append(i).append("\t").append(this.resourceSuppliers[i]).append("\n");
         }

         sb.append(prefix).append("systemSize=").append(this.systemSize).append("\n");
         sb.append(prefix).append("sortedSystems=").append("\n");

         for(int i = 0; i < this.systemSize; ++i) {
            sb.append(prefix).append("\t- ").append(i).append("\t").append(this.sortedSystems[i]).append("\n");
         }

         sb.append(prefix).append("dataChanges=").append("\n");

         for(int i = 0; i < this.dataChanges.length; ++i) {
            sb.append(prefix).append("\t- ").append(i).append("\t").append(this.dataChanges[i]).append("\n");
         }

      }
   }
}
