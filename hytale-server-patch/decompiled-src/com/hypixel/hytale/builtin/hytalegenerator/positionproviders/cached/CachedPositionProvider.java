package com.hypixel.hytale.builtin.hytalegenerator.positionproviders.cached;

import com.hypixel.hytale.builtin.hytalegenerator.VectorUtil;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.Objects;
import javax.annotation.Nonnull;

public class CachedPositionProvider extends PositionProvider {
   @Nonnull
   private final PositionProvider positionProvider;
   private final int sectionSize;
   private CacheThreadMemory cache;

   public CachedPositionProvider(@Nonnull PositionProvider positionProvider, int sectionSize, int cacheSize, boolean useInternalThreadData) {
      if (sectionSize > 0 && cacheSize >= 0) {
         this.positionProvider = positionProvider;
         this.sectionSize = sectionSize;
         this.cache = new CacheThreadMemory(cacheSize);
      } else {
         throw new IllegalArgumentException();
      }
   }

   public void positionsIn(@Nonnull PositionProvider.Context context) {
      this.get(context);
   }

   public void get(@Nonnull PositionProvider.Context context) {
      Vector3i minSection = this.sectionAddress(context.minInclusive);
      Vector3i maxSection = this.sectionAddress(context.maxExclusive);
      Vector3i sectionAddress = minSection.clone();

      for(sectionAddress.x = minSection.x; sectionAddress.x <= maxSection.x; ++sectionAddress.x) {
         for(sectionAddress.z = minSection.z; sectionAddress.z <= maxSection.z; ++sectionAddress.z) {
            for(sectionAddress.y = minSection.y; sectionAddress.y <= maxSection.y; ++sectionAddress.y) {
               long key = HashUtil.hash((long)sectionAddress.x, (long)sectionAddress.y, (long)sectionAddress.z);
               Vector3d[] section = (Vector3d[])this.cache.sections.get(key);
               if (section == null) {
                  Vector3d sectionMin = this.sectionMin(sectionAddress);
                  Vector3d sectionMax = sectionMin.clone().add((double)this.sectionSize, (double)this.sectionSize, (double)this.sectionSize);
                  ArrayList<Vector3d> generatedPositions = new ArrayList();
                  Objects.requireNonNull(generatedPositions);
                  PositionProvider.Context childContext = new PositionProvider.Context(sectionMin, sectionMax, generatedPositions::add, (Vector3d)null);
                  this.positionProvider.positionsIn(childContext);
                  section = new Vector3d[generatedPositions.size()];
                  generatedPositions.toArray(section);
                  this.cache.sections.put(key, section);
                  this.cache.expirationList.addFirst(key);
                  if (this.cache.expirationList.size() > this.cache.size) {
                     long removedKey = (Long)this.cache.expirationList.removeLast();
                     this.cache.sections.remove(removedKey);
                  }
               }

               for(Vector3d position : section) {
                  if (VectorUtil.isInside(position, context.minInclusive, context.maxExclusive)) {
                     context.consumer.accept(position.clone());
                  }
               }
            }
         }
      }

   }

   @Nonnull
   private Vector3i sectionAddress(@Nonnull Vector3d pointer) {
      Vector3i address = pointer.toVector3i();
      address.x = this.sectionFloor(address.x) / this.sectionSize;
      address.y = this.sectionFloor(address.y) / this.sectionSize;
      address.z = this.sectionFloor(address.z) / this.sectionSize;
      return address;
   }

   @Nonnull
   private Vector3d sectionMin(@Nonnull Vector3i sectionAddress) {
      Vector3d min = sectionAddress.toVector3d();
      min.x *= (double)this.sectionSize;
      min.y *= (double)this.sectionSize;
      min.z *= (double)this.sectionSize;
      return min;
   }

   private int toSectionAddress(double position) {
      int positionAddress = (int)position;
      positionAddress = this.sectionFloor(positionAddress);
      positionAddress /= this.sectionSize;
      return positionAddress;
   }

   public int sectionFloor(int voxelAddress) {
      return voxelAddress < 0 ? voxelAddress - voxelAddress % this.sectionSize - this.sectionSize : voxelAddress - voxelAddress % this.sectionSize;
   }
}
