package com.hypixel.hytale.builtin.hytalegenerator.datastructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;

public class WeightedMap<T> {
   @Nonnull
   private final Set<T> elementSet;
   @Nonnull
   private final List<T> elements;
   @Nonnull
   private final List<Double> weights;
   @Nonnull
   private final Map<T, Integer> indices;
   private double totalWeight;
   private boolean immutable;

   public WeightedMap(@Nonnull WeightedMap<T> other) {
      this.totalWeight = 0.0;
      this.immutable = false;
      this.totalWeight = other.totalWeight;
      this.elementSet = new HashSet(other.elementSet);
      this.elements = new ArrayList(other.elements);
      this.weights = new ArrayList(other.weights);
      this.indices = new HashMap(other.indices);
      this.immutable = other.immutable;
   }

   public WeightedMap() {
      this(2);
   }

   public WeightedMap(int initialCapacity) {
      this.totalWeight = 0.0;
      this.immutable = false;
      this.elementSet = new HashSet(initialCapacity);
      this.elements = new ArrayList(initialCapacity);
      this.weights = new ArrayList(initialCapacity);
      this.indices = new HashMap(initialCapacity);
   }

   @Nonnull
   public WeightedMap<T> add(@Nonnull T element, double weight) {
      if (element == null) {
         throw new NullPointerException();
      } else if (this.immutable) {
         throw new IllegalStateException("method can't be called when object is immutable");
      } else if (weight < 0.0) {
         throw new IllegalArgumentException("weight must be positive");
      } else {
         this.elements.add(element);
         this.weights.add(weight);
         this.elementSet.add(element);
         this.totalWeight += weight;
         this.indices.put(element, this.indices.size());
         return this;
      }
   }

   public double get(@Nonnull T element) {
      if (element == null) {
         throw new NullPointerException();
      } else if (this.immutable) {
         throw new IllegalStateException("method can't be called when object is immutable");
      } else {
         return !this.elementSet.contains(element) ? 0.0 : (Double)this.weights.get((Integer)this.indices.get(element));
      }
   }

   public T pick(@Nonnull Random rand) {
      if (rand == null) {
         throw new NullPointerException();
      } else if (this.elements.isEmpty()) {
         throw new IllegalStateException("can't be empty when calling this method");
      } else {
         double pointer = rand.nextDouble() * this.totalWeight;

         for(int i = 0; i < this.elements.size(); ++i) {
            pointer -= (Double)this.weights.get(i);
            if (pointer <= 0.0) {
               return (T)this.elements.get(i);
            }
         }

         return (T)this.elements.getLast();
      }
   }

   public int size() {
      return this.elements.size();
   }

   @Nonnull
   public List<T> allElements() {
      return new ArrayList(this.elements);
   }

   public void makeImmutable() {
      this.immutable = true;
   }

   public boolean isImmutable() {
      return this.immutable;
   }

   public void forEach(@Nonnull BiConsumer<T, Double> consumer) {
      for(int i = 0; i < this.elements.size(); ++i) {
         consumer.accept(this.elements.get(i), (Double)this.weights.get(i));
      }

   }

   @Nonnull
   public String toString() {
      String var10000 = String.valueOf(this.elementSet);
      return "WeighedMap{elementSet=" + var10000 + ", elements=" + String.valueOf(this.elements) + ", weights=" + String.valueOf(this.weights) + ", indices=" + String.valueOf(this.indices) + ", totalWeight=" + this.totalWeight + ", immutable=" + this.immutable + "}";
   }
}
