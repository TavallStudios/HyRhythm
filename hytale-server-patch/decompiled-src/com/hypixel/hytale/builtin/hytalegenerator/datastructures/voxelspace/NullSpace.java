package com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace;

import com.hypixel.hytale.math.vector.Vector3i;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NullSpace<V> implements VoxelSpace<V> {
   @Nonnull
   private static final NullSpace INSTANCE = new NullSpace();

   @Nonnull
   public static <V> NullSpace<V> instance() {
      return INSTANCE;
   }

   @Nonnull
   public static <V> NullSpace<V> instance(@Nonnull Class<V> clazz) {
      return INSTANCE;
   }

   private NullSpace() {
   }

   public boolean set(V content, int x, int y, int z) {
      return false;
   }

   public boolean set(V content, @Nonnull Vector3i position) {
      return this.set(content, position.x, position.y, position.z);
   }

   public void set(V content) {
   }

   public void setOrigin(int x, int y, int z) {
   }

   @Nullable
   public V getContent(int x, int y, int z) {
      return null;
   }

   @Nullable
   public V getContent(@Nonnull Vector3i position) {
      return (V)this.getContent(position.x, position.y, position.z);
   }

   public boolean replace(V replacement, int x, int y, int z, @Nonnull Predicate<V> mask) {
      return false;
   }

   public void pasteFrom(@Nonnull VoxelSpace<V> source) {
   }

   public int getOriginX() {
      return 0;
   }

   public int getOriginY() {
      return 0;
   }

   public int getOriginZ() {
      return 0;
   }

   @Nonnull
   public String getName() {
      return "null_space";
   }

   public boolean isInsideSpace(int x, int y, int z) {
      return false;
   }

   public boolean isInsideSpace(@Nonnull Vector3i position) {
      return this.isInsideSpace(position.x, position.y, position.z);
   }

   public void forEach(VoxelConsumer<? super V> action) {
   }

   public int minX() {
      return 0;
   }

   public int maxX() {
      return 0;
   }

   public int minY() {
      return 0;
   }

   public int maxY() {
      return 0;
   }

   public int minZ() {
      return 0;
   }

   public int maxZ() {
      return 0;
   }

   public int sizeX() {
      return 0;
   }

   public int sizeY() {
      return 0;
   }

   public int sizeZ() {
      return 0;
   }
}
