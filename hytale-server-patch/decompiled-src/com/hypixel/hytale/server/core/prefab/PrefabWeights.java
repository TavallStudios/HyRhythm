package com.hypixel.hytale.server.core.prefab;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.Object2DoubleMapCodec;
import com.hypixel.hytale.codec.validation.LegacyValidator;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.common.util.ArrayUtil;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMaps;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabWeights {
   public static final Supplier<Object2DoubleMap<String>> MAP_SUPPLIER = Object2DoubleOpenHashMap::new;
   public static final Codec<Object2DoubleMap<String>> MAP_CODEC;
   public static final Codec<PrefabWeights> CODEC;
   public static final PrefabWeights NONE;
   public static final double DEFAULT_WEIGHT = 1.0;
   public static final char DELIMITER_CHAR = ',';
   public static final char ASSIGNMENT_CHAR = '=';
   private double defaultWeight;
   private Object2DoubleMap<String> weightsLookup;
   protected double sum;
   protected double[] weights;
   protected volatile boolean initialized;

   public PrefabWeights() {
      this((Object2DoubleMap)MAP_SUPPLIER.get());
   }

   private PrefabWeights(Object2DoubleMap<String> weights) {
      this.weightsLookup = weights;
      this.defaultWeight = 1.0;
   }

   public int size() {
      return this.weightsLookup.size();
   }

   @Nullable
   public <T> T get(@Nonnull T[] elements, @Nonnull Function<T, String> nameFunc, @Nonnull Random random) {
      return (T)this.get(elements, nameFunc, random.nextDouble());
   }

   @Nullable
   public <T> T get(@Nonnull T[] elements, @Nonnull Function<T, String> nameFunc, double value) {
      if (value < 0.0) {
         return null;
      } else {
         this.initialize(elements, nameFunc);
         if (this.weights.length != elements.length) {
            return null;
         } else {
            double weightedValue = Math.min(value, 0.99999) * this.sum;

            for(int i = 0; i < this.weights.length; ++i) {
               if (weightedValue <= this.weights[i]) {
                  return (T)elements[i];
               }
            }

            return null;
         }
      }
   }

   public double getWeight(String prefab) {
      return this.weightsLookup.getOrDefault(prefab, this.defaultWeight);
   }

   public void setWeight(String prefab, double weight) {
      if (this != NONE) {
         checkWeight(prefab, weight);
         this.weightsLookup.put(prefab, weight);
      }
   }

   public void removeWeight(String prefab) {
      if (this != NONE) {
         this.weightsLookup.removeDouble(prefab);
      }
   }

   public double getDefaultWeight() {
      return this.defaultWeight;
   }

   public void setDefaultWeight(double defaultWeight) {
      if (this != NONE) {
         this.defaultWeight = Math.max(0.0, defaultWeight);
      }
   }

   @Nonnull
   public String getMappingString() {
      if (this.weightsLookup.isEmpty()) {
         return "";
      } else {
         StringBuilder sb = new StringBuilder();

         Object2DoubleMap.Entry<String> entry;
         for(ObjectIterator var2 = Object2DoubleMaps.fastIterable(this.weightsLookup).iterator(); var2.hasNext(); sb.append((String)entry.getKey()).append('=').append(entry.getDoubleValue())) {
            entry = (Object2DoubleMap.Entry)var2.next();
            if (!sb.isEmpty()) {
               sb.append(',').append(' ');
            }
         }

         return sb.toString();
      }
   }

   @Nonnull
   public String toString() {
      double var10000 = this.defaultWeight;
      return "PrefabWeights{default=" + var10000 + ", weights=" + this.getMappingString() + "}";
   }

   private <T> void initialize(@Nonnull T[] elements, @Nonnull Function<T, String> nameFunc) {
      if (!this.initialized) {
         synchronized(this) {
            if (!this.initialized) {
               double sum = 0.0;
               double[] weights = new double[elements.length];

               for(int i = 0; i < elements.length; ++i) {
                  String name = (String)nameFunc.apply(elements[i]);
                  sum += this.getWeight(name);
                  weights[i] = sum;
               }

               this.sum = sum;
               this.weights = weights;
               this.initialized = true;
            }
         }
      }
   }

   @Nonnull
   public static PrefabWeights parse(@Nonnull String mappingString) {
      Object2DoubleMap<String> map = null;

      int endPoint;
      for(int startPoint = 0; startPoint < mappingString.length(); startPoint = endPoint + 1) {
         endPoint = mappingString.indexOf(44, startPoint);
         if (endPoint == -1) {
            endPoint = mappingString.length();
         }

         int equalsPoint = mappingString.indexOf(61, startPoint);
         if (equalsPoint <= startPoint) {
            break;
         }

         String name = mappingString.substring(startPoint, equalsPoint).trim();
         String value = mappingString.substring(equalsPoint + 1, endPoint).trim();
         double weight = Double.parseDouble(value);
         if (map == null) {
            map = (Object2DoubleMap)MAP_SUPPLIER.get();
         }

         map.put(name, weight);
      }

      return map == null ? NONE : new PrefabWeights(map);
   }

   public Set<Object2DoubleMap.Entry<String>> entrySet() {
      return this.weightsLookup.object2DoubleEntrySet();
   }

   private static void checkWeight(String prefab, double weight) {
      if (weight < 0.0) {
         throw new IllegalArgumentException(String.format("Negative weight %.5f assigned to prefab %s", weight, prefab));
      }
   }

   static {
      MAP_CODEC = new Object2DoubleMapCodec<Object2DoubleMap<String>>(Codec.STRING, MAP_SUPPLIER, false);
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(PrefabWeights.class, PrefabWeights::new).append(new KeyedCodec("Default", Codec.DOUBLE), (weights, def) -> weights.defaultWeight = def, (weights) -> weights.defaultWeight).documentation("The default weight to use for entries that are not specifically mapped to a weight value.").addValidator(Validators.greaterThanOrEqual(0.0)).add()).append(new KeyedCodec("Weights", MAP_CODEC), (weights, map) -> weights.weightsLookup = map, (weights) -> weights.weightsLookup).documentation("The mapping of prefab names to weight values.").addValidator(new WeightMapValidator()).add()).build();
      NONE = new PrefabWeights(Object2DoubleMaps.emptyMap()) {
         {
            this.sum = 0.0;
            this.weights = ArrayUtil.EMPTY_DOUBLE_ARRAY;
            this.initialized = true;
         }
      };
   }

   private static class WeightMapValidator implements LegacyValidator<Object2DoubleMap<String>> {
      public void accept(@Nonnull Object2DoubleMap<String> stringObject2DoubleMap, ValidationResults results) {
         ObjectIterator var3 = Object2DoubleMaps.fastIterable(stringObject2DoubleMap).iterator();

         while(var3.hasNext()) {
            Object2DoubleMap.Entry<String> entry = (Object2DoubleMap.Entry)var3.next();
            PrefabWeights.checkWeight((String)entry.getKey(), entry.getDoubleValue());
         }

      }
   }
}
