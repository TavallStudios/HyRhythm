package com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers;

import com.hypixel.hytale.builtin.hytalegenerator.ArrayUtil;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.performanceinstruments.MemInstrument;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NSimplePixelBuffer<T> extends NPixelBuffer<T> {
   @Nonnull
   private static final Bounds3i bounds;
   @Nonnull
   private final Class<T> pixelType;
   @Nonnull
   private State state;
   @Nullable
   private ArrayContents<T> arrayContents;
   @Nullable
   private T singleValue;

   public NSimplePixelBuffer(@Nonnull Class<T> pixelType) {
      this.pixelType = pixelType;
      this.state = NSimplePixelBuffer.State.EMPTY;
      this.arrayContents = null;
      this.singleValue = null;
   }

   @Nullable
   public T getPixelContent(@Nonnull Vector3i position) {
      assert bounds.contains(position);

      Object var10000;
      switch (this.state.ordinal()) {
         case 1 -> var10000 = this.singleValue;
         case 2 -> var10000 = this.arrayContents.array[index(position)];
         default -> var10000 = null;
      }

      return (T)var10000;
   }

   public void setPixelContent(@Nonnull Vector3i position, @Nullable T value) {
      assert bounds.contains(position);

      switch (this.state.ordinal()) {
         case 1:
            if (this.singleValue == value) {
               return;
            }

            this.switchFromSingleValueToArray();
            this.setPixelContent(position, value);
            break;
         case 2:
            this.arrayContents.array[index(position)] = value;
            break;
         default:
            this.state = NSimplePixelBuffer.State.SINGLE_VALUE;
            this.singleValue = value;
      }

   }

   @Nonnull
   public Class<T> getPixelType() {
      return this.pixelType;
   }

   public void copyFrom(@Nonnull NSimplePixelBuffer<T> sourceBuffer) {
      this.state = sourceBuffer.state;
      switch (this.state.ordinal()) {
         case 1:
            this.singleValue = sourceBuffer.singleValue;
            break;
         case 2:
            this.arrayContents = new ArrayContents<T>();
            ArrayUtil.copy(sourceBuffer.arrayContents.array, this.arrayContents.array);
            break;
         default:
            return;
      }

   }

   @Nonnull
   public MemInstrument.Report getMemoryUsage() {
      long size_bytes = 128L;
      if (this.arrayContents != null) {
         size_bytes += this.arrayContents.getMemoryUsage().size_bytes();
      }

      return new MemInstrument.Report(size_bytes);
   }

   private void ensureContents() {
      if (this.arrayContents == null) {
         this.arrayContents = new ArrayContents<T>();
      }
   }

   private void switchFromSingleValueToArray() {
      assert this.state == NSimplePixelBuffer.State.SINGLE_VALUE;

      this.state = NSimplePixelBuffer.State.ARRAY;
      this.arrayContents = new ArrayContents<T>();
      Arrays.fill(this.arrayContents.array, this.singleValue);
      this.singleValue = null;
   }

   private static int index(@Nonnull Vector3i position) {
      return position.y + position.x * SIZE.y + position.z * SIZE.y * SIZE.x;
   }

   static {
      bounds = new Bounds3i(Vector3i.ZERO, SIZE);
   }

   public static class ArrayContents<T> implements MemInstrument {
      @Nonnull
      private final T[] array;

      public ArrayContents() {
         this.array = (T[])(new Object[NPixelBuffer.SIZE.x * NPixelBuffer.SIZE.y * NPixelBuffer.SIZE.z]);
      }

      @Nonnull
      public MemInstrument.Report getMemoryUsage() {
         long size_bytes = 16L + 8L * (long)this.array.length;
         return new MemInstrument.Report(size_bytes);
      }
   }

   private static enum State {
      EMPTY,
      SINGLE_VALUE,
      ARRAY;
   }
}
