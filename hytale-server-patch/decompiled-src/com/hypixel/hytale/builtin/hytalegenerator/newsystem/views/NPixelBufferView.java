package com.hypixel.hytale.builtin.hytalegenerator.newsystem.views;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace.VoxelConsumer;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace.VoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.GridUtils;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.NBufferBundle;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.bufferbundle.buffers.NPixelBuffer;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NPixelBufferView<T> implements VoxelSpace<T> {
   public static final int Y_LEVEL_BUFFER_GRID = 0;
   public static final int Y_LEVEL_VOXEL_GRID = 0;
   @Nonnull
   private final Class<T> voxelType;
   @Nonnull
   private final NBufferBundle.Access.View bufferAccess;
   @Nonnull
   private final Bounds3i bounds_voxelGrid;
   @Nonnull
   private final Vector3i size_voxelGrid;

   public NPixelBufferView(@Nonnull NBufferBundle.Access.View bufferAccess, @Nonnull Class<T> pixelType) {
      assert bufferAccess.getBounds_bufferGrid().min.y <= 0 && bufferAccess.getBounds_bufferGrid().max.y > 0;

      this.bufferAccess = bufferAccess;
      this.voxelType = pixelType;
      this.bounds_voxelGrid = bufferAccess.getBounds_bufferGrid();
      GridUtils.toVoxelGrid_fromBufferGrid(this.bounds_voxelGrid);
      this.bounds_voxelGrid.min.y = 0;
      this.bounds_voxelGrid.max.y = 1;
      this.size_voxelGrid = this.bounds_voxelGrid.getSize();
   }

   public boolean set(T content, int x, int y, int z) {
      return this.set(content, new Vector3i(x, y, z));
   }

   public boolean set(T value, @Nonnull Vector3i position_voxelGrid) {
      assert this.bounds_voxelGrid.contains(position_voxelGrid);

      NPixelBuffer<T> buffer = this.getBuffer(position_voxelGrid);
      Vector3i positionInBuffer_voxelGrid = position_voxelGrid.clone();
      GridUtils.toVoxelGridInsideBuffer_fromWorldGrid(positionInBuffer_voxelGrid);
      buffer.setPixelContent(positionInBuffer_voxelGrid, value);
      return true;
   }

   public void set(T content) {
      throw new UnsupportedOperationException();
   }

   public void setOrigin(int x, int y, int z) {
      throw new UnsupportedOperationException();
   }

   @Nullable
   public T getContent(int x, int y, int z) {
      return (T)this.getContent(new Vector3i(x, y, z));
   }

   @Nullable
   public T getContent(@Nonnull Vector3i position_voxelGrid) {
      assert this.bounds_voxelGrid.contains(position_voxelGrid);

      NPixelBuffer<T> buffer = this.getBuffer(position_voxelGrid);
      Vector3i positionInBuffer_voxelGrid = position_voxelGrid.clone();
      GridUtils.toVoxelGridInsideBuffer_fromWorldGrid(positionInBuffer_voxelGrid);
      return buffer.getPixelContent(positionInBuffer_voxelGrid);
   }

   @Nonnull
   private NPixelBuffer<T> getBuffer(@Nonnull Vector3i position_voxelGrid) {
      assert this.bounds_voxelGrid.contains(position_voxelGrid);

      Vector3i localBufferPosition_bufferGrid = position_voxelGrid.clone();
      GridUtils.toBufferGrid_fromVoxelGrid(localBufferPosition_bufferGrid);
      return (NPixelBuffer)this.bufferAccess.getBuffer(localBufferPosition_bufferGrid).buffer();
   }

   public boolean replace(T replacement, int x, int y, int z, @Nonnull Predicate<T> mask) {
      return false;
   }

   public void pasteFrom(@Nonnull VoxelSpace<T> source) {
      throw new UnsupportedOperationException();
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

   public String getName() {
      throw new UnsupportedOperationException();
   }

   public boolean isInsideSpace(int x, int y, int z) {
      return this.isInsideSpace(new Vector3i(x, y, z));
   }

   public boolean isInsideSpace(@Nonnull Vector3i position) {
      return this.bounds_voxelGrid.contains(position);
   }

   public void forEach(VoxelConsumer<? super T> action) {
      throw new UnsupportedOperationException();
   }

   public int minX() {
      return this.bounds_voxelGrid.min.x;
   }

   public int maxX() {
      return this.bounds_voxelGrid.max.x;
   }

   public int minY() {
      return this.bounds_voxelGrid.min.y;
   }

   public int maxY() {
      return this.bounds_voxelGrid.max.y;
   }

   public int minZ() {
      return this.bounds_voxelGrid.min.z;
   }

   public int maxZ() {
      return this.bounds_voxelGrid.max.z;
   }

   public int sizeX() {
      return this.size_voxelGrid.x;
   }

   public int sizeY() {
      return this.size_voxelGrid.y;
   }

   public int sizeZ() {
      return this.size_voxelGrid.z;
   }
}
