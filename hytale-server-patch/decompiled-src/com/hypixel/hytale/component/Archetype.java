package com.hypixel.hytale.component;

import com.hypixel.hytale.component.data.unknown.UnknownComponents;
import com.hypixel.hytale.component.query.ExactArchetypeQuery;
import com.hypixel.hytale.component.query.Query;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Archetype<ECS_TYPE> implements Query<ECS_TYPE> {
   @Nonnull
   private static final Archetype EMPTY;
   private final int minIndex;
   private final int count;
   @Nonnull
   private final ComponentType<ECS_TYPE, ?>[] componentTypes;
   @Nonnull
   private final ExactArchetypeQuery<ECS_TYPE> exactQuery = new ExactArchetypeQuery<ECS_TYPE>(this);

   public static <ECS_TYPE> Archetype<ECS_TYPE> empty() {
      return EMPTY;
   }

   private Archetype(int minIndex, int count, @Nonnull ComponentType<ECS_TYPE, ?>[] componentTypes) {
      this.minIndex = minIndex;
      this.count = count;
      this.componentTypes = componentTypes;
   }

   public int getMinIndex() {
      return this.minIndex;
   }

   public int count() {
      return this.count;
   }

   public int length() {
      return this.componentTypes.length;
   }

   @Nullable
   public ComponentType<ECS_TYPE, ?> get(int index) {
      return this.componentTypes[index];
   }

   public boolean isEmpty() {
      return this.componentTypes.length == 0;
   }

   public boolean contains(@Nonnull ComponentType<ECS_TYPE, ?> componentType) {
      int index = componentType.getIndex();
      return index < this.componentTypes.length && this.componentTypes[index] == componentType;
   }

   public boolean contains(@Nonnull Archetype<ECS_TYPE> archetype) {
      if (this != archetype && !archetype.isEmpty()) {
         for(int i = archetype.minIndex; i < archetype.componentTypes.length; ++i) {
            ComponentType<ECS_TYPE, ?> componentType = archetype.componentTypes[i];
            if (componentType != null && !this.contains(componentType)) {
               return false;
            }
         }

         return true;
      } else {
         return true;
      }
   }

   public void validateComponentType(@Nonnull ComponentType<ECS_TYPE, ?> componentType) {
      if (!this.contains(componentType)) {
         String var10002 = String.valueOf(componentType);
         throw new IllegalArgumentException("ComponentType is not in archetype: " + var10002 + ", " + String.valueOf(this));
      }
   }

   public void validateComponents(@Nonnull Component<ECS_TYPE>[] components, @Nullable ComponentType<ECS_TYPE, UnknownComponents<ECS_TYPE>> ignore) {
      int len = Math.max(this.componentTypes.length, components.length);

      for(int index = 0; index < len; ++index) {
         ComponentType<ECS_TYPE, ?> componentType = index >= this.componentTypes.length ? null : this.componentTypes[index];
         Component<ECS_TYPE> component = index >= components.length ? null : components[index];
         if (componentType == null) {
            if (component != null && (ignore == null || index != ignore.getIndex())) {
               throw new IllegalStateException("Invalid component at index " + index + " expected null but found " + String.valueOf(component.getClass()));
            }
         } else {
            Class<?> typeClass = componentType.getTypeClass();
            if (component == null) {
               throw new IllegalStateException("Invalid component at index " + index + " expected " + String.valueOf(typeClass) + " but found null");
            }

            Class<? extends Component> aClass = component.getClass();
            if (!aClass.equals(typeClass)) {
               throw new IllegalStateException("Invalid component at index " + index + " expected " + String.valueOf(typeClass) + " but found " + String.valueOf(aClass));
            }
         }
      }

   }

   public boolean hasSerializableComponents(@Nonnull ComponentRegistry.Data<ECS_TYPE> data) {
      if (this.isEmpty()) {
         return false;
      } else if (this.contains(data.getRegistry().getNonSerializedComponentType())) {
         return false;
      } else {
         for(int index = this.minIndex; index < this.componentTypes.length; ++index) {
            ComponentType<ECS_TYPE, ?> componentType = this.componentTypes[index];
            if (componentType != null && data.getComponentCodec(componentType) != null) {
               return true;
            }
         }

         return false;
      }
   }

   @Nonnull
   public Archetype<ECS_TYPE> getSerializableArchetype(@Nonnull ComponentRegistry.Data<ECS_TYPE> data) {
      if (this.isEmpty()) {
         return EMPTY;
      } else if (this.contains(data.getRegistry().getNonSerializedComponentType())) {
         return EMPTY;
      } else {
         int lastSerializableIndex = this.componentTypes.length - 1;

         for(int index = this.componentTypes.length - 1; index >= this.minIndex; --index) {
            ComponentType<ECS_TYPE, ?> componentType = this.componentTypes[index];
            if (componentType != null && data.getComponentCodec(componentType) != null) {
               lastSerializableIndex = index;
               break;
            }
         }

         if (lastSerializableIndex < this.minIndex) {
            return EMPTY;
         } else {
            ComponentType<ECS_TYPE, ?>[] serializableComponentTypes = new ComponentType[lastSerializableIndex + 1];
            int serializableMinIndex = this.minIndex;

            for(int index = serializableMinIndex; index < serializableComponentTypes.length; ++index) {
               ComponentType<ECS_TYPE, ?> componentType = this.componentTypes[index];
               if (componentType != null && data.getComponentCodec(componentType) != null) {
                  serializableMinIndex = Math.min(serializableMinIndex, index);
                  serializableComponentTypes[index] = componentType;
               }
            }

            return new Archetype<ECS_TYPE>(this.minIndex, serializableComponentTypes.length, serializableComponentTypes);
         }
      }
   }

   @Nonnull
   public ExactArchetypeQuery<ECS_TYPE> asExactQuery() {
      return this.exactQuery;
   }

   @Nonnull
   public static <ECS_TYPE> Archetype<ECS_TYPE> of(@Nonnull ComponentType<ECS_TYPE, ?> componentTypes) {
      if (componentTypes == null) {
         throw new IllegalArgumentException("ComponentType in Archetype cannot be null");
      } else {
         int index = componentTypes.getIndex();
         ComponentType<ECS_TYPE, ?>[] arr = new ComponentType[index + 1];
         arr[index] = componentTypes;
         return new Archetype<ECS_TYPE>(index, 1, arr);
      }
   }

   @SafeVarargs
   public static <ECS_TYPE> Archetype<ECS_TYPE> of(@Nonnull ComponentType<ECS_TYPE, ?>... componentTypes) {
      if (componentTypes.length == 0) {
         return EMPTY;
      } else {
         for(int i = 0; i < componentTypes.length; ++i) {
            ComponentType<ECS_TYPE, ?> componentType = componentTypes[i];
            if (componentType == null) {
               throw new IllegalArgumentException("ComponentType in Archetype cannot be null (Index: " + i + ")");
            }
         }

         ComponentRegistry<ECS_TYPE> registry = componentTypes[0].getRegistry();
         int minIndex = 2147483647;
         int maxIndex = -2147483648;

         for(int i = 0; i < componentTypes.length; ++i) {
            componentTypes[i].validateRegistry(registry);
            int index = componentTypes[i].getIndex();
            if (index < minIndex) {
               minIndex = index;
            }

            if (index > maxIndex) {
               maxIndex = index;
            }

            for(int n = i + 1; n < componentTypes.length; ++n) {
               if (componentTypes[i] == componentTypes[n]) {
                  throw new IllegalArgumentException("ComponentType provided multiple times! " + Arrays.toString(componentTypes));
               }
            }
         }

         ComponentType<ECS_TYPE, ?>[] arr = new ComponentType[maxIndex + 1];

         for(ComponentType<ECS_TYPE, ?> componentType : componentTypes) {
            arr[componentType.getIndex()] = componentType;
         }

         return new Archetype<ECS_TYPE>(minIndex, componentTypes.length, arr);
      }
   }

   @Nonnull
   public static <ECS_TYPE, T extends Component<ECS_TYPE>> Archetype<ECS_TYPE> add(@Nonnull Archetype<ECS_TYPE> archetype, @Nonnull ComponentType<ECS_TYPE, T> componentType) {
      if (archetype.isEmpty()) {
         return of(componentType);
      } else if (archetype.contains(componentType)) {
         String var10002 = String.valueOf(archetype);
         throw new IllegalArgumentException("ComponentType is already in Archetype! " + var10002 + ", " + String.valueOf(componentType));
      } else {
         archetype.validateRegistry(componentType.getRegistry());
         int index = componentType.getIndex();
         int minIndex = Math.min(index, archetype.minIndex);
         int newLength = Math.max(index + 1, archetype.componentTypes.length);
         ComponentType<ECS_TYPE, ?>[] arr = (ComponentType[])Arrays.copyOf(archetype.componentTypes, newLength);
         arr[index] = componentType;
         return new Archetype<ECS_TYPE>(minIndex, archetype.count + 1, arr);
      }
   }

   public static <ECS_TYPE, T extends Component<ECS_TYPE>> Archetype<ECS_TYPE> remove(@Nonnull Archetype<ECS_TYPE> archetype, @Nonnull ComponentType<ECS_TYPE, T> componentType) {
      if (archetype.isEmpty()) {
         throw new IllegalArgumentException("Archetype is already empty!");
      } else if (!archetype.contains(componentType)) {
         String var10002 = String.valueOf(archetype);
         throw new IllegalArgumentException("Archetype doesn't contain ComponentType! " + var10002 + ", " + String.valueOf(componentType));
      } else {
         int oldLength = archetype.componentTypes.length;
         int oldMinIndex = archetype.minIndex;
         int oldMaxIndex = oldLength - 1;
         if (oldMinIndex == oldMaxIndex) {
            return EMPTY;
         } else {
            int newCount = archetype.count - 1;
            int index = componentType.getIndex();
            if (index == oldMaxIndex) {
               int maxIndex;
               for(maxIndex = index - 1; maxIndex > oldMinIndex && archetype.componentTypes[maxIndex] == null; --maxIndex) {
               }

               return new Archetype<ECS_TYPE>(oldMinIndex, newCount, (ComponentType[])Arrays.copyOf(archetype.componentTypes, maxIndex + 1));
            } else {
               ComponentType<ECS_TYPE, ?>[] arr = (ComponentType[])Arrays.copyOf(archetype.componentTypes, oldLength);
               arr[index] = null;
               if (index != oldMinIndex) {
                  return new Archetype<ECS_TYPE>(oldMinIndex, newCount, arr);
               } else {
                  int minIndex;
                  for(minIndex = index + 1; minIndex < oldLength && arr[minIndex] == null; ++minIndex) {
                  }

                  return new Archetype<ECS_TYPE>(minIndex, newCount, arr);
               }
            }
         }
      }
   }

   public boolean test(@Nonnull Archetype<ECS_TYPE> archetype) {
      return archetype.contains(this);
   }

   public boolean requiresComponentType(@Nonnull ComponentType<ECS_TYPE, ?> componentType) {
      return this.contains(componentType);
   }

   public void validateRegistry(@Nonnull ComponentRegistry<ECS_TYPE> registry) {
      if (!this.isEmpty()) {
         this.componentTypes[this.minIndex].validateRegistry(registry);
      }
   }

   public void validate() {
      for(int i = this.minIndex; i < this.componentTypes.length; ++i) {
         ComponentType<ECS_TYPE, ?> componentType = this.componentTypes[i];
         if (componentType != null) {
            componentType.validate();
         }
      }

   }

   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Archetype<?> archetype = (Archetype)o;
         return Arrays.equals(this.componentTypes, archetype.componentTypes);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.componentTypes);
   }

   @Nonnull
   public String toString() {
      return "Archetype{componentTypes=" + Arrays.toString(this.componentTypes) + "}";
   }

   static {
      EMPTY = new Archetype(0, 0, ComponentType.EMPTY_ARRAY);
   }
}
