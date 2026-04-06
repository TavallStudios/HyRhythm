package com.hypixel.hytale.server.flock.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.codec.validation.validator.IntArrayValidator;
import com.hypixel.hytale.math.random.RandomExtra;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class RangeSizeFlockAsset extends FlockAsset {
   public static final BuilderCodec<RangeSizeFlockAsset> CODEC;
   private static final int[] DEFAULT_SIZE;
   protected int[] size;

   protected RangeSizeFlockAsset(String id) {
      super(id);
      this.size = DEFAULT_SIZE;
   }

   protected RangeSizeFlockAsset() {
      this.size = DEFAULT_SIZE;
   }

   public int[] getSize() {
      return this.size;
   }

   public int getMinFlockSize() {
      return this.size[0];
   }

   public int pickFlockSize() {
      return RandomExtra.randomRange(Math.max(1, this.size[0]), this.size[1]);
   }

   @Nonnull
   public static RangeSizeFlockAsset getUnknownFor(String id) {
      return new RangeSizeFlockAsset(id);
   }

   @Nonnull
   public String toString() {
      String var10000 = Arrays.toString(this.size);
      return "RangeSizeFlockAsset{size=" + var10000 + "} " + super.toString();
   }

   static {
      CODEC = ((BuilderCodec.Builder)((BuilderCodec.Builder)BuilderCodec.builder(RangeSizeFlockAsset.class, RangeSizeFlockAsset::new, ABSTRACT_CODEC).documentation("A flock definition in which the initial random size is picked from a range.")).appendInherited(new KeyedCodec("Size", Codec.INT_ARRAY), (flock, o) -> flock.size = o, (flock) -> flock.size, (flock, parent) -> flock.size = parent.size).documentation("An array with two values specifying the random range from which to pick the size of the flock when it spawns. e.g. [ 2, 4 ] will randomly pick a size between two and four (inclusive).").addValidator(Validators.nonNull()).addValidator(Validators.intArraySize(2)).addValidator(new IntArrayValidator(Validators.greaterThan(0))).add()).build();
      DEFAULT_SIZE = new int[]{1, 1};
   }
}
